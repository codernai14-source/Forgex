<template>
  <div class="page-wrap">
    <a-card class="content-card" :bordered="false">
      <a-row :gutter="16">
        <!-- 左侧：部门树 -->
        <a-col :span="6">
          <div class="card-title">组织架构</div>
          <DeptTree
            ref="deptTreeRef"
            @select="onSelectNode"
          />
        </a-col>

        <!-- 右侧：职位列表 -->
        <a-col :span="18">
            <!-- 操作按钮和表格 -->
          <div class="table-area">
            <fx-dynamic-table
              ref="tableRef"
              :table-code="'PositionTable'"
              :request="handleRequest"
              :dict-options="dictOptions"
              :fallback-config="fallbackConfig"
              row-key="id"
            >
              <template #toolbar>
                <a-space>
                  <a-button type="primary" @click="openAdd" v-permission="'sys:position:add'">
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
                  <a-button
                    type="link"
                    size="small"
                    @click="openEdit(record)"
                    v-permission="'sys:position:edit'"
                  >
                    {{ $t('common.edit') }}
                  </a-button>
                  <a-popconfirm
                    :title="$t('common.confirmDeleteMessage')"
                    :ok-text="$t('common.confirm')"
                    :cancel-text="$t('common.cancel')"
                    @confirm="handleDelete(record.id)"
                  >
                    <a-button
                      type="link"
                      size="small"
                      danger
                      v-permission="'sys:position:delete'"
                    >
                      {{ $t('common.delete') }}
                    </a-button>
                  </a-popconfirm>
                </a-space>
              </template>
            </fx-dynamic-table>
          </div>
        </a-col>
      </a-row>
    </a-card>

    <!-- 新增/编辑弹窗 -->
    <BaseFormDialog
      v-model:open="visible"
      :title="isEdit ? '编辑岗位' : '新增岗位'"
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
        <a-form-item label="所属部门" name="departmentId">
           <a-tree-select
            v-model:value="formData.departmentId"
            style="width: 100%"
            :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
            :tree-data="treeData"
            placeholder="请选择所属部门"
            tree-default-expand-all
            :field-names="{
              children: 'children',
              label: 'deptName',
              value: 'id',
            }"
            allow-clear
          />
        </a-form-item>

        <a-form-item label="岗位名称" name="positionName">
          <a-input
            v-model:value="formData.positionName"
            placeholder="请输入岗位名称"
          />
        </a-form-item>

        <a-form-item label="岗位编码" name="positionCode">
          <a-input
            v-model:value="formData.positionCode"
            placeholder="请输入岗位编码"
            :disabled="isEdit"
          />
        </a-form-item>

        <a-form-item label="岗位级别" name="positionLevel">
          <a-select
            v-model:value="formData.positionLevel"
            placeholder="请选择岗位级别"
            style="width: 100%"
          >
            <a-select-option
              v-for="option in positionLevelOptions"
              :key="option.value"
              :value="Number(option.value)"
            >
              {{ option.label }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="排序号" name="orderNum">
          <a-input-number
            v-model:value="formData.orderNum"
            :min="0"
            placeholder="请输入排序号"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item :label="$t('common.status')" name="status">
          <a-radio-group v-model:value="formData.status">
            <a-radio :value="true">{{ $t('common.enabled') }}</a-radio>
            <a-radio :value="false">{{ $t('common.disabled') }}</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item label="备注" name="remark">
          <a-textarea
            v-model:value="formData.remark"
            placeholder="请输入备注"
            :rows="4"
          />
        </a-form-item>
      </a-form>
    </BaseFormDialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import {
  PlusOutlined
} from '@ant-design/icons-vue'
import DeptTree from '@/components/system/DeptTree.vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import { getDepartmentTree } from '@/api/system/department'
import {
  getPositionPage,
  createPosition,
  updatePosition,
  deletePosition
} from '@/api/system/position'
import type { FxTableConfig } from '@/api/system/tableConfig'
import { useDict } from '@/hooks/useDict'
import type { Position, PositionSaveParam } from './types'

// 租户 ID
const currentTenantId = ref<string | null>(null)
const deptTreeRef = ref()
const treeData = ref<any[]>([])

// 瀛楀吀鏁版嵁
const { dictItems: positionLevelOptions } = useDict('position_level')
const { dictItems: statusOptions } = useDict('status')

// 搜索表单
const search表单 = ref({
  positionName: '',
  positionCode: '',
  status: undefined,
  departmentId: undefined as string | undefined
})

// 表格相关
const tableRef = ref()
const loading = ref(false)

// 字典配置
const dictOptions = ref({
  status: statusOptions,
  positionLevel: positionLevelOptions
})

const fallbackConfig: Partial<FxTableConfig> = {
  columns: [
    { field: 'positionName', title: '岗位名称', width: 180, align: 'left' },
    { field: 'positionCode', title: '岗位编码', width: 140, align: 'left' },
    { field: 'positionLevel', title: '岗位级别', width: 120, align: 'center', dictCode: 'positionLevel' },
    { field: 'orderNum', title: '排序', width: 90, align: 'center' },
    { field: 'status', title: '状态', width: 100, align: 'center', dictCode: 'status' },
    { field: 'remark', title: '备注', width: 220, align: 'left' },
    { field: 'createTime', title: '创建时间', width: 180, align: 'center' },
    { field: 'action', title: '操作', width: 160, align: 'center', fixed: 'right' }
  ],
  queryFields: [
    { field: 'positionName', label: '岗位名称', queryType: 'input', queryOperator: 'like' },
    { field: 'positionCode', label: '岗位编码', queryType: 'input', queryOperator: 'like' },
    { field: 'status', label: '状态', queryType: 'select', queryOperator: 'eq', dictCode: 'status' }
  ]
}

/**
 * 处理表格数据请求
 */
const handleRequest = async (payload: { 
  page: { current: number; pageSize: number }; 
  query: Record<string, any>; 
  sorter?: { field?: string; order?: string } 
}) => {
  if (!currentTenantId.value) {
    return {
      records: [],
      total: 0
    }
  }
  
  loading.value = true
  try {
    const params: any = {
      pageNum: payload.page.current,
      pageSize: payload.page.pageSize,
      tenantId: currentTenantId.value,
      ...search表单.value,
      ...payload.query
    }
    
    // 确保total是数字类型
    const total = typeof data.total === 'number' ? data.total : parseInt(String(data.total) || '0', 10)
    return { records: data.records || [], total: total }
  } catch (e) {
    console.error('加载岗位列表失败', e)
    return {
      records: [],
      total: 0
    }
  } finally {
    loading.value = false
  }
}

// 弹窗
const visible = ref(false)
const isEdit = ref(false)
const formLoading = ref(false)
const formRef = ref()
const formData = ref<PositionSaveParam & { departmentId?: string }>({
  tenantId: '',
  positionName: '',
  positionCode: '',
  orderNum: 0,
  status: true
})

// 表单验证规则
const rules = {
  departmentId: [{ required: true, message: '请选择所属部门', trigger: 'change' }],
  positionName: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }],
  positionCode: [{ required: true, message: '请输入岗位编码', trigger: 'blur' }]
}

