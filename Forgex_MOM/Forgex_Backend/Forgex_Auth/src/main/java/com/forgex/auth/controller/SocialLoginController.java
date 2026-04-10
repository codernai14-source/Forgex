package com.forgex.auth.controller;

import com.forgex.auth.service.SocialLoginService;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.web.R;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 社交登录控制器
 * <p>
 * 提供第三方社交登录的授权 URL 获取和回调处理功能。
 * </p>
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>获取第三方平台授权 URL</li>
 *   <li>处理微信登录回调</li>
 *   <li>处理钉钉登录回调</li>
 * </ul>
 * <p><strong>支持的第三方平台：</strong></p>
 * <ul>
 *   <li>微信（wechat）</li>
 *   <li>钉钉（dingtalk）</li>
 * </ul>
 * <p><strong>登录流程：</strong></p>
 * <ol>
 *   <li>前端调用/authorizeUrl 接口获取授权 URL</li>
 *   <li>前端跳转到第三方授权页面</li>
 *   <li>用户授权后，第三方平台回调/callback/{platform}接口</li>
 *   <li>后端处理回调，完成登录</li>
 * </ol>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-03-28
 * @see SocialLoginService
 */
@RestController
@RequestMapping("/auth/social")
@RequiredArgsConstructor
public class SocialLoginController {

    /**
     * 社交登录服务
     */
    private final SocialLoginService socialLoginService;

    /**
     * 获取第三方平台授权 URL
     * <p>
     * 根据平台类型构建对应的授权 URL，用于前端跳转到第三方授权页面。
     * </p>
     * <p><strong>执行流程：</strong></p>
     * <ol>
     *   <li>参数校验：平台类型不能为空</li>
     *   <li>调用服务构建授权 URL</li>
     *   <li>构建失败时返回操作失败</li>
     *   <li>成功时返回授权 URL</li>
     * </ol>
     * 
     * @param platform 第三方平台类型，如"wechat"、"dingtalk"
     * @return 包含授权 URL 的统一返回结构
     * @throws com.forgex.common.exception.BusinessException 平台类型不支持时抛出
     * @see SocialLoginService#buildAuthorizeUrl(String)
     */
    @GetMapping("/authorizeUrl")
    public R<String> authorizeUrl(@RequestParam("platform") String platform) {
        // 参数校验：平台类型不能为空
        if (!StringUtils.hasText(platform)) {
            return R.fail(CommonPrompt.PARAM_EMPTY);
        }
        
        // 调用服务构建授权 URL
        String url = socialLoginService.buildAuthorizeUrl(platform);
        
        // 构建失败时返回操作失败
        if (!StringUtils.hasText(url)) {
            return R.fail(CommonPrompt.OPERATION_FAILED);
        }
        
        // 成功时返回授权 URL
        return R.ok(url);
    }

    /**
     * 微信登录回调接口
     * <p>
     * 处理微信授权后的回调请求。
     * 当前版本暂未实现，直接返回操作失败。
     * </p>
     * 
     * @param code 授权码，第三方平台返回
     * @param state 状态参数，用于防止 CSRF 攻击
     * @return 包含登录结果的统一返回结构，当前版本返回操作失败
     */
    @GetMapping("/callback/wechat")
    public R<String> wechatCallback(@RequestParam(value = "code", required = false) String code,
                                    @RequestParam(value = "state", required = false) String state) {
        // 当前版本暂未实现，直接返回操作失败
        return R.fail(CommonPrompt.OPERATION_FAILED);
    }

    /**
     * 钉钉登录回调接口
     * <p>
     * 处理钉钉授权后的回调请求。
     * 当前版本暂未实现，直接返回操作失败。
     * </p>
     * 
     * @param code 授权码，第三方平台返回
     * @param state 状态参数，用于防止 CSRF 攻击
     * @return 包含登录结果的统一返回结构，当前版本返回操作失败
     */
    @GetMapping("/callback/dingtalk")
    public R<String> dingtalkCallback(@RequestParam(value = "code", required = false) String code,
                                      @RequestParam(value = "state", required = false) String state) {
        // 当前版本暂未实现，直接返回操作失败
        return R.fail(CommonPrompt.OPERATION_FAILED);
    }
}
