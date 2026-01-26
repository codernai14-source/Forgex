/**
 * 浅色主题 Token 配置
 *
 * 基于 Ant Design 6 设计规范和 Tailwind Gray 色系。
 * 提供完整的设计 Token 集合，包括颜色、字体、圆角、阴影等。
 *
 * @remarks
 * 颜色体系：
 * - 品牌色：拂晓蓝 (#1677ff)
 * - 文本色：Tailwind Gray 系列（gray-800 到 gray-300）
 * - 背景色：从 gray-50 到 white 的渐变层次
 * - 边框色：gray-200 和 gray-100
 *
 * 对比度标准：
 * - 主文本对比度 ≥ 4.5:1（WCAG AA 标准）
 * - 大文本对比度 ≥ 3:1（WCAG AA 标准）
 *
 * @see {@link ThemeTokens} Token 类型定义
 * @see {@link darkTokens} 暗色主题 Token
 *
 * @example
 * ```typescript
 * import { lightTokens } from './tokens'
 *
 * // 使用浅色主题 Token
 * const primaryColor = lightTokens.colorPrimary // '#1677ff'
 * const textColor = lightTokens.colorText // '#1f2937'
 * ```
 */
export const lightTokens = {
    // ==================== 品牌色 ====================
    colorPrimary: '#1677ff',
    colorPrimaryHover: '#4096ff',
    colorPrimaryActive: '#0958d9',
    colorPrimaryBg: '#e6f4ff',
    colorPrimaryBgHover: '#bae0ff',
    colorPrimaryBorder: '#91caff',
    colorPrimaryBorderHover: '#69b1ff',
    colorPrimaryText: '#1677ff',
    colorPrimaryTextHover: '#4096ff',
    colorPrimaryTextActive: '#0958d9',
    // ==================== 功能色 ====================
    colorSuccess: '#52c41a',
    colorSuccessBg: '#f6ffed',
    colorSuccessBorder: '#b7eb8f',
    colorWarning: '#faad14',
    colorWarningBg: '#fffbe6',
    colorWarningBorder: '#ffe58f',
    colorError: '#ff4d4f',
    colorErrorBg: '#fff2f0',
    colorErrorBorder: '#ffccc7',
    colorInfo: '#1677ff',
    colorInfoBg: '#e6f4ff',
    colorInfoBorder: '#91caff',
    // ==================== 文本色 ====================
    colorText: '#1f2937', // gray-800
    colorTextSecondary: '#4b5563', // gray-600
    colorTextTertiary: '#6b7280', // gray-500
    colorTextQuaternary: '#9ca3af', // gray-400
    colorTextDisabled: '#d1d5db', // gray-300
    // ==================== 背景色 ====================
    colorBgBase: '#f9fafb', // gray-50 - 页面背景
    colorBgContainer: '#ffffff', // 容器背景（卡片、表格）
    colorBgElevated: '#ffffff', // 浮层背景（弹窗、下拉）
    colorBgLayout: '#f3f4f6', // gray-100 - 布局背景
    colorBgSpotlight: '#fafafa', // 聚焦背景
    colorBgMask: 'rgba(0, 0, 0, 0.45)', // 遮罩背景
    // ==================== 边框色 ====================
    colorBorder: '#e5e7eb', // gray-200
    colorBorderSecondary: '#f3f4f6', // gray-100
    colorSplit: '#e5e7eb', // gray-200
    // ==================== 填充色 ====================
    colorFill: '#f3f4f6', // gray-100
    colorFillSecondary: '#f9fafb', // gray-50
    colorFillTertiary: '#fafafa',
    colorFillQuaternary: '#ffffff',
    colorFillAlter: '#f9fafb', // 表头背景
    // ==================== 字体 ====================
    fontSize: 14,
    fontSizeLG: 16,
    fontSizeSM: 12,
    fontSizeXL: 20,
    fontSizeHeading1: 38,
    fontSizeHeading2: 30,
    fontSizeHeading3: 24,
    fontSizeHeading4: 20,
    fontSizeHeading5: 16,
    fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif',
    fontFamilyCode: '"SFMono-Regular", Consolas, "Liberation Mono", Menlo, Courier, monospace',
    lineHeight: 1.5715,
    lineHeightLG: 1.5,
    lineHeightSM: 1.66,
    lineHeightHeading1: 1.21,
    lineHeightHeading2: 1.35,
    lineHeightHeading3: 1.35,
    lineHeightHeading4: 1.4,
    lineHeightHeading5: 1.5,
    // ==================== 圆角 ====================
    borderRadius: 6,
    borderRadiusLG: 8,
    borderRadiusSM: 4,
    borderRadiusXS: 2,
    borderRadiusOuter: 4,
    // ==================== 阴影 ====================
    boxShadow: '0 1px 2px 0 rgba(0, 0, 0, 0.03), 0 1px 6px -1px rgba(0, 0, 0, 0.02), 0 2px 4px 0 rgba(0, 0, 0, 0.02)',
    boxShadowSecondary: '0 6px 16px 0 rgba(0, 0, 0, 0.08), 0 3px 6px -4px rgba(0, 0, 0, 0.12), 0 9px 28px 8px rgba(0, 0, 0, 0.05)',
    boxShadowTertiary: '0 1px 2px 0 rgba(0, 0, 0, 0.03), 0 1px 6px -1px rgba(0, 0, 0, 0.02), 0 2px 4px 0 rgba(0, 0, 0, 0.02)',
    // ==================== 控件尺寸 ====================
    controlHeight: 32,
    controlHeightLG: 40,
    controlHeightSM: 24,
    controlHeightXS: 16,
    // ==================== 间距 ====================
    padding: 16,
    paddingLG: 24,
    paddingSM: 12,
    paddingXS: 8,
    paddingXXS: 4,
    margin: 16,
    marginLG: 24,
    marginSM: 12,
    marginXS: 8,
    marginXXS: 4,
};
/**
 * 暗色主题 Token 配置
 *
 * 基于 Tailwind Slate 色系，针对暗色模式优化对比度和舒适度。
 * 采用深色背景和高对比度文本，减少视觉疲劳。
 *
 * @remarks
 * 颜色体系：
 * - 品牌色：保持与浅色模式一致的拂晓蓝
 * - 文本色：Slate 系列高对比度颜色（e2e8f0 到 334155）
 * - 背景色：从深到浅的三层结构（0f1419 → 1a1f26 → 242a33）
 * - 边框色：适度的 Slate 色系
 *
 * 对比度标准：
 * - 主文本对比度 ≥ 7:1（WCAG AAA 标准）
 * - 次要文本对比度 ≥ 4.5:1（WCAG AA 标准）
 *
 * 背景层次：
 * - colorBgBase: 页面最底层背景（最深）
 * - colorBgContainer: 卡片、表格等容器背景（适度）
 * - colorBgElevated: 弹窗、抽屉等浮层背景（最亮）
 *
 * @see {@link ThemeTokens} Token 类型定义
 * @see {@link lightTokens} 浅色主题 Token
 *
 * @example
 * ```typescript
 * import { darkTokens } from './tokens'
 *
 * // 使用暗色主题 Token
 * const bgColor = darkTokens.colorBgBase // '#0f1419'
 * const textColor = darkTokens.colorText // '#e2e8f0'
 * ```
 */
