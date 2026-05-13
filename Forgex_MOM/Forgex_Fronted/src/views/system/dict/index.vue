<template>
  <div class="dict-container">
    <FxDynamicTable
      ref="tableRef"
      table-code="DictTable"
      :request="handleRequest"
      :dict-options="dictOptions"
      :default-expand-all-rows="true"
      :show-query-form="true"
      row-key="id"
    >
      <template #toolbar>
        <a-button data-guide-id="sys-dict-add" type="primary" @click="handleAdd(null)">{{ $tl('新增字典') }}</a-button>
      </template>

      <template #moduleId="{ record }">
        <a-tag v-if="resolveModuleLabel(record)" color="blue">
          {{ resolveModuleLabel(record) }}
        </a-tag>
        <span v-else>-</span>
      </template>

      <template #status="{ record }">
        <a-tag
          v-if="resolveStatusTag(record.status)"
          :color="resolveStatusTag(record.status)?.color"
          :style="resolveStatusTag(record.status)?.style"
        >
          {{ resolveStatusTag(record.status)?.label }}
        </a-tag>
        <span v-else>{{ record.status ?? '-' }}</span>
      </template>

      <template #action="{ record }">
        <a-space>
          <a v-if="!record.dictValue" @click="handleAdd(record)">{{ $tl('新增子项') }}</a>
          <a @click="handleEdit(record)">{{ $tl('编辑') }}</a>
          <a style="color: #ff4d4f" @click="handleDelete(record)">{{ $tl('删除') }}</a>
        </a-space>
      </template>
    </FxDynamicTable>

    <BaseFormDialog
      v-model:open="dialogVisible"
      :title="dialogTitle"
      width="640px"
      @submit="handleSubmit"
      @cancel="handleDialogClose"
    >
      <a-form ref="formRef" :model="form" :rules="formRules" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item :label="$tl('字典名称')" name="dictName">
          <a-input v-model:value="form.dictName" :placeholder="$tl('请输入字典名称')" />
        </a-form-item>

        <a-form-item :label="$tl('所属模块')" name="moduleId">
          <a-select
            v-model:value="form.moduleId"
            :options="moduleOptions"
            :disabled="isChildNode"
            allow-clear
            :placeholder="$tl('请选择所属模块')"
          />
        </a-form-item>

        <a-form-item v-if="!isChildNode" :label="$tl('字典编码')" name="dictCode">
          <a-input v-model:value="form.dictCode" :placeholder="$tl('请输入字典编码')" />
        </a-form-item>

        <a-form-item v-else :label="$tl('字典值')" name="dictValue">
          <a-input v-model:value="form.dictValue" :placeholder="$tl('请输入字典值')" />
        </a-form-item>

        <a-form-item v-if="isChildNode" :label="$tl('国际化配置')" name="dictValueI18nJson">
          <I18nInput
            v-model="form.dictValueI18nJson"
            mode="simple"
            :placeholder="$tl('请输入字典值（可点击右侧地球图标配置多语言）')"
          />
        </a-form-item>

        <a-form-item v-if="isChildNode" :label="$tl('标签样式')">
          <TagStyleConfig ref="tagStyleConfigRef" />
        </a-form-item>

        <a-form-item :label="$tl('排序号')" name="orderNum">
          <a-input-number v-model:value="form.orderNum" :min="0" style="width: 100%" />
        </a-form-item>

        <a-form-item :label="$tl('状态')" name="status">
          <a-radio-group v-model:value="form.status">
            <a-radio v-for="option in statusOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item :label="$tl('备注')" name="remark">
          <a-textarea v-model:value="form.remark" :rows="3" :placeholder="$tl('请输入备注')" />
        </a-form-item>
      </a-form>
    </BaseFormDialog>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { Modal, type FormInstance, type Rule } from 'ant-design-vue'
import http from '@/api/http'
import { listModules } from '@/api/system/module'

import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import I18nInput from '@/components/common/I18nInput.vue'
import TagStyleConfig from '@/components/system/TagStyleConfig.vue'
import { useDict } from '@/hooks/useDict'
import { translateLegacyText } from '@/utils/legacyI18n'

const { dictItems: statusOptions } = useDict('status')

const tableRef = ref()
const formRef = ref<FormInstance>()
const dialogVisible = ref(false)
const dialogTitle = ref('')
const tagStyleConfigRef = ref()
const moduleOptions = ref<Array<{ label: string; value: number }>>([])

const dictOptions = computed(() => ({
  status: statusOptions.value,
  moduleId: moduleOptions.value,
}))


const form = reactive({
  id: null as number | null,
  parentId: 0,
  moduleId: undefined as number | undefined,
  dictName: '',
  dictCode: '',
  dictValue: '',
  dictValueI18nJson: '',
  orderNum: 0,
  status: 1,
  remark: '',
})

const formRules = computed<Record<string, Rule[]>>(() => ({
  dictName: [{ required: true, message: translateLegacyText('请输入字典名称'), trigger: 'blur' }],
  dictCode: isChildNode.value ? [] : [{ required: true, message: translateLegacyText('请输入字典编码'), trigger: 'blur' }],
  dictValue: isChildNode.value ? [{ required: true, message: translateLegacyText('请输入字典值'), trigger: 'blur' }] : [],
  status: [{ required: true, message: translateLegacyText('请选择状态'), trigger: 'change' }],
}))

