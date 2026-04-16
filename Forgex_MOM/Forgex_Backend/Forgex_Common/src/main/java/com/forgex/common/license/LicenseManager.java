package com.forgex.common.license;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/**
 * License 管理器
 * <p>
 * 负责读取 request-info、加载 license 文件、验签并输出统一的运行时授权状态。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public class LicenseManager {

    private static final String DEFAULT_PRODUCT = "Forgex";
    private static final String DEFAULT_EDITION = "standard";
    private static final String TOOL_VERSION = "1.0.0";
    private static final Pattern BASE64_PATTERN = Pattern.compile("[^A-Za-z0-9+/=]");

    private final ObjectMapper objectMapper;
    private final DeploymentProperties deploymentProperties;
    private final LicenseProperties licenseProperties;
    private final MachineFingerprintService machineFingerprintService;
    private final AtomicReference<LicenseRuntimeInfo> cache = new AtomicReference<>();

    public LicenseManager(
            ObjectMapper objectMapper,
            DeploymentProperties deploymentProperties,
            LicenseProperties licenseProperties,
            MachineFingerprintService machineFingerprintService
    ) {
        this.objectMapper = objectMapper;
        this.deploymentProperties = deploymentProperties;
        this.licenseProperties = licenseProperties;
        this.machineFingerprintService = machineFingerprintService;
    }

    /**
     * 获取当前授权状态。
     *
     * @return 授权运行时状态
     */
    public LicenseRuntimeInfo current() {
        LicenseRuntimeInfo snapshot = cache.get();
        if (snapshot != null) {
            return snapshot;
        }
        return refresh();
    }

    /**
     * 重新加载授权状态。
     *
     * @return 最新授权状态
     */
    public synchronized LicenseRuntimeInfo refresh() {
        LicenseRequestInfo requestInfo = ensureRequestInfo();
        LicenseRuntimeInfo runtimeInfo = new LicenseRuntimeInfo();
        runtimeInfo.setInstanceCode(deploymentProperties.getInstanceCode());
        runtimeInfo.setRequestInfo(requestInfo);
        runtimeInfo.setCustomerCode(requestInfo == null ? null : requestInfo.getCustomerCode());
        runtimeInfo.setMachineCode(requestInfo == null ? machineFingerprintService.resolveMachineCode() : requestInfo.getMachineCode());
        runtimeInfo.setRequestInfoPath(requestInfoPath().toString());
        runtimeInfo.setLicensePath(licensePath().toString());
        String publicKeyBase64 = resolvePublicKey();
        runtimeInfo.setPublicKeyConfigured(StringUtils.hasText(publicKeyBase64));
        runtimeInfo.setLastCheckedAt(OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        if (!licenseProperties.isEnabled()) {
            runtimeInfo.setStatus(LicenseStatus.VALID);
            runtimeInfo.setValid(true);
            runtimeInfo.setMessage("授权校验已禁用");
            cache.set(runtimeInfo);
            return runtimeInfo;
        }

        if (!Files.exists(licensePath())) {
            runtimeInfo.setStatus(LicenseStatus.UNLICENSED);
            runtimeInfo.setValid(false);
            runtimeInfo.setMessage("未检测到 license.lic");
            cache.set(runtimeInfo);
            return runtimeInfo;
        }

        try {
            String rawLicense = sanitizeLicenseText(Files.readString(licensePath(), StandardCharsets.UTF_8));
            String[] segments = rawLicense.split("\\.");
            if (segments.length != 2) {
                return invalid(runtimeInfo, "授权码格式错误");
            }

            byte[] payloadBytes = Base64.getUrlDecoder().decode(segments[0]);
            byte[] signatureBytes = Base64.getUrlDecoder().decode(segments[1]);
            LicensePayload payload = objectMapper.readValue(payloadBytes, LicensePayload.class);

            runtimeInfo.setLicenseId(payload.getLicenseId());
            runtimeInfo.setEdition(payload.getEdition());
            runtimeInfo.setCustomerName(payload.getCustomerName());
            runtimeInfo.setModules(payload.getModules());
            runtimeInfo.setMaxUsers(payload.getMaxUsers());
            runtimeInfo.setMaxTenants(payload.getMaxTenants());
            runtimeInfo.setIssuedAt(payload.getIssuedAt());
            runtimeInfo.setEffectiveAt(payload.getEffectiveAt());
            runtimeInfo.setExpireAt(payload.getExpireAt());
            runtimeInfo.setDurationDays(payload.getDurationDays());
            runtimeInfo.setGraceDays(payload.getGraceDays());

            if (isDevLicenseOnly(payload) && !isDevProfile()) {
                return invalid(runtimeInfo, "开发环境授权仅允许在 dev 环境使用");
            }

            if (!StringUtils.hasText(publicKeyBase64)) {
                return invalid(runtimeInfo, "未配置 Ed25519 公钥");
            }

            if (!verifySignature(payloadBytes, signatureBytes, publicKeyBase64)) {
                return invalid(runtimeInfo, "授权签名校验失败");
            }

            runtimeInfo.setSignatureValid(true);
            runtimeInfo.setCustomerMatched(requestInfo != null && safeEquals(payload.getCustomerCode(), requestInfo.getCustomerCode()));
            runtimeInfo.setMachineMatched(requestInfo != null && safeEquals(payload.getMachineCode(), requestInfo.getMachineCode()));

            if (!runtimeInfo.isCustomerMatched()) {
                return invalid(runtimeInfo, "客户码不匹配");
            }
            if (!runtimeInfo.isMachineMatched()) {
                return invalid(runtimeInfo, "机器码不匹配");
            }

            OffsetDateTime now = OffsetDateTime.now();
            OffsetDateTime effectiveAt = parseDateTime(payload.getEffectiveAt());
            OffsetDateTime expireAt = parseDateTime(payload.getExpireAt());
            int graceDays = payload.getGraceDays() == null ? 0 : payload.getGraceDays();

            if (effectiveAt != null && effectiveAt.isAfter(now)) {
                return invalid(runtimeInfo, "授权尚未生效");
            }

            if (expireAt == null) {
                runtimeInfo.setStatus(LicenseStatus.VALID);
                runtimeInfo.setValid(true);
                runtimeInfo.setMessage("授权有效");
                cache.set(runtimeInfo);
                return runtimeInfo;
            }

            long remainingDays = Duration.between(now, expireAt).toDays();
            runtimeInfo.setRemainingDays(remainingDays);

            if (expireAt.isBefore(now)) {
                OffsetDateTime graceDeadline = expireAt.plusDays(graceDays);
                if (graceDays > 0 && !graceDeadline.isBefore(now)) {
                    runtimeInfo.setStatus(LicenseStatus.GRACE);
                    runtimeInfo.setValid(true);
                    runtimeInfo.setMessage("授权已过期，当前处于宽限期");
                } else {
                    runtimeInfo.setStatus(LicenseStatus.EXPIRED);
                    runtimeInfo.setValid(false);
                    runtimeInfo.setMessage("授权已过期");
                }
                cache.set(runtimeInfo);
                return runtimeInfo;
            }

            if (remainingDays <= licenseProperties.getWarningDays()) {
                runtimeInfo.setStatus(LicenseStatus.EXPIRING_SOON);
                runtimeInfo.setValid(true);
                runtimeInfo.setMessage("授权即将到期");
            } else {
                runtimeInfo.setStatus(LicenseStatus.VALID);
                runtimeInfo.setValid(true);
                runtimeInfo.setMessage("授权有效");
            }
            cache.set(runtimeInfo);
            return runtimeInfo;
        } catch (Exception ex) {
            return invalid(runtimeInfo, "授权解析失败: " + ex.getMessage());
        }
    }

    /**
     * 获取授权请求信息。
     *
     * @return 请求信息对象
     */
    public LicenseRequestInfo getRequestInfo() {
        return ensureRequestInfo();
    }

    /**
     * 读取授权历史记录。
     *
     * @return 历史记录列表
     */
    public List<Map<String, Object>> readHistory() {
        Path historyPath = historyPath();
        if (!Files.exists(historyPath)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(historyPath.toFile(), new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    private LicenseRuntimeInfo invalid(LicenseRuntimeInfo runtimeInfo, String message) {
        runtimeInfo.setStatus(LicenseStatus.INVALID);
        runtimeInfo.setValid(false);
        runtimeInfo.setMessage(message);
        cache.set(runtimeInfo);
        return runtimeInfo;
    }

    private LicenseRequestInfo ensureRequestInfo() {
        Path path = requestInfoPath();
        if (Files.exists(path)) {
            try {
                return objectMapper.readValue(path.toFile(), LicenseRequestInfo.class);
            } catch (Exception ignored) {
                // 继续走重新生成逻辑
            }
        }

        LicenseRequestInfo requestInfo = new LicenseRequestInfo();
        requestInfo.setProduct(DEFAULT_PRODUCT);
        requestInfo.setEdition(DEFAULT_EDITION);
        requestInfo.setMachineCode(machineFingerprintService.resolveMachineCode());
        requestInfo.setCustomerCode(generateCustomerCode(requestInfo.getMachineCode()));
        requestInfo.setOsType(System.getProperty("os.name", "unknown").toLowerCase(Locale.ROOT));
        requestInfo.setHostname(resolveHostName());
        requestInfo.setGeneratedAt(OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        requestInfo.setToolVersion(TOOL_VERSION);

        try {
            Files.createDirectories(path.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), requestInfo);
        } catch (Exception ignored) {
            // 运行时目录可能只读，此时退化为内存态 request-info
        }
        return requestInfo;
    }

    private String generateCustomerCode(String machineCode) {
        String instanceCode = deploymentProperties.getInstanceCode();
        if (!StringUtils.hasText(instanceCode)) {
            instanceCode = "DEFAULT";
        }
        String base = instanceCode.trim().toUpperCase(Locale.ROOT) + "|" + machineCode;
        String suffix = UUID.nameUUIDFromBytes(base.getBytes(StandardCharsets.UTF_8))
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase(Locale.ROOT);
        return licenseProperties.getCustomerCodePrefix() + "-" + suffix;
    }

    private String resolveHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception ignored) {
            return "unknown-host";
        }
    }

    private boolean verifySignature(byte[] payloadBytes, byte[] signatureBytes, String publicKeyBase64) throws Exception {
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
        KeyFactory keyFactory = KeyFactory.getInstance("Ed25519");
        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        Signature signature = Signature.getInstance("Ed25519");
        signature.initVerify(publicKey);
        signature.update(payloadBytes);
        return signature.verify(signatureBytes);
    }

    private OffsetDateTime parseDateTime(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean safeEquals(String left, String right) {
        return StringUtils.hasText(left) && StringUtils.hasText(right) && left.trim().equalsIgnoreCase(right.trim());
    }

    /**
     * 解析 Ed25519 公钥。
     * <p>
     * 优先使用配置项，开发环境允许从授权目录读取公钥文件，避免 IDE 或服务进程未继承环境变量。
     * </p>
     *
     * @return Base64 公钥
     */
    private String resolvePublicKey() {
        if (StringUtils.hasText(licenseProperties.getPublicKey())) {
            return sanitizeBase64(licenseProperties.getPublicKey());
        }
        Path publicKeyPath = publicKeyPath();
        if (!Files.exists(publicKeyPath)) {
            return "";
        }
        try {
            return sanitizeBase64(Files.readString(publicKeyPath, StandardCharsets.UTF_8));
        } catch (Exception ignored) {
            return "";
        }
    }

    /**
     * 清洗 Base64 文本，移除 BOM、问号和其他非 Base64 字符。
     *
     * @param value 原始文本
     * @return 清洗后的 Base64 文本
     */
    private String sanitizeBase64(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String normalized = value.replace("\uFEFF", "").trim();
        return BASE64_PATTERN.matcher(normalized).replaceAll("");
    }

    /**
     * 清洗授权文本，移除 BOM 和空白字符，保留 Base64Url 与分隔符。
     *
     * @param value 原始授权文本
     * @return 清洗后的授权文本
     */
    private String sanitizeLicenseText(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.replace("\uFEFF", "").replaceAll("\\s+", "");
    }

    /**
     * 判断授权是否为开发环境专用授权。
     *
     * @param payload 授权载荷
     * @return true=开发环境专用授权
     */
    private boolean isDevLicenseOnly(LicensePayload payload) {
        return payload != null
                && "dev".equalsIgnoreCase(payload.getEdition())
                && StringUtils.hasText(payload.getRemark())
                && "DEV_ONLY_LOCAL_LICENSE".equalsIgnoreCase(payload.getRemark().trim());
    }

    /**
     * 判断当前部署环境是否为 dev。
     *
     * @return true=dev 环境
     */
    private boolean isDevProfile() {
        return StringUtils.hasText(deploymentProperties.getProfile())
                && "dev".equalsIgnoreCase(deploymentProperties.getProfile().trim());
    }

    private Path requestInfoPath() {
        return Path.of(deploymentProperties.getLicenseDir(), licenseProperties.getRequestInfoFileName());
    }

    private Path licensePath() {
        return Path.of(deploymentProperties.getLicenseDir(), licenseProperties.getFileName());
    }

    private Path historyPath() {
        return Path.of(deploymentProperties.getLicenseDir(), licenseProperties.getHistoryFileName());
    }

    private Path publicKeyPath() {
        return Path.of(deploymentProperties.getLicenseDir(), licenseProperties.getPublicKeyFileName());
    }
}
