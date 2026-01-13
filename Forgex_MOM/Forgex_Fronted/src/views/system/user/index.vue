<template>
  <div class="user-management">
    <!-- 搜索栏 -->
    <a-card :bordered="false" style="margin-bottom: 16px;">
      <a-form layout="inline">
        <a-form-item label="用户名">
          <a-input
            v-model:value="queryForm.username"
            placeholder="请输入用户名"
            allow-clear
            style="width: 200px;"
          />
        </a-form-item>
        
        <a-form-item label="手机号">
          <a-input
            v-model:value="queryForm.phone"
            placeholder="请输入手机号"
            allow-clear
            style="width: 200px;"
          />
        </a-form-item>
        
        <a-form-item label="部门">
          <a-tree-select
            v-model:value="queryForm.departmentId"
            placeholder="请选择部门"
            allow-clear
            tree-default-expand-all
            :tree-data="departmentTreeData"
            :field-names="{ label: 'deptName', value: 'id', children: 'children' }"
            style="width: 200px;"
          />
        </a-form-item>
        
        <a-form-item label="职位">
          <a-select
            v-model:value="queryForm.positionId"
            placeholder="请选择职位"
            allow-clear
            style="width: 200px;"
          >
            <a-select-option
              v-for="pos in positionList"
              :key="pos.id"
              :value="pos.id"
            >
              {{ pos.positionName }}
            </a-select-option>
          </a-select>
        </a-form-item>
        
        <a-form-item label="状态">
          <a-select
            v-model:value="queryForm.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 120px;"
          >
            <a-select-option :value="true">启用</a-select-option>
            <a-select-option :value="false">禁用</a-select-option>
          </a-select>
        </a-form-item>
        
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              搜索
            </a-button>
            <a-button @click="handleReset">
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>
    
    <!-- 操作栏 -->
    <a-card :bordered="false">
      <div style="margin-bottom: 16px;">
        <a-space>
          <a-button
            v-permission="'sys:user:create'"
            type="primary"
            @click="openAddDialog"
          >
            新增用户
          </a-button>
          <a-button
            v-permission="'sys:user:delete'"
            danger
            :disabled="selectedRowKeys.length === 0"
            @click="handleBatchDelete"
          >
            批量删除
          </a-button>
          <a-button
            v-permission="'sys:user:export'"
            @click="handleExport"
          >
            导出
          </a-button>
        </a-space>
      </div>
      
      <!-- 表格 -->
      <a-table
        :columns="columns"
        :data-source="userList"
        :loading="loading"
        :pagination="{
          current: pagination.current,
          pageSize: pagination.pageSize,
          total: pagination.total,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total: number) => `共 ${total} 条`,
        }"
        :row-selection="{
          selectedRowKeys,
          onChange: handleSelectionChange,
        }"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'avatar'">
            <a-avatar :src="record.avatar ? (record.avatar.startsWith('http') || record.avatar.startsWith('data:') ? record.avatar : (record.avatar.startsWith('/api') ? record.avatar : '/api' + (record.avatar.startsWith('/') ? '' : '/') + record.avatar)) : ''">
              <template #icon><UserOutlined /></template>
            </a-avatar>
          </template>
          
          <template v-else-if="column.key === 'gender'">
            <a-tag v-if="record.gender === 1" color="blue">男</a-tag>
            <a-tag v-else-if="record.gender === 2" color="pink">女</a-tag>
            <a-tag v-else color="default">未知</a-tag>
          </template>
          
          <template v-else-if="column.key === 'status'">
            <a-tag v-if="record.status === true" color="success">启用</a-tag>
            <a-tag v-else color="error">禁用</a-tag>
          </template>
          
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a
                v-permission="'sys:user:edit'"
                @click="openEditDialog(record)"
              >
                编辑
              </a>
              <a
                v-permission="'sys:user:edit'"
                @click="handleUpdateStatus(record.id, !record.status)"
              >
                {{ record.status ? '禁用' : '启用' }}
              </a>
              <a
                v-permission="'sys:user:resetPwd'"
                @click="handleResetPassword(record.id)"
              >
                重置密码
              </a>
              <a
                v-permission="'sys:user:delete'"
                style="color: #ff4d4f;"
                @click="handleDelete(record.id)"
              >
                删除
              </a>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
    
    <!-- 新增/编辑弹窗 -->
    <UserFormDialog
      v-model:open="dialogVisible"
      :is-edit="isEdit"
      :user-id="currentUserId"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
