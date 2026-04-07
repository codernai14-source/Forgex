import type { ThemeTokens, LayoutConfig } from './types'
import type { CSSProperties } from 'vue'

/**
 * 调整颜色亮度
 * 
 * 将十六进制颜色转换为 HSL，调整亮度后转回十六进制。
 * 
 * @param hexColor - 十六进制颜色（如 "#1677ff"）
 * @param amount - 亮度调整量（-1 到 1 之间，负值变暗，正值变亮）
 * @returns 调整后的十六进制颜色
 */
function adjustColor(hexColor: string, amount: number): string {
  if (!hexColor || !hexColor.startsWith('#')) {
    return hexColor
  }
  
  // 移除 # 并解析 RGB
  const hex = hexColor.slice(1)
  const r = parseInt(hex.substring(0, 2), 16)
  const g = parseInt(hex.substring(2, 4), 16)
  const b = parseInt(hex.substring(4, 6), 16)
  
  // 转换为 HSL
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
  
  // 调整亮度
  l = Math.max(0, Math.min(1, l + amount))
  
  // 转回 RGB
  const hue2rgb = (p: number, q: number, t: number) => {
    if (t < 0) t += 1
    if (t > 1) t -= 1
    if (t < 1/6) return p + (q - p) * 6 * t
    if (t < 1/2) return q
    if (t < 2/3) return p + (q - p) * (2/3 - t) * 6
    return p
  }
  
  let rNew, gNew, bNew
  if (s === 0) {
    rNew = gNew = bNew = l
  } else {
    const q = l < 0.5 ? l * (1 + s) : l + s - l * s
    const p = 2 * l - q
    rNew = hue2rgb(p, q, h + 1/3)
    gNew = hue2rgb(p, q, h)
    bNew = hue2rgb(p, q, h - 1/3)
  }
  
  // 转回十六进制
  const toHex = (x: number) => {
    const hex = Math.round(x * 255).toString(16)
    return hex.length === 1 ? '0' + hex : hex
  }
  
  return '#' + toHex(rNew) + toHex(gNew) + toHex(bNew)
}

/**
 * 将 Theme Token 转换为 CSS 变量
 * 
 * 将主题 Token 对象转换为 CSS 自定义属性（CSS Variables），
 * 保持与现有 --fx-xxx 变量命名的兼容性。生成的变量可直接应用到 DOM 元素上。
 * 
 * @param tokens - 主题 Token 对象（lightTokens 或 darkTokens）
 * @param layoutConfig - 布局配置对象，包含主题色、字号、圆角等设置
 * @returns CSS 属性对象，可直接用于 Vue 的 style 绑定
 * 
 * @remarks
 * 生成的 CSS 变量分类：
 * - 布局背景：--fx-layout-bg, --fx-bg-base, --fx-bg-layout
 * - 容器背景：--fx-header-bg, --fx-sider-bg, --fx-content-bg 等
 * - 边框：--fx-border-color, --fx-border-secondary, --fx-split-color
 * - 文本：--fx-text-primary, --fx-text-secondary 等
 * - 填充色：--fx-fill, --fx-fill-secondary, --fx-fill-alter
 * - 主题色：--fx-primary, --fx-primary-hover, --fx-primary-active
 * - 功能色：--fx-success, --fx-warning, --fx-error, --fx-info
 * - 字体：--fx-font-size, --fx-font-family, --fx-line-height
 * - 圆角：--fx-radius, --fx-radius-lg, --fx-radius-sm
 * - 阴影：--fx-shadow, --fx-shadow-secondary
 * - 控件尺寸：--fx-control-height 系列
 * - 间距：--fx-padding, --fx-margin 系列
 * 
 * @example
 * ```typescript
 * const variables = generateCSSVariables(lightTokens, layoutConfig)
 * // 应用到根元素
 * document.documentElement.style = variables
 * 
 * // 在 Vue 组件中使用
 * <div :style="cssVariables">
 *   <div class="custom-box">使用 CSS 变量</div>
 * </div>
 * 
 * // 在 CSS 中引用
 * .custom-box {
 *   background: var(--fx-bg-container);
 *   color: var(--fx-text-primary);
 *   border-radius: var(--fx-radius);
 * }
 * ```
 */
