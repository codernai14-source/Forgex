import http from '../http'

export function captchaImage() {
  return http.post('/auth/captcha/image')
}

export function captchaSlider() {
  return http.post('/auth/captcha/slider')
}

export function captchaSliderValidate(data: { id: string; track: any }) {
  return http.post('/auth/captcha/slider/validate', data)
}

