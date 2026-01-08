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
