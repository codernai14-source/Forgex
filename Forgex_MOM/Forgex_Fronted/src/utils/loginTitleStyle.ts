import type { CSSProperties } from 'vue'
import type { LoginSubtitleStyle, LoginTitleStyle } from '@/api/system/config'

/**
 * 登录页标题 / 副标题可视化样式计算。
 * <p>
 * 配置弹窗和登录页共用，避免再让用户手写 CSS。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */

export const LOGIN_FONT_PRESETS = [
  { key: 'system', value: '-apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Microsoft YaHei", sans-serif' },
  { key: 'yahei', value: '"Microsoft YaHei", "PingFang SC", sans-serif' },
  { key: 'pingfang', value: '"PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif' },
  { key: 'sourceHan', value: '"Source Han Sans SC", "Noto Sans SC", "Microsoft YaHei", sans-serif' },
  { key: 'orbitron', value: "'Orbitron', 'Segoe UI', sans-serif" },
  { key: 'segoe', value: '"Segoe UI", "Microsoft YaHei", sans-serif' },
  { key: 'arial', value: 'Arial, Helvetica, sans-serif' },
  { key: 'georgia', value: 'Georgia, "Times New Roman", serif' },
] as const

/**
 * 把颜色选择器可能返回的对象收成十六进制字符串。
 *
 * @param value 选择器值
 * @param fallback 回退色
 * @returns 十六进制颜色
 */
export function toHexColor(value: unknown, fallback: string): string {
  if (typeof value === 'string' && value.trim()) {
    return value.trim()
  }
  if (value && typeof value === 'object') {
    const color = value as { toHexString?: () => string; toRgbString?: () => string }
    if (typeof color.toHexString === 'function') {
      return color.toHexString()
    }
  }
  return fallback
}

/**
 * 解析已存字体到预设值，找不到时回落系统默认。
 *
 * @param fontFamily 已存 font-family
 * @returns 预设 value
 */
export function resolveFontPreset(fontFamily?: string): string {
  const current = String(fontFamily || '').trim()
  if (!current) {
    return LOGIN_FONT_PRESETS[0].value
  }
  const matched = LOGIN_FONT_PRESETS.find(item => item.value === current)
  return matched?.value || LOGIN_FONT_PRESETS[0].value
}

/**
 * 根据可视化配置生成标题 CSS。
 *
 * @param style 标题样式
 * @returns 可直接绑定到元素的样式对象
 */
export function buildLoginTitleCss(style?: Partial<LoginTitleStyle> | null): CSSProperties {
  const fontSize = Number(style?.fontSize || 28)
  const letterSpacing = Number(style?.letterSpacing || 0)
  const css: CSSProperties = {
    fontFamily: resolveFontPreset(style?.fontFamily),
    fontSize: `${fontSize}px`,
    fontWeight: (style?.fontWeight || '600') as CSSProperties['fontWeight'],
    fontStyle: (style?.fontStyle || 'normal') as CSSProperties['fontStyle'],
    letterSpacing: `${letterSpacing}px`,
  }
  if (style?.colorMode === 'gradient') {
    const from = style.gradientFrom || '#05d9e8'
    const to = style.gradientTo || '#ff2a6d'
    const angle = Number(style.gradientAngle ?? 90)
    css.backgroundImage = `linear-gradient(${angle}deg, ${from}, ${to})`
    css.backgroundClip = 'text'
    css.webkitBackgroundClip = 'text'
    css.color = 'transparent'
    css.webkitTextFillColor = 'transparent'
  } else {
    css.color = style?.color || '#ffffff'
    css.webkitTextFillColor = style?.color || '#ffffff'
  }
  return css
}

/**
 * 根据可视化配置生成副标题 CSS。
 *
 * @param style 副标题样式
 * @returns 可直接绑定到元素的样式对象
 */
export function buildLoginSubtitleCss(style?: Partial<LoginSubtitleStyle> | null): CSSProperties {
  return {
    fontFamily: resolveFontPreset(style?.fontFamily),
    fontSize: `${Number(style?.fontSize || 13)}px`,
    color: style?.color || '#9ca3af',
    fontWeight: (style?.fontWeight || 'normal') as CSSProperties['fontWeight'],
    fontStyle: (style?.fontStyle || 'normal') as CSSProperties['fontStyle'],
    letterSpacing: `${Number(style?.letterSpacing || 0)}px`,
  }
}
