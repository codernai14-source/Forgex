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

export interface InviteCode {
  id: string
  inviteCode: string
  departmentId: string
  departmentName?: string
  positionId?: string
  positionName?: string
  roleId?: string
  roleName?: string
  expireTime: string
  maxRegisterCount: number
  usedCount: number
  remainCount?: number
  status: boolean
  statusLabel?: string
  remark?: string
  tenantId: string
  createTime?: string
  createBy?: string
}

export interface InviteCodeQueryParam {
  tenantId?: string
  inviteCode?: string
  departmentId?: string
  roleId?: string
  status?: boolean
}

export interface InviteCodeSaveParam {
  tenantId?: string
  departmentId: string
  positionId?: string
  roleId: string
  expireTime: string
  maxRegisterCount: number
  remark?: string
}

export interface InviteRecord {
  id: string
  inviteId: string
  inviteCode: string
  userId: string
  account: string
  username: string
  departmentId?: string
  positionId?: string
  roleId?: string
  roleName?: string
  registerIp?: string
  registerRegion?: string
  registerTime: string
  status: number
}

