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
package com.forgex.auth.service.impl;

import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.IdUtil;
import com.forgex.auth.service.CaptchaService;
import com.forgex.common.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.forgex.auth.domain.config.CaptchaConfig;
import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cn.hutool.json.JSONUtil;

/**
 * 验证码服务实现
 * <p>实现图片验证码生成/校验与滑块令牌校验。</p>
 * <h3>验证码生成逻辑：</h3>
 * <ol>
 *   <li><strong>图片验证码</strong>：使用本地生成图片验证码，答案写入Redis；校验比对后删除防复用。</li>
 *   <li><strong>滑块验证码</strong>：调用tianai-captcha生成滑块数据，返回渲染所需结构（含id）。</li>
 * </ol>
 * <h3>验证码校验逻辑：</h3>
 * <ol>
 *   <li><strong>图片验证码</strong>：按前缀读取Redis中的答案，忽略大小写比对，通过后删除键防复用。</li>
 *   <li><strong>滑块验证码</strong>：根据配置读取令牌前缀与过期设置，判断令牌是否存在。</li>
 * </ol>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see com.forgex.auth.service.CaptchaService
 * @see com.forgex.auth.domain.config.CaptchaConfig
 */
@Service
public class CaptchaServiceImpl implements CaptchaService {
    /** Redis操作模板，用于存取验证码数据 */
    @Autowired
    private StringRedisTemplate redis;
    /** 通用配置服务，用于读取单键JSON的验证码配置 */
    @Autowired
    private ConfigService configService;
    /** 滑块验证码应用，用于生成和校验滑块验证码 */
    @Autowired
    private cloud.tianai.captcha.application.ImageCaptchaApplication captchaApplication;

    /**
     * 生成图片验证码
     * <p>生成图片验证码，将答案写入Redis，并返回验证码ID与图片Base64。</p>
     * <p>处理流程：</p>
     * <ol>
     *   <li>读取配置获取图片参数（宽度、高度、字符长度）</li>
     *   <li>生成图片验证码与答案</li>
     *   <li>将验证码答案写入Redis并设置过期时间</li>
     *   <li>返回包含验证码ID与图片Base64的Map</li>
     * </ol>
     * @return Map，包含 {@code captchaId} 与 {@code imageBase64}
     * @see CaptchaUtil#createLineCaptcha(int, int, int, int)
     */
    @Override
    public Map<String, String> generateImageCaptcha() {
        // 读取验证码配置
        CaptchaConfig cfg = configService.getJson("login.captcha", CaptchaConfig.class, CaptchaConfig.defaults());
        // 获取图片宽度，默认120
        int w = cfg.getImage() != null ? cfg.getImage().getWidth() : 120;
        // 获取图片高度，默认40
        int h = cfg.getImage() != null ? cfg.getImage().getHeight() : 40;
        // 获取验证码字符长度，默认4
        int len = cfg.getImage() != null ? cfg.getImage().getLength() : 4;
        // 创建线段干扰的图片验证码
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(w, h, len, 100);
        // 获取验证码答案
        String code = captcha.getCode();
        // 生成唯一ID作为验证码ID
        String id = IdUtil.fastUUID();
        // 获取Redis键前缀，默认为"captcha:image"
        String imagePrefix = cfg.getImage() != null && StringUtils.hasText(cfg.getImage().getKeyPrefix()) ? cfg.getImage().getKeyPrefix() : "captcha:image";
        // 获取验证码过期时间，默认120秒
        int imageExpireSec = cfg.getImage() != null ? cfg.getImage().getExpireSeconds() : 120;
        // 构造Redis键
        String key = imagePrefix + ":" + id;
        // 将验证码答案存入Redis，设置过期时间
        redis.opsForValue().set(key, code, imageExpireSec, TimeUnit.SECONDS);
        // 创建返回结果Map
        Map<String, String> map = new HashMap<>();
        // 设置验证码ID
        map.put("captchaId", id);
        // 设置图片Base64编码
        map.put("imageBase64", captcha.getImageBase64());
        return map;
    }

    /**
     * 生成滑块验证码
     * <p>逻辑：调用 tianai-captcha 生成滑块数据，返回渲染所需结构（含 id）</p>
     *
     * @return 验证码渲染数据
     */
    @Override
    public Object generateSliderCaptcha() {
        // 调用tianai-captcha生成滑块验证码
        var res = captchaApplication.generateCaptcha(CaptchaTypeConstant.SLIDER);
        // 返回验证码渲染数据
        return res.getData();
    }

