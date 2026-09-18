import { afterEach, describe, expect, it } from 'vitest'
import { setLocale } from '@/locales'
import { resolveMenuTitle } from '@/utils/menuI18n'
import { translateLegacyText } from '@/utils/legacyI18n'

describe('menu and legacy i18n', () => {
  afterEach(() => {
    setLocale('zh-CN')
  })

  it('formats legacy text patterns with captured values', () => {
    expect(translateLegacyText('自动映射成功，共匹配 8 个字段', 'en-US'))
      .toBe('Auto mapping succeeded. 8 fields matched.')
  })

  it('keeps zh-CN legacy content unchanged', () => {
    expect(translateLegacyText('客户租户已创建：锻冶', 'zh-CN'))
      .toBe('客户租户已创建：锻冶')
  })

  it('resolves a structured menu title in the active locale', () => {
    setLocale('en-US')

    expect(resolveMenuTitle({
      'zh-CN': '系统管理',
      'en-US': 'System Management',
      'zh-TW': '系統管理',
    })).toBe('System Management')
  })
})
