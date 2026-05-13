<template>
  <div class="packaging-page">
    <div class="page-header">
      <div>
        <a-tag color="blue">{{ t('basic.packaging.sectionTag') }}</a-tag>
        <h1>{{ t('basic.packaging.title') }}</h1>
        <p>{{ t('basic.packaging.description') }}</p>
      </div>
      <a-space>
        <a-button v-permission="'basic:packaging:add'" type="primary" @click="openCreate">
          <PlusOutlined /> {{ t('basic.packaging.add') }}
        </a-button>
      </a-space>
    </div>

    <FxDynamicTable ref="tableRef" table-code="BasicPackagingTypeTable" :request="handleRequest" row-key="id">
      <template #status="{ record }">
        <a-tag :color="record.status === 1 ? 'success' : 'default'">
          {{ record.status === 1 ? t('common.enable') : t('common.disable') }}
        </a-tag>
      </template>

      <template #action="{ record }">
        <a-space>
          <a v-permission="'basic:packaging:edit'" @click="openEdit(record)">{{ t('common.edit') }}</a>
          <a v-permission="'basic:packaging:delete'" class="danger-link" @click="handleDelete(record)">{{ t('common.delete') }}</a>
        </a-space>
      </template>
    </FxDynamicTable>

    <a-modal
      v-model:open="dialogVisible"
      :title="dialogTitle"
      width="800px"
      :confirm-loading="saving"
      @ok="handleSave"
      @cancel="dialogVisible = false"
    >
      <a-form :model="form" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item :label="t('basic.packaging.fields.code')" required>
              <a-input v-model:value="form.packagingCode" :placeholder="t('basic.packaging.placeholder.code')" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="t('basic.packaging.fields.name')" required>
              <a-input v-model:value="form.packagingName" :placeholder="t('basic.packaging.placeholder.name')" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item :label="t('basic.packaging.fields.material')">
              <a-select v-model:value="form.packagingMaterial" :placeholder="t('basic.packaging.placeholder.material')" allow-clear>
                <a-select-option value="carton">{{ t('basic.packaging.materials.carton') }}</a-select-option>
                <a-select-option value="wooden_box">{{ t('basic.packaging.materials.woodenBox') }}</a-select-option>
                <a-select-option value="pallet">{{ t('basic.packaging.materials.pallet') }}</a-select-option>
                <a-select-option value="iron_drum">{{ t('basic.packaging.materials.ironDrum') }}</a-select-option>
                <a-select-option value="plastic_bag">{{ t('basic.packaging.materials.plasticBag') }}</a-select-option>
                <a-select-option value="bubble_wrap">{{ t('basic.packaging.materials.bubbleWrap') }}</a-select-option>
                <a-select-option value="other">{{ t('basic.packaging.materials.other') }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="t('basic.packaging.fields.unitCost')">
              <a-input-number v-model:value="form.unitCost" :min="0" :precision="2" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item :label="t('basic.packaging.fields.lengthMm')">
              <a-input-number v-model:value="form.lengthMm" :min="0" :precision="2" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item :label="t('basic.packaging.fields.widthMm')">
              <a-input-number v-model:value="form.widthMm" :min="0" :precision="2" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item :label="t('basic.packaging.fields.heightMm')">
              <a-input-number v-model:value="form.heightMm" :min="0" :precision="2" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item :label="t('basic.packaging.fields.weightKg')">
              <a-input-number v-model:value="form.weightKg" :min="0" :precision="2" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="t('basic.packaging.fields.maxLoadKg')">
              <a-input-number v-model:value="form.maxLoadKg" :min="0" :precision="2" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item :label="t('basic.packaging.fields.status')">
              <a-select v-model:value="form.status" :options="statusOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="t('basic.packaging.fields.sortOrder')">
              <a-input-number v-model:value="form.sortOrder" :min="0" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item :label="t('basic.packaging.fields.remark')">
          <a-textarea v-model:value="form.remark" :rows="2" :placeholder="t('basic.packaging.placeholder.remark')" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { useI18n } from 'vue-i18n'
import { createPackagingType, deletePackagingType, getPackagingTypePage, updatePackagingType } from '@/api/basic/packaging'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'

const { t } = useI18n()

const tableRef = ref()
const dialogVisible = ref(false)
const saving = ref(false)
const isEdit = ref(false)

const form = reactive<any>({
  id: null,
  packagingCode: '',
  packagingName: '',
  packagingMaterial: '',
  lengthMm: null,
  widthMm: null,
  heightMm: null,
  weightKg: null,
  maxLoadKg: null,
  unitCost: null,
  status: 1,
  sortOrder: 0,
  remark: '',
})

const statusOptions = computed(() => [
  { value: 1, label: t('common.enable') },
  { value: 0, label: t('common.disable') },
])

const dialogTitle = computed(() => (isEdit.value ? t('basic.packaging.dialog.edit') : t('basic.packaging.dialog.create')))

function handleRequest(payload: {
  page: { current: number; pageSize: number }
  query: Record<string, any>
}) {
  return getPackagingTypePage({
    pageNum: payload.page.current,
    pageSize: payload.page.pageSize,
    ...payload.query,
  })
}

function resetForm() {
  form.id = null
  form.packagingCode = ''
  form.packagingName = ''
  form.packagingMaterial = ''
  form.lengthMm = null
  form.widthMm = null
  form.heightMm = null
  form.weightKg = null
  form.maxLoadKg = null
  form.unitCost = null
  form.status = 1
  form.sortOrder = 0
  form.remark = ''
}

function openCreate() {
  resetForm()
  isEdit.value = false
  dialogVisible.value = true
}

function openEdit(record: any) {
  resetForm()
  Object.assign(form, record)
  isEdit.value = true
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.packagingCode || !form.packagingName) {
    message.warning(t('validation.required'))
    return
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await updatePackagingType(form)
      message.success(t('common.updateSuccess'))
    } else {
      await createPackagingType(form)
      message.success(t('common.createSuccess'))
    }
    dialogVisible.value = false
    tableRef.value?.refresh?.()
  } finally {
    saving.value = false
  }
}

function handleDelete(record: any) {
  Modal.confirm({
    title: t('common.delete'),
    content: t('basic.packaging.confirmDelete', { name: record.packagingName || '' }),
    onOk: async () => {
      await deletePackagingType(record.id)
      message.success(t('common.deleteSuccess'))
      tableRef.value?.refresh?.()
    },
  })
}
</script>

<style scoped lang="less" src="@/styles/views/basic/packaging/index.less"></style>
