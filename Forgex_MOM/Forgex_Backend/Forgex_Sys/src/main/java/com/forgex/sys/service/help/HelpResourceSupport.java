package com.forgex.sys.service.help;

import com.forgex.sys.domain.entity.SysHelpResource;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 帮助资源公共规则。
 * <p>
 * 负责路径规范化、覆盖解析和扩展名/外链校验，便于单测覆盖核心规则。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-09-11
 */
public final class HelpResourceSupport {

    /** 手册类型。 */
    public static final String DOC_MANUAL = "MANUAL";

    /** 视频类型。 */
    public static final String DOC_VIDEO = "VIDEO";

    /** 全局范围。 */
    public static final String SCOPE_GLOBAL = "GLOBAL";

    /** 菜单覆盖范围。 */
    public static final String SCOPE_MENU = "MENU";

    /** 上传文件来源。 */
    public static final String SOURCE_FILE = "FILE";

    /** 外链来源。 */
    public static final String SOURCE_EXTERNAL = "EXTERNAL_URL";

    /** 联系二维码上传模块编码。 */
    public static final String MODULE_CONTACT = "sys_help_contact";

    private static final Set<String> MANUAL_EXTS = Set.of("pdf", "doc", "docx", "xls", "xlsx", "txt", "md");
    private static final Set<String> VIDEO_EXTS = Set.of("mp4");
    private static final Set<String> CONTACT_EXTS = Set.of("jpg", "jpeg", "png");

    private HelpResourceSupport() {
    }

