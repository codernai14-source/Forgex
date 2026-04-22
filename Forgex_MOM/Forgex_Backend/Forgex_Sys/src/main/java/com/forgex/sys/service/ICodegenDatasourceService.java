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
package com.forgex.sys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.sys.domain.dto.CodegenDatasourceDTO;
import com.forgex.sys.domain.dto.CodegenDatasourceQueryDTO;
import com.forgex.sys.domain.entity.SysCodegenDatasource;
import com.forgex.sys.domain.param.CodegenDatasourceSaveParam;
import com.forgex.sys.domain.param.CodegenDatasourceTestParam;

import java.util.List;

/**
 * 代码生成数据源服务接口
 *
 * @author coder_nai@163.com
 * @since 2026-04-21
 */
public interface ICodegenDatasourceService extends IService<SysCodegenDatasource> {

    /**
     * 分页查询数据源
     *
     * @param page 分页对象
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<CodegenDatasourceDTO> pageDatasources(Page<SysCodegenDatasource> page, CodegenDatasourceQueryDTO query);

    /**
     * 查询可用数据源列表
     *
     * @return 已启用数据源
     */
    List<CodegenDatasourceDTO> listEnabledDatasources();

    /**
     * 根据 ID 获取数据源详情
     *
     * @param id 主键 ID
     * @return 数据源详情
     */
    CodegenDatasourceDTO getDatasourceById(Long id);

    /**
     * 保存数据源
     *
     * @param param 保存参数
     * @return 主键 ID
     */
    Long saveDatasource(CodegenDatasourceSaveParam param);

    /**
     * 删除数据源
     *
     * @param id 主键 ID
     */
    void deleteDatasource(Long id);

    /**
     * 测试数据源连接
     *
     * @param param 测试参数
     * @return 是否成功
     */
    boolean testConnection(CodegenDatasourceTestParam param);

    /**
     * 按 ID 获取实体
     *
     * @param id 主键 ID
     * @return 数据源实体
     */
    SysCodegenDatasource requireEntity(Long id);
}
