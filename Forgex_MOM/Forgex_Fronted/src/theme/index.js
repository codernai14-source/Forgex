/**
 * 主题系统统一导出
 *
 * 提供完整的主题系统功能，包括：
 * - 类型定义：ThemeTokens、ThemeMode、LayoutConfig
 * - Token 配置：lightTokens、darkTokens
 * - 主题 Composables：useAntdTheme、useSystemTheme
 * - CSS 变量生成：generateCSSVariables、generateCSSVariablesWithCache
 * - 主题预设：THEME_PRESETS、getPresetColor、isPresetColor
 *
 * @module theme
 *
 * @example
 * ```typescript
 * // 导入类型
 * import type { ThemeMode, LayoutConfig } from '@/theme'
 *
 * // 导入 Token
 * import { lightTokens, darkTokens } from '@/theme'
 *
 * // 导入 Composables
 * import { useAntdTheme, useSystemTheme } from '@/theme'
 *
 * // 导入 CSS 变量生成函数
 * import { generateCSSVariablesWithCache } from '@/theme'
 *
 * // 导入主题预设
 * import { THEME_PRESETS, getPresetColor } from '@/theme'
 * ```
 */
// ==================== Token 配置 ====================
export { lightTokens, darkTokens } from './tokens';
// ==================== 主题 Composables ====================
export { useAntdTheme } from './antdTheme';
export { useSystemTheme, resolveThemeMode } from '../composables/useSystemTheme';
// ==================== CSS 变量生成 ====================
export { generateCSSVariables, generateCSSVariablesWithCache } from './cssVariables';
// ==================== 主题预设 ====================
export { THEME_PRESETS, getPresetColor, isPresetColor } from './presets';
