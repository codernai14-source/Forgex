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
package com.forgex.auth.service;

import java.util.Map;

/**
 * 验证码服务接口
 * 作用：定义验证码生成与校验的统一能力，支持图片与滑块两种形态；
 * 说明：图片验证码采用后端本地生成并写入Redis；滑块验证码采用tianai或Redis令牌模式。
 */
public interface CaptchaService {
    /**
     * 生成图片验证码
     * 逻辑：生成图片 -> 写入Redis并返回 `captchaId` 与 `imageBase64`
     * @return 包含 `captchaId` 与 `imageBase64` 的键值对
     */
    Map<String, String> generateImageCaptcha();

    /**
     * 校验图片验证码
     * 逻辑：读取Redis中 `captchaId` 对应的答案 -> 比对成功后删除键
     * @param id 验证码ID
     * @param value 用户输入的验证码值
     * @return 是否校验通过
     */
    boolean verifyImage(String id, String value);

    /**
     * 校验滑块验证码（令牌）
     * 逻辑：根据配置选择键前缀 -> 判断令牌是否存在
     * @param token 前端校验成功后由后端发放的一次性令牌
     * @return 是否校验通过
     */
    boolean verifySlider(String token);
}