/**
 * 选择部门
 */
async function onSelectNode(keys: string[], node: any) {
  // node is the data object or dataRef depending on how it's passed
  // In DeptTree, we emit (keys, info.node.dataRef || info.node)
  // So 'node' here is the data object
  
  if (keys.length > 0) {
    search表单.value.departmentId = keys[0]
  } else {
    search表单.value.departmentId = undefined
  }
  // Reset pagination if needed, but here we just reload
  await handleSearch()
}

/**
 * 加载部门树数据（用于下拉选择）
 */
async function loadDeptTreeData() {
  if (!currentTenantId.value) return
  try {
    const data = await getDepartmentTree({ tenantId: currentTenantId.value })
    treeData.value = data || []
  } catch (e) {
    console.error(e)
  }
}

/**
 * 搜索
 */
async function handleSearch() {
  await tableRef.value?.refresh?.()
}

/**
 * 重置
 */
async function handleReset() {
  const currentDeptId = search表单.value.departmentId
  search表单.value = {
    positionName: '',
    positionCode: '',
    status: undefined,
    departmentId: currentDeptId // Keep selected department
  }
  await tableRef.value?.refresh?.()
}

/**
 * 新增
 */
function openAdd() {
  isEdit.value = false
  visible.value = true
  // 如果选中了部门，自动填入
  const defaultDeptId = search表单.value.departmentId
  
  formData.value = {
    tenantId: currentTenantId.value!,
    departmentId: defaultDeptId,
    positionName: '',
    positionCode: '',
    orderNum: 0,
    status: true
  }
}

