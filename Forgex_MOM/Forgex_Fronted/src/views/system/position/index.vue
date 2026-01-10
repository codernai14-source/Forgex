<template>
  <div class="page-wrap">
    <a-card class="content-card" :bordered="false">
      <a-row :gutter="16">
        <!-- 左侧：部门树 -->
        <a-col :span="6">
          <DeptTree
            ref="deptTreeRef"
            @select="onSelectNode"
          />
        </a-col>

        <!-- 右侧：职位列表 -->
        <a-col :span="18">
          <!-- 搜索区域 -->
          <div class="search-area">
            <a-form layout="inline" :model="searchForm">
              <a-form-item label="职位名称">
                <a-input
                  v-model:value="searchForm.positionName"
                  placeholder="请输入职位名称"
                  allow-clear
                  style="width: 150px"
                />
              </a-form-item>
              <a-form-item label="职位编码">
                <a-input
                  v-model:value="searchForm.positionCode"
                  placeholder="请输入职位编码"
                  allow-clear
                  style="width: 150px"
                />
              </a-form-item>
              <a-form-item label="状态">
                <a-select
                  v-model:value="searchForm.status"
                  placeholder="请选择状态"
                  allow-clear
                  style="width: 100px"
                >
                  <a-select-option :value="true">启用</a-select-option>
                  <a-select-option :value="false">禁用</a-select-option>
                </a-select>
              </a-form-item>
              <a-form-item>
                <a-space>
                  <a-button type="primary" @click="handleSearch">
                    <template #icon><SearchOutlined /></template>
                    搜索
                  </a-button>
                  <a-button @click="handleReset">
                    <template #icon><ReloadOutlined /></template>
                    重置
                  </a-button>
                </a-space>
              </a-form-item>
            </a-form>
          </div>

          <!-- 操作按钮和表格 -->
          <div class="table-area">
            <div class="toolbar">
              <a-space>
                <a-button type="primary" @click="openAdd" v-permission="'sys:position:add'">
                  <template #icon><PlusOutlined /></template>
                  新增职位
                </a-button>
              </a-space>
            </div>

            <a-table
              :columns="columns"
              :data-source="positions"
              :loading="loading"
              row-key="id"
              :pagination="false"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'status'">
                  <a-tag :color="record.status === true ? 'green' : 'red'">
                    {{ record.status === true ? '启用' : '禁用' }}
                  </a-tag>
                </template>
                <template v-else-if="column.key === 'action'">
                  <a-space>
                    <a-button
                      type="link"
                      size="small"
                      @click="openEdit(record)"
                      v-permission="'sys:position:edit'"
                    >
                      编辑
                    </a-button>
                    <a-popconfirm
                      title="确定要删除这个职位吗？"
                      ok-text="确定"
                      cancel-text="取消"
                      @confirm="handleDelete(record.id)"
                    >
                      <a-button
                        type="link"
                        size="small"
                        danger
                        v-permission="'sys:position:delete'"
                      >
                        删除
                      </a-button>
                    </a-popconfirm>
                  </a-space>
                </template>
              </template>
            </a-table>
          </div>
        </a-col>
      </a-row>
    </a-card>

    <!-- 新增/编辑弹窗 -->
    <FormContainer
      v-model:open="visible"
      :title="isEdit ? '编辑职位' : '新增职位'"
      :confirm-loading="formLoading"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
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

        <a-form-item label="职位名称" name="positionName">
          <a-input
            v-model:value="formData.positionName"
            placeholder="请输入职位名称"
          />
        </a-form-item>

        <a-form-item label="职位编码" name="positionCode">
          <a-input
            v-model:value="formData.positionCode"
            placeholder="请输入职位编码"
            :disabled="isEdit"
          />
        </a-form-item>

        <a-form-item label="职位级别" name="positionLevel">
          <a-input-number
            v-model:value="formData.positionLevel"
            :min="1"
            placeholder="请输入职位级别"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="排序号" name="orderNum">
          <a-input-number
            v-model:value="formData.orderNum"
            :min="0"
            placeholder="请输入排序号"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="formData.status">
            <a-radio :value="true">启用</a-radio>
            <a-radio :value="false">禁用</a-radio>
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
    </FormContainer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined
} from '@ant-design/icons-vue'
import DeptTree from '@/components/system/DeptTree.vue'
import FormContainer from '@/components/common/FormContainer.vue'
import { getDepartmentTree } from '@/api/sys/department'
import {
  listPositions,
  createPosition,
  updatePosition,
  deletePosition
} from '@/api/sys/position'
import type { Position, PositionSaveParam } from './types'

