import i18n, { getLocale, type LocaleCode } from '@/locales'
import legacyTextMessages from '@/locales/legacyText'
import { watch } from 'vue'

type TranslatePattern = {
  match: RegExp
  values: Partial<Record<LocaleCode, string>>
}

const zhLocales: LocaleCode[] = ['zh-CN']

const legacyPatterns: TranslatePattern[] = [
  {
    match: /^自动映射成功，共匹配\s+(\d+)\s+个字段$/,
    values: {
      'en-US': 'Auto mapping succeeded. {0} fields matched.',
      'zh-TW': '自動映射成功，共匹配 {0} 個欄位',
      'ja-JP': '自動マッピングに成功しました。{0} 項目が一致しました。',
      'ko-KR': '자동 매핑 성공. {0}개 필드가 일치했습니다.',
    },
  },
  {
    match: /^智能匹配成功，共匹配\s+(\d+)\s+个字段$/,
    values: {
      'en-US': 'Smart matching succeeded. {0} fields matched.',
      'zh-TW': '智慧匹配成功，共匹配 {0} 個欄位',
      'ja-JP': 'スマート照合に成功しました。{0} 項目が一致しました。',
      'ko-KR': '스마트 매칭 성공. {0}개 필드가 일치했습니다.',
    },
  },
  {
    match: /^有\s+(\d+)\s+条映射关系未完善，请补充源字段和目标字段$/,
    values: {
      'en-US': '{0} mapping relations are incomplete. Please fill source and target fields.',
      'zh-TW': '有 {0} 條映射關係未完善，請補充來源欄位和目標欄位',
      'ja-JP': '{0} 件のマッピング関係が未完成です。ソース項目とターゲット項目を補完してください。',
      'ko-KR': '{0}개 매핑 관계가 완성되지 않았습니다. 소스 필드와 대상 필드를 보완하세요.',
    },
  },
  {
    match: /^客户租户已创建：(.+)$/,
    values: {
      'en-US': 'Customer tenant created: {0}',
      'zh-TW': '客戶租戶已建立：{0}',
      'ja-JP': '顧客テナントを作成しました: {0}',
      'ko-KR': '고객 테넌트 생성됨: {0}',
    },
  },
  {
    match: /^供应商租户已关联：(.+)$/,
    values: {
      'en-US': 'Supplier tenant linked: {0}',
      'zh-TW': '供應商租戶已關聯：{0}',
      'ja-JP': 'サプライヤーテナントを連携しました: {0}',
      'ko-KR': '공급업체 테넌트 연결됨: {0}',
    },
  },
  {
    match: /^同步完成：总数\s+(\d+)，失败\s+(\d+)$/,
    values: {
      'en-US': 'Sync completed: total {0}, failed {1}',
      'zh-TW': '同步完成：總數 {0}，失敗 {1}',
      'ja-JP': '同期完了: 合計 {0}、失敗 {1}',
      'ko-KR': '동기화 완료: 총 {0}, 실패 {1}',
    },
  },
  {
    match: /^导入完成：新增\s+(\d+)，更新\s+(\d+)$/,
    values: {
      'en-US': 'Import completed: created {0}, updated {1}',
      'zh-TW': '匯入完成：新增 {0}，更新 {1}',
      'ja-JP': 'インポート完了: 追加 {0}、更新 {1}',
      'ko-KR': '가져오기 완료: 추가 {0}, 업데이트 {1}',
    },
  },
]

const translatedTextAttr = 'data-fx-i18n-text'
const originalAttrPrefix = 'data-fx-i18n-origin-'
const translatableAttributes = ['title', 'placeholder', 'aria-label', 'ok-text', 'cancel-text']

let observer: MutationObserver | null = null
let updateScheduled = false
let isTranslating = false
let localeWatcherInstalled = false
const originalTextByNode = new WeakMap<Text, string>()

function formatTemplate(template: string, args: string[]) {
  return template.replace(/\{(\d+)\}/g, (_, index) => args[Number(index)] ?? '')
}

function currentLocale(locale?: LocaleCode): LocaleCode {
  return locale || getLocale()
}

function shouldTranslate(locale: LocaleCode) {
  return !zhLocales.includes(locale)
}

function translateNormalizedText(text: string, locale: LocaleCode): string {
  const trimmed = text.trim()
  if (!trimmed || !/[\u4e00-\u9fff]/.test(trimmed)) {
    return text
  }

  const entry = legacyTextMessages[trimmed]
  if (entry?.[locale]) {
    return entry[locale] as string
  }

  if (locale === 'zh-TW' && entry?.['zh-CN']) {
    return entry['zh-CN'] as string
  }

  for (const pattern of legacyPatterns) {
    const match = trimmed.match(pattern.match)
    const template = pattern.values[locale]
    if (match && template) {
      return formatTemplate(template, match.slice(1))
    }
  }

  return text
}

