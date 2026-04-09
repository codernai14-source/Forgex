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
import com.forgex.sys.domain.dto.DictDTO;
import com.forgex.sys.domain.param.DictPageParam;
import com.forgex.sys.domain.vo.DictItemVO;
import com.forgex.sys.domain.vo.DictTreeVO;

import java.util.List;

/**
 * 数据字典服务接口
 * 
 * @author coder_nai@163.com
 * @date 2025-01-13
 */
public interface IDictService {
    
    /**
     * 获取字典树
     * 
     * @param tenantId 租户ID
     * @return 字典树列表
     */
    List<DictTreeVO> getDictTree(Long tenantId);

    /**
     * 鍒嗛〉鑾峰彇瀛楀吀鏍戞牴鑺傜偣锛堟惡甯﹀叾瀹屾暣瀛愭爲锛?
     *
     * @param tenantId 绉熸埛ID
     * @param pageNum  椤电爜
     * @param pageSize 姣忛〉鏉℃暟
     * @return 鍒嗛〉瀛楀吀鏍?
     */
    IPage<DictTreeVO> pageDictTree(Long tenantId, DictPageParam pageParam);
    
    /**
     * 根据字典编码获取字典项列表
     * 
     * @param dictCode 字典编码
     * @param tenantId 租户ID
     * @return 字典项列表
     */
    List<DictItemVO> getDictItemsByCode(String dictCode, Long tenantId);

    List<DictItemVO> getDictItemsByPath(String nodePath, Long tenantId);
    
    /**
     * 新增字典
     * 
     * @param dictDTO 字典数据
     */
    void addDict(DictDTO dictDTO);
    
    /**
     * 更新字典
     * 
     * @param dictDTO 字典数据
     */
    void updateDict(DictDTO dictDTO);
    
    /**
     * 删除字典
     * 
     * @param id 字典ID
     */
    void deleteDict(Long id, Long tenantId);
    
    /**
     * 清除字典缓存
     * 
     * @param dictCode 字典编码
     * @param tenantId 租户ID
     */
    void clearDictCache(String dictCode, Long tenantId);
}