const isChildNode = computed(() => !!(form.parentId && form.parentId > 0))

function mapDictTreeNodes(nodes: any[]): any[] {
  return (nodes || []).map((item: any) => ({
    ...item,
    createByName: item.createByName || item.createBy || '',
    updateByName: item.updateByName || item.updateBy || '',
    createBy: item.createByName || item.createBy || '',
    updateBy: item.updateByName || item.updateBy || '',
    children: Array.isArray(item.children) ? mapDictTreeNodes(item.children) : [],
  }))
}

function resolveModuleLabel(record: any) {
  if (record?.moduleName) {
    return record.moduleName
  }
  const option = moduleOptions.value.find((item) => String(item.value) === String(record?.moduleId))
  return option?.label || ''
}

function resolveStatusTag(value: unknown) {
  const normalizedValue = value === true || value === 1 || value === '1' ? 1 : 0
  const dictItem = statusOptions.value.find((item) => String(item?.value) === String(normalizedValue))
  if (!dictItem) {
    return null
  }
  const style =
    dictItem.tagStyle?.borderColor || dictItem.tagStyle?.backgroundColor
      ? {
          borderColor: dictItem.tagStyle?.borderColor,
          backgroundColor: dictItem.tagStyle?.backgroundColor,
        }
      : undefined

  return {
    label: dictItem.label,
    color: dictItem.tagStyle?.color || dictItem.color || 'blue',
    style,
  }
}

const handleRequest = async (payload: {
  page: { current: number; pageSize: number }
  query: Record<string, any>
}) => {
  try {
    const res = await http.post('/sys/dict/page', {
      pageNum: payload.page.current,
      pageSize: payload.page.pageSize,
      dictCode: payload.query?.dictCode,
      dictName: payload.query?.dictName,
      moduleId: payload.query?.moduleId,
    })
    return {
      records: mapDictTreeNodes(res?.records || []),
      total: Number(res?.total || 0),
    }
  } catch (error) {
    console.error('load dict page failed', error)
    return {
      records: [],
      total: 0,
    }
  }
}

async function loadModules() {
  try {
    const modules: any[] = await listModules({})
    moduleOptions.value = (modules || []).map((item: any) => ({
      label: item.name,
      value: Number(item.id),
    }))
  } catch (error) {
    console.error('load modules failed', error)
    moduleOptions.value = []
  }
}

function resetForm() {
  form.id = null
  form.parentId = 0
  form.moduleId = undefined
  form.dictName = ''
  form.dictCode = ''
  form.dictValue = ''
  form.dictValueI18nJson = ''
  form.orderNum = 0
  form.status = 1
  form.remark = ''
  formRef.value?.clearValidate?.()
  tagStyleConfigRef.value?.setTagStyleJson('')
}

function openDialog() {
  dialogVisible.value = true
  nextTick(() => {
    formRef.value?.clearValidate?.()
  })
}

function handleAdd(row: any) {
  resetForm()
  dialogTitle.value = row ? translateLegacyText('新增字典子项') : translateLegacyText('新增字典类型')
  form.parentId = row ? Number(row.id) : 0
  form.moduleId = row?.moduleId != null ? Number(row.moduleId) : undefined
  openDialog()
}

function handleEdit(row: any) {
  resetForm()
  dialogTitle.value = translateLegacyText('编辑字典')
  form.id = Number(row.id)
  form.parentId = Number(row.parentId || 0)
  form.moduleId = row?.moduleId != null ? Number(row.moduleId) : undefined
  form.dictName = row.dictName || ''
  form.dictCode = row.dictCode || ''
  form.dictValue = row.dictValue || ''
  form.dictValueI18nJson = row.dictValueI18nJson || ''
  form.orderNum = Number(row.orderNum || 0)
  form.status = row.status === 0 || row.status === '0' ? 0 : 1
  form.remark = row.remark || ''
  openDialog()
  nextTick(() => {
    tagStyleConfigRef.value?.setTagStyleJson(row.tagStyleJson || '')
  })
}

function handleDelete(row: any) {
  Modal.confirm({
    title: translateLegacyText('提示'),
    content: translateLegacyText('确定要删除该字典吗？'),
    okText: translateLegacyText('确定'),
    cancelText: translateLegacyText('取消'),
    onOk: async () => {
      await http.post('/sys/dict/delete', { id: row.id })
      await tableRef.value?.refresh?.()
    },
  })
}

async function handleSubmit() {
  await formRef.value?.validate()
  const tagStyleJson = tagStyleConfigRef.value?.getTagStyleJson() || ''
  const url = form.id ? '/sys/dict/update' : '/sys/dict/create'
  await http.post(url, {
    ...form,
    moduleId: form.moduleId ?? null,
    tagStyleJson,
  })
  dialogVisible.value = false
  resetForm()
  await tableRef.value?.refresh?.()
}

function handleDialogClose() {
  dialogVisible.value = false
  resetForm()
}

onMounted(async () => {
  await loadModules()
  await tableRef.value?.refresh?.()
})
</script>

<style scoped lang="less" src="@/styles/views/system/dict/index.less"></style>
