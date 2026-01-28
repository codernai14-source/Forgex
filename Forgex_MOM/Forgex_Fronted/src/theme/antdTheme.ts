import { computed } from 'vue'
import type { Ref } from 'vue'
import { theme as antdTheme } from 'ant-design-vue'
import type { ThemeConfig } from 'ant-design-vue/es/config-provider/context'
import { lightTokens, darkTokens } from './tokens'
import type { LayoutConfig } from './types'

/**
 * 解析字号字符串为数字
 * 
 * 将 CSS 字号字符串（如 "14px"）转换为数字值。
 * 如果解析失败，返回默认值 14。
 * 
 * @param fontSize - 字号字符串（如 "14px", "16px"）
 * @returns 字号数字值
 * 
 * @example
 * ```typescript
 * parseFontSize("14px") // 14
 * parseFontSize("16px") // 16
 * parseFontSize("invalid") // 14 (默认值)
 * ```
 */
function parseFontSize(fontSize: string): number {
  const num = parseInt(fontSize, 10)
  return isNaN(num) ? 14 : num
}

/**
 * 获取组件级 Token 定制配置
 * 
 * 为 Ant Design 组件提供统一的样式定制，包括：
 * - Button: 字重、阴影、hover 效果
 * - Modal: 圆角、内边距
 * - Table: 表头样式、行 hover、边框
 * - Input: hover 和 focus 状态
 * - Select: 选项 hover 和选中状态
 * - Card: 阴影、头部样式
 * - Menu: 背景、颜色、高度
 * - Drawer: 阴影
 * - Tabs: 颜色、下划线
 * 
 * @param mode - 主题模式（light 或 dark）
 * @param primaryColor - 主品牌色
 * @returns 组件 Token 配置对象
 * 
 * @remarks
 * 组件样式遵循以下原则：
 * - 统一的视觉风格
 * - 柔和的交互效果
 * - 适配浅色和暗色模式
 * - 符合 Ant Design 6 规范
 */
function getComponentTokens(mode: 'light' | 'dark', primaryColor: string) {
  const isDark = mode === 'dark'
  const baseTokens = isDark ? darkTokens : lightTokens
  
  return {
    Button: {
      // 字重
      fontWeight: 500,
      // 移除默认阴影
      primaryShadow: 'none',
      // 文本按钮 hover 背景
      colorTextHover: isDark 
        ? 'rgba(22, 119, 255, 0.12)' 
        : 'rgba(22, 119, 255, 0.08)',
    },
    
    Modal: {
      // 圆角
      borderRadiusLG: baseTokens.borderRadiusLG,
      // 内边距
      contentPadding: baseTokens.paddingLG,
    },
    
    Table: {
      // 表头
      headerBg: baseTokens.colorFillAlter,
      headerColor: baseTokens.colorTextSecondary,
      headerBorderRadius: baseTokens.borderRadius,
      // 行 hover
      rowHoverBg: isDark 
        ? baseTokens.colorFillSecondary 
        : baseTokens.colorFill,
      // 边框
      borderColor: baseTokens.colorBorderSecondary,
      // 字体
      headerFontWeight: 500,
      // 单元格内边距
      cellPaddingBlock: 12,
      cellPaddingInline: 16,
    },
    
    Input: {
      // Hover 状态
      hoverBorderColor: baseTokens.colorPrimaryHover,
      // Focus 状态
      activeBorderColor: baseTokens.colorPrimary,
      activeShadow: isDark
        ? '0 0 0 2px rgba(22, 119, 255, 0.15)'
        : '0 0 0 2px rgba(22, 119, 255, 0.1)',
    },
    
    Select: {
      // 选项 hover
      optionSelectedBg: baseTokens.colorPrimaryBg,
      optionActiveBg: isDark
        ? baseTokens.colorFillSecondary
        : baseTokens.colorFill,
    },
    
    Card: {
      // 阴影
      boxShadow: isDark
        ? 'none'
        : baseTokens.boxShadow,
      // 头部
      headerBg: 'transparent',
      headerFontSize: baseTokens.fontSizeLG,
      headerFontWeight: 600,
      headerHeight: 56,
    },
    
    Menu: {
      // 背景
      itemBg: 'transparent',
      itemSelectedBg: isDark
        ? baseTokens.colorFill
        : baseTokens.colorFillSecondary,
      itemHoverBg: isDark
        ? baseTokens.colorFillSecondary
        : baseTokens.colorFill,
      itemActiveBg: isDark
        ? baseTokens.colorFillTertiary
        : baseTokens.colorFillSecondary,
      // 颜色
      itemColor: baseTokens.colorText,
      itemSelectedColor: baseTokens.colorPrimary,
      itemHoverColor: baseTokens.colorPrimaryHover,
      // 子菜单
      subMenuItemBg: isDark
        ? 'rgba(0, 0, 0, 0.2)'
        : 'rgba(0, 0, 0, 0.02)',
      // 高度
      itemHeight: 40,
      itemMarginBlock: 4,
      itemMarginInline: 4,
      itemBorderRadius: baseTokens.borderRadius,
      // 图标
      iconSize: 16,
      iconMarginInlineEnd: 10,
    },
    
    Drawer: {
      // 阴影
      boxShadow: isDark
        ? '-6px 0 16px 0 rgba(0, 0, 0, 0.4)'
        : '-6px 0 16px 0 rgba(0, 0, 0, 0.08)',
    },
    
    Tabs: {
      // 颜色
      itemColor: baseTokens.colorTextSecondary,
      itemSelectedColor: baseTokens.colorPrimary,
      itemHoverColor: baseTokens.colorPrimaryHover,
      itemActiveColor: baseTokens.colorPrimaryActive,
      // 下划线
      inkBarColor: baseTokens.colorPrimary,
    },
  }
}

