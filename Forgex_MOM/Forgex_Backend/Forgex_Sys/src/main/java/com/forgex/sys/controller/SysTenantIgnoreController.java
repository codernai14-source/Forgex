/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
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
