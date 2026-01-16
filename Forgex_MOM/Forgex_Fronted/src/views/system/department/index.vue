<template>
  <div class="page-wrap">
    <a-card class="content-card" :bordered="false">
      <a-row :gutter="16">
        <!-- 左侧：部门树 -->
        <a-col :span="6">
          <DeptTree
            ref="deptTreeRef"
            :show-add="true"
            @select="onSelectNode"
            @add="openAdd(null)"
          />
        </a-col>

        <!-- 右侧：部门详情/编辑 -->
        <a-col :span="18">
          <div class="detail-container">
            <div v-if="!selectedDept" class="empty-state">
              <a-empty description="请选择左侧部门查看详情" />
            </div>

            <div v-else>
              <!-- 详情模式 -->
              <div v-if="!isEditing" class="detail-view">
                <div class="detail-header">
                  <h3>{{ selectedDept.deptName }}</h3>
                  <a-space>
                    <a-button
                      type="primary"
                      @click="openAdd(selectedDept.id)"
                      v-permission="'sys:department:create'"
                    >
                      <template #icon><PlusOutlined /></template>
                      新增子部门
                    </a-button>
                    <a-button
                      @click="startEdit"
                      v-permission="'sys:department:edit'"
                    >
                      <template #icon><EditOutlined /></template>
                      编辑
                    </a-button>
                    <a-popconfirm
                      title="确定要删除这个部门吗？"
                      :ok-text="$t('common.confirm')"
                      :cancel-text="$t('common.cancel')"
                      @confirm="handleDelete"
                    >
                      <a-button
                        danger
                        v-permission="'sys:department:delete'"
                      >
                        <template #icon><DeleteOutlined /></template>
                        删除
                      </a-button>
                    </a-popconfirm>
                  </a-space>
                </div>

                <a-descriptions :column="2" bordered>
                  <a-descriptions-item label="部门编码">
                    {{ selectedDept.deptCode }}
                  </a-descriptions-item>
                  <a-descriptions-item label="组织类型">
                    {{ getOrgTypeLabel(selectedDept.orgType) }}
                  </a-descriptions-item>
                  <a-descriptions-item label="组织层级">
                    {{ selectedDept.orgLevel }}
                  </a-descriptions-item>
                  <a-descriptions-item label="状态">
                    <a-tag :color="selectedDept.status === true ? 'green' : 'red'">
                      {{ selectedDept.status === true ? '启用' : '禁用' }}
                    </a-tag>
                  </a-descriptions-item>
                  <a-descriptions-item label="负责人">
                    {{ selectedDept.leader || '-' }}
                  </a-descriptions-item>
                  <a-descriptions-item label="联系电话">
                    {{ selectedDept.phone || '-' }}
                  </a-descriptions-item>
                  <a-descriptions-item label="邮箱" :span="2">
                    {{ selectedDept.email || '-' }}
                  </a-descriptions-item>
                  <a-descriptions-item label="排序号">
                    {{ selectedDept.orderNum }}
                  </a-descriptions-item>
                  <a-descriptions-item label="创建时间">
                    {{ selectedDept.createTime }}
                  </a-descriptions-item>
                  <a-descriptions-item label="创建人">
                    {{ selectedDept.createBy || '-' }}
                  </a-descriptions-item>
                  <a-descriptions-item label="更新时间">
                    {{ selectedDept.updateTime }}
                  </a-descriptions-item>
                  <a-descriptions-item label="更新人">
                    {{ selectedDept.updateBy || '-' }}
                  </a-descriptions-item>
                </a-descriptions>
              </div>

              <!-- 编辑模式 -->
              <div v-else class="edit-view">
                <div class="edit-header">
                  <h3>{{ formData.id ? '编辑部门' : '新增部门' }}</h3>
                </div>

                <a-form
                  ref="formRef"
                  :model="formData"
                  :rules="rules"
                  :label-col="{ span: 6 }"
                  :wrapper-col="{ span: 16 }"
                >
                  <a-form-item label="部门名称" name="deptName">
                    <a-input
                      v-model:value="formData.deptName"
                      placeholder="请输入部门名称"
                    />
                  </a-form-item>

                  <a-form-item label="部门编码" name="deptCode">
                    <a-input
                      v-model:value="formData.deptCode"
                      placeholder="请输入部门编码"
                    />
                  </a-form-item>

                  <a-form-item label="组织类型" name="orgType">
                    <a-select
                      v-model:value="formData.orgType"
                      placeholder="请选择组织类型"
                    >
                      <a-select-option v-for="option in orgTypeOptions" :key="option.value" :value="option.value">
                        {{ option.label }}
                      </a-select-option>
                    </a-select>
                  </a-form-item>

                  <a-form-item label="组织层级" name="orgLevel">
                    <a-input-number
                      v-model:value="formData.orgLevel"
                      :min="1"
                      :max="5"
                      placeholder="请输入组织层级"
                      style="width: 100%"
                    />
                  </a-form-item>

                  <a-form-item label="负责人" name="leader">
                    <a-input
                      v-model:value="formData.leader"
                      placeholder="请输入负责人"
                    />
                  </a-form-item>

                  <a-form-item label="联系电话" name="phone">
                    <a-input
                      v-model:value="formData.phone"
                      placeholder="请输入联系电话"
                    />
                  </a-form-item>

                  <a-form-item label="邮箱" name="email">
                    <a-input
                      v-model:value="formData.email"
                      placeholder="请输入邮箱"
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

                  <a-form-item :wrapper-col="{ offset: 6, span: 16 }">
                    <a-space>
                      <a-button type="primary" :loading="saving" @click="handleSave">
                        保存
                      </a-button>
                      <a-button @click="cancelEdit">
                        取消
                      </a-button>
                    </a-space>
                  </a-form-item>
                </a-form>
              </div>
            </div>
          </div>
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script setup lang="ts">
/**
 * 部门管理页面
 * 
 * 功能：
 * 1. 部门树形结构展示
 * 2. 部门详情查看
 * 3. 部门新增、编辑、删除
 * 
 * @author Forgex
 * @version 1.0.0
 */