/**
 * 生成 Ant Design 主题配置
 * 
 * 根据布局配置和主题模式，动态生成 Ant Design Vue 的主题配置对象。
 * 配置包括全局 Token 和组件级 Token，支持浅色/暗色模式自动切换。
 * 
 * @param layoutConfig - 布局配置响应式引用，包含主题色、字号、圆角等设置
 * @param resolvedMode - 解析后的主题模式（light 或 dark）
 * @returns 包含 computedTheme 的对象，computedTheme 是响应式的 ThemeConfig
 * 
 * @remarks
 * 主题配置特性：
 * - 自动适配浅色/暗色模式
 * - 支持自定义主题色
 * - 支持字号和圆角调整
 * - 包含 10+ 组件的样式定制
 * - 使用 Ant Design 官方算法（如果可用）
 * 
 * @example
 * ```typescript
 * const layoutConfig = ref<LayoutConfig>({
 *   themeMode: 'light',
 *   themeColor: '#1677ff',
 *   fontSize: '14px',
 *   borderRadius: 6
 * })
 * const resolvedMode = ref<'light' | 'dark'>('light')
 * 
 * const { computedTheme } = useAntdTheme(layoutConfig, resolvedMode)
 * 
 * // 在 ConfigProvider 中使用
 * <a-config-provider :theme="computedTheme">
 *   <App />
 * </a-config-provider>
 * ```
 */
export function useAntdTheme(
  layoutConfig: Ref<LayoutConfig>,
  resolvedMode: Ref<'light' | 'dark'>
) {
  const computedTheme = computed<ThemeConfig>(() => {
    const mode = resolvedMode.value
    const isDark = mode === 'dark'
    const tokens = isDark ? darkTokens : lightTokens
    const primaryColor = layoutConfig.value.themeColor || tokens.colorPrimary
    const fontSize = parseFontSize(layoutConfig.value.fontSize)
    const borderRadius = layoutConfig.value.borderRadius
    
    // 检查是否支持 algorithm
    const hasAlgorithm = antdTheme && antdTheme.darkAlgorithm && antdTheme.defaultAlgorithm
    
    const themeConfig: ThemeConfig = {
      token: {
        // 品牌色
        colorPrimary: primaryColor,
        
        // 文本色
        colorText: tokens.colorText,
        colorTextSecondary: tokens.colorTextSecondary,
        colorTextTertiary: tokens.colorTextTertiary,
        colorTextQuaternary: tokens.colorTextQuaternary,
        
        // 背景色
        colorBgBase: tokens.colorBgBase,
        colorBgContainer: tokens.colorBgContainer,
        colorBgElevated: tokens.colorBgElevated,
        colorBgLayout: tokens.colorBgLayout,
        colorBgSpotlight: tokens.colorBgSpotlight,
        colorBgMask: tokens.colorBgMask,
        
        // 边框色
        colorBorder: tokens.colorBorder,
        colorBorderSecondary: tokens.colorBorderSecondary,
        colorSplit: tokens.colorSplit,
        
        // 填充色
        colorFill: tokens.colorFill,
        colorFillSecondary: tokens.colorFillSecondary,
        colorFillTertiary: tokens.colorFillTertiary,
        colorFillQuaternary: tokens.colorFillQuaternary,
        
        // 功能色
        colorSuccess: tokens.colorSuccess,
        colorWarning: tokens.colorWarning,
        colorError: tokens.colorError,
        colorInfo: tokens.colorInfo,
        
        // 字体
        fontSize,
        fontSizeLG: tokens.fontSizeLG,
        fontSizeSM: tokens.fontSizeSM,
        fontFamily: tokens.fontFamily,
        lineHeight: tokens.lineHeight,
        lineHeightLG: tokens.lineHeightLG,
        lineHeightSM: tokens.lineHeightSM,
        
        // 圆角
        borderRadius,
        borderRadiusLG: tokens.borderRadiusLG,
        borderRadiusSM: tokens.borderRadiusSM,
        borderRadiusXS: tokens.borderRadiusXS,
        
        // 阴影
        boxShadow: tokens.boxShadow,
        boxShadowSecondary: tokens.boxShadowSecondary,
        
        // 控件尺寸
        controlHeight: tokens.controlHeight,
        controlHeightLG: tokens.controlHeightLG,
        controlHeightSM: tokens.controlHeightSM,
      },
      components: getComponentTokens(mode, primaryColor),
    }
    
    // 如果支持 algorithm，使用官方算法
    if (hasAlgorithm) {
      themeConfig.algorithm = isDark 
        ? antdTheme.darkAlgorithm 
        : antdTheme.defaultAlgorithm
    }
    
    return themeConfig
  })
  
  return {
    computedTheme
  }
}
