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
import com.forgex.sys.domain.entity.SysDepartment;
import com.forgex.sys.mapper.SysDepartmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sys/department")
@RequiredArgsConstructor
public class SysDepartmentController {

    private final SysDepartmentMapper departmentMapper;

    @PostMapping("/list")
    public R<List<SysDepartment>> list() {
        LambdaQueryWrapper<SysDepartment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDepartment::getStatus, true).orderByAsc(SysDepartment::getOrderNum);
        List<SysDepartment> departments = departmentMapper.selectList(wrapper);
        return R.ok(departments);
    }
}

