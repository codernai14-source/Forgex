package com.forgex.common.license;

import cn.hutool.crypto.digest.DigestUtil;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

/**
 * 机器指纹生成服务
 * <p>
 * 负责生成当前宿主机的稳定机器码，优先使用安装器注入值，其次回退到宿主机特征拼装。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public class MachineFingerprintService {

    private final LicenseProperties licenseProperties;

    public MachineFingerprintService(LicenseProperties licenseProperties) {
        this.licenseProperties = licenseProperties;
    }

    /**
     * 获取当前机器码。
     *
     * @return SHA-256 机器码
     */
    public String resolveMachineCode() {
        if (StringUtils.hasText(licenseProperties.getOverrideMachineCode())) {
            return normalize(licenseProperties.getOverrideMachineCode());
        }

        List<String> parts = new ArrayList<>();
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);

        if (osName.contains("win")) {
            addIfHasText(parts, System.getenv("COMPUTERNAME"));
            addIfHasText(parts, System.getenv("PROCESSOR_IDENTIFIER"));
            addIfHasText(parts, System.getenv("SystemDrive"));
        } else {
            addIfHasText(parts, readFileQuietly("/etc/machine-id"));
            addIfHasText(parts, readFileQuietly("/sys/class/dmi/id/product_uuid"));
            addIfHasText(parts, System.getenv("HOSTNAME"));
        }

        addIfHasText(parts, resolveHostName());
        parts.addAll(resolveMacAddresses());

        if (parts.isEmpty()) {
            parts.add("FORGEX-UNKNOWN-HOST");
        }

        String joined = String.join("|", parts).toUpperCase(Locale.ROOT);
        return DigestUtil.sha256Hex(joined.getBytes(StandardCharsets.UTF_8)).toUpperCase(Locale.ROOT);
    }

    private void addIfHasText(List<String> parts, String value) {
        if (StringUtils.hasText(value)) {
            parts.add(normalize(value));
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().replace(" ", "").toUpperCase(Locale.ROOT);
    }

    private String readFileQuietly(String filePath) {
        try {
            Path path = Path.of(filePath);
            if (!Files.exists(path)) {
                return null;
            }
            return Files.readString(path).trim();
        } catch (Exception ignored) {
            return null;
        }
    }

    private String resolveHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception ignored) {
            return null;
        }
    }

    private List<String> resolveMacAddresses() {
        try {
            List<String> macs = new ArrayList<>();
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces == null) {
                return macs;
            }
            for (NetworkInterface networkInterface : Collections.list(interfaces)) {
                if (!networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.isVirtual()) {
                    continue;
                }
                byte[] hardwareAddress = networkInterface.getHardwareAddress();
                if (hardwareAddress == null || hardwareAddress.length == 0) {
                    continue;
                }
                StringBuilder sb = new StringBuilder();
                for (byte b : hardwareAddress) {
                    sb.append(String.format("%02X", b));
                }
                if (sb.length() > 0) {
                    macs.add(sb.toString());
                }
            }
            return macs;
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }
}
