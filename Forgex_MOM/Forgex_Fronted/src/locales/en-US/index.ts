/**
 * 英文翻译统一导出
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

export default mergeWithFallback(zhCN, {
  common,
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
