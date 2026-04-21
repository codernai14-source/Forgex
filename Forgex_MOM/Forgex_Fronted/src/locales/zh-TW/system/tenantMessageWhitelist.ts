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
  title: '租戶消息白名單',
  guide: {
    title: '用途說明',
    description: '租戶消息白名單用於控制跨租戶消息發送權限：只有發送方租戶被授權向接收方租戶發送消息時，系統才允許跨租戶消息投遞；同租戶消息以及平台租戶（tenantId=0）不受此限制。',
  },
  modal: {
    addTitle: '新增白名單',
    editTitle: '編輯白名單',
  },
  form: {
    senderTenant: '發送方租戶',
    senderTenantPlaceholder: '請選擇發送方租戶',
    receiverTenant: '接收方租戶',
    receiverTenantPlaceholder: '請選擇接收方租戶',
    remarkPlaceholder: '請輸入備註說明',
  },
  confirm: {
    deleteContent: '確定要刪除這條白名單配置嗎？',
    batchDeleteTitle: '確認刪除',
    batchDeleteContent: '確定要刪除選中的 {count} 條白名單配置嗎？',
  },
  message: {
    selectSenderTenant: '請選擇發送方租戶',
    selectReceiverTenant: '請選擇接收方租戶',
    sameTenantNotAllowed: '發送方租戶和接收方租戶不能相同',
    saveSuccess: '保存白名單成功',
    deleteSuccess: '刪除白名單成功',
  },
}

