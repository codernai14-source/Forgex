<template>
  <a-modal
      v-model:open="dialogVisible"
      :title="formTitle"
      width="700px"
      :confirm-loading="submitting"
      @ok="handleSubmit"
      @cancel="handleCancel"
  >
    <a-form :model="formData" layout="vertical">
      <a-form-item :label="t('label.binding.selectTemplate')" required>
        <a-input
            v-model:value="formData.templateName"
            :placeholder="t('label.binding.selectTemplate')"
            readonly
            @click="handleSelectTemplate"
        >
          <template #suffix>
            <SearchOutlined style="color: #1890ff; cursor: pointer;" />
          </template>
        </a-input>
        <div v-if="formData.templateId" class="template-info">
          <a-tag color="blue">{{ formData.templateCode }}</a-tag>
          <span style="margin-left: 8px; font-size: 12px; color: #999;">{{ t('label.binding.templateId') }}: {{ formData.templateId }}</span>
        </div>
      </a-form-item>

      <a-form-item :label="t('label.binding.bindingType')" required>
        <a-select v-model:value="formData.bindingType" :placeholder="t('label.binding.selectBindingType')">
          <a-select-option value="MATERIAL">
            <a-tag color="blue">{{ t('label.binding.matchByMaterial') }}</a-tag>
          </a-select-option>
          <a-select-option value="SUPPLIER">
            <a-tag color="green">{{ t('label.binding.matchBySupplier') }}</a-tag>
          </a-select-option>
          <a-select-option value="CUSTOMER">
            <a-tag color="orange">{{ t('label.binding.matchByCustomer') }}</a-tag>
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item :label="t('label.binding.bindingValue')" required>
        <a-input
            v-model:value="formData.bindingValue"
            :placeholder="getBindingValuePlaceholder()"
        />
      </a-form-item>

      <a-form-item :label="t('label.print.factory')">
        <a-input
            v-model:value="formData.factoryName"
            :placeholder="t('label.binding.globalIfEmpty')"
            readonly
            @click="handleSelectFactory"
        >
          <template #suffix>
            <SearchOutlined style="color: #1890ff; cursor: pointer;" />
          </template>
        </a-input>
        <div v-if="formData.factoryId" class="factory-info">
          <a-tag color="green">{{ formData.factoryName }}</a-tag>
          <span style="margin-left: 8px; font-size: 12px; color: #999;">{{ t('label.binding.factoryId') }}: {{ formData.factoryId }}</span>
        </div>
      </a-form-item>

      <a-form-item :label="t('label.binding.priority')" required>
        <a-radio-group v-model:value="formData.priority">
          <a-radio :value="1">
            <a-tag color="red">{{ t('label.binding.priorityHighWithValue') }}</a-tag>
          </a-radio>
          <a-radio :value="2">
            <a-tag color="orange">{{ t('label.binding.priorityMediumWithValue') }}</a-tag>
          </a-radio>
          <a-radio :value="3">
            <a-tag color="blue">{{ t('label.binding.priorityLowWithValue') }}</a-tag>
          </a-radio>
        </a-radio-group>
        <div class="priority-tip">
          <InfoCircleOutlined style="color: #1890ff; margin-right: 4px;" />
          {{ t('label.binding.priorityTip') }}
        </div>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined, InfoCircleOutlined } from '@ant-design/icons-vue'
import { useI18n } from 'vue-i18n'
import { labelBindingApi } from '@/api/label/binding'

const { t } = useI18n()

const props = defineProps<{
  visible: boolean
  bindingData?: any
}>()

const emit = defineEmits(['update:visible', 'success'])

const submitting = ref(false)

const dialogVisible = computed({
  get: () => {
        return props.visible
  },
  set: (val) => {
        emit('update:visible', val)
  }
})

const formTitle = computed(() =>
    props.bindingData ? t('label.binding.editTitle') : t('label.binding.addTitle')
)

const formData = ref<any>({
  id: undefined,
  templateId: undefined,
  templateCode: '',
  templateName: '',
  bindingType: '',
  bindingValue: '',
  factoryId: undefined,
  factoryName: '',
  priority: 3
})

watch(() => props.visible, (newVal) => {
    if (newVal && props.bindingData) {
    // 编辑模式，填充数据
    formData.value = {
      id: props.bindingData.id,
      templateId: props.bindingData.templateId,
      templateCode: props.bindingData.templateCode,
      templateName: props.bindingData.templateName,
      bindingType: props.bindingData.bindingType,
      bindingValue: props.bindingData.bindingValue,
      factoryId: props.bindingData.factoryId,
      factoryName: props.bindingData.factoryName,
      priority: props.bindingData.priority ?? 3
    }
      } else if (newVal) {
    // 新增模式，重置表单
    resetForm()
  }
})

function resetForm() {
  formData.value = {
    id: undefined,
    templateId: undefined,
    templateCode: '',
    templateName: '',
    bindingType: '',
    bindingValue: '',
    factoryId: undefined,
    factoryName: '',
    priority: 3
  }
}

function getBindingValuePlaceholder() {
  const placeholders: Record<string, string> = {
    MATERIAL: t('label.binding.materialPlaceholder'),
    SUPPLIER: t('label.binding.supplierPlaceholder'),
    CUSTOMER: t('label.binding.customerPlaceholder')
  }
  return placeholders[formData.value.bindingType] || t('label.binding.selectBindingTypeFirst')
}

function handleSelectTemplate() {
  message.info(t('label.binding.templateSelectPending'))
}

function handleSelectFactory() {
  message.info(t('label.binding.factorySelectPending'))
}

async function handleSubmit() {
  // 表单验证
  if (!formData.value.templateId) {
    message.warning(t('label.binding.selectTemplate'))
    return
  }
  if (!formData.value.bindingType) {
    message.warning(t('label.binding.selectBindingType'))
    return
  }
  if (!formData.value.bindingValue) {
    message.warning(t('label.binding.inputBindingValue'))
    return
  }
  if (!formData.value.priority) {
    message.warning(t('label.binding.selectPriority'))
    return
  }

  submitting.value = true
  try {
    if (props.bindingData) {
      // 编辑模式
      await labelBindingApi.update({
        id: formData.value.id,
        priority: formData.value.priority,
        factoryId: formData.value.factoryId
      })
      message.success(t('message.updateSuccess'))
    } else {
      // 新增模式
      await labelBindingApi.add({
        templateId: formData.value.templateId,
        bindingType: formData.value.bindingType,
        bindingValue: formData.value.bindingValue,
        factoryId: formData.value.factoryId,
        priority: formData.value.priority
      })
      message.success(t('message.createSuccess'))
    }
    emit('success')
    dialogVisible.value = false
  } catch (error: any) {
    message.error(error.message || (props.bindingData ? t('message.updateFailed') : t('message.createFailed')))
  } finally {
    submitting.value = false
  }
}

function handleCancel() {
  dialogVisible.value = false
  resetForm()
}
</script>

<style scoped lang="less" src="@/styles/views/label/binding/components/binding-form-dialog.less"></style>

