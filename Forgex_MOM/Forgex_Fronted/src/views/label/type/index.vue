<template>
  <div class="page-container">
    <FxDynamicTable
      ref="tableRef"
      table-code="LabelTypeTable"
      :request="loadData"
      row-key="id"
    >
      <template #toolbar>
        <a-space>
          <a-button type="primary" @click="openCreate">
            <PlusOutlined /> 新增
          </a-button>
        </a-space>
      </template>

      <template #isEnabled="{ record }">
        <a-tag :color="record.isEnabled ? 'green' : 'default'">
          {{ record.isEnabled ? '启用' : '停用' }}
        </a-tag>
      </template>

      <template #action="{ record }">
        <a-space>
          <a @click="openEdit(record)">编辑</a>
          <a @click="toggleEnabled(record)">{{ record.isEnabled ? '停用' : '启用' }}</a>
          <a class="danger-link" @click="handleDelete(record)">删除</a>
        </a-space>
      </template>
    </FxDynamicTable>

    <a-modal v-model:open="editorVisible" :title="editorTitle" @ok="handleSubmit" @cancel="closeEditor">
      <a-form :model="form" layout="vertical">
        <a-form-item label="类型编码" required>
          <a-input v-model:value="form.typeCode" />
        </a-form-item>
        <a-form-item label="类型名称" required>
          <a-input v-model:value="form.typeName" />
        </a-form-item>
        <a-form-item label="是否启用">
          <a-switch v-model:checked="form.isEnabled" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { labelTypeApi } from '@/api/label/type'

const tableRef = ref()
const editorVisible = ref(false)
const editorTitle = ref('新增标签类型')
const editingId = ref<number | null>(null)
const form = ref<any>({ typeCode: '', typeName: '', isEnabled: true })

function loadData(params: any) {
  return labelTypeApi.page(params)
}

function openCreate() {
  editingId.value = null
  form.value = { typeCode: '', typeName: '', isEnabled: true }
  editorTitle.value = '新增标签类型'
  editorVisible.value = true
}

function openEdit(record: any) {
  editingId.value = record.id
  form.value = { typeCode: record.typeCode, typeName: record.typeName, isEnabled: !!record.isEnabled }
  editorTitle.value = '编辑标签类型'
  editorVisible.value = true
}

async function handleSubmit() {
  if (!form.value.typeCode || !form.value.typeName) {
    message.warning('请填写完整')
    return
  }
  if (editingId.value) {
    await labelTypeApi.update({ id: editingId.value, ...form.value })
  } else {
    await labelTypeApi.add(form.value)
  }
  editorVisible.value = false
  tableRef.value?.reload()
}

function closeEditor() {
  editorVisible.value = false
}

function toggleEnabled(record: any) {
  Modal.confirm({
    title: record.isEnabled ? '确认停用？' : '确认启用？',
    onOk: async () => {
      await labelTypeApi.enable(record.id, !record.isEnabled)
      tableRef.value?.reload()
    }
  })
}

function handleDelete(record: any) {
  Modal.confirm({
    title: '确认删除？',
    onOk: async () => {
      await labelTypeApi.delete(record.id)
      tableRef.value?.reload()
    }
  })
}
</script>
