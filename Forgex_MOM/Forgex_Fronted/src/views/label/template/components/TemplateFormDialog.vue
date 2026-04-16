
<template>
  <BaseFormDialog
      v-model:visible="dialogVisible"
      :title="formTitle"
      :form-items="formItems"
      :model-value="formData"
      width="900px"
      @save="handleSubmit"
      @cancel="handleCancel"
  >
    <template #templateContent="{ value, onChange }">
      <LabelTemplateEditor
          :model-value="value"
          @update:model-value="onChange"
      />
    </template>
  </BaseFormDialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { labelTemplateApi } from '@/api/label/template'
import LabelTemplateEditor from './LabelTemplateEditor.vue'

const props = defineProps<{
  visible: boolean
  templateData?: any
}>()

const emit = defineEmits(['update:visible', 'success'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const formTitle = computed(() =>
    props.templateData ? '编辑模板' : '新增模板'
)

const formData = ref<any>({
  templateCode: '',
  templateName: '',
  templateType: '',
  templateContent: '{}',
  description: '',
  status: 1,
  isDefault: false
})

const formItems = computed(() => [
  {
    label: '模板编码',
    field: 'templateCode',
    component: 'a-input',
    rules: [{ required: true, message: '请输入模板编码' }]
  },
  {
    label: '模板名称',
    field: 'templateName',
    component: 'a-input',
    rules: [{ required: true, message: '请输入模板名称' }]
  },
  {
    label: '模板类型',
    field: 'templateType',
    component: 'a-select',
    rules: [{ required: true, message: '请选择模板类型' }],
    options: [
      { label: '物料标签', value: 'MATERIAL' },
      { label: '供应商标签', value: 'SUPPLIER' },
      { label: '客户唛头', value: 'CUSTOMER_MARK' },
      { label: '工位标签', value: 'WORKSTATION' },
      { label: '设备标签', value: 'EQUIPMENT' },
      { label: '库位标签', value: 'LOCATION' }
    ]
  },
  {
    label: '模板内容',
    field: 'templateContent',
    slot: 'templateContent',
    rules: [{ required: true, message: '请设计模板内容' }]
  },
  {
    label: '描述',
    field: 'description',
    component: 'a-textarea',
    props: { rows: 3 }
  },
  {
    label: '状态',
    field: 'status',
    component: 'a-radio-group',
    options: [
      { label: '启用', value: 1 },
      { label: '禁用', value: 0 }
    ]
  }
])

watch(() => props.templateData, (val) => {
  if (val) {
    formData.value = { ...val }
  } else {
    resetForm()
  }
}, { immediate: true })

function resetForm() {
  formData.value = {
    templateCode: '',
    templateName: '',
    templateType: '',
    templateContent: '{}',
    description: '',
    status: 1,
    isDefault: false
  }
}

async function handleSubmit() {
  if (props.templateData) {
    await labelTemplateApi.update(formData.value)
  } else {
    await labelTemplateApi.add(formData.value)
  }
  emit('success')
  dialogVisible.value = false
}

function handleCancel() {
  dialogVisible.value = false
  resetForm()
}
</script>
