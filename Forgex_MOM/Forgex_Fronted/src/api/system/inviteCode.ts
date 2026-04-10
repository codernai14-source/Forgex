/*
 * Copyright 2026 coder_nai@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import request from '@/api/http'

/**
 * 分页查询邀请码列表
 */
export const getInviteCodePage = (params: any) => {
  return request.post('/sys/invite-code/page', params)
}

/**
 * 获取邀请码详情
 */
export const getInviteCode = (params: { id: string }) => {
  return request.post('/sys/invite-code/get', params)
}

/**
 * 新增邀请码
 */
export const createInviteCode = (params: any) => {
  return request.post('/sys/invite-code/create', params)
}

/**
 * 停用邀请码
 */
export const disableInviteCode = (params: { id: string }) => {
  return request.post('/sys/invite-code/disable', params)
}

/**
 * 删除邀请码
 */
export const deleteInviteCode = (params: { id: string }) => {
  return request.post('/sys/invite-code/delete', params)
}

/**
 * 分页查询邀请码使用记录
 */
export const getInviteRecordPage = (params: any) => {
  return request.post('/sys/invite-code/record/page', params)
}

