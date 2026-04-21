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

export default {
  title: '租户消息白名单',
  guide: {
    title: '用途说明',
    description: '租户消息白名单用于控制跨租户消息发送权限：只有发送方租户被授权向接收方租户发送消息时，系统才允许跨租户消息投递；同租户消息以及平台租户（tenantId=0）不受该限制。',
  },
  modal: {
    addTitle: '新增白名单',
    editTitle: '编辑白名单',
  },
  form: {
    senderTenant: '发送方租户',
    senderTenantPlaceholder: '请选择发送方租户',
    receiverTenant: '接收方租户',
    receiverTenantPlaceholder: '请选择接收方租户',
    remarkPlaceholder: '请输入备注说明',
  },
  confirm: {
    deleteContent: '确定要删除这条白名单配置吗？',
    batchDeleteTitle: '确认删除',
    batchDeleteContent: '确定要删除选中的 {count} 条白名单配置吗？',
  },
  message: {
    selectSenderTenant: '请选择发送方租户',
    selectReceiverTenant: '请选择接收方租户',
    sameTenantNotAllowed: '发送方租户和接收方租户不能相同',
    saveSuccess: '保存白名单成功',
    deleteSuccess: '删除白名单成功',
  },
}

