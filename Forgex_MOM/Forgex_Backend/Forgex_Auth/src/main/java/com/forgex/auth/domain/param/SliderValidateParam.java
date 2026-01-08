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
 * 作用：承载前端提交的滑块轨迹与验证码ID，用于后端执行校验并发放令牌。
 */
@Data
public class SliderValidateParam {
    /** 滑块验证码ID（生成阶段返回的id） */
    private String id;
    /** 前端传入的轨迹数据对象（包含滑动路径、用时等） */
    private ImageCaptchaTrack track;
}
