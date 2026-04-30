import type { ThemeTokens, LayoutConfig } from './types'
import type { CSSProperties } from 'vue'

/**
 * 璋冩暣棰滆壊浜害
 * 
 * 灏嗗崄鍏繘鍒堕鑹茶浆鎹负 HSL锛岃皟鏁翠寒搴﹀悗杞洖鍗佸叚杩涘埗銆?
 * 
 * @param hexColor - 鍗佸叚杩涘埗棰滆壊锛堝 "#1677ff"锛?
 * @param amount - 浜害璋冩暣閲忥紙-1 鍒?1 涔嬮棿锛岃礋鍊煎彉鏆楋紝姝ｅ€煎彉浜級
 * @returns 调整后的十六进制颜色
 */
function adjustColor(hexColor: string, amount: number): string {
  if (!hexColor || !hexColor.startsWith('#')) {
    return hexColor
  }
  
  // 绉婚櫎 # 骞惰В鏋?RGB
  const hex = hexColor.slice(1)
  const r = parseInt(hex.substring(0, 2), 16)
  const g = parseInt(hex.substring(2, 4), 16)
  const b = parseInt(hex.substring(4, 6), 16)
  
  // 杞崲涓?HSL
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
  
  // 璋冩暣浜害
  l = Math.max(0, Math.min(1, l + amount))
  
  // 杞洖 RGB
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
  
  // 杞洖鍗佸叚杩涘埗
  const toHex = (x: number) => {
    const hex = Math.round(x * 255).toString(16)
    return hex.length === 1 ? '0' + hex : hex
  }
  
  return '#' + toHex(rNew) + toHex(gNew) + toHex(bNew)
}

