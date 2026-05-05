import type { ThemeTokens, LayoutConfig } from './types'
import type { CSSProperties } from 'vue'

/**
 * 调整十六进制颜色亮度。
 *
 * @param hexColor 十六进制颜色，例如 "#1677ff"。
 * @param amount 亮度调整值，范围通常为 -1 到 1，正数变亮，负数变暗。
 * @returns 调整后的十六进制颜色。
 */
function adjustColor(hexColor: string, amount: number): string {
  if (!hexColor || !hexColor.startsWith('#')) {
    return hexColor
  }

  const hex = hexColor.slice(1)
  const r = parseInt(hex.substring(0, 2), 16)
  const g = parseInt(hex.substring(2, 4), 16)
  const b = parseInt(hex.substring(4, 6), 16)

  // RGB 转 HSL。
  const rNorm = r / 255
  const gNorm = g / 255
  const bNorm = b / 255

  const max = Math.max(rNorm, gNorm, bNorm)
  const min = Math.min(rNorm, gNorm, bNorm)
  let h = 0
  let s = 0
  let l = (max + min) / 2

  if (max !== min) {
    const d = max - min
    s = l > 0.5 ? d / (2 - max - min) : d / (max + min)

    switch (max) {
      case rNorm:
        h = ((gNorm - bNorm) / d + (gNorm < bNorm ? 6 : 0)) / 6
        break
      case gNorm:
        h = ((bNorm - rNorm) / d + 2) / 6
        break
      case bNorm:
        h = ((rNorm - gNorm) / d + 4) / 6
        break
    }
  }

  l = Math.max(0, Math.min(1, l + amount))

  const hue2rgb = (p: number, q: number, t: number) => {
    if (t < 0) t += 1
    if (t > 1) t -= 1
    if (t < 1 / 6) return p + (q - p) * 6 * t
    if (t < 1 / 2) return q
    if (t < 2 / 3) return p + (q - p) * (2 / 3 - t) * 6
    return p
  }

  let rNew, gNew, bNew
  if (s === 0) {
    rNew = gNew = bNew = l
  } else {
    const q = l < 0.5 ? l * (1 + s) : l + s - l * s
    const p = 2 * l - q
    rNew = hue2rgb(p, q, h + 1 / 3)
    gNew = hue2rgb(p, q, h)
    bNew = hue2rgb(p, q, h - 1 / 3)
  }

  const toHex = (x: number) => {
    const hex = Math.round(x * 255).toString(16)
    return hex.length === 1 ? '0' + hex : hex
  }

  return '#' + toHex(rNew) + toHex(gNew) + toHex(bNew)
}

/**
 * 将十六进制颜色转换为 rgba 字符串。
 *
 * @param hexColor 十六进制颜色，支持 #RGB 或 #RRGGBB。
 * @param alpha 透明度，范围为 0 到 1。
 * @param 降级方案 颜色解析失败时返回的备用值。
 */
function withAlpha(hexColor: string, alpha: number, 降级方案: string): string {
  if (!hexColor || !hexColor.startsWith('#')) {
    return 降级方案
  }

  let hex = hexColor.slice(1)
  if (hex.length === 3) {
    hex = hex.split('').map(char => `${char}${char}`).join('')
  }
  if (hex.length !== 6) {
    return 降级方案
  }

  const r = parseInt(hex.substring(0, 2), 16)
  const g = parseInt(hex.substring(2, 4), 16)
  const b = parseInt(hex.substring(4, 6), 16)
  const normalizedAlpha = Math.max(0, Math.min(1, alpha))

  return `rgba(${r}, ${g}, ${b}, ${normalizedAlpha})`
}

/**
 * 生成 Theme Token 对应的 CSS 变量。
 *
 * @param tokens 主题 Token 集合。
 * @param layoutConfig 布局配置。
 * @returns 可直接绑定到 Vue style 的 CSS 变量对象。
 */
