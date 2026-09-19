package com.forgex.sys.service.help;

import com.forgex.sys.domain.entity.SysHelpResource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 帮助资源覆盖解析与校验单测。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-09-11
 */
class HelpResourceSupportTest {

    /**
     * 路径应去掉查询串和尾部斜杠。
     */
    @Test
    void normalizeMenuPath_shouldStripQueryAndSlash() {
        assertEquals("/workspace/sys/user", HelpResourceSupport.normalizeMenuPath("/workspace/sys/user/?tab=1"));
        assertEquals("", HelpResourceSupport.normalizeMenuPath("  "));
    }

    /**
     * 精确匹配优先于前缀匹配。
     */
    @Test
    void resolveMenuResources_shouldPreferExactMatch() {
        SysHelpResource parent = resource(1L, "/workspace/sys/user", 2);
        SysHelpResource exact = resource(2L, "/workspace/sys/user/detail", 1);
        List<SysHelpResource> matched = HelpResourceSupport.resolveMenuResources(
                "/workspace/sys/user/detail",
                List.of(parent, exact)
        );
        assertEquals(1, matched.size());
        assertEquals(2L, matched.get(0).getId());
    }

    /**
     * 无精确匹配时回退到最长前缀。
     */
    @Test
    void resolveMenuResources_shouldUseLongestPrefix() {
        SysHelpResource root = resource(1L, "/workspace/sys", 1);
        SysHelpResource user = resource(2L, "/workspace/sys/user", 3);
        List<SysHelpResource> matched = HelpResourceSupport.resolveMenuResources(
                "/workspace/sys/user/detail",
                List.of(root, user)
        );
        assertEquals(1, matched.size());
        assertEquals(2L, matched.get(0).getId());
    }

    /**
     * 无菜单命中时返回空列表，交由服务层回退全局。
     */
    @Test
    void resolveMenuResources_shouldReturnEmptyWhenNoMatch() {
        SysHelpResource other = resource(1L, "/workspace/basic/material", 1);
        assertTrue(HelpResourceSupport.resolveMenuResources("/workspace/sys/user", List.of(other)).isEmpty());
    }

    /**
     * 扩展名与外链校验。
     */
    @Test
    void validation_shouldAcceptSafeValuesOnly() {
        assertTrue(HelpResourceSupport.isAllowedExt("MANUAL", "docx"));
        assertTrue(HelpResourceSupport.isAllowedExt("VIDEO", "mp4"));
        assertFalse(HelpResourceSupport.isAllowedExt("VIDEO", "avi"));
        assertTrue(HelpResourceSupport.isSafeExternalUrl("https://wiki.example.com/doc"));
        assertFalse(HelpResourceSupport.isSafeExternalUrl("javascript:alert(1)"));
        assertFalse(HelpResourceSupport.isSafeExternalUrl("ftp://files"));
        assertEquals("globalHasMenu", HelpResourceSupport.validateScopeBinding("GLOBAL", 12L, "/workspace/sys/user"));
        assertEquals("menuMissing", HelpResourceSupport.validateScopeBinding("MENU", null, ""));
        assertEquals("ok", HelpResourceSupport.validateScopeBinding("GLOBAL", null, ""));
    }

    /**
     * 构造测试资源。
     *
     * @param id 主键
     * @param path 菜单路径
     * @param sort 排序号
     * @return 资源
     */
    private SysHelpResource resource(Long id, String path, int sort) {
        SysHelpResource item = new SysHelpResource();
        item.setId(id);
        item.setMenuPath(path);
        item.setSortOrder(sort);
        item.setScopeType(HelpResourceSupport.SCOPE_MENU);
        item.setStatus(1);
        return item;
    }
}
