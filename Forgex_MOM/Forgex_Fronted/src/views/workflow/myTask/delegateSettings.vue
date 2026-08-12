<template>
  <div v-permission="'wf:execution:delegate'" class="page-wrap">
    <a-card :bordered="false" class="delegate-card">
      <template #title>{{ t('workflow.myTask.delegateSettingsTitle') }}</template>
      <a-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 14 }"
      >
        <a-form-item :label="t('workflow.myTask.currentUser')">
          <a-input :value="currentUserText" disabled />
        </a-form-item>

        <a-form-item :label="t('workflow.myTask.delegateUser')" name="delegateUserId">
          <a-select
            v-model:value="formData.delegateUserId"
            :options="userOptions"
            :loading="loadingUsers"
            show-search
            allow-clear
            :filter-option="filterUserOption"
            :placeholder="t('workflow.myTask.delegateUserPlaceholder')"
          />
        </a-form-item>

        <a-form-item :label="t('workflow.myTask.delegateComment')" name="comment">
          <a-textarea
            v-model:value="formData.comment"
            :rows="4"
            :placeholder="t('workflow.myTask.actionCommentPlaceholder')"
          />
        </a-form-item>

        <a-form-item :wrapper-col="{ offset: 4, span: 14 }">
          <a-space>
            <a-button type="primary" :loading="submitting" @click="handleSave">
              <template #icon><SaveOutlined /></template>
              {{ t('workflow.myTask.saveDelegate') }}
            </a-button>
            <a-popconfirm
              :title="t('workflow.myTask.delegateCancelConfirm')"
              :ok-text="t('common.confirm')"
              :cancel-text="t('common.cancel')"
              @confirm="handleCancelDelegate"
            >
              <a-button danger :loading="canceling">
                <template #icon><StopOutlined /></template>
                {{ t('workflow.myTask.cancelDelegate') }}
              </a-button>
            </a-popconfirm>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import { SaveOutlined, StopOutlined } from '@ant-design/icons-vue'
import { cancelDelegate, saveDelegate } from '@/api/workflow/execution'
import { getUserList } from '@/api/system/user'
import { useUserStore } from '@/stores/user'

const { t } = useI18n({ useScope: 'global' })
const userStore = useUserStore()

const formRef = ref()
const loadingUsers = ref(false)
const submitting = ref(false)
const canceling = ref(false)
const userOptions = ref<Array<{ label: string; value: number }>>([])

const formData = reactive({
  delegateUserId: undefined as number | undefined,
  comment: '',
})

const currentUserId = computed(() => userStore.userInfo?.id ? Number(userStore.userInfo.id) : undefined)
const currentUserText = computed(() => {
  const info = userStore.userInfo
  if (!info) return sessionStorage.getItem('account') || '-'
  return info.username && info.account ? `${info.username}(${info.account})` : (info.username || info.account || '-')
})

const rules = computed(() => ({
  delegateUserId: [
    {
      validator: async (_rule: any, value: number | undefined) => {
        if (!value) {
          throw new Error(t('workflow.myTask.delegateUserRequired'))
        }
        if (currentUserId.value && Number(value) === currentUserId.value) {
          throw new Error(t('workflow.myTask.delegateUserSameAsCurrent'))
        }
      },
      trigger: 'change',
    },
  ],
}))

function filterUserOption(input: string, option: any) {
  return String(option?.label || '').toLowerCase().includes(input.toLowerCase())
}

async function loadUsers() {
  try {
    loadingUsers.value = true
    const data: any = await getUserList({ pageNum: 1, pageSize: 1000 } as any)
    userOptions.value = (data.records || [])
      .filter((item: any) => item?.id)
      .map((item: any) => ({
        label: `${item.username || item.account || item.id}${item.account ? `(${item.account})` : ''}`,
        value: Number(item.id),
      }))
  } catch (error: any) {
    message.error(error?.message || t('workflow.myTask.loadUserFailed'))
  } finally {
    loadingUsers.value = false
  }
}

async function handleSave() {
  if (!currentUserId.value) {
    message.warning(t('workflow.myTask.currentUserMissing'))
    return
  }
  try {
    await formRef.value?.validate()
    submitting.value = true
    await saveDelegate({
      delegatorUserId: currentUserId.value,
      delegateUserId: Number(formData.delegateUserId),
      comment: formData.comment,
    })
    message.success(t('workflow.myTask.delegateSaveSuccess'))
  } catch (error: any) {
    if (error?.errorFields) {
      return
    }
    message.error(error?.message || t('workflow.myTask.delegateSaveFailed'))
  } finally {
    submitting.value = false
  }
}

async function handleCancelDelegate() {
  if (!currentUserId.value) {
    message.warning(t('workflow.myTask.currentUserMissing'))
    return
  }
  try {
    canceling.value = true
    await cancelDelegate({ delegatorUserId: currentUserId.value })
    message.success(t('workflow.myTask.delegateCancelSuccess'))
    formRef.value?.resetFields?.()
    formData.comment = ''
  } catch (error: any) {
    message.error(error?.message || t('workflow.myTask.delegateCancelFailed'))
  } finally {
    canceling.value = false
  }
}

onMounted(loadUsers)
</script>

<style scoped lang="less">
.page-wrap {
  padding: 16px;
  min-height: 0;
  box-sizing: border-box;
}

.delegate-card {
  max-width: 860px;
  border-radius: 8px;
}
</style>
