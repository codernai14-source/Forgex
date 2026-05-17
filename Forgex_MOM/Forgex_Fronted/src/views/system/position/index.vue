<template>
  <div class="page-wrap fx-sys-position-page">
    <a-row :gutter="16" class="position-page__layout">
      <a-col :span="6" class="position-page__sidebar">
        <div class="card-title">{{ $tl('组织架构') }}</div>
        <DeptTree
          ref="deptTreeRef"
          :show-title-bar="false"
          @select="onSelectNode"
        />
      </a-col>

      <a-col :span="18" class="position-page__main">
        <div class="table-area">
          <fx-dynamic-table
            ref="tableRef"
            :table-code="'PositionTable'"
            :request="handleRequest"
            :dict-options="dictOptions"
            row-key="id"
          >
            <template #toolbar>
              <a-space>
                <a-button data-guide-id="sys-position-add" type="primary" @click="openAdd" v-permission="'sys:position:add'">
                  <template #icon><PlusOutlined /></template>
                  {{ $t('system.position.addPosition') }}
                </a-button>
              </a-space>
            </template>

            <template #status="{ record }">
              <a-tag :color="record.status === true ? 'green' : 'red'">
                {{ record.status === true ? $t('common.enabled') : $t('common.disabled') }}
              </a-tag>
            </template>

            <template #action="{ record }">
              <a-space>
                <a-button type="link" size="small" @click="openEdit(record)" v-permission="'sys:position:edit'">
                  {{ $t('common.edit') }}
                </a-button>
                <a-popconfirm
                  :title="$t('common.confirmDeleteMessage')"
                  :ok-text="$t('common.confirm')"
                  :cancel-text="$t('common.cancel')"
                  @confirm="handleDelete(record.id)"
                >
                  <a-button type="link" size="small" danger v-permission="'sys:position:delete'">
                    {{ $t('common.delete') }}
                  </a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </fx-dynamic-table>
        </div>
      </a-col>
    </a-row>

    <BaseFormDialog
      v-model:open="visible"
      :title="isEdit ? $tl('编辑岗位') : $tl('新增岗位')"
      :confirm-loading="formLoading"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 19 }"
      >
        <a-form-item :label="$tl('所属部门')" name="departmentId">
          <a-tree-select
            v-model:value="formData.departmentId"
            style="width: 100%"
            :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
            :tree-data="treeData"
            :placeholder="$tl('请选择所属部门')"
            tree-default-expand-all
            :field-names="{ children: 'children', label: 'deptName', value: 'id' }"
            allow-clear
          />
        </a-form-item>

        <a-form-item :label="$tl('岗位名称')" name="positionName">
          <a-input v-model:value="formData.positionName" :placeholder="$tl('请输入岗位名称')" />
        </a-form-item>

        <a-form-item :label="$tl('岗位编码')" name="positionCode">
          <a-input v-model:value="formData.positionCode" :placeholder="$tl('请输入岗位编码')" :disabled="isEdit" />
        </a-form-item>

        <a-form-item :label="$tl('岗位级别')" name="positionLevel">
          <a-select v-model:value="formData.positionLevel" :placeholder="$tl('请选择岗位级别')" style="width: 100%">
            <a-select-option v-for="option in positionLevelOptions" :key="option.value" :value="Number(option.value)">
              {{ option.label }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item :label="$tl('排序号')" name="orderNum">
          <a-input-number v-model:value="formData.orderNum" :min="0" :placeholder="$tl('请输入排序号')" style="width: 100%" />
        </a-form-item>

        <a-form-item :label="$t('common.status')" name="status">
          <a-radio-group v-model:value="formData.status">
            <a-radio :value="true">{{ $t('common.enabled') }}</a-radio>
            <a-radio :value="false">{{ $t('common.disabled') }}</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item :label="$tl('备注')" name="remark">
          <a-textarea v-model:value="formData.remark" :placeholder="$tl('请输入备注')" :rows="4" />
        </a-form-item>
      </a-form>
    </BaseFormDialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import DeptTree from '@/components/system/DeptTree.vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import { getDepartmentTree } from '@/api/system/department'
import { getPositionPage, createPosition, updatePosition, deletePosition } from '@/api/system/position'
import { useDict } from '@/hooks/useDict'
import { translateLegacyText } from '@/utils/legacyI18n'
import type { PositionSaveParam } from './types'

const currentTenantId = ref<string | null>(null)
const deptTreeRef = ref()
const treeData = ref<any[]>([])
const { dictItems: positionLevelOptions } = useDict('position_level')
const { dictItems: statusOptions } = useDict('status')
const searchForm = ref({
  positionName: '',
  positionCode: '',
  status: undefined,
  departmentId: undefined as string | undefined,
})
const tableRef = ref()
const loading = ref(false)
const dictOptions = ref({
  status: statusOptions,
  positionLevel: positionLevelOptions,
})

const handleRequest = async (payload: { page: { current: number; pageSize: number }; query: Record<string, any>; sorter?: { field?: string; order?: string } }) => {
  if (!currentTenantId.value) {
    return { records: [], total: 0 }
  }
  loading.value = true
  try {
    const params: any = {
      pageNum: payload.page.current,
      pageSize: payload.page.pageSize,
      tenantId: currentTenantId.value,
      ...searchForm.value,
      ...payload.query,
    }
    const data = await getPositionPage(params)
    const total = typeof data.total === 'number' ? data.total : parseInt(String(data.total) || '0', 10)
    return { records: data.records || [], total }
  } catch (e) {
    console.error('加载岗位列表失败', e)
    return { records: [], total: 0 }
  } finally {
    loading.value = false
  }
}

const visible = ref(false)
const isEdit = ref(false)
const formLoading = ref(false)
const formRef = ref()
const formData = ref<PositionSaveParam & { departmentId?: string }>({
  tenantId: '',
  positionName: '',
  positionCode: '',
  orderNum: 0,
  status: true,
})

const rules = {
  departmentId: [{ required: true, message: translateLegacyText('请选择所属部门'), trigger: 'change' }],
  positionName: [{ required: true, message: translateLegacyText('请输入岗位名称'), trigger: 'blur' }],
  positionCode: [{ required: true, message: translateLegacyText('请输入岗位编码'), trigger: 'blur' }],
}

async function onSelectNode(keys: string[]) {
  searchForm.value.departmentId = keys.length > 0 ? keys[0] : undefined
  await handleSearch()
}

async function loadDeptTreeData() {
  if (!currentTenantId.value) return
  try {
    const data = await getDepartmentTree({ tenantId: currentTenantId.value })
    treeData.value = data || []
  } catch (e) {
    console.error(e)
  }
}

async function handleSearch() {
  await tableRef.value?.refresh?.()
}

async function handleReset() {
  const currentDeptId = searchForm.value.departmentId
  searchForm.value = {
    positionName: '',
    positionCode: '',
    status: undefined,
    departmentId: currentDeptId,
  }
  await tableRef.value?.refresh?.()
}

function openAdd() {
  isEdit.value = false
  visible.value = true
  formData.value = {
    tenantId: currentTenantId.value!,
    departmentId: searchForm.value.departmentId,
    positionName: '',
    positionCode: '',
    orderNum: 0,
    status: true,
  }
}

function openEdit(record: any) {
  isEdit.value = true
  visible.value = true
  formData.value = {
    id: record.id,
    tenantId: record.tenantId,
    departmentId: record.departmentId,
    positionName: record.positionName,
    positionCode: record.positionCode,
    positionLevel: record.positionLevel,
    orderNum: record.orderNum,
    status: record.status,
    remark: record.remark,
  }
}

function handleCancel() {
  visible.value = false
  formRef.value?.resetFields()
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    formLoading.value = true
    if (isEdit.value) {
      await updatePosition(formData.value)
    } else {
      await createPosition(formData.value)
    }
    visible.value = false
    await tableRef.value?.refresh?.()
  } catch (e: any) {
    if (e.errorFields) return
    console.error('保存失败', e)
  } finally {
    formLoading.value = false
  }
}

async function handleDelete(id: string) {
  try {
    await deletePosition({ id, tenantId: currentTenantId.value! })
    await tableRef.value?.refresh?.()
  } catch (e) {
    console.error('删除失败', e)
  }
}

onMounted(async () => {
  const tid = sessionStorage.getItem('tenantId')
  if (tid) {
    currentTenantId.value = tid
    await tableRef.value?.refresh?.()
    await loadDeptTreeData()
  }
})
</script>

<style scoped lang="less" src="@/styles/views/system/position/index.less"></style>