// 租户ID
const currentTenantId = ref<string | null>(null)
const deptTreeRef = ref()
const treeData = ref<any[]>([])

// 表格列定义
const columns = [
  { title: '职位名称', dataIndex: 'positionName', key: 'positionName', width: 150 },
  { title: '职位编码', dataIndex: 'positionCode', key: 'positionCode', width: 150 },
  { title: '职位级别', dataIndex: 'positionLevel', key: 'positionLevel', width: 100 },
  { title: '排序号', dataIndex: 'orderNum', key: 'orderNum', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '备注', dataIndex: 'remark', key: 'remark', ellipsis: true },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', fixed: 'right', width: 150 }
]

// 搜索表单
const searchForm = ref({
  positionName: '',
  positionCode: '',
  status: undefined,
  departmentId: undefined as string | undefined
})

// 表格数据
const positions = ref<Position[]>([])
const loading = ref(false)

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
  status: true,
  departmentId: undefined
})

// 表单验证规则
const rules = {
  departmentId: [{ required: true, message: '请选择所属部门', trigger: 'change' }],
  positionName: [{ required: true, message: '请输入职位名称', trigger: 'blur' }],
  positionCode: [{ required: true, message: '请输入职位编码', trigger: 'blur' }]
}

/**
 * 选择部门
 */
function onSelectNode(keys: string[], node: any) {
  // node is the data object or dataRef depending on how it's passed
  // In DeptTree, we emit (keys, info.node.dataRef || info.node)
  // So 'node' here is the data object
  
  if (keys.length > 0) {
    searchForm.value.departmentId = keys[0]
  } else {
    searchForm.value.departmentId = undefined
  }
  // Reset pagination if needed, but here we just reload
  handleSearch()
}

/**
 * 加载部门树数据（用于下拉选）
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
 * 加载职位列表
 */
async function loadPositions() {
  if (!currentTenantId.value) {
    return
  }
  try {
    loading.value = true
    const data = await listPositions({
      tenantId: currentTenantId.value,
      ...searchForm.value
    })
    positions.value = data || []
  } catch (e) {
    message.error('加载职位列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索
 */
function handleSearch() {
  loadPositions()
}

/**
 * 重置
 */
function handleReset() {
  const currentDeptId = searchForm.value.departmentId
  searchForm.value = {
    positionName: '',
    positionCode: '',
    status: undefined,
    departmentId: currentDeptId // Keep selected department
  }
  loadPositions()
}

/**
 * 新增
 */
function openAdd() {
  isEdit.value = false
  visible.value = true
  // 如果选中了部门，自动填入
  const defaultDeptId = searchForm.value.departmentId
  
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
      message.success('更新成功')
    } else {
      // 新增
      await createPosition(formData.value)
      message.success('新增成功')
    }

    visible.value = false
    await loadPositions()
  } catch (e: any) {
    if (e.errorFields) {
      // 表单验证失败
      return
    }
    message.error(e.message || '保存失败')
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
    message.success('删除成功')
    await loadPositions()
  } catch (e: any) {
    message.error(e.message || '删除失败')
  }
}

onMounted(async () => {
  const tid = sessionStorage.getItem('tenantId')
  if (tid) {
    currentTenantId.value = tid
    await loadPositions()
    await loadDeptTreeData()
  }
})
</script>

<style scoped>
.page-wrap {
  padding: 16px;
}

.content-card {
  min-height: calc(100vh - 120px);
}

.search-area {
  margin-bottom: 16px;
}

.toolbar {
  margin-bottom: 16px;
}
</style>
