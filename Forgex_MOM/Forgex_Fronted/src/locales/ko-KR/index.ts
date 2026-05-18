/**
 * 韩文翻译统一导出
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
  moveUp: '위로',
  moveDown: '아래로',
  jsonArrayEditor: {
    structuredView: '구조화 보기',
    rawJson: '원본 JSON',
    emptyData: '데이터 없음',
    invalidJson: 'JSON 데이터가 올바르지 않습니다. 구조화 보기에서 수정 후 저장하세요',
    detailTitle: 'JSON 데이터 상세',
  },
  dataSourceConfig: {
    viewDetail: '데이터 상세',
    detailTitle: '데이터 소스 JSON 상세',
    summaryEmpty: '데이터 없음',
    summaryCount: '데이터 {count}건',
    summaryInvalid: 'JSON 데이터가 올바르지 않습니다',
    summaryHint: '데이터 상세를 눌러 확인하고 편집하세요',
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