export function generateCSSVariables(
  tokens: ThemeTokens,
  layoutConfig: LayoutConfig
): CSSProperties {
  const primaryColor = layoutConfig.themeColor || tokens.colorPrimary
  const radius = `${layoutConfig.borderRadius}px`
  const fontSize = layoutConfig.fontSize || '14px'
  const contentWidth = layoutConfig.contentWidth === 'fixed' ? '1200px' : '100%'
  const themeColorSoft = withAlpha(primaryColor, 0.12, tokens.colorPrimaryBg)
  const themeColorSoftStrong = withAlpha(primaryColor, 0.18, tokens.colorPrimaryBgHover)
  const textSecondarySoft = withAlpha(tokens.colorTextSecondary, 0.24, tokens.colorBorder)
  const textSecondarySoftHover = withAlpha(tokens.colorTextSecondary, 0.38, tokens.colorSplit)

  return {
    // ==================== 布局背景 ====================
    '--fx-layout-bg': tokens.colorBgBase,
    '--fx-layout-color': tokens.colorText,
    '--fx-bg-base': tokens.colorBgBase,
    '--fx-bg-layout': tokens.colorBgLayout,

    // ==================== 容器背景 ====================
    '--fx-header-bg': tokens.colorBgContainer,
    '--fx-header-color': tokens.colorText,
    '--fx-sider-bg': tokens.colorBgContainer,
    '--fx-sider-mini-bg': tokens.colorBgContainer,
    '--fx-content-bg': tokens.colorBgContainer,
    '--fx-footer-bg': tokens.colorBgContainer,
    '--fx-bg-container': tokens.colorBgContainer,
    '--fx-bg-elevated': tokens.colorBgElevated,

    // ==================== 边框 ====================
    '--fx-border-color': tokens.colorBorder,
    '--fx-border-secondary': tokens.colorBorderSecondary,
    '--fx-split-color': tokens.colorSplit,

    // ==================== 文本 ====================
    '--fx-text-color': tokens.colorTextSecondary,
    '--fx-text-primary': tokens.colorText,
    '--fx-text-secondary': tokens.colorTextSecondary,
    '--fx-text-tertiary': tokens.colorTextTertiary,
    '--fx-text-disabled': tokens.colorTextDisabled,

    // ==================== 填充与页签 ====================
    '--fx-tab-bg': tokens.colorFillAlter,
    '--fx-tab-hover-bg': tokens.colorFill,
    '--fx-fill': tokens.colorFill,
    '--fx-fill-secondary': tokens.colorFillSecondary,
    '--fx-fill-alter': tokens.colorFillAlter,

    // ==================== 主题色 ====================
    '--fx-theme-color': primaryColor,
    '--fx-primary': primaryColor,
    '--fx-primary-hover': adjustColor(primaryColor, 0.15),
    '--fx-primary-active': adjustColor(primaryColor, -0.1),
    '--fx-primary-bg': themeColorSoft,
    '--fx-primary-soft': themeColorSoft,
    '--fx-primary-soft-strong': themeColorSoftStrong,

    // ==================== 功能色 ====================
    '--fx-success': tokens.colorSuccess,
    '--fx-success-bg': tokens.colorSuccessBg,
    '--fx-warning': tokens.colorWarning,
    '--fx-warning-bg': tokens.colorWarningBg,
    '--fx-error': tokens.colorError,
    '--fx-error-bg': tokens.colorErrorBg,
    '--fx-info': tokens.colorInfo,
    '--fx-info-bg': tokens.colorInfoBg,

    // ==================== 字体 ====================
    '--fx-font-size': fontSize,
    '--fx-font-size-lg': `${tokens.fontSizeLG}px`,
    '--fx-font-size-sm': `${tokens.fontSizeSM}px`,
    '--fx-font-family': tokens.fontFamily,
    '--fx-line-height': tokens.lineHeight.toString(),

    // ==================== 圆角 ====================
    '--fx-radius': radius,
    '--fx-radius-lg': `${tokens.borderRadiusLG}px`,
    '--fx-radius-sm': `${tokens.borderRadiusSM}px`,
    '--fx-radius-xs': `${tokens.borderRadiusXS}px`,

    // ==================== 阴影 ====================
    '--fx-shadow': tokens.boxShadow,
    '--fx-shadow-secondary': tokens.boxShadowSecondary,

    // ==================== 控件尺寸 ====================
    '--fx-control-height': `${tokens.controlHeight}px`,
    '--fx-control-height-lg': `${tokens.controlHeightLG}px`,
    '--fx-control-height-sm': `${tokens.controlHeightSM}px`,

    // ==================== 间距 ====================
    '--fx-padding': `${tokens.padding}px`,
    '--fx-padding-lg': `${tokens.paddingLG}px`,
    '--fx-padding-sm': `${tokens.paddingSM}px`,
    '--fx-padding-xs': `${tokens.paddingXS}px`,
    '--fx-margin': `${tokens.margin}px`,
    '--fx-margin-lg': `${tokens.marginLG}px`,
    '--fx-margin-sm': `${tokens.marginSM}px`,
    '--fx-margin-xs': `${tokens.marginXS}px`,

    // ==================== 其他 ====================
    '--fx-content-width': contentWidth,
    '--fx-bg-mask': tokens.colorBgMask,

    // 顶栏快捷按钮等辅助色。
    '--fx-header-shortcut-bg': tokens.colorFillSecondary,
    '--fx-header-shortcut-color': tokens.colorTextSecondary,
    '--fx-header-btn-hover-bg': tokens.colorFill,

    // 全局搜索面板变量。
    '--fx-search-bg': tokens.colorBgElevated,
    '--fx-search-input-bg': tokens.colorBgContainer,
    '--fx-search-section-bg': tokens.colorFillAlter,
    '--fx-search-hover-bg': themeColorSoft,
    '--fx-search-active-bg': themeColorSoftStrong,
    '--fx-search-icon-bg': themeColorSoft,
    '--fx-search-highlight-bg': themeColorSoftStrong,
    '--fx-search-highlight-color': primaryColor,
    '--fx-search-badge-bg': themeColorSoft,
    '--fx-search-badge-color': primaryColor,
    '--fx-search-badge-border': themeColorSoftStrong,
    '--fx-search-module-bg': tokens.colorFillSecondary,
    '--fx-search-module-color': tokens.colorTextSecondary,
    '--fx-search-module-border': tokens.colorBorder,
    '--fx-search-scrollbar-thumb': textSecondarySoft,
    '--fx-search-scrollbar-thumb-hover': textSecondarySoftHover,
    '--fx-search-shadow': tokens.boxShadowSecondary,
  } as CSSProperties
}

/**
 * CSS 变量缓存。
 */
const cssVariablesCache = new Map<string, CSSProperties>()

/**
 * 带缓存的 CSS 变量生成函数。
 *
 * @param tokens 主题 Token 集合。
 * @param layoutConfig 布局配置。
 * @returns 可直接绑定到 Vue style 的 CSS 变量对象。
 */
export function generateCSSVariablesWithCache(
  tokens: ThemeTokens,
  layoutConfig: LayoutConfig
): CSSProperties {
  const cacheKey = `${tokens.colorBgBase}-${tokens.colorBgContainer}-${tokens.colorBgElevated}-${layoutConfig.themeMode}-${layoutConfig.fontSize}-${layoutConfig.borderRadius}-${layoutConfig.themeColor}`

  if (cssVariablesCache.has(cacheKey)) {
    return cssVariablesCache.get(cacheKey)!
  }

  const variables = generateCSSVariables(tokens, layoutConfig)
  cssVariablesCache.set(cacheKey, variables)

  // 限制缓存数量。
  if (cssVariablesCache.size > 50) {
    const firstKey = cssVariablesCache.keys().next().value
    if (typeof firstKey === 'string') {
      cssVariablesCache.delete(firstKey)
    }
  }

  return variables
}
