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
  title: 'テナント消息ホワイトリスト',
  guide: {
    title: '用途説明',
    description: 'テナント消息ホワイトリストは、テナント間消息送信権限を制御します。送信元テナントが送信先テナントへの送信を許可されている場合のみ、クロステナントの消息配信が可能です。同一テナント内の消息とプラットフォームテナント（tenantId=0）は制限されません。',
  },
  modal: {
    addTitle: 'ホワイトリスト追加',
    editTitle: 'ホワイトリスト編集',
  },
  form: {
    senderTenant: '送信元テナント',
    senderTenantPlaceholder: '送信元テナントを選択してください',
    receiverTenant: '送信先テナント',
    receiverTenantPlaceholder: '送信先テナントを選択してください',
    remarkPlaceholder: '備考を入力してください',
  },
  confirm: {
    deleteContent: 'このホワイトリスト設定を削除してもよろしいですか？',
    batchDeleteTitle: '削除確認',
    batchDeleteContent: '選択した {count} 件のホワイトリスト設定を削除してもよろしいですか？',
  },
  message: {
    selectSenderTenant: '送信元テナントを選択してください',
    selectReceiverTenant: '送信先テナントを選択してください',
    sameTenantNotAllowed: '送信元テナントと送信先テナントは同一にできません',
    saveSuccess: 'ホワイトリストを保存しました',
    deleteSuccess: 'ホワイトリストを削除しました',
  },
}

