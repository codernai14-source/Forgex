<template>
  <div class="receiver-selector">
    <a-form-item :label="t('common.receiverSelector.receiverType')" required>
      <a-select
        v-model:value="localReceiver.receiverType"
        :placeholder="t('common.receiverSelector.selectReceiverType')"
        @change="handleTypeChange"
      >
        <a-select-option value="USER">{{ t('common.receiverSelector.typeUser') }}</a-select-option>
        <a-select-option value="ROLE">{{ t('common.receiverSelector.typeRole') }}</a-select-option>
        <a-select-option value="DEPT">{{ t('common.receiverSelector.typeDept') }}</a-select-option>
        <a-select-option value="POSITION">{{ t('common.receiverSelector.typePosition') }}</a-select-option>
        <a-select-option value="CUSTOM">{{ t('common.receiverSelector.typeCustom') }}</a-select-option>
      </a-select>
    </a-form-item>

    <a-form-item v-if="localReceiver.receiverType !== 'CUSTOM'" :label="getReceiverLabel()" required>
      <a-select
        v-model:value="localReceiver.receiverIds"
        mode="multiple"
        :placeholder="t('common.receiverSelector.selectReceiver', { label: getReceiverLabel() })"
        :options="receiverOptions"
        :loading="loading"
        :filter-option="filterOption"
        show-search
        allow-clear
        @change="handleReceiverChange"
      >
        <template #notFoundContent>
          <a-empty :description="loading ? t('common.loading') : t('common.noData')" />
        </template>
      </a-select>
    </a-form-item>

    <a-form-item v-else :label="t('common.description')">
      <a-alert
        :message="t('common.receiverSelector.customTitle')"
        :description="t('common.receiverSelector.customDescription')"
        type="info"
        show-icon
      />
    </a-form-item>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import { getRoleList } from '@/api/system/role'
import { listDepartments } from '@/api/system/department'
import { listPositions } from '@/api/system/position'
import { getUserList } from '@/api/system/user'

interface Receiver {
  receiverType?: string
  receiverIds: string[]
}

interface Props {
  modelValue: Receiver
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: Receiver]
}>()

const { t } = useI18n()
const localReceiver = ref<Receiver>({ ...props.modelValue })
const loading = ref(false)
const receiverOptions = ref<Array<{ label: string; value: string }>>([])

function getReceiverLabel() {
  const labelMap: Record<string, string> = {
    USER: t('common.receiverSelector.labelUser'),
    ROLE: t('common.receiverSelector.labelRole'),
    DEPT: t('common.receiverSelector.labelDept'),
    POSITION: t('common.receiverSelector.labelPosition'),
  }
  return labelMap[localReceiver.value.receiverType || ''] || t('common.receiverSelector.receiver')
}

function filterOption(input: string, option: any) {
  return String(option?.label || '').toLowerCase().includes(input.toLowerCase())
}

async function loadReceiverOptions() {
  if (!localReceiver.value.receiverType) {
    receiverOptions.value = []
    return
  }

  loading.value = true
  try {
    let data: Array<{ label: string; value: string }> = []

    switch (localReceiver.value.receiverType) {
      case 'USER': {
        const userRes = await getUserList({ pageNum: 1, pageSize: 1000 })
        data = (userRes.records || []).map((item: any) => ({
          label: `${item.username}(${item.account})`,
          value: String(item.id),
        }))
        break
      }
      case 'ROLE': {
        const roleRes = await getRoleList({})
        data = (roleRes || []).map((item: any) => ({
          label: item.roleName,
          value: String(item.id),
        }))
        break
      }
      case 'DEPT': {
        const deptRes = await listDepartments({})
        data = (deptRes || []).map((item: any) => ({
          label: item.deptName,
          value: String(item.id),
        }))
        break
      }
      case 'POSITION': {
        const posRes = await listPositions({})
        data = (posRes || []).map((item: any) => ({
          label: item.positionName,
          value: String(item.id),
        }))
        break
      }
    }

    receiverOptions.value = data
  } catch (error) {
    console.error('[ReceiverSelector] load receiver options failed:', error)
    message.error(t('common.receiverSelector.loadFailed'))
  } finally {
    loading.value = false
  }
}

function handleTypeChange() {
  localReceiver.value.receiverIds = []
  if (localReceiver.value.receiverType !== 'CUSTOM') {
    void loadReceiverOptions()
  }
  emitChange()
}

function handleReceiverChange() {
  emitChange()
}

function emitChange() {
  emit('update:modelValue', { ...localReceiver.value })
}

watch(() => props.modelValue, (newVal) => {
  localReceiver.value = { ...newVal }
  if (newVal.receiverType && newVal.receiverType !== 'CUSTOM') {
    void loadReceiverOptions()
  }
}, { deep: true })

onMounted(() => {
  if (localReceiver.value.receiverType && localReceiver.value.receiverType !== 'CUSTOM') {
    void loadReceiverOptions()
  }
})
</script>
