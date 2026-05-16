<template>
  <div class="page-container">
    <FxDynamicTable ref="tableRef" table-code="LabelTemplateTable" :request="loadData" row-key="id">
      <template #toolbar>
        <a-button type="primary" @click="openCreate"><PlusOutlined /> 新增</a-button>
      </template>
      <template #isEnabled="{ record }">
        <a-tag :color="record.isEnabled ? 'green' : 'default'">{{ record.isEnabled ? '启用' : '停用' }}</a-tag>
      </template>
      <template #action="{ record }">
        <a-space>
          <a @click="openDesigner(record)">设计</a>
          <a @click="openPreview(record)">预览</a>
          <a @click="openEdit(record)">编辑</a>
          <a class="danger-link" @click="handleDelete(record)">删除</a>
        </a-space>
      </template>
    </FxDynamicTable>

    <a-modal v-model:open="editorVisible" :title="editorTitle" width="680px" @ok="handleSubmit">
      <a-form :model="form" layout="vertical">
        <a-row :gutter="12">
          <a-col :span="12"><a-form-item label="模板编码" required><a-input v-model:value="form.templateCode" :disabled="!!editingId" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="模板名称" required><a-input v-model:value="form.templateName" /></a-form-item></a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="12"><a-form-item label="标签类型"><a-select v-model:value="form.typeId" allow-clear :options="typeOptions" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="纸张规格"><a-input v-model:value="form.paperSize" /></a-form-item></a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="12"><a-form-item label="纸张宽度(mm)"><a-input-number v-model:value="form.paperWidth" :min="10" style="width:100%" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="纸张高度(mm)"><a-input-number v-model:value="form.paperHeight" :min="10" style="width:100%" /></a-form-item></a-col>
        </a-row>
        <a-form-item label="是否启用"><a-switch v-model:checked="form.isEnabled" /></a-form-item>
        <a-form-item label="描述"><a-textarea v-model:value="form.description" :rows="3" /></a-form-item>
      </a-form>
    </a-modal>

    <a-drawer v-model:open="designerVisible" title="标签模板设计" width="100vw" :body-style="{ padding: 0 }" destroy-on-close>
      <LabelTemplateEditor v-if="designerTemplate" :template="designerTemplate" @saved="handleDesignerSaved" />
    </a-drawer>

    <a-modal v-model:open="previewVisible" title="模板预览" width="900px" :footer="null">
      <div v-if="previewData" class="preview-wrap">
        <LabelPreview :template="previewData" />
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Modal } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { labelTemplateApi } from '@/api/label/template'
import { labelTypeApi } from '@/api/label/type'
import LabelTemplateEditor from './components/LabelTemplateEditor.vue'
import LabelPreview from './components/LabelPreview.vue'

const tableRef = ref()
const editorVisible = ref(false)
const designerVisible = ref(false)
const previewVisible = ref(false)
const editorTitle = ref('新增标签模板')
const editingId = ref<number | null>(null)
const designerTemplate = ref<any>(null)
const previewData = ref<any>(null)
const typeOptions = ref<any[]>([])
const form = ref<any>({})

function defaultForm() {
  return { templateCode: '', templateName: '', typeId: undefined, paperSize: 'CUSTOM', paperWidth: 100, paperHeight: 60, isEnabled: true, description: '' }
}

function loadData(params: any) {
  return labelTemplateApi.page(params)
}

async function loadTypes() {
  const res = await labelTypeApi.options()
  typeOptions.value = (res || []).map((item: any) => ({ label: item.typeName, value: item.id }))
}

function openCreate() {
  editingId.value = null
  form.value = defaultForm()
  editorTitle.value = '新增标签模板'
  editorVisible.value = true
}

function openEdit(record: any) {
  editingId.value = record.id
  form.value = { ...defaultForm(), ...record }
  editorTitle.value = '编辑标签模板'
  editorVisible.value = true
}

async function handleSubmit() {
  if (editingId.value) {
    await labelTemplateApi.update({ id: editingId.value, ...form.value })
  } else {
    await labelTemplateApi.add(form.value)
  }
  editorVisible.value = false
  tableRef.value?.reload()
}

async function openDesigner(record: any) {
  designerTemplate.value = await labelTemplateApi.designDetail(record.id)
  designerVisible.value = true
}

function handleDesignerSaved() {
  designerVisible.value = false
  tableRef.value?.reload()
}

async function openPreview(record: any) {
  previewData.value = await labelTemplateApi.preview(record.id)
  previewVisible.value = true
}

function handleDelete(record: any) {
  Modal.confirm({
    title: '确认删除？',
    onOk: async () => {
      await labelTemplateApi.delete(record.id)
      tableRef.value?.reload()
    }
  })
}

onMounted(loadTypes)
</script>

<style scoped>
.preview-wrap {
  display: flex;
  justify-content: center;
  padding: 24px;
  background: #f5f5f5;
}
.danger-link {
  color: #ff4d4f;
}
</style>