/**
 * 灏嗗崄鍏繘鍒堕鑹茶浆鎹负甯﹂€忔槑搴︾殑 rgba 棰滆壊銆?
 *
 * @param hexColor - 鍗佸叚杩涘埗棰滆壊锛堟敮鎸?#RGB / #RRGGBB锛?
 * @param alpha - 閫忔槑搴︼紙0 鍒?1锛?
 * @param 降级方案 - 鏃犳硶杞崲鏃剁殑鍏滃簳棰滆壊
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
 * 灏?Theme Token 杞崲涓?CSS 鍙橀噺
 * 
 * 灏嗕富棰?Token 瀵硅薄杞崲涓?CSS 鑷畾涔夊睘鎬э紙CSS Variables锛夛紝
 * 淇濇寔涓庣幇鏈?--fx-xxx 鍙橀噺鍛藉悕鐨勫吋瀹规€с€傜敓鎴愮殑鍙橀噺鍙洿鎺ュ簲鐢ㄥ埌 DOM 鍏冪礌涓娿€?
 * 
 * @param tokens - 涓婚 Token 瀵硅薄锛坙ightTokens 鎴?darkTokens锛?
 * @param layoutConfig - 甯冨眬閰嶇疆瀵硅薄锛屽寘鍚富棰樿壊銆佸瓧鍙枫€佸渾瑙掔瓑璁剧疆
 * @returns CSS 灞炴€у璞★紝鍙洿鎺ョ敤浜?Vue 鐨?style 缁戝畾
 * 
 * @remarks
 * 鐢熸垚鐨?CSS 鍙橀噺鍒嗙被锛?
 * - 甯冨眬鑳屾櫙锛?-fx-layout-bg, --fx-bg-base, --fx-bg-layout
 * - 瀹瑰櫒鑳屾櫙锛?-fx-header-bg, --fx-sider-bg, --fx-content-bg 绛?
 * - 杈规锛?-fx-border-color, --fx-border-secondary, --fx-split-color
 * - 鏂囨湰锛?-fx-text-primary, --fx-text-secondary 绛?
 * - 濉厖鑹诧細--fx-fill, --fx-fill-secondary, --fx-fill-alter
 * - 涓婚鑹诧細--fx-primary, --fx-primary-hover, --fx-primary-active
 * - 功能色：--fx-success, --fx-warning, --fx-error, --fx-info
 * - 瀛椾綋锛?-fx-font-size, --fx-font-family, --fx-line-height
 * - 鍦嗚锛?-fx-radius, --fx-radius-lg, --fx-radius-sm
 * - 闃村奖锛?-fx-shadow, --fx-shadow-secondary
 * - 鎺т欢灏哄锛?-fx-control-height 绯诲垪
 * - 闂磋窛锛?-fx-padding, --fx-margin 绯诲垪
 * 
 * @example
 * ```typescript
 * const variables = generateCSSVariables(lightTokens, layoutConfig)
 * // 应用到根元素
 * document.documentElement.style = variables
 * 
 * // 鍦?Vue 缁勪欢涓娇鐢?
 * <div :style="cssVariables">
 *   <div class="custom-box">浣跨敤 CSS 鍙橀噺</div>
 * </div>
 * 
 * // 鍦?CSS 涓紩鐢?
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
  const primaryColor = layoutConfig.themeColor || tokens.colorPrimary
  const radius = `${layoutConfig.borderRadius}px`
  const fontSize = layoutConfig.fontSize || '14px'
  const contentWidth = layoutConfig.contentWidth === 'fixed' ? '1200px' : '100%'
  const themeColorSoft = withAlpha(primaryColor, 0.12, tokens.colorPrimaryBg)
  const themeColorSoftStrong = withAlpha(primaryColor, 0.18, tokens.colorPrimaryBgHover)
  const textSecondarySoft = withAlpha(tokens.colorTextSecondary, 0.24, tokens.colorBorder)
  const textSecondarySoftHover = withAlpha(tokens.colorTextSecondary, 0.38, tokens.colorSplit)

  return {
    // ==================== 甯冨眬鑳屾櫙 ====================
    '--fx-layout-bg': tokens.colorBgBase,
    '--fx-layout-color': tokens.colorText,
    '--fx-bg-base': tokens.colorBgBase,
    '--fx-bg-layout': tokens.colorBgLayout,
    
    // ==================== 瀹瑰櫒鑳屾櫙 ====================
    '--fx-header-bg': tokens.colorBgContainer,
    '--fx-header-color': tokens.colorText,
    '--fx-sider-bg': tokens.colorBgContainer,
    '--fx-sider-mini-bg': tokens.colorBgContainer,
    '--fx-content-bg': tokens.colorBgContainer,
    '--fx-footer-bg': tokens.colorBgContainer,
    '--fx-bg-container': tokens.colorBgContainer,
    '--fx-bg-elevated': tokens.colorBgElevated,
    
    // ==================== 杈规 ====================
    '--fx-border-color': tokens.colorBorder,
    '--fx-border-secondary': tokens.colorBorderSecondary,
    '--fx-split-color': tokens.colorSplit,
    
    // ==================== 鏂囨湰 ====================
    '--fx-text-color': tokens.colorTextSecondary,
    '--fx-text-primary': tokens.colorText,
    '--fx-text-secondary': tokens.colorTextSecondary,
    '--fx-text-tertiary': tokens.colorTextTertiary,
    '--fx-text-disabled': tokens.colorTextDisabled,
    
    // ==================== 濉厖鑹诧紙鐢ㄤ簬 hover銆佽〃澶寸瓑锛?===================
    '--fx-tab-bg': tokens.colorFillAlter,
    '--fx-tab-hover-bg': tokens.colorFill,
    '--fx-fill': tokens.colorFill,
    '--fx-fill-secondary': tokens.colorFillSecondary,
    '--fx-fill-alter': tokens.colorFillAlter,
    
    // ==================== 涓婚鑹?====================
    // 浣跨敤鐢ㄦ埛閫夋嫨鐨勪富棰樿壊锛岃€屼笉鏄浐瀹氱殑 Token 棰滆壊
    '--fx-theme-color': primaryColor,
    '--fx-primary': primaryColor,
    '--fx-primary-hover': adjustColor(primaryColor, 0.15),
    '--fx-primary-active': adjustColor(primaryColor, -0.1),
    '--fx-primary-bg': adjustColor(primaryColor, -0.85),
    '--fx-primary-soft': themeColorSoft,
    '--fx-primary-soft-strong': themeColorSoftStrong,

    // ==================== 鍔熻兘鑹?====================
    '--fx-success': tokens.colorSuccess,
    '--fx-success-bg': tokens.colorSuccessBg,
    '--fx-warning': tokens.colorWarning,
    '--fx-warning-bg': tokens.colorWarningBg,
    '--fx-error': tokens.colorError,
    '--fx-error-bg': tokens.colorErrorBg,
    '--fx-info': tokens.colorInfo,
    '--fx-info-bg': tokens.colorInfoBg,
    
    // ==================== 瀛椾綋 ====================
    '--fx-font-size': fontSize,
    '--fx-font-size-lg': `${tokens.fontSizeLG}px`,
    '--fx-font-size-sm': `${tokens.fontSizeSM}px`,
    '--fx-font-family': tokens.fontFamily,
    '--fx-line-height': tokens.lineHeight.toString(),
    
    // ==================== 鍦嗚 ====================
    '--fx-radius': radius,
    '--fx-radius-lg': `${tokens.borderRadiusLG}px`,
    '--fx-radius-sm': `${tokens.borderRadiusSM}px`,
    '--fx-radius-xs': `${tokens.borderRadiusXS}px`,
    
    // ==================== 闃村奖 ====================
    '--fx-shadow': tokens.boxShadow,
    '--fx-shadow-secondary': tokens.boxShadowSecondary,
    
    // ==================== 鎺т欢灏哄 ====================
    '--fx-control-height': `${tokens.controlHeight}px`,
    '--fx-control-height-lg': `${tokens.controlHeightLG}px`,
    '--fx-control-height-sm': `${tokens.controlHeightSM}px`,
    
    // ==================== 闂磋窛 ====================
    '--fx-padding': `${tokens.padding}px`,
    '--fx-padding-lg': `${tokens.paddingLG}px`,
    '--fx-padding-sm': `${tokens.paddingSM}px`,
    '--fx-padding-xs': `${tokens.paddingXS}px`,
    '--fx-margin': `${tokens.margin}px`,
    '--fx-margin-lg': `${tokens.marginLG}px`,
    '--fx-margin-sm': `${tokens.marginSM}px`,
    '--fx-margin-xs': `${tokens.marginXS}px`,
    
    // ==================== 鍏朵粬 ====================
    '--fx-content-width': contentWidth,
    '--fx-bg-mask': tokens.colorBgMask,
    
    // 椤舵爮蹇嵎閿?按钮等辅助色
    '--fx-header-shortcut-bg': tokens.colorFillSecondary,
    '--fx-header-shortcut-color': tokens.colorTextSecondary,
    '--fx-header-btn-hover-bg': tokens.colorFill,

    // 鍏ㄥ眬鎼滅储娴眰璇箟鑹?
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
 * CSS 鍙橀噺缂撳瓨
 * 
 * 浣跨敤 Map 缂撳瓨宸茬敓鎴愮殑 CSS 鍙橀噺瀵硅薄锛岄伩鍏嶉噸澶嶈绠椼€?
 * 缂撳瓨閿敱涓婚鑹层€佹ā寮忋€佸瓧鍙枫€佸渾瑙掔瓑鍙傛暟缁勫悎鑰屾垚銆?
 * 
 * @internal
 */
