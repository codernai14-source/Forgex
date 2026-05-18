/**
 * 繁体中文翻译统一导出
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
import zhCN from '../zh-CN'
import { mergeWithFallback } from '../mergeWithFallback'

const commonOverrides = mergeWithFallback(zhCN.common, {
  ...common,
  moveUp: '上移',
  moveDown: '下移',
  jsonArrayEditor: {
    structuredView: '結構化視圖',
    rawJson: '原始 JSON',
    emptyData: '無資料',
    invalidJson: 'JSON 資料無效，請在結構化視圖中修正後儲存',
    detailTitle: 'JSON 資料詳情',
  },
  dataSourceConfig: {
    viewDetail: '資料詳情',
    detailTitle: '資料來源 JSON 詳情',
    summaryEmpty: '暫無資料',
    summaryCount: '{count} 筆資料',
    summaryInvalid: 'JSON 資料無效',
    summaryHint: '點擊資料詳情查看與編輯',
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
  personalHomepage,
  profile,
  operationLog,
})
