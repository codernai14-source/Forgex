package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.R;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.sys.domain.dto.OnlineUserQueryDTO;
import com.forgex.sys.domain.vo.OnlineUserVO;
import com.forgex.sys.service.IOnlineUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 在线用户 Controller。
 * <p>
 * 提供在线用户的分页列表、在线数量统计、强制下线等接口。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */
@RestController
@RequestMapping("/sys/online")
@RequiredArgsConstructor
public class OnlineUserController {

    private final IOnlineUserService onlineUserService;

    /**
     * 分页查询在线用户列表。
     *
     * @param query 查询参数
     * @return 在线用户分页结果
     */
    @RequirePerm("sys:online:list")
    @PostMapping("/list")
    public R<IPage<OnlineUserVO>> list(@RequestBody OnlineUserQueryDTO query) {
        long current = query != null && query.getCurrent() != null ? query.getCurrent() : 1L;
        long size = query != null && query.getSize() != null ? query.getSize() : 20L;
        Page<OnlineUserVO> page = new Page<>(current, size);
        return R.ok(onlineUserService.pageOnlineUsers(page, query));
    }

    /**
     * 获取当前租户在线用户数。
     *
     * @param body 请求体（兼容扩展，当前未使用）
     * @return 在线用户数
     */
    @RequirePerm("sys:online:list")
    @PostMapping("/count")
    public R<Long> count(@RequestBody Map<String, Object> body) {
        Long tenantId = TenantContext.get();
        return R.ok(onlineUserService.countOnlineUsers(tenantId));
    }

    /**
     * 强制下线指定 token 的会话。
     *
     * @param body 请求体，包含 token
     * @return 是否成功
     */
    @RequirePerm("sys:online:kickout")
    @PostMapping("/kickout")
    public R<Boolean> kickout(@RequestBody Map<String, Object> body) {
        String token = body == null ? null : (String) body.get("token");
        return R.ok(CommonPrompt.STOP_SUCCESS, onlineUserService.kickout(token));
    }
}
