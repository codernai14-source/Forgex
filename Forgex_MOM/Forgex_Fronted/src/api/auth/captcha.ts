import http from '../http'

/**
 * 获取验证码图片 API
 * @returns 验证码图片信息（base64 或 URL）
 * @throws 获取失败时抛出异常
 */
export function captchaImage() {
  return http.post('/auth/captcha/image')
}

/**
 * 获取滑块验证码 API
 * @returns 滑块验证码信息（包含图片、滑块位置等）
 * @throws 获取失败时抛出异常
 */
export function captchaSlider() {
  return http.post('/auth/captcha/slider')
}

/**
 * 滑块验证码验证 API
 * @param data 验证参数
 * @param data.id 验证码 ID
 * @param data.track 滑动轨迹数据
 * @returns 验证结果
 * @throws 验证失败时抛出异常
 */
export function captchaSliderValidate(data: { id: string; track: any }) {
  return http.post('/auth/captcha/slider/validate', data)
}

