/**
 * 主题预设配置
 *
 * 提供多套预设主题色盘，用户可快速切换主题风格。
 * 所有预设色均经过精心挑选，确保视觉舒适度和可访问性。
 *
 * @module presets
 */
export interface ThemePreset {
  /** 主题名称（中文） */
  name: string
  /** 主题色值（HEX 格式） */
  value: string
  /** 主题描述 */
  description: string
}

/**
 * 预设主题色盘
 *
 * 提供 7 种精选主题色，涵盖常见的品牌色和功能色。
 * 所有颜色饱和度适中，符合 WCAG 2.1 对比度标准（AA 级）。
 *
 * @remarks
 * 色盘设计原则：
 * - 色彩饱和度适中，避免过于刺眼
 * - 符合无障碍标准（WCAG AA）
 * - 覆盖冷暖色调和中性色
 * - 适配浅色和暗色模式
 *
 * @example
 * ```typescript
 * // 遍历所有预设
 * THEME_PRESETS.forEach(preset => {
 *   console.log(`${preset.name}: ${preset.value}`)
 * })
 *
 * // 在 UI 中展示
 * <div v-for="preset in THEME_PRESETS" :key="preset.value">
 *   <div :style="{ background: preset.value }"></div>
 *   <span>{{ preset.name }}</span>
 * </div>
 * ```
 */
export const THEME_PRESETS: ThemePreset[] = [
  {
    name: '拂晓蓝',
    value: '#1677ff',
    description: 'Ant Design 默认蓝'
  },
  {
    name: '薄暮紫',
    value: '#722ED1',
    description: '优雅紫色'
  },
  {
    name: '青色',
    value: '#13C2C2',
    description: '清新青色'
  },
  {
    name: '极光绿',
    value: '#52C41A',
    description: '生机绿色'
  },
  {
    name: '日暮橙',
    value: '#FA8C16',
    description: '温暖橙色'
  },
  {
    name: '火山红',
    value: '#F5222D',
    description: '热情红色'
  },
  {
    name: '中性灰',
    value: '#8C8C8C',
    description: '专业灰色'
  },
]

/**
 * 获取预设主题色
 *
 * 根据颜色值查找对应的主题预设对象。
 *
 * @param value - 主题色值（HEX 格式，如 "#1677ff"）
 * @returns 匹配的主题预设对象，未找到则返回 undefined
 *
 * @example
 * ```typescript
 * const preset = getPresetColor('#1677ff')
 * if (preset) {
 *   console.log(preset.name) // "拂晓蓝"
 *   console.log(preset.description) // "Ant Design 默认蓝"
 * }
 * ```
 */
export function getPresetColor(value: string): ThemePreset | undefined {
  return THEME_PRESETS.find(preset => preset.value === value)
}

/**
 * 验证主题色是否为预设色
 *
 * 检查给定的颜色值是否存在于预设色盘中。
 *
 * @param value - 主题色值（HEX 格式，如 "#1677ff"）
 * @returns 如果是预设色返回 true，否则返回 false
 *
 * @example
 * ```typescript
 * isPresetColor('#1677ff') // true
 * isPresetColor('#123456') // false
 *
 * // 用于表单验证
 * if (!isPresetColor(userInput)) {
 *   console.warn('请选择预设主题色')
 * }
 * ```
 */
export function isPresetColor(value: string): boolean {
  return THEME_PRESETS.some(preset => preset.value === value)
}
