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
package com.forgex.auth.domain.param;

import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import lombok.Data;

/**
 * 滑块验证码校验参数
 * <p>
 * 作用：承载前端提交的滑块轨迹与验证码 ID，用于后端执行校验并发放令牌。
 * </p>
 *
 * @author coder_nai
 * @version 1.0.0
 * @since 2026-01-08
 * @see com.forgex.auth.controller.AuthController
 * @see com.forgex.auth.service.CaptchaService
 */
@Data
public class SliderValidateParam {
    /**
     * 滑块验证码 ID
     * <p>
     * 生成阶段返回的验证码标识，用于在 Redis 中查找对应的验证码答案
     * </p>
     */
    private String id;

    /**
     * 前端传入的轨迹数据对象
     * <p>
     * 包含滑动路径、用时、速度等信息，用于后端验证是否为人工操作
     * </p>
     *
     * @see cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack
     */
    private ImageCaptchaTrack track;
}