    /**
     * 规范化工作区路径：去掉查询串、空白和尾部斜杠。
     *
     * @param menuPath 原始路径
     * @return 规范化路径；空输入返回空串
     */
    public static String normalizeMenuPath(String menuPath) {
        if (!StringUtils.hasText(menuPath)) {
            return "";
        }
        String path = menuPath.trim().replace('\\', '/');
        int queryIndex = path.indexOf('?');
        if (queryIndex >= 0) {
            path = path.substring(0, queryIndex);
        }
        int hashIndex = path.indexOf('#');
        if (hashIndex >= 0) {
            path = path.substring(0, hashIndex);
        }
        while (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    /**
     * 判断候选菜单路径是否覆盖当前页。
     *
     * @param currentPath 当前页规范化路径
     * @param candidatePath 资源绑定路径
     * @return 精确匹配或作为父路径前缀时返回 {@code true}
     */
    public static boolean matchesMenuPath(String currentPath, String candidatePath) {
        String current = normalizeMenuPath(currentPath);
        String candidate = normalizeMenuPath(candidatePath);
        if (!StringUtils.hasText(current) || !StringUtils.hasText(candidate)) {
            return false;
        }
        return current.equals(candidate) || current.startsWith(candidate + "/");
    }

    /**
     * 按精确匹配 → 最长前缀 → 空列表的顺序解析菜单文档。
     *
     * @param currentPath 当前页路径
     * @param menuResources 已启用的菜单范围资源
     * @return 命中列表；未命中返回空列表
     */
    public static List<SysHelpResource> resolveMenuResources(String currentPath, List<SysHelpResource> menuResources) {
        String current = normalizeMenuPath(currentPath);
        if (!StringUtils.hasText(current) || menuResources == null || menuResources.isEmpty()) {
            return List.of();
        }
        List<SysHelpResource> exact = new ArrayList<>();
        List<SysHelpResource> prefixes = new ArrayList<>();
        for (SysHelpResource item : menuResources) {
            if (item == null || !StringUtils.hasText(item.getMenuPath())) {
                continue;
            }
            String candidate = normalizeMenuPath(item.getMenuPath());
            if (current.equals(candidate)) {
                exact.add(item);
            } else if (current.startsWith(candidate + "/")) {
                prefixes.add(item);
            }
        }
        if (!exact.isEmpty()) {
            return sortByOrder(exact);
        }
        if (prefixes.isEmpty()) {
            return List.of();
        }
        int maxLength = prefixes.stream()
                .map(item -> normalizeMenuPath(item.getMenuPath()).length())
                .max(Integer::compareTo)
                .orElse(0);
        List<SysHelpResource> longest = prefixes.stream()
                .filter(item -> normalizeMenuPath(item.getMenuPath()).length() == maxLength)
                .toList();
        return sortByOrder(longest);
    }

    /**
     * 按排序号和主键排序。
     *
     * @param resources 资源列表
     * @return 新的有序列表
     */
    public static List<SysHelpResource> sortByOrder(List<SysHelpResource> resources) {
        if (resources == null || resources.isEmpty()) {
            return List.of();
        }
        List<SysHelpResource> copy = new ArrayList<>(resources);
        copy.sort(Comparator
                .comparing((SysHelpResource item) -> item.getSortOrder() == null ? 0 : item.getSortOrder())
                .thenComparing(item -> item.getId() == null ? 0L : item.getId()));
        return copy;
    }

    /**
     * 规范化扩展名。
     *
     * @param fileName 文件名
     * @param fileExt 已有扩展名
     * @return 小写扩展名，不含点
     */
    public static String normalizeExt(String fileName, String fileExt) {
        if (StringUtils.hasText(fileExt)) {
            return fileExt.trim().replace(".", "").toLowerCase(Locale.ROOT);
        }
        if (!StringUtils.hasText(fileName) || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    /**
     * 判断扩展名是否允许。
     *
     * @param docType 文档类型或联系模块编码
     * @param ext 扩展名
     * @return 允许返回 {@code true}
     */
    public static boolean isAllowedExt(String docType, String ext) {
        if (!StringUtils.hasText(ext)) {
            return false;
        }
        String normalized = ext.toLowerCase(Locale.ROOT);
        if (MODULE_CONTACT.equals(docType)) {
            return CONTACT_EXTS.contains(normalized);
        }
        if (DOC_VIDEO.equalsIgnoreCase(docType)) {
            return VIDEO_EXTS.contains(normalized);
        }
        return MANUAL_EXTS.contains(normalized);
    }

    /**
     * 校验外链是否为 http(s)。
     *
     * @param url 外链
     * @return 合法返回 {@code true}
     */
    public static boolean isSafeExternalUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return false;
        }
        String value = url.trim().toLowerCase(Locale.ROOT);
        if (value.startsWith("javascript:") || value.startsWith("data:") || value.startsWith("vbscript:")) {
            return false;
        }
        return value.startsWith("http://") || value.startsWith("https://");
    }

    /**
     * 解析上传模块编码。
     *
     * @param docType 文档类型或联系模块
     * @return moduleCode
     */
    /**
     * 校验范围与菜单绑定是否一致。
     *
     * @param scopeType 范围
     * @param menuId 菜单 ID
     * @param menuPath 菜单路径
     * @return {@code GLOBAL} 绑了菜单返回 {@code globalHasMenu}；{@code MENU} 缺字段返回 {@code menuMissing}；否则 {@code ok}
     */
    public static String validateScopeBinding(String scopeType, Long menuId, String menuPath) {
        boolean hasMenu = menuId != null || StringUtils.hasText(normalizeMenuPath(menuPath));
        if (SCOPE_GLOBAL.equalsIgnoreCase(scopeType) && hasMenu) {
            return "globalHasMenu";
        }
        if (SCOPE_MENU.equalsIgnoreCase(scopeType) && (menuId == null || !StringUtils.hasText(normalizeMenuPath(menuPath)))) {
            return "menuMissing";
        }
        return "ok";
    }

    /**
     * 解析上传模块编码。
     *
     * @param docType 文档类型或联系模块
     * @return moduleCode
     */
    public static String resolveModuleCode(String docType) {
        if (MODULE_CONTACT.equals(docType)) {
            return MODULE_CONTACT;
        }
        if (DOC_VIDEO.equalsIgnoreCase(docType)) {
            return "sys_help_video";
        }
        return "sys_help_manual";
    }
}
