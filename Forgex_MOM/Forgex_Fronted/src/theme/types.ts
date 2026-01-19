/**
 * 主题 Token 类型定义
 * 
 * 对齐 Ant Design 6 的 Token 体系，定义了完整的设计 Token 集合。
 * 包括颜色、字体、圆角、阴影、尺寸、间距等所有设计元素。
 * 
 * @remarks
 * - 浅色模式使用 Tailwind Gray 色系
 * - 暗色模式使用 Tailwind Slate 色系
 * - 所有颜色符合 WCAG 对比度标准
 * 
 * @see {@link lightTokens} 浅色主题 Token 配置
 * @see {@link darkTokens} 暗色主题 Token 配置
 */
export interface ThemeTokens {
  // ==================== 品牌色 ====================
  /** 主品牌色，用于主要按钮、链接等 */
  colorPrimary: string
  /** 主品牌色悬停态 */
  colorPrimaryHover: string
  /** 主品牌色激活态 */
  colorPrimaryActive: string
  /** 主品牌色背景 */
  colorPrimaryBg: string
  /** 主品牌色背景悬停态 */
  colorPrimaryBgHover: string
  /** 主品牌色边框 */
  colorPrimaryBorder: string
  /** 主品牌色边框悬停态 */
  colorPrimaryBorderHover: string
  /** 主品牌色文本 */
  colorPrimaryText: string
  /** 主品牌色文本悬停态 */
  colorPrimaryTextHover: string
  /** 主品牌色文本激活态 */
  colorPrimaryTextActive: string
  
  // ==================== 功能色 ====================
  /** 成功色，用于成功状态提示 */
  colorSuccess: string
  /** 成功色背景 */
  colorSuccessBg: string
  /** 成功色边框 */
  colorSuccessBorder: string
  /** 警告色，用于警告状态提示 */
  colorWarning: string
  /** 警告色背景 */
  colorWarningBg: string
  /** 警告色边框 */
  colorWarningBorder: string
  /** 错误色，用于错误状态提示 */
  colorError: string
  /** 错误色背景 */
  colorErrorBg: string
  /** 错误色边框 */
  colorErrorBorder: string
  /** 信息色，用于信息状态提示 */
  colorInfo: string
  /** 信息色背景 */
  colorInfoBg: string
  /** 信息色边框 */
  colorInfoBorder: string
  
  // ==================== 文本色 ====================
  /** 主文本色，用于标题、正文等 */
  colorText: string
  /** 次要文本色，用于辅助信息 */
  colorTextSecondary: string
  /** 三级文本色，用于次要辅助信息 */
  colorTextTertiary: string
  /** 四级文本色，用于占位符等 */
  colorTextQuaternary: string
  /** 禁用文本色 */
  colorTextDisabled: string
  
  // ==================== 背景色 ====================
  /** 基础背景色，页面最底层背景 */
  colorBgBase: string
  /** 容器背景色，用于卡片、表格等 */
  colorBgContainer: string
  /** 浮层背景色，用于弹窗、抽屉等 */
  colorBgElevated: string
  /** 布局背景色，用于内容区域 */
  colorBgLayout: string
  /** 聚光灯背景色，用于高亮区域 */
  colorBgSpotlight: string
  /** 遮罩背景色 */
  colorBgMask: string
  
  // ==================== 边框色 ====================
  /** 主边框色 */
  colorBorder: string
  /** 次要边框色，用于分割线等 */
  colorBorderSecondary: string
  /** 分割线颜色 */
  colorSplit: string
  
  // ==================== 填充色 ====================
  /** 一级填充色，用于悬停背景 */
  colorFill: string
  /** 二级填充色 */
  colorFillSecondary: string
  /** 三级填充色 */
  colorFillTertiary: string
  /** 四级填充色 */
  colorFillQuaternary: string
  /** 替代填充色，用于表头等 */
  colorFillAlter: string
  
  // ==================== 字体 ====================
  /** 基础字号（14px） */
  fontSize: number
  /** 大字号（16px） */
  fontSizeLG: number
  /** 小字号（12px） */
  fontSizeSM: number
  /** 超大字号（20px） */
  fontSizeXL: number
  /** 一级标题字号（38px） */
  fontSizeHeading1: number
  /** 二级标题字号（30px） */
  fontSizeHeading2: number
  /** 三级标题字号（24px） */
  fontSizeHeading3: number
  /** 四级标题字号（20px） */
  fontSizeHeading4: number
  /** 五级标题字号（16px） */
  fontSizeHeading5: number
  /** 字体族 */
  fontFamily: string
  /** 代码字体族 */
  fontFamilyCode: string
  /** 基础行高（1.5714） */
  lineHeight: number
  /** 大行高（1.5） */
  lineHeightLG: number
  /** 小行高（1.6667） */
  lineHeightSM: number
  /** 一级标题行高（1.2105） */
  lineHeightHeading1: number
  /** 二级标题行高（1.2667） */
  lineHeightHeading2: number
  /** 三级标题行高（1.3333） */
  lineHeightHeading3: number
  /** 四级标题行高（1.4） */
  lineHeightHeading4: number
  /** 五级标题行高（1.5） */
  lineHeightHeading5: number
  
