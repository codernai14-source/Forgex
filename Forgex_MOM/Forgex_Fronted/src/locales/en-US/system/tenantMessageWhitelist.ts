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
  title: 'Tenant Message Whitelist',
  guide: {
    title: 'Usage',
    description: 'The tenant message whitelist controls cross-tenant message delivery. Messages can be sent across tenants only when the sender tenant is authorized to send to the receiver tenant. Intra-tenant messages and platform tenant messages (tenantId=0) are not restricted.',
  },
  modal: {
    addTitle: 'Add Whitelist',
    editTitle: 'Edit Whitelist',
  },
  form: {
    senderTenant: 'Sender Tenant',
    senderTenantPlaceholder: 'Please select the sender tenant',
    receiverTenant: 'Receiver Tenant',
    receiverTenantPlaceholder: 'Please select the receiver tenant',
    remarkPlaceholder: 'Please enter remarks',
  },
  confirm: {
    deleteContent: 'Are you sure you want to delete this whitelist entry?',
    batchDeleteTitle: 'Confirm Delete',
    batchDeleteContent: 'Are you sure you want to delete the selected {count} whitelist entries?',
  },
  message: {
    selectSenderTenant: 'Please select the sender tenant',
    selectReceiverTenant: 'Please select the receiver tenant',
    sameTenantNotAllowed: 'The sender tenant and receiver tenant cannot be the same',
    saveSuccess: 'Whitelist saved successfully',
    deleteSuccess: 'Whitelist deleted successfully',
  },
}

