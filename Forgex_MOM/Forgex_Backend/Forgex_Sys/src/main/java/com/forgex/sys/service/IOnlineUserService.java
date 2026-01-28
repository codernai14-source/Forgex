package com.forgex.sys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.sys.domain.dto.OnlineUserQueryDTO;
import com.forgex.sys.domain.vo.OnlineUserVO;

/**
 * 在线用户服务接口。
 * <p>
 * 基于登录上下文/会话数据，提供在线用户统计、列表与强制下线能力。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */
public interface IOnlineUserService {

    /**
     * 分页查询在线用户。
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return 在线用户分页结果
     */
    IPage<OnlineUserVO> pageOnlineUsers(Page<OnlineUserVO> page, OnlineUserQueryDTO query);

    /**
     * 统计指定租户的在线用户数。
     *
     * @param tenantId 租户ID
     * @return 在线用户数
     */
    Long countOnlineUsers(Long tenantId);

    /**
     * 强制下线指定 token 对应的会话。
     *
     * @param token tokenValue
     * @return 是否执行成功
     */
    boolean kickout(String token);
}
