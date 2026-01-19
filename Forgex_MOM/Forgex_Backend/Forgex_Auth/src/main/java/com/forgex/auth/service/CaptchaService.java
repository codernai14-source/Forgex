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
 * <p>
 * 作用：定义验证码生成与校验的统一能力，支持图片与滑块两种形态
 * </p>
 * <p>说明：</p>
 * <ul>
 *   <li>图片验证码采用后端本地生成并写入Redis</li>
 *   <li>滑块验证码采用tianai或Redis令牌模式</li>
 * </ul>
 * <p>主要功能：</p>
 * <ul>
 *   <li>{@link #generateImageCaptcha()} - 生成图片验证码</li>
 *   <li>{@link #generateSliderCaptcha()} - 生成滑块验证码</li>
 *   <li>{@link #validateSlider(String, Object)} - 校验滑块轨迹并发放令牌</li>
 *   <li>{@link #verifyImage(String, String)} - 校验图片验证码</li>
 *   <li>{@link #verifySlider(String)} - 校验滑块验证码（令牌）</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see com.forgex.auth.service.impl.CaptchaServiceImpl
 */
public interface CaptchaService {
    /**
     * 生成图片验证码
     * <p>
     * 逻辑：生成图片 -> 写入Redis并返回 {@code captchaId} 与 {@code imageBase64}
     * </p>
     * <p>详细流程：</p>
     * <ul>
     *   <li>读取配置获取图片参数（宽度、高度、字符长度）</li>
     *   <li>生成图片验证码与答案</li>
     *   <li>将验证码答案写入Redis并设置过期时间</li>
     *   <li>返回包含验证码ID与图片Base64的Map</li>
     * </ul>
     *
     * @return 包含 {@code captchaId} 与 {@code imageBase64} 的键值对
     * @see #verifyImage(String, String)
     */
    Map<String, String> generateImageCaptcha();

    /**
     * 生成滑块验证码
     * <p>
     * 逻辑：调用 tianai-captcha 生成滑块数据，返回渲染所需结构（含 id）
     * </p>
     * <p>详细流程：</p>
     * <ul>
     *   <li>调用tianai-captcha生成滑块验证码数据</li>
     *   <li>返回滑块图片、缺口图片、验证码ID等渲染所需数据</li>
     * </ul>
     *
     * @return 验证码渲染数据，包含滑块图片、缺口图片、验证码ID等
     * @see #validateSlider(String, Object)
     * @see #verifySlider(String)
     */
    Object generateSliderCaptcha();

    /**
     * 校验滑块轨迹并发放令牌
     * <p>
     * 逻辑：校验前端轨迹 -> 成功后生成一次性令牌并写入Redis（前缀/过期来自 JSON 配置）
     * </p>
     * <p>详细流程：</p>
     * <ul>
     *   <li>接收前端传来的滑块轨迹数据</li>
     *   <li>调用tianai-captcha校验轨迹是否正确</li>
     *   <li>校验通过后生成一次性令牌</li>
     *   <li>将令牌存入Redis，设置过期时间</li>
     *   <li>返回令牌供登录时使用</li>
     * </ul>
     *
     * @param id 验证码ID
     * @param track 滑块轨迹
     * @return 令牌字符串（登录时作为验证码使用），校验失败返回null
     * @see #verifySlider(String)
     */
    String validateSlider(String id, Object track);

    /**
     * 校验图片验证码
     * <p>
     * 逻辑：读取Redis中 {@code captchaId} 对应的答案 -> 比对成功后删除键
     * </p>
     * <p>详细流程：</p>
     * <ul>
     *   <li>根据验证码ID从Redis中获取正确答案</li>
     *   <li>忽略大小写比对用户输入的验证码</li>
     *   <li>比对成功后删除Redis中的验证码，防止重复使用</li>
     * </ul>
     *
     * @param id 验证码ID
     * @param value 用户输入的验证码值
     * @return 是否校验通过
     * @see #generateImageCaptcha()
     */
    boolean verifyImage(String id, String value);

    /**
     * 校验滑块验证码（令牌）
     * <p>
     * 逻辑：根据配置选择键前缀 -> 判断令牌是否存在
     * </p>
     * <p>详细流程：</p>
     * <ul>
     *   <li>读取验证码配置</li>
     *   <li>根据是否二次验证选择Redis键前缀</li>
     *   <li>检查令牌是否存在于Redis中</li>
     * </ul>
     *
     * @param token 前端校验成功后由后端发放的一次性令牌
     * @return 是否校验通过
     * @see #validateSlider(String, Object)
     */
    boolean verifySlider(String token);
}
