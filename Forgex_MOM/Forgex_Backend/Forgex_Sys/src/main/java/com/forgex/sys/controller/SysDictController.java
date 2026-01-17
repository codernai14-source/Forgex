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

import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.util.CurrentUserUtils;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.DictDTO;
import com.forgex.sys.domain.param.DictItemsByPathParam;
import com.forgex.sys.domain.param.DictItemsParam;
import com.forgex.sys.domain.param.IdParam;
import com.forgex.sys.domain.vo.DictItemVO;
import com.forgex.sys.domain.vo.DictTreeVO;
import com.forgex.sys.service.IDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据字典 Controller
 * 
 * @author coder_nai@163.com
 * @date 2025-01-13
 */
@RestController
@RequestMapping("/sys/dict")
public class SysDictController {
    
    @Autowired
    private IDictService dictService;
    
    /**
     * 获取字典树
     */
    @PostMapping("/tree")
    public R<List<DictTreeVO>> tree() {
        Long tenantId = getCurrentTenantId();
        List<DictTreeVO> tree = dictService.getDictTree(tenantId);
        return R.ok(tree);
    }
    
    /**
     * 根据字典编码获取字典项
     */
    @PostMapping("/items")
    public R<List<DictItemVO>> items(@RequestBody DictItemsParam param) {
        String dictCode = param.getDictCode();
        if (dictCode == null || dictCode.isEmpty()) {
            return R.fail(CommonPrompt.DICT_CODE_CANNOT_BE_EMPTY);
        }

        Long tenantId = getCurrentTenantId();
        List<DictItemVO> items = dictService.getDictItemsByCode(dictCode, tenantId);
        return R.ok(items);
    }

    @PostMapping("/itemsByPath")
    public R<List<DictItemVO>> itemsByPath(@RequestBody DictItemsByPathParam param) {
        String nodePath = param == null ? null : param.getNodePath();
        if (nodePath == null || nodePath.isEmpty()) {
            return R.fail(CommonPrompt.NODE_PATH_CANNOT_BE_EMPTY);
        }
        Long tenantId = getCurrentTenantId();
        return R.ok(dictService.getDictItemsByPath(nodePath, tenantId));
    }
    
    /**
     * 新增字典
     */
    @PostMapping("/create")
    public R<Boolean> create(@RequestBody DictDTO dictDTO) {
        dictDTO.setTenantId(getCurrentTenantId());
        dictService.addDict(dictDTO);
        return R.ok(true);
    }
    
    /**
     * 更新字典
     */
    @PostMapping("/update")
    public R<Boolean> update(@RequestBody DictDTO dictDTO) {
        dictService.updateDict(dictDTO);
        return R.ok(true);
    }
    
    /**
     * 删除字典
     */
    @PostMapping("/delete")
    public R<Boolean> delete(@RequestBody IdParam param) {
        Long id = param.getId();
        if (id == null) {
            return R.fail(CommonPrompt.DICT_ID_CANNOT_BE_EMPTY);
        }

        dictService.deleteDict(id);
        return R.ok(true);
    }
    
    /**
     * 获取当前租户ID
     * 
     * @return 当前租户ID，若获取失败则返回默认租户ID 1L
     */
    private Long getCurrentTenantId() {
        Long tenantId = CurrentUserUtils.getTenantId();
        return tenantId != null ? tenantId : 1L; // 默认租户
    }
}