  // ==================== 圆角 ====================
  /** 基础圆角（6px） */
  borderRadius: number
  /** 大圆角（8px） */
  borderRadiusLG: number
  /** 小圆角（4px） */
  borderRadiusSM: number
  /** 超小圆角（2px） */
  borderRadiusXS: number
  /** 外层圆角（4px） */
  borderRadiusOuter: number
  
  // ==================== 阴影 ====================
  /** 基础阴影 */
  boxShadow: string
  /** 次要阴影 */
  boxShadowSecondary: string
  /** 三级阴影 */
  boxShadowTertiary: string
  
  // ==================== 控件尺寸 ====================
  /** 基础控件高度（32px） */
  controlHeight: number
  /** 大控件高度（40px） */
  controlHeightLG: number
  /** 小控件高度（24px） */
  controlHeightSM: number
  /** 超小控件高度（16px） */
  controlHeightXS: number
  
  // ==================== 间距 ====================
  /** 基础内边距（16px） */
  padding: number
  /** 大内边距（24px） */
  paddingLG: number
  /** 小内边距（12px） */
  paddingSM: number
  /** 超小内边距（8px） */
  paddingXS: number
  /** 极小内边距（4px） */
  paddingXXS: number
  /** 基础外边距（16px） */
  margin: number
  /** 大外边距（24px） */
  marginLG: number
  /** 小外边距（12px） */
  marginSM: number
  /** 超小外边距（8px） */
  marginXS: number
  /** 极小外边距（4px） */
  marginXXS: number
}

/**
 * 主题模式枚举
 * 
 * @remarks
 * - `light`: 浅色模式
 * - `dark`: 暗色模式
 * - `system`: 跟随系统主题
 */
export type ThemeMode = 'light' | 'dark' | 'system'

/**
 * 布局配置接口
 * 
 * 定义了整个应用的布局配置选项，包括主题、布局模式、组件显示等。
 * 配置会保存到 localStorage 和后端，实现跨设备同步。
 * 
 * @remarks
 * 配置保存位置：
 * - 本地：`localStorage.getItem('fx-layout-config')`
 * - 后端：通过 `saveUserLayoutStyle` API 保存
 * 
 * @see {@link MainLayout} 主布局组件
 */
export interface LayoutConfig {
  /** 是否启用左侧双列菜单 */
  leftDoubleMenu: boolean
  /** 布局模式：垂直、垂直双列、水平、混合 */
  layoutMode: 'vertical' | 'vertical-mix' | 'top' | 'mix'
  /** 内容宽度：流式或定宽 */
  contentWidth: 'fluid' | 'fixed'
  /** 字号大小（13px/14px/16px） */
  fontSize: string
  /** 圆角大小（0-16px） */
  borderRadius: number
  /** 主题模式 */
  themeMode: ThemeMode
  /** 主题色（十六进制颜色值） */
  themeColor: string
  /** 是否显示顶栏 */
  headerVisible: boolean
  /** 顶栏模式：固定、自动、滚动隐藏 */
  headerMode: 'fixed' | 'auto' | 'hide-on-scroll'
  /** 顶栏菜单对齐方式 */
  headerMenuAlign: 'left' | 'center' | 'right'
  /** 是否启用标签栏 */
  tabBarEnabled: boolean
  /** 标签栏最大数量 */
  tabBarMax: number
  /** 标签栏是否可拖拽 */
  tabBarDraggable: boolean
  /** 标签栏是否显示图标 */
  tabBarShowIcon: boolean
  /** 标签栏样式：Chrome 风格或卡片风格 */
  tabBarStyle: 'chrome' | 'card'
  /** 是否显示全局搜索 */
  widgetGlobalSearch: boolean
  /** 是否显示主题切换 */
  widgetThemeSwitch: boolean
  /** 是否显示语言切换 */
  widgetLangSwitch: boolean
  /** 是否显示全屏按钮 */
  widgetFullscreen: boolean
  /** 是否显示通知按钮 */
  widgetNotification: boolean
  /** 是否显示侧边栏折叠按钮 */
  widgetSiderCollapse: boolean
  /** 是否显示刷新按钮 */
  widgetRefresh: boolean
  /** 是否启用水印 */
  watermarkEnabled: boolean
  /** 水印文本内容 */
  watermarkText: string
  /** 是否启用页面动画 */
  animateEnabled: boolean
  /** 是否启用加载指示器 */
  loadingIndicatorEnabled: boolean
  /** 页面切换动画：水平滑动或渐隐 */
  pageTransition: 'horizontal' | 'fade'
  /** 是否显示页脚版权信息 */
  footerCopyrightEnabled: boolean
}
