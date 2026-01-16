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
import com.forgex.sys.domain.param.CommonTableQueryParam;

/**
 * 通用表格数据服务接口
 * <p>提供通用表格数据查询的业务逻辑，包括登录日志查询等功能。</p>
 * 
 * @author coder_nai@163.com
 * @date 2026-01-16
 */
public interface CommonTableDataService {
    
    /**
     * 查询通用表格数据
     * 
     * @param param 查询参数，包含tableCode、当前页、每页大小和查询条件
     * @return 分页查询结果
     */
    IPage<?> queryTableData(CommonTableQueryParam param);
}