/**
 * 用户管理页面
 * 
 * 功能：
 * 1. 用户列表查询（分页、搜索）
 * 2. 新增、编辑、删除用户
 * 3. 用户状态管理、密码重置
 * 
 * @author Forgex
 * @version 1.0.0
 */
import { ref, onMounted } from 'vue'
import { UserOutlined } from '@ant-design/icons-vue'
import UserFormDialog from './components/UserFormDialog.vue'
import { useUser } from './hooks/useUser'
import { getDepartmentTree } from '@/api/system/department'
import { listPositions } from '@/api/system/position'
import type { Department, Position } from './types'

// 用户列表逻辑
const {
  loading,
  userList,
  pagination,
  queryForm,
  selectedRowKeys,
  fetchUserList,
  handleSearch,
  handleReset,
  handlePageChange,
  handleDelete,
  handleBatchDelete,
  handleResetPassword,
  handleUpdateStatus,
  handleSelectionChange,
  handleExport,
} = useUser()

// 部门树数据
const departmentTreeData = ref<Department[]>([])

// 职位列表
const positionList = ref<Position[]>([])

// 弹窗状态
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentUserId = ref<string>()

/**
 * 打开新增弹窗
 */
function openAddDialog() {
  isEdit.value = false
  currentUserId.value = undefined
  dialogVisible.value = true
}

/**
 * 打开编辑弹窗
 */
function openEditDialog(record: any) {
  isEdit.value = true
  currentUserId.value = record.id
  dialogVisible.value = true
}

/**
 * 表单提交成功回调
 */
function handleFormSuccess() {
  fetchUserList()
}

// 表格列定义
const columns = [
  { title: '头像', dataIndex: 'avatar', key: 'avatar', width: 80, align: 'center' },
  { title: '用户名', dataIndex: 'username', key: 'username', width: 120 },
  { title: '邮箱', dataIndex: 'email', key: 'email', width: 180 },
  { title: '手机号', dataIndex: 'phone', key: 'phone', width: 130 },
  { title: '性别', key: 'gender', width: 80 },
  { title: '部门', dataIndex: 'departmentName', key: 'departmentName', width: 120 },
  { title: '职位', dataIndex: 'positionName', key: 'positionName', width: 120 },
  { title: '入职时间', dataIndex: 'entryDate', key: 'entryDate', width: 120 },
  { title: '状态', key: 'status', width: 80 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '创建人', dataIndex: 'createBy', key: 'createBy', width: 100 },
  { title: '修改时间', dataIndex: 'updateTime', key: 'updateTime', width: 180 },
  { title: '修改人', dataIndex: 'updateBy', key: 'updateBy', width: 100 },
  { title: '操作', key: 'action', width: 200, fixed: 'right' },
]

// 表格分页改变
function handleTableChange(pag: any) {
  handlePageChange(pag.current, pag.pageSize)
}

/**
 * 加载部门树数据
 */
async function loadDepartmentTree() {
  try {
    const result = await getDepartmentTree({ tenantId: '1' })
    departmentTreeData.value = result || []
  } catch (error) {
    console.error('加载部门树失败:', error)
  }
}

/**
 * 加载职位列表
 */
async function loadPositionList() {
  try {
    const result = await listPositions({})
    positionList.value = result || []
  } catch (error) {
    console.error('加载职位列表失败:', error)
  }
}

// 初始化
onMounted(() => {
  loadDepartmentTree()
  loadPositionList()
  fetchUserList()
})
</script>

<style scoped lang="less">
.user-management {
  padding: 16px;
}
</style>