export function translateLegacyText(text: string, locale?: LocaleCode): string {
  const targetLocale = currentLocale(locale)
  if (!shouldTranslate(targetLocale)) {
    return text
  }

  const leading = text.match(/^\s*/)?.[0] ?? ''
  const trailing = text.match(/\s*$/)?.[0] ?? ''
  const core = text.slice(leading.length, text.length - trailing.length)
  const translated = translateNormalizedText(core, targetLocale)
  return translated === core ? text : `${leading}${translated}${trailing}`
}

export function translateLegacyContent(content: any): any {
  if (typeof content === 'string') {
    return translateLegacyText(content)
  }
  if (content && typeof content === 'object' && 'content' in content && typeof content.content === 'string') {
    return {
      ...content,
      content: translateLegacyText(content.content),
    }
  }
  return content
}

function getOriginAttrName(attrName: string) {
  return `${originalAttrPrefix}${attrName.replace(/[^a-zA-Z0-9_-]/g, '-')}`
}

function translateTextNode(node: Text, locale: LocaleCode) {
  const parent = node.parentElement
  if (!parent || parent.closest('script,style')) {
    return
  }

  const current = node.nodeValue || ''
  const original = originalTextByNode.get(node) || current
  if (originalTextByNode.has(node) && /[\u4e00-\u9fff]/.test(current) && current !== original) {
    originalTextByNode.set(node, current)
  }
  if (!originalTextByNode.has(node) && /[\u4e00-\u9fff]/.test(original)) {
    originalTextByNode.set(node, original)
    parent.setAttribute(translatedTextAttr, 'true')
  }

  const source = originalTextByNode.get(node) || current
  const next = shouldTranslate(locale) ? translateLegacyText(source, locale) : source
  if (node.nodeValue !== next) {
    node.nodeValue = next
  }
}

function translateElementAttributes(element: Element, locale: LocaleCode) {
  for (const attr of translatableAttributes) {
    const attrValue = element.getAttribute(attr)
    const originAttr = getOriginAttrName(attr)
    const source = element.getAttribute(originAttr) || attrValue || ''
    if (!source || !/[\u4e00-\u9fff]/.test(source)) {
      continue
    }

    if (!element.hasAttribute(originAttr)) {
      element.setAttribute(originAttr, source)
    }

    const next = shouldTranslate(locale) ? translateLegacyText(source, locale) : source
    if (element.getAttribute(attr) !== next) {
      element.setAttribute(attr, next)
    }
  }
}

function translateNode(node: Node, locale: LocaleCode) {
  if (node.nodeType === Node.TEXT_NODE) {
    translateTextNode(node as Text, locale)
    return
  }

  if (node.nodeType !== Node.ELEMENT_NODE) {
    return
  }

  const element = node as Element
  translateElementAttributes(element, locale)

  const walker = document.createTreeWalker(element, NodeFilter.SHOW_TEXT)
  let current = walker.nextNode()
  while (current) {
    translateTextNode(current as Text, locale)
    current = walker.nextNode()
  }

  element.querySelectorAll<HTMLElement>('[title],[placeholder],[aria-label]').forEach(child => {
    translateElementAttributes(child, locale)
  })
}

function applyLegacyDomTranslation() {
  if (isTranslating || typeof document === 'undefined') {
    return
  }

  isTranslating = true
  try {
    translateNode(document.body, getLocale())
  } finally {
    isTranslating = false
  }
}

function scheduleLegacyDomTranslation() {
  if (updateScheduled) {
    return
  }
  updateScheduled = true
  window.requestAnimationFrame(() => {
    updateScheduled = false
    applyLegacyDomTranslation()
  })
}

export function installLegacyI18nBridge() {
  if (typeof window === 'undefined' || typeof document === 'undefined') {
    return
  }

  if (!localeWatcherInstalled) {
    localeWatcherInstalled = true
    watch(() => i18n.global.locale.value, () => scheduleLegacyDomTranslation())
  }

  if (!observer) {
    observer = new MutationObserver(mutations => {
      if (isTranslating) {
        return
      }
      if (mutations.some(mutation => mutation.type === 'childList' || mutation.type === 'attributes' || mutation.type === 'characterData')) {
        scheduleLegacyDomTranslation()
      }
    })
    observer.observe(document.body, {
      childList: true,
      subtree: true,
      characterData: true,
      attributes: true,
      attributeFilter: translatableAttributes,
    })
  }

  scheduleLegacyDomTranslation()
}
