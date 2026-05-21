/**
 * 日文翻译统一导出
 */
import common from './common'
import layout from './layout'
import validation from './validation'
import message from './message'
import system from './system'
import workflow from './workflow'
import personalHomepage from './personalHomepage'
import profile from './profile'
import operationLog from './operationLog'
import integration from './integration'
import basic from './basic'
import auth from './auth'
import guide from './guide'
import label from './label'
import report from './report'
import job from './job'
import fallback from './fallback'
import zhCN from '../zh-CN'
import { mergeWithFallback } from '../mergeWithFallback'

const commonOverrides = mergeWithFallback(zhCN.common, {
  ...common,
  moveUp: '上へ',
  moveDown: '下へ',
  jsonArrayEditor: {
    structuredView: '構造化ビュー',
    rawJson: '元の JSON',
    emptyData: 'データなし',
    invalidJson: 'JSON データが無効です。構造化ビューで修正して保存してください',
    detailTitle: 'JSON データ詳細',
  },
  dataSourceConfig: {
    viewDetail: 'データ詳細',
    detailTitle: 'データソース JSON 詳細',
    summaryEmpty: 'データなし',
    summaryCount: '{count} 件のデータ',
    summaryInvalid: 'JSON データが無効です',
    summaryHint: 'データ詳細をクリックして確認・編集してください',
  },
})

export default mergeWithFallback(zhCN, {
  common: commonOverrides,
  layout,
  validation,
  message,
  system,
  workflow,
  integration,
  basic,
  auth,
  guide,
  label,
  report,
  job,
  fallback,
  personalHomepage,
  profile,
  operationLog,
})