export const darkTokens = {
    // ==================== 品牌色 ====================
    colorPrimary: '#1677ff',
    colorPrimaryHover: '#3c89e8',
    colorPrimaryActive: '#0958d9',
    colorPrimaryBg: '#111d2c',
    colorPrimaryBgHover: '#112545',
    colorPrimaryBorder: '#15325b',
    colorPrimaryBorderHover: '#15417e',
    colorPrimaryText: '#3c89e8',
    colorPrimaryTextHover: '#65a9f3',
    colorPrimaryTextActive: '#1677ff',
    // ==================== 功能色 ====================
    colorSuccess: '#49aa19',
    colorSuccessBg: '#162312',
    colorSuccessBorder: '#274916',
    colorWarning: '#d89614',
    colorWarningBg: '#2b2111',
    colorWarningBorder: '#594214',
    colorError: '#dc4446',
    colorErrorBg: '#2c1618',
    colorErrorBorder: '#58181c',
    colorInfo: '#1677ff',
    colorInfoBg: '#111d2c',
    colorInfoBorder: '#15325b',
    // ==================== 文本色 ====================
    colorText: '#e2e8f0', // 主文本 - 高对比度
    colorTextSecondary: '#94a3b8', // 次要文本
    colorTextTertiary: '#64748b', // 三级文本
    colorTextQuaternary: '#475569', // 四级文本
    colorTextDisabled: '#334155', // 禁用文本
    // ==================== 背景色 ====================
    colorBgBase: '#0f1419', // 页面背景 - 最深
    colorBgContainer: '#1a1f26', // 容器背景 - 适度
    colorBgElevated: '#242a33', // 浮层背景 - 最亮
    colorBgLayout: '#141920', // 布局背景
    colorBgSpotlight: '#1e2329', // 聚焦背景
    colorBgMask: 'rgba(0, 0, 0, 0.65)', // 遮罩背景
    // ==================== 边框色 ====================
    colorBorder: '#2d3748', // 主边框
    colorBorderSecondary: '#1e293b', // 次要边框
    colorSplit: '#334155', // 分割线
    // ==================== 填充色 ====================
    colorFill: '#1e293b', // 主填充
    colorFillSecondary: '#1a1f26', // 次要填充
    colorFillTertiary: '#141920', // 三级填充
    colorFillQuaternary: '#0f1419', // 四级填充
    colorFillAlter: '#1e293b', // 表头背景
    // ==================== 字体（与浅色模式一致）====================
    fontSize: 14,
    fontSizeLG: 16,
    fontSizeSM: 12,
    fontSizeXL: 20,
    fontSizeHeading1: 38,
    fontSizeHeading2: 30,
    fontSizeHeading3: 24,
    fontSizeHeading4: 20,
    fontSizeHeading5: 16,
    fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif',
    fontFamilyCode: '"SFMono-Regular", Consolas, "Liberation Mono", Menlo, Courier, monospace',
    lineHeight: 1.5715,
    lineHeightLG: 1.5,
    lineHeightSM: 1.66,
    lineHeightHeading1: 1.21,
    lineHeightHeading2: 1.35,
    lineHeightHeading3: 1.35,
    lineHeightHeading4: 1.4,
    lineHeightHeading5: 1.5,
    // ==================== 圆角（与浅色模式一致）====================
    borderRadius: 6,
    borderRadiusLG: 8,
    borderRadiusSM: 4,
    borderRadiusXS: 2,
    borderRadiusOuter: 4,
    // ==================== 阴影（暗色模式加强）====================
    boxShadow: '0 1px 2px 0 rgba(0, 0, 0, 0.3), 0 1px 6px -1px rgba(0, 0, 0, 0.2), 0 2px 4px 0 rgba(0, 0, 0, 0.2)',
    boxShadowSecondary: '0 6px 16px 0 rgba(0, 0, 0, 0.4), 0 3px 6px -4px rgba(0, 0, 0, 0.48), 0 9px 28px 8px rgba(0, 0, 0, 0.3)',
    boxShadowTertiary: '0 1px 2px 0 rgba(0, 0, 0, 0.3), 0 1px 6px -1px rgba(0, 0, 0, 0.2), 0 2px 4px 0 rgba(0, 0, 0, 0.2)',
    // ==================== 控件尺寸（与浅色模式一致）====================
    controlHeight: 32,
    controlHeightLG: 40,
    controlHeightSM: 24,
    controlHeightXS: 16,
    // ==================== 间距（与浅色模式一致）====================
    padding: 16,
    paddingLG: 24,
    paddingSM: 12,
    paddingXS: 8,
    paddingXXS: 4,
    margin: 16,
    marginLG: 24,
    marginSM: 12,
    marginXS: 8,
    marginXXS: 4,
};
