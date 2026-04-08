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
    name: '默认',
    value: '#1677ff',
    description: '经典品牌蓝'
  },
  {
    name: '紫罗兰',
    value: '#7c5cff',
    description: '偏冷感的紫色主题'
  },
  {
    name: '樱花粉',
    value: '#ec4899',
    description: '柔和而醒目的粉色主题'
  },
  {
    name: '柠檬黄',
    value: '#f6c445',
    description: '明亮温暖的黄色主题'
  },
  {
    name: '天蓝色',
    value: '#5b8ff9',
    description: '通透清爽的天蓝主题'
  },
  {
    name: '浅绿色',
    value: '#34d399',
    description: '轻盈自然的浅绿主题'
  },
  {
    name: '锌色灰',
    value: '#71717a',
    description: '克制中性的锌灰主题'
  },
  {
    name: '深绿色',
    value: '#14b8a6',
    description: '沉稳饱满的深绿主题'
  },
  {
    name: '深蓝色',
    value: '#1d4ed8',
    description: '更深邃的蓝色主题'
  },
  {
    name: '橙黄色',
    value: '#f97316',
    description: '活力鲜明的橙黄主题'
  },
  {
    name: '玫瑰红',
    value: '#e11d48',
    description: '高辨识度的玫瑰红主题'
  },
  {
    name: '中性色',
    value: '#525252',
    description: '均衡耐看的中性色主题'
  },
  {
    name: '石板灰',
    value: '#475569',
    description: '带蓝调的石板灰主题'
  },
  {
    name: '中灰色',
    value: '#6b7280',
    description: '更柔和的中灰主题'
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