import DeptTree from '@/components/system/DeptTree.vue'
import { onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'
import {
  getDepartmentTree,
  getDepartment,
  createDepartment,
  updateDepartment,
  deleteDepartment
} from '@/api/system/department'
import { useDict } from '@/hooks/useDict'
import type { Department, DepartmentSaveParam } from './types'

// 租户ID
const currentTenantId = ref<string | null>(null)
const deptTreeRef = ref()

// 选中的节点
const selectedKeys = ref<string[]>([])
const selectedDept = ref<Department | null>(null)

// 编辑状态
const isEditing = ref(false)
const saving = ref(false)
const formRef = ref()
const formData = ref<DepartmentSaveParam>({
  tenantId: '',
  orgType: '',
  orgLevel: 1,
  deptName: '',
  deptCode: '',
  orderNum: 0,
  status: true
})

// 表单验证规则
const rules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  deptCode: [{ required: true, message: '请输入部门编码', trigger: 'blur' }],
  orgType: [{ required: true, message: '请选择组织类型', trigger: 'change' }],
  orgLevel: [{ required: true, message: '请输入组织层级', trigger: 'blur' }]
}

// 字典数据
const { dictItems: orgTypeOptions } = useDict('org_type')
const { dictItems: orgLevelOptions } = useDict('org_level')

/**
 * 加载部门树
 */
async function loadTree() {
  if (!currentTenantId.value) {
    return
  }
  try {
    treeLoading.value = true
    const data = await getDepartmentTree({ tenantId: currentTenantId.value })
    treeData.value = data || []
  } catch (e) {
    message.error('加载部门树失败')
  } finally {
    treeLoading.value = false
  }
}

/**
 * 选择节点
 */
async function onSelectNode(keys: string[]) {
  if (keys.length === 0) {
    selectedKeys.value = []
    selectedDept.value = null
    isEditing.value = false
    return
  }

  const id = keys[0]
  selectedKeys.value = [id]

  try {
    const data = await getDepartment({
      id,
      tenantId: currentTenantId.value!
    })
    selectedDept.value = data
    isEditing.value = false
  } catch (e) {
    message.error('加载部门详情失败')
  }
}

/**
 * 新增部门
 */
function openAdd(parentId: string | null) {
  isEditing.value = true
  formData.value = {
    tenantId: currentTenantId.value!,
    parentId: parentId || undefined,
    orgType: '',
    orgLevel: 1,
    deptName: '',
    deptCode: '',
    orderNum: 0,
    status: true
  }
}

/**
 * 开始编辑
 */
function startEdit() {
  if (!selectedDept.value) {
    return
  }
  isEditing.value = true
  formData.value = {
    id: selectedDept.value.id,
    tenantId: selectedDept.value.tenantId,
    parentId: selectedDept.value.parentId,
    orgType: selectedDept.value.orgType,
    orgLevel: selectedDept.value.orgLevel,
    deptName: selectedDept.value.deptName,
    deptCode: selectedDept.value.deptCode,
    leader: selectedDept.value.leader,
    phone: selectedDept.value.phone,
    email: selectedDept.value.email,
    orderNum: selectedDept.value.orderNum,
    status: selectedDept.value.status
  }
}

/**
 * 取消编辑
 */
function cancelEdit() {
  isEditing.value = false
  formRef.value?.resetFields()
}

/**
 * 保存
 */
async function handleSave() {
  try {
    await formRef.value?.validate()
    saving.value = true

    if (formData.value.id) {
      // 更新
      await updateDepartment(formData.value)
      message.success('更新成功')
    } else {
      // 新增
      await createDepartment(formData.value)
      message.success('新增成功')
    }

    isEditing.value = false
    await loadTree()

    // 重新选中当前节点
    if (formData.value.id) {
      await onSelectNode([formData.value.id])
    }
  } catch (e: any) {
    if (e.errorFields) {
      // 表单验证失败
      return
    }
    message.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

/**
 * 删除
 */
async function handleDelete() {
  if (!selectedDept.value) {
    return
  }
  try {
    await deleteDepartment({
      id: selectedDept.value.id,
      tenantId: currentTenantId.value!
    })
    message.success('删除成功')
    selectedKeys.value = []
    selectedDept.value = null
    await refreshTree()
  } catch (e: any) {
    message.error(e.message || '删除失败')
  }
}

/**
 * 获取组织类型标签
 */
function getOrgTypeLabel(orgType: string): string {
  const option = orgTypeOptions.value.find(opt => opt.value === orgType)
  return option ? option.label : orgType
}

onMounted(async () => {
  const tid = sessionStorage.getItem('tenantId')
  if (tid) {
    currentTenantId.value = tid
    // await loadTree() // Auto loaded by component
  }
})
</script>

<style scoped>
.page-wrap {
  padding: 16px;
  height: calc(100vh - 84px); /* Adjust based on header height */
  overflow: hidden;
}

.content-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

:deep(.ant-card-body) {
  flex: 1;
  overflow: hidden;
  height: 100%;
}

.detail-container {
  padding-left: 16px;
  height: 100%;
  overflow-y: auto;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.detail-header,
.edit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-header h3,
.edit-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
}
</style>
