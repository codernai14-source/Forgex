package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.common.web.R;
import com.forgex.sys.domain.entity.SysTenantIgnore;
import com.forgex.sys.mapper.SysTenantIgnoreMapper;
import com.forgex.sys.service.SysTenantIgnoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 租户忽略控制器。
 * <p>
 * 提供忽略租户上下文的管理与诊断接口，用于临时绕过租户过滤执行系统任务。
 */
@RestController
@RequestMapping({"/sys/tenant/ignore","/sys/tenant-ignore"})
public class SysTenantIgnoreController {
    @Autowired
    private SysTenantIgnoreMapper mapper;
    @Autowired
    private SysTenantIgnoreService service;

    @GetMapping
    public R<List<SysTenantIgnore>> list() {
        List<SysTenantIgnore> list = mapper.selectList(new LambdaQueryWrapper<>());
        return R.ok(list);
    }

    @PostMapping
    public R<Boolean> create(@RequestBody SysTenantIgnore entity) {
        mapper.insert(entity);
        return R.ok(true);
    }

    @PutMapping
    public R<Boolean> update(@RequestBody SysTenantIgnore entity) {
        mapper.updateById(entity);
        return R.ok(true);
    }

    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        mapper.deleteById(id);
        return R.ok(true);
    }

    @PostMapping("/reload")
    public R<Boolean> reload() {
        return service.reload();
    }
}