export function generateCSSVariables(
  tokens: ThemeTokens,
  layoutConfig: LayoutConfig
): CSSProperties {
  const radius = `${layoutConfig.borderRadius}px`
  const fontSize = layoutConfig.fontSize || '14px'
  const contentWidth = layoutConfig.contentWidth === 'fixed' ? '1200px' : '100%'
  
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
    
    // ==================== 填充色（用于 hover、表头等）====================
    '--fx-tab-bg': tokens.colorFillAlter,
    '--fx-tab-hover-bg': tokens.colorFill,
    '--fx-fill': tokens.colorFill,
    '--fx-fill-secondary': tokens.colorFillSecondary,
    '--fx-fill-alter': tokens.colorFillAlter,
    
    // ==================== 主题色 ====================
    // 使用用户选择的主题色，而不是固定的 Token 颜色
    '--fx-theme-color': layoutConfig.themeColor || tokens.colorPrimary,
    '--fx-primary': layoutConfig.themeColor || tokens.colorPrimary,
    '--fx-primary-hover': adjustColor(layoutConfig.themeColor || tokens.colorPrimary, 0.15),
    '--fx-primary-active': adjustColor(layoutConfig.themeColor || tokens.colorPrimary, -0.1),
    '--fx-primary-bg': adjustColor(layoutConfig.themeColor || tokens.colorPrimary, -0.85),
    
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
    
    // 顶栏快捷键/按钮等辅助色
    '--fx-header-shortcut-bg': tokens.colorFillSecondary,
    '--fx-header-shortcut-color': tokens.colorTextSecondary,
    '--fx-header-btn-hover-bg': tokens.colorFill,
  } as CSSProperties
}

/**
 * CSS 变量缓存
 * 
 * 使用 Map 缓存已生成的 CSS 变量对象，避免重复计算。
 * 缓存键由主题色、模式、字号、圆角等参数组合而成。
 * 
 * @internal
 */
const cssVariablesCache = new Map<string, CSSProperties>()

/**
 * 生成 CSS 变量（带缓存）
 * 
 * 带缓存优化的 CSS 变量生成函数。首次调用时生成并缓存结果，
 * 后续相同参数的调用直接返回缓存值，提升性能。
 * 
 * @param tokens - 主题 Token 对象（lightTokens 或 darkTokens）
 * @param layoutConfig - 布局配置对象
 * @returns CSS 属性对象，可直接用于 Vue 的 style 绑定
 * 
 * @remarks
 * 缓存策略：
 * - 缓存键：由关键背景 Token、themeMode、fontSize、borderRadius、themeColor 组合
 * - 缓存上限：50 个条目
 * - 淘汰策略：FIFO（先进先出），超过上限时删除最早的条目
 * 
 * 性能优化：
 * - 避免重复计算 CSS 变量对象
 * - 减少对象创建和内存分配
 * - 适合频繁切换主题的场景
 * 
 * @example
 * ```typescript
 * // 首次调用，生成并缓存
 * const vars1 = generateCSSVariablesWithCache(lightTokens, config)
 * 
 * // 相同参数，直接返回缓存
 * const vars2 = generateCSSVariablesWithCache(lightTokens, config)
 * console.log(vars1 === vars2) // true
 * 
 * // 不同参数，重新生成
 * const vars3 = generateCSSVariablesWithCache(darkTokens, config)
 * console.log(vars1 === vars3) // false
 * ```
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
  
  // 限制缓存大小
  if (cssVariablesCache.size > 50) {
    const firstKey = cssVariablesCache.keys().next().value
    cssVariablesCache.delete(firstKey)
  }
  
  return variables
}