    /**
     * 校验滑块轨迹并发放令牌
     * <p>逻辑：校验前端轨迹 -> 成功后生成一次性令牌并写入Redis（前缀/过期来自 JSON 配置）</p>
     *
     * @param id 验证码ID
     * @param track 滑块轨迹
     * @return 令牌字符串（登录时作为验证码使用），校验失败返回null
     */
    @Override
    public String validateSlider(String id, Object track) {
        if (!StringUtils.hasText(id) || track == null) {
            return null;
        }
        ImageCaptchaTrack imageCaptchaTrack;
        if (track instanceof ImageCaptchaTrack) {
            imageCaptchaTrack = (ImageCaptchaTrack) track;
        } else if (track instanceof Map<?, ?> map) {
            imageCaptchaTrack = JSONUtil.toBean(JSONUtil.parseObj(map), ImageCaptchaTrack.class);
        } else {
            imageCaptchaTrack = JSONUtil.toBean(JSONUtil.parseObj(track), ImageCaptchaTrack.class);
        }
        if (imageCaptchaTrack == null) {
            return null;
        }
        // 调用tianai-captcha校验滑块轨迹
        var matching = captchaApplication.matching(id, imageCaptchaTrack);
        // 校验成功
        if (matching != null && matching.isSuccess()) {
            // 生成唯一令牌
            String token = IdUtil.fastUUID();
            // 读取验证码配置
            CaptchaConfig cfg = configService.getJson("login.captcha", CaptchaConfig.class, CaptchaConfig.defaults());
            // 判断是否启用二次验证
            boolean secondary = cfg.getSlider() != null && cfg.getSlider().isSecondaryEnabled();
            // 根据是否二次验证选择Redis键前缀
            String keyPrefix = secondary
                    ? (cfg.getSlider() != null && StringUtils.hasText(cfg.getSlider().getSecondaryKeyPrefix()) ? cfg.getSlider().getSecondaryKeyPrefix() : "captcha:secondary")
                    : (cfg.getSlider() != null && StringUtils.hasText(cfg.getSlider().getKeyPrefix()) ? cfg.getSlider().getKeyPrefix() : "captcha:slider");
            // 获取令牌过期时间，默认120秒
            int expireSec = cfg.getSlider() != null ? cfg.getSlider().getTokenExpireSeconds() : 120;
            // 将令牌存入Redis，设置过期时间
            redis.opsForValue().set(keyPrefix + ":" + token, "1", expireSec, TimeUnit.SECONDS);
            return token;
        }
        return null;
    }

    /**
     * 校验图片验证码
     * <p>逻辑：按前缀读取答案 -> 忽略大小写比对 -> 通过后删除Redis键</p>
     *
     * @param id 验证码ID
     * @param value 用户输入的验证码
     * @return 是否通过
     */
    @Override
    public boolean verifyImage(String id, String value) {
        // 校验参数是否为空
        if (!StringUtils.hasText(id) || !StringUtils.hasText(value)) return false;
        // 读取验证码配置
        CaptchaConfig cfg = configService.getJson("login.captcha", CaptchaConfig.class, CaptchaConfig.defaults());
        // 获取Redis键前缀，默认为"captcha:image"
        String imagePrefix = cfg.getImage() != null && StringUtils.hasText(cfg.getImage().getKeyPrefix()) ? cfg.getImage().getKeyPrefix() : "captcha:image";
        // 构造Redis键
        String key = imagePrefix + ":" + id;
        // 从Redis中获取正确答案
        String expect = redis.opsForValue().get(key);
        // 验证码不存在或已过期
        if (expect == null) return false;
        // 忽略大小写比对验证码
        boolean ok = expect.equalsIgnoreCase(value);
        // 比对成功后删除Redis中的验证码，防止重复使用
        if (ok) redis.delete(key);
        return ok;
    }

    /**
     * 校验滑块令牌
     * <p>逻辑：读取 {@code login.captcha} 配置 -> 根据是否二次验证选择前缀 -> 判断令牌键是否存在</p>
     *
     * @param token 一次性令牌
     * @return 是否通过
     */
    @Override
    public boolean verifySlider(String token) {
        // 校验令牌是否为空
        if (!StringUtils.hasText(token)) return false;
        // 读取验证码配置
        CaptchaConfig cfg = configService.getJson("login.captcha", CaptchaConfig.class, CaptchaConfig.defaults());
        // 获取滑块验证码提供者，默认为"redis-token"
        String provider = cfg.getSlider() != null && StringUtils.hasText(cfg.getSlider().getProvider()) ? cfg.getSlider().getProvider() : "redis-token";
        // 使用redis-token模式
        if ("redis-token".equalsIgnoreCase(provider)) {
            // 判断是否启用二次验证
            boolean secondary = cfg.getSlider() != null && cfg.getSlider().isSecondaryEnabled();
            // 根据是否二次验证选择Redis键前缀
            String keyPrefix = secondary
                    ? (cfg.getSlider() != null && StringUtils.hasText(cfg.getSlider().getSecondaryKeyPrefix()) ? cfg.getSlider().getSecondaryKeyPrefix() : "captcha:secondary")
                    : (cfg.getSlider() != null && StringUtils.hasText(cfg.getSlider().getKeyPrefix()) ? cfg.getSlider().getKeyPrefix() : "captcha:slider");
            // 构造Redis键
            String key = keyPrefix + ":" + token;
            // 从Redis中获取令牌值
            String v = redis.opsForValue().get(key);
            // 令牌存在则校验通过
            return v != null;
        }
        return false;
    }
}
