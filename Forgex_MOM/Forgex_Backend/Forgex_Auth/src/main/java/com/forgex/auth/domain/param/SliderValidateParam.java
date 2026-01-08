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
