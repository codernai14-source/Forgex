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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.util.CurrentUserUtils;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.DictDTO;
import com.forgex.sys.domain.param.DictItemsByPathParam;
import com.forgex.sys.domain.param.DictItemsParam;
import com.forgex.sys.domain.param.DictPageParam;
import com.forgex.sys.domain.param.IdParam;
import com.forgex.sys.domain.vo.DictItemVO;
import com.forgex.sys.domain.vo.DictTreeVO;
import com.forgex.sys.service.IDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sys/dict")
public class SysDictController {

    @Autowired
    private IDictService dictService;

    @PostMapping("/tree")
    public R<List<DictTreeVO>> tree() {
        return R.ok(dictService.getDictTree(getCurrentTenantId()));
    }

    @PostMapping("/page")
    public R<IPage<DictTreeVO>> page(@RequestBody(required = false) DictPageParam param) {
        DictPageParam query = param == null ? new DictPageParam() : param;
        return R.ok(dictService.pageDictTree(getCurrentTenantId(), query));
    }

    @PostMapping("/items")
    public R<List<DictItemVO>> items(@RequestBody DictItemsParam param) {
        String dictCode = param.getDictCode();
        if (dictCode == null || dictCode.isEmpty()) {
            return R.fail(CommonPrompt.DICT_CODE_CANNOT_BE_EMPTY);
        }
        return R.ok(dictService.getDictItemsByCode(dictCode, getCurrentTenantId()));
    }

    @PostMapping("/itemsByPath")
    public R<List<DictItemVO>> itemsByPath(@RequestBody DictItemsByPathParam param) {
        String nodePath = param == null ? null : param.getNodePath();
        if (nodePath == null || nodePath.isEmpty()) {
            return R.fail(CommonPrompt.NODE_PATH_CANNOT_BE_EMPTY);
        }
        return R.ok(dictService.getDictItemsByPath(nodePath, getCurrentTenantId()));
    }

    @PostMapping("/create")
    public R<Boolean> create(@RequestBody DictDTO dictDTO) {
        dictDTO.setTenantId(getCurrentTenantId());
        dictService.addDict(dictDTO);
        return R.ok(CommonPrompt.CREATE_SUCCESS, true);
    }

    @PostMapping("/update")
    public R<Boolean> update(@RequestBody DictDTO dictDTO) {
        dictDTO.setTenantId(getCurrentTenantId());
        dictService.updateDict(dictDTO);
        return R.ok(CommonPrompt.UPDATE_SUCCESS, true);
    }

    @PostMapping("/delete")
    public R<Boolean> delete(@RequestBody IdParam param) {
        Long id = param.getId();
        if (id == null) {
            return R.fail(CommonPrompt.DICT_ID_CANNOT_BE_EMPTY);
        }
        dictService.deleteDict(id, getCurrentTenantId());
        return R.ok(CommonPrompt.DELETE_SUCCESS, true);
    }

    private Long getCurrentTenantId() {
        Long tenantId = CurrentUserUtils.getTenantId();
        return tenantId != null ? tenantId : 1L;
    }
}