/**
 * 编辑
 */
function openEdit(record: any) { // Type assertion needed if Position type is not updated yet
  isEdit.value = true
  visible.value = true
  formData.value = {
    id: record.id,
    tenantId: record.tenantId,
    departmentId: record.departmentId, // Ensure backend returns this
    positionName: record.positionName,
    positionCode: record.positionCode,
    positionLevel: record.positionLevel,
    orderNum: record.orderNum,
    status: record.status,
    remark: record.remark
  }
}

/**
 * 取消
 */
function handleCancel() {
  visible.value = false
  formRef.value?.resetFields()
}

/**
 * 提交
 */
async function handleSubmit() {
  try {
    await formRef.value?.validate()
    formLoading.value = true

    if (isEdit.value) {
      // 更新
      await updatePosition(formData.value)
      // 成功提示由后端返回，在 http 拦截器中统一处理
    } else {
      // 新增
      await createPosition(formData.value)
      // 成功提示由后端返回，在 http 拦截器中统一处理
    }

    visible.value = false
    await tableRef.value?.refresh?.()
  } catch (e: any) {
    if (e.errorFields) {
      // 表单验证失败
      return
    }
    console.error('保存失败', e)
  } finally {
    formLoading.value = false
  }
}

/**
 * 删除
 */
async function handleDelete(id: string) {
  try {
    await deletePosition({
      id,
      tenantId: currentTenantId.value!
    })
    // 成功提示由后端返回，在 http 拦截器中统一处理
    await tableRef.value?.refresh?.()
  } catch (e: any) {
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

<style scoped>
.page-wrap {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  padding: 16px;
  box-sizing: border-box;
}

.content-card {
  display: flex;
  flex-direction: column;
  flex: 1 1 auto;
  min-height: 0;
}

.content-card :deep(.ant-card-body) {
  display: flex;
  flex-direction: column;
  flex: 1 1 auto;
  min-height: 0;
  height: 100%;
}

.content-card :deep(.ant-row) {
  flex: 1 1 auto;
  min-height: 0;
}

.content-card :deep(.ant-col) {
  min-height: 0;
}

.table-area {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

.table-area :deep(.fx-dynamic-table) {
  flex: 1 1 auto;
  min-height: 0;
}

.card-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 16px;
  color: var(--fx-text-primary, #1d2129);
}

.search-area {
  width: 100%;
  margin-bottom: 16px;
}

.detail-container {
  padding-left: 16px;
  height: 100%;
  overflow-y: auto;
}

:deep(.fx-query-form) {
  display: flex;
  flex-direction: column;
  width: 100%;
}

:deep(.fx-query-fields-container) {
  display: flex;
  flex-direction: column;
  width: 100%;
  position: relative;
}

:deep(.fx-query-fields) {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  width: 100%;
}

:deep(.fx-query-fields :deep(.ant-form-item)) {
  margin: 0;
}

:deep(.fx-query-actions) {
  display: flex;
  justify-content: flex-end;
  width: 100%;
  margin-top: 12px;
}
</style>