const cssVariablesCache = new Map<string, CSSProperties>()

/**
 * 鐢熸垚 CSS 鍙橀噺锛堝甫缂撳瓨锛?
 * 
 * 甯︾紦瀛樹紭鍖栫殑 CSS 鍙橀噺鐢熸垚鍑芥暟銆傞娆¤皟鐢ㄦ椂鐢熸垚骞剁紦瀛樼粨鏋滐紝
 * 鍚庣画鐩稿悓鍙傛暟鐨勮皟鐢ㄧ洿鎺ヨ繑鍥炵紦瀛樺€硷紝鎻愬崌鎬ц兘銆?
 * 
 * @param tokens - 涓婚 Token 瀵硅薄锛坙ightTokens 鎴?darkTokens锛?
 * @param layoutConfig - 甯冨眬閰嶇疆瀵硅薄
 * @returns CSS 灞炴€у璞★紝鍙洿鎺ョ敤浜?Vue 鐨?style 缁戝畾
 * 
 * @remarks
 * 缂撳瓨绛栫暐锛?
 * - 缂撳瓨閿細鐢卞叧閿儗鏅?Token銆乼hemeMode銆乫ontSize銆乥orderRadius銆乼hemeColor 缁勫悎
 * - 缂撳瓨涓婇檺锛?0 涓潯鐩?
 * - 娣樻卑绛栫暐锛欶IFO锛堝厛杩涘厛鍑猴級锛岃秴杩囦笂闄愭椂鍒犻櫎鏈€鏃╃殑鏉＄洰
 * 
 * 鎬ц兘浼樺寲锛?
 * - 閬垮厤閲嶅璁＄畻 CSS 鍙橀噺瀵硅薄
 * - 鍑忓皯瀵硅薄鍒涘缓鍜屽唴瀛樺垎閰?
 * - 閫傚悎棰戠箒鍒囨崲涓婚鐨勫満鏅?
 * 
 * @example
 * ```typescript
 * // 棣栨璋冪敤锛岀敓鎴愬苟缂撳瓨
 * const vars1 = generateCSSVariablesWithCache(lightTokens, config)
 * 
 * // 鐩稿悓鍙傛暟锛岀洿鎺ヨ繑鍥炵紦瀛?
 * const vars2 = generateCSSVariablesWithCache(lightTokens, config)
 * console.log(vars1 === vars2) // true
 * 
 * // 涓嶅悓鍙傛暟锛岄噸鏂扮敓鎴?
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
  
  // 闄愬埗缂撳瓨澶у皬
  if (cssVariablesCache.size > 50) {
    const firstKey = cssVariablesCache.keys().next().value
    if (typeof firstKey === 'string') {
      cssVariablesCache.delete(firstKey)
    }
  }
  
  return variables
}
