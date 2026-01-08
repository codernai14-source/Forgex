package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.SysUserDTO;
import com.forgex.sys.domain.dto.SysUserQueryDTO;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.service.ISysUserService;
import com.forgex.sys.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户管理Controller
 * 
 * 职责：
 * - 接收HTTP请求
 * - 参数校验（调用Validator）
 * - 调用Service层方法
 * - 返回响应结果
 * 
 * @author Forgex Team
 * @date 2025-01-07
 */
@RestController
@RequestMapping("/sys/user")
@RequiredArgsConstructor
public class UserController {
    
    private final ISysUserService userService;
    private final UserValidator userValidator;
    
    /**
     * 分页查询用户列表（兼容旧接口）
     */
    @PostMapping("/list")
    public R<IPage<SysUserDTO>> list(@RequestBody Map<String, Object> body) {
        // 1. 参数解析
        int pageNo = parseInteger(body.get("pageNo"), 1);
        int pageSize = parseInteger(body.get("pageSize"), 10);
        Page<SysUser> page = new Page<>(pageNo, pageSize);
        
        SysUserQueryDTO query = new SysUserQueryDTO();
        query.setAccount(parseString(body.get("account")));
        query.setUsername(parseString(body.get("username")));
        query.setStatus(parseInteger(body.get("status"), null));
        
        // 2. 调用Service
        IPage<SysUserDTO> result = userService.pageUsers(page, query);
        
        // 3. 返回结果
        return R.ok(result);
    }
    
    /**
     * 分页查询用户列表
     */
    @GetMapping("/page")
    public R<IPage<SysUserDTO>> page(Page<SysUser> page, SysUserQueryDTO query) {
        return R.ok(userService.pageUsers(page, query));
    }
    
    /**
     * 查询用户列表
     */
    @GetMapping("/list")
    public R<List<SysUserDTO>> listUsers(SysUserQueryDTO query) {
        return R.ok(userService.listUsers(query));
    }
    
    /**
     * 根据ID获取用户详情
     */
    @GetMapping("/{id}")
    public R<SysUserDTO> getById(@PathVariable Long id) {
        userValidator.validateId(id);
        return R.ok(userService.getUserById(id));
    }
    
    /**
     * 新增用户
     */
    @PostMapping
    public R<Void> add(@RequestBody @Validated SysUserDTO userDTO) {
        // 1. 数据校验
        userValidator.validateForAdd(userDTO);
        
        // 2. 调用Service
        userService.addUser(userDTO);
        
        // 3. 返回结果
        return R.ok();
    }
    
    /**
     * 新增用户（兼容旧接口）
     */
    @PostMapping("/create")
    public R<Boolean> create(@RequestBody SysUserDTO userDTO) {
        userValidator.validateForAdd(userDTO);
        userService.addUser(userDTO);
        return R.ok(true);
    }
    
    /**
     * 更新用户
     */
    @PutMapping
    public R<Void> update(@RequestBody @Validated SysUserDTO userDTO) {
        // 1. 数据校验
        userValidator.validateForUpdate(userDTO);
        
        // 2. 调用Service
        userService.updateUser(userDTO);
        
        // 3. 返回结果
        return R.ok();
    }
    
    /**
     * 更新用户（兼容旧接口）
     */
    @PostMapping("/update")
    public R<Boolean> updateOld(@RequestBody SysUserDTO userDTO) {
        userValidator.validateForUpdate(userDTO);
        userService.updateUser(userDTO);
        return R.ok(true);
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        // 1. 数据校验
        userValidator.validateForDelete(id);
        
        // 2. 调用Service
        userService.deleteUser(id);
        
        // 3. 返回结果
        return R.ok();
    }
    
    /**
     * 删除用户（兼容旧接口）
     */
    @PostMapping("/delete")
    public R<Boolean> deleteOld(@RequestBody Map<String, Object> body) {
        Long id = parseLong(body.get("id"));
        userValidator.validateForDelete(id);
        userService.deleteUser(id);
        return R.ok(true);
    }
    
    /**
     * 批量删除用户
     */
    @DeleteMapping("/batch")
    public R<Void> batchDelete(@RequestBody List<Long> ids) {
        // 1. 校验每个ID
        for (Long id : ids) {
            userValidator.validateForDelete(id);
        }
        
        // 2. 调用Service
        userService.batchDeleteUsers(ids);
        
        // 3. 返回结果
        return R.ok();
    }
    
    /**
     * 解析Long类型参数
     */
    private Long parseLong(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        if (obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * 解析Integer类型参数
     */
    private Integer parseInteger(Object obj, Integer defaultValue) {
        if (obj == null) {
            return defaultValue;
        }
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        if (obj instanceof String) {
            try {
                return Integer.parseInt((String) obj);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * 解析String类型参数
     */
    private String parseString(Object obj) {
        if (obj == null) {
            return null;
        }
        String str = obj.toString();
        return str.isEmpty() ? null : str;
    }
}
