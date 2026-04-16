<template>
  <BaseFormDialog
      v-model:visible="dialogVisible"
      :title="formTitle"
      :form-items="formItems"
      :model-value="formData"
      width="600px"
      @save="handleSubmit"
      @cancel="handleCancel"
  />
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { labelBindingApi } from '@/api/label/binding'

const props = defineProps<{
  visible: boolean
  bindingData?: any
}>()

const emit = defineEmits(['update:visible', 'success'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const formTitle = computed(() =>
    props.bindingData ? '编辑绑定关系' : '新增绑定关系'
)

const formData = ref<any>({
  templateId: undefined,
  bindingType: '',
  bindingValue: '',
  factoryId: undefined,
  priority: 3
})

const formItems = computed(() => [
  {
    label: '模板ID',
    field: 'templateId',
    component: 'a-input-number',
    rules: [{ required: true, message: '请选择模板' }],
    props: { style: { width: '100%' }, placeholder: '请输入模板ID' }
  },
  {
    label: '绑定类型',
    field: 'bindingType',
    component: 'a-select',
    rules: [{ required: true, message: '请选择绑定类型' }],
    options: [
      { label: '按物料', value: 'MATERIAL' },
      { label: '按供应商', value: 'SUPPLIER' },
      { label: '按客户', value: 'CUSTOMER' }
    ]
  },
  {
    label: '绑定值',
    field: 'bindingValue',
    component: 'a-input',
    rules: [{ required: true, message: '请输入绑定值' }],
    props: { placeholder: '根据绑定类型输入对应的ID或编码' }
  },
  {
    label: '工厂ID',
    field: 'factoryId',
    component: 'a-input-number',
    props: { style: { width: '100%' }, placeholder: '不填表示全局' }
  },
  {
    label: '优先级',
    field: 'priority',
    component: 'a-select',
    rules: [{ required: true, message: '请选择优先级' }],
    options: [
      { label: '高', value: 1 },
      { label: '中', value: 2 },
      { label: '低', value: 3 }
    ]
  }
])

watch(() => props.bindingData, (val) => {
  if (val) {
    formData.value = { ...val }
  } else {
    resetForm()
  }
}, { immediate: true })

function resetForm() {
  formData.value = {
    templateId: undefined,
    bindingType: '',
    bindingValue: '',
    factoryId: undefined,
    priority: 3
  }
}

async function handleSubmit() {
  if (props.bindingData) {
    await labelBindingApi.update(formData.value)
  } else {
    await labelBindingApi.add(formData.value)
  }
  emit('success')
  dialogVisible.value = false
}

function handleCancel() {
  dialogVisible.value = false
  resetForm()
}
</script>
