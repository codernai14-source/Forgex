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

import com.forgex.common.web.R;
import com.forgex.sys.domain.param.InitApplyParam;
import com.forgex.sys.domain.vo.InitStatusVO;

/**
 * 系统初始化服务。
 * <p>
 * 职责：查询是否首次使用、执行初始化（清库 + 写入安全配置 + 创建用户/租户/角色/绑定）。
 */
public interface InitService {
    /**
     * 查询初始化状态。
     * @return 初始化状态视图对象
     */
    R<InitStatusVO> status();
    /**
     * 提交初始化。
     * @param param 初始化参数
     * @return 是否成功
     */
    R<Boolean> apply(InitApplyParam param);
}
