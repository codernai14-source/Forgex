package com.forgex.sys.domain.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 运行时帮助资源覆盖解析结果。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-09-11
 */
@Data
public class SysHelpPageResolveDTO {

    /** 最终命中范围：MENU 或 GLOBAL。 */
    private String resolvedScope;

    /** 规范化后的当前页路径。 */
    private String menuPath;

    /** 命中的资源列表，已按 sort_order 排序。 */
    private List<SysHelpResourceDTO> items = new ArrayList<>();
}
