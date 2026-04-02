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
package com.forgex.sys.validator;

import com.forgex.common.exception.BusinessException;
import com.forgex.sys.domain.param.TableConfigBatchDeleteParam;
import com.forgex.sys.domain.param.TableConfigGetParam;
import com.forgex.sys.domain.param.UserColumnConfigParam;
import com.forgex.sys.domain.vo.TableConfigDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 表格配置数据校验器
 * 
 * 职责：
 * - 校验表格配置参数的合法性
 * - 校验必填字段
 * - 不包含业务逻辑，只做参数校验
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class TableConfigValidator {
    
    /**
     * 校验表格配置 ID 合法性
     * 
     * @param param 查询参数
     * @throws BusinessException 参数校验失败时抛出
     */
    public void validateId(TableConfigGetParam param) {
        Assert.notNull(param, "查询参数不能为空");
        Assert.notNull(param.getId(), "表格配置 ID 不能为空");
    }
    
    /**
     * 校验表格配置 ID 合法性（简化版）
     * 
     * @param id 表格配置 ID
     * @throws BusinessException 参数校验失败时抛出
     */
    public void validateId(Long id) {
        Assert.notNull(id, "表格配置 ID 不能为空");
    }
    
    /**
     * 校验批量删除的 ID 列表
     * 
     * @param param 批量删除参数
     * @throws BusinessException 参数校验失败时抛出
     */
    public void validateBatchIds(TableConfigBatchDeleteParam param) {
        Assert.notNull(param, "批量删除参数不能为空");
        Assert.isTrue(!CollectionUtils.isEmpty(param.getIds()), "表格配置 ID 列表不能为空");
    }
    
    /**
     * 校验创建表格配置参数
     * 
     * @param vo 表格配置详情 VO
     * @throws BusinessException 参数校验失败时抛出
     */
    public void validateForCreate(TableConfigDetailVO vo) {
        Assert.notNull(vo, "表格配置参数不能为空");
        Assert.hasText(vo.getTableCode(), "表格编码不能为空");
        Assert.hasText(vo.getTableNameI18nJson(), "表格名称不能为空");
    }
    
    /**
     * 校验更新表格配置参数
     * 
     * @param vo 表格配置详情 VO
     * @throws BusinessException 参数校验失败时抛出
     */
    public void validateForUpdate(TableConfigDetailVO vo) {
        Assert.notNull(vo, "表格配置参数不能为空");
        Assert.notNull(vo.getId(), "表格配置 ID 不能为空");
    }
    
    /**
     * 校验用户列配置参数
     * 
     * @param param 用户列配置参数
     * @throws BusinessException 参数校验失败时抛出
     */
    public void validateUserColumnConfig(UserColumnConfigParam param) {
        Assert.notNull(param, "用户列配置参数不能为空");
        Assert.isTrue(StringUtils.hasText(param.getTableCode()), "表格编码不能为空");
        Assert.isTrue(!CollectionUtils.isEmpty(param.getColumns()), "列配置不能为空");
    }
    
    /**
     * 校验表格编码参数
     * 
     * @param tableCode 表格编码
     * @throws BusinessException 参数校验失败时抛出
     */
    public void validateTableCode(String tableCode) {
        Assert.isTrue(StringUtils.hasText(tableCode), "表格编码不能为空");
    }
}
