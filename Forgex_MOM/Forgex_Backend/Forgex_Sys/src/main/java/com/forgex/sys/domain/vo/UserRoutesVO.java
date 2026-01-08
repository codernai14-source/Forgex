package com.forgex.sys.domain.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 用户路由VO
 * 
 * @author Forgex Team
 * @date 2025-01-07
 */
@Data
public class UserRoutesVO {
    
    /**
     * 模块列表
     */
    private List<Map<String, Object>> modules;
    
    /**
     * 路由列表
     */
    private List<Map<String, Object>> routes;
    
    /**
     * 按钮权限列表
     */
    private List<String> buttons;
}
