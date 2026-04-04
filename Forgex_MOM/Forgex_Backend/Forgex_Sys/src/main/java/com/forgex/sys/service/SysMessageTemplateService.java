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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.sys.domain.dto.SysMessageTemplateSaveDTO;
import com.forgex.sys.domain.param.SysMessageTemplateParam;
import com.forgex.sys.domain.vo.SysMessageTemplateVO;

/**
 * 消息模板服务接口
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
public interface SysMessageTemplateService {
    
    /**
     * 分页查询消息模板
     * 
     * @param param 查询参数
     * @return 分页结果
     */
    Page<SysMessageTemplateVO> page(SysMessageTemplateParam param);
    
    /**
     * 根据ID查询消息模板详情
     * 
     * @param id 模板ID
     * @return 模板详情
     */
    SysMessageTemplateVO getById(Long id);
    
    /**
     * 保存消息模板(新增或修改)
     * 
     * @param dto 保存参数
     * @return 模板ID
     */
    Long save(SysMessageTemplateSaveDTO dto);
    
    /**
     * 删除消息模板
     * 
     * @param id 模板ID
     * @return 是否成功
     */
    boolean delete(Long id);
    
    /**
     * 批量删除消息模板
     * 
     * @param ids 模板ID列表
     * @return 是否成功
     */
    boolean deleteBatch(java.util.List<Long> ids);
    
    /**
     * 检查模板编码是否存在
     * 
     * @param code 模板编码
     * @return true=存在，false=不存在
     */
    boolean existsByCode(String code);
    
    /**
     * 检查模板编码是否存在（排除指定 ID）
     * 
     * @param code 模板编码
     * @param id 要排除的模板 ID
     * @return true=存在，false=不存在
     */
    boolean existsByCodeExcludeId(String code, Long id);
}



