import type { ThemeTokens, LayoutConfig } from './types'
import type { CSSProperties } from 'vue'

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
    '--fx-theme-color': layoutConfig.themeColor || tokens.colorPrimary,
    '--fx-primary': tokens.colorPrimary,
    '--fx-primary-hover': tokens.colorPrimaryHover,
    '--fx-primary-active': tokens.colorPrimaryActive,
    '--fx-primary-bg': tokens.colorPrimaryBg,
    
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
 * - 缓存键：由 colorPrimary、themeMode、fontSize、borderRadius、themeColor 组合
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
  const cacheKey = `${tokens.colorPrimary}-${layoutConfig.themeMode}-${layoutConfig.fontSize}-${layoutConfig.borderRadius}-${layoutConfig.themeColor}`
  
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
