<template>
  <div class="menu-container">
    <a-card :bordered="false" class="main-card">
      <div class="menu-layout">
        <!-- 左侧模块标签 -->
        <div class="module-tabs">
          <a-tabs
            v-model:activeKey="activeModuleId"
            tab-position="left"
            @change="handleModuleChange"
          >
            <a-tab-pane
              v-for="module in modules"
              :key="module.id"
              :tab="module.name"
            />
          </a-tabs>
        </div>
        
        <!-- 右侧内容区 -->
        <div class="content-area">
          <!-- 搜索区域 -->
          <div class="search-bar">
            <a-form layout="inline">
              <a-form-item label="菜单名称">
                <a-input
                  v-model:value="queryParams.name"
                  placeholder="请输入菜单名称"
                  style="width: 200px"
                  allow-clear
                />
              </a-form-item>
              
              <a-form-item label="状态">
                <a-select
                  v-model:value="queryParams.status"
                  placeholder="请选择状态"
                  style="width: 120px"
                  allow-clear
                >
                  <a-select-option :value="1">启用</a-select-option>
                  <a-select-option :value="0">禁用</a-select-option>
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
          
          <!-- 操作按钮 -->
          <div class="table-toolbar">
            <a-space>
              <a-button
                v-permission="'sys:menu:add'"
                type="primary"
                @click="handleAdd"
              >
                <template #icon><PlusOutlined /></template>
                新增菜单
              </a-button>
              
              <a-button
                v-permission="'sys:menu:delete'"
                danger
                :disabled="selectedRowKeys.length === 0"
                @click="handleBatchDelete"
              >
                <template #icon><DeleteOutlined /></template>
                批量删除
              </a-button>
            </a-space>
          </div>
          
          <!-- 树形表格 -->
          <a-table
            :columns="columns"
            :data-source="menuList"
            :loading="loading"
            :row-selection="{
              selectedRowKeys,
              onChange: handleSelectionChange
            }"
            :pagination="false"
            row-key="id"
            :default-expand-all-rows="true"
          >
            <!-- 菜单类型 -->
            <template #type="{ record }">
              <a-tag v-if="record.type === 'catalog'" color="blue">目录</a-tag>
              <a-tag v-else-if="record.type === 'menu'" color="green">菜单</a-tag>
              <a-tag v-else-if="record.type === 'button'" color="orange">按钮</a-tag>
            </template>
            
            <!-- 菜单模式 -->
            <template #menuMode="{ record }">
              <a-tag v-if="record.menuMode === 'embedded'" color="blue">内嵌</a-tag>
              <a-tag v-else-if="record.menuMode === 'external'" color="purple">外联</a-tag>
            </template>
            
            <!-- 图标 -->
            <template #icon="{ record }">
              <component v-if="record.icon" :is="record.icon" />
            </template>
            
            <!-- 可见性 -->
            <template #visible="{ record }">
              <a-tag v-if="record.visible === 1" color="success">显示</a-tag>
              <a-tag v-else color="default">隐藏</a-tag>
            </template>
            
            <!-- 状态 -->
            <template #status="{ record }">
              <a-tag v-if="record.status === 1" color="success">启用</a-tag>
              <a-tag v-else color="error">禁用</a-tag>
            </template>
            
            <!-- 操作 -->
            <template #action="{ record }">
              <a-space>
                <a
                  v-permission="'sys:menu:edit'"
                  @click="handleEdit(record)"
                >
                  编辑
                </a>
                <a-divider type="vertical" />
                <a
                  v-permission="'sys:menu:delete'"
                  class="danger-link"
                  @click="handleDelete(record.id)"
                >
                  删除
                </a>
              </a-space>
            </template>
          </a-table>
        </div>
      </div>
    </a-card>
    
    <!-- 新增/编辑弹窗 -->
    <BaseFormDialog
      v-model:open="visible"
      :title="formTitle"
      :loading="submitLoading"
      width="800px"
      @submit="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="所属模块" name="moduleId">
              <a-select
                v-model:value="formData.moduleId"
                placeholder="请选择所属模块"
              >
                <a-select-option value="1">系统管理</a-select-option>
                <a-select-option value="2">生产管理</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          
          <a-col :span="12">
            <a-form-item label="父菜单" name="parentId">
              <a-tree-select
                v-model:value="formData.parentId"
                :tree-data="menuTreeData"
                placeholder="请选择父菜单"
                tree-default-expand-all
                allow-clear
              />
            </a-form-item>
          </a-col>
          
          <a-col :span="12">
            <a-form-item label="菜单类型" name="type">
              <a-select
                v-model:value="formData.type"
                placeholder="请选择菜单类型"
                @change="handleTypeChange"
              >
                <a-select-option
                  v-for="item in MENU_TYPE_OPTIONS"
                  :key="item.value"
                  :value="item.value"
                >
                  {{ item.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          
          <a-col :span="12">
            <a-form-item label="菜单层级" name="menuLevel">
              <a-select
                v-model:value="formData.menuLevel"
                placeholder="请选择菜单层级"
              >
                <a-select-option
                  v-for="item in MENU_LEVEL_OPTIONS"
                  :key="item.value"
                  :value="item.value"
                >
                  {{ item.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          
          <a-col :span="12">
            <a-form-item label="菜单名称" name="name">
              <a-input
                v-model:value="formData.name"
                placeholder="请输入菜单名称"
              />
            </a-form-item>
          </a-col>
          
          <a-col v-if="showPath" :span="12">
            <a-form-item label="菜单路径" name="path">
              <a-input
                v-model:value="formData.path"
                placeholder="请输入菜单路径"
              />
            </a-form-item>
          </a-col>
          
          <a-col :span="12">
            <a-form-item label="菜单图标" name="icon">
              <a-input
                v-model:value="formData.icon"
                placeholder="请输入图标名称"
              />
            </a-form-item>
          </a-col>
          
          <a-col v-if="formData.type !== 'button'" :span="12">
            <a-form-item label="菜单模式" name="menuMode">
              <a-select
                v-model:value="formData.menuMode"
                placeholder="请选择菜单模式"
                @change="handleModeChange"
              >
                <a-select-option
                  v-for="item in MENU_MODE_OPTIONS"
                  :key="item.value"
                  :value="item.value"
                >
                  {{ item.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          
          <a-col v-if="showComponentKey" :span="12">
            <a-form-item label="组件Key" name="componentKey">
              <a-input
                v-model:value="formData.componentKey"
                placeholder="请输入组件Key"
              />
            </a-form-item>
          </a-col>
          
          <a-col v-if="showExternalUrl" :span="12">
            <a-form-item label="外联URL" name="externalUrl">
              <a-input
                v-model:value="formData.externalUrl"
                placeholder="请输入外联URL"
              />
            </a-form-item>
          </a-col>
          
          <a-col v-if="showPermKey" :span="12">
            <a-form-item label="权限标识" name="permKey">
              <a-input
                v-model:value="formData.permKey"
                placeholder="请输入权限标识"
              />
            </a-form-item>
          </a-col>
          
          <a-col :span="12">
            <a-form-item label="排序号" name="orderNum">
              <a-input-number
                v-model:value="formData.orderNum"
                :min="0"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          
          <a-col :span="12">
            <a-form-item label="是否可见" name="visible">
              <a-radio-group v-model:value="formData.visible">
                <a-radio :value="1">显示</a-radio>
                <a-radio :value="0">隐藏</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
          
          <a-col :span="12">
            <a-form-item label="状态" name="status">
              <a-radio-group v-model:value="formData.status">
                <a-radio :value="1">启用</a-radio>
                <a-radio :value="0">禁用</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </BaseFormDialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import { useMenu } from './hooks/useMenu'
import { useMenuForm } from './hooks/useMenuForm'
import { MENU_TYPE_OPTIONS, MENU_MODE_OPTIONS, MENU_LEVEL_OPTIONS } from './types'
import { listModules } from '@/api/sys/module'

// 模块列表
const modules = ref<any[]>([])
const activeModuleId = ref<string>('')

// 表格列定义
const columns = [
  { title: '菜单名称', dataIndex: 'name', width: 200 },
  { title: '菜单类型', dataIndex: 'type', width: 100, slots: { customRender: 'type' } },
  { title: '路径', dataIndex: 'path', width: 150, ellipsis: true },
  { title: '图标', dataIndex: 'icon', width: 80, slots: { customRender: 'icon' } },
  { title: '菜单模式', dataIndex: 'menuMode', width: 100, slots: { customRender: 'menuMode' } },
  { title: '权限标识', dataIndex: 'permKey', width: 150, ellipsis: true },
  { title: '排序', dataIndex: 'orderNum', width: 80 },
  { title: '可见', dataIndex: 'visible', width: 80, slots: { customRender: 'visible' } },
  { title: '状态', dataIndex: 'status', width: 80, slots: { customRender: 'status' } },
  { title: '创建时间', dataIndex: 'createTime', width: 180 },
  { title: '创建人', dataIndex: 'createBy', width: 120 },
  { title: '修改时间', dataIndex: 'updateTime', width: 180 },
  { title: '修改人', dataIndex: 'updateBy', width: 120 },
  { title: '操作', key: 'action', width: 150, fixed: 'right', slots: { customRender: 'action' } }
]

// 菜单树数据（用于父菜单选择）
const menuTreeData = ref([
  {
    title: '根目录',
    value: '0',
    children: []
  }
])

// 使用列表Hook
const {
  loading,
  menuList,
  queryParams,
  selectedRowKeys,
  loadMenuList,
  handleSearch,
  handleReset,
  handleDelete,
  handleBatchDelete,
  handleSelectionChange
} = useMenu()

// 使用表单Hook
const emit = defineEmits(['success'])
const {
  formRef,
  visible,
  submitLoading,
  formData,
  formTitle,
  showPath,
  showComponentKey,
  showPermKey,
  showExternalUrl,
  rules,
  openAdd,
  openEdit,
  handleCancel,
  handleSubmit,
  handleTypeChange,
  handleModeChange
} = useMenuForm(emit)

// 新增
const handleAdd = () => {
  openAdd()
}

// 编辑
const handleEdit = (record: any) => {
  openEdit(record)
}

// 加载模块列表
const loadModules = async () => {
  try {
    const tenantId = sessionStorage.getItem('tenantId')
    if (!tenantId) return
    
    const res = await listModules({ tenantId })
    modules.value = res || []
    
    // 默认选中第一个模块
    if (modules.value.length > 0) {
      activeModuleId.value = String(modules.value[0].id)
      queryParams.moduleId = activeModuleId.value
      await loadMenuList()
    }
  } catch (error) {
    console.error('加载模块列表失败:', error)
  }
}

// 切换模块
const handleModuleChange = async (moduleId: string) => {
  activeModuleId.value = moduleId
  queryParams.moduleId = moduleId
  await loadMenuList()
}

// 组件挂载时加载数据
onMounted(async () => {
  await loadModules()
})
</script>

<style scoped lang="less">
.menu-container {
  padding: 16px;
  height: calc(100vh - 120px);
  
  .main-card {
    height: 100%;
    
    :deep(.ant-card-body) {
      height: 100%;
      padding: 0;
    }
  }
  
  .menu-layout {
    display: flex;
    height: 100%;
    
    .module-tabs {
      width: 180px;
      border-right: 1px solid #f0f0f0;
      background: #fafafa;
      
      :deep(.ant-tabs) {
        height: 100%;
        
        .ant-tabs-nav {
          margin: 0;
        }
        
        .ant-tabs-tab {
          padding: 12px 24px;
          margin: 0;
          
          &:hover {
            background: #e6f7ff;
          }
          
          &.ant-tabs-tab-active {
            background: #1890ff;
            color: #fff;
            
            .ant-tabs-tab-btn {
              color: #fff;
            }
          }
        }
      }
    }
    
    .content-area {
      flex: 1;
      padding: 16px;
      overflow: auto;
      
      .search-bar {
        margin-bottom: 16px;
        padding: 16px;
        background: #fafafa;
        border-radius: 4px;
      }
      
      .table-toolbar {
        margin-bottom: 16px;
      }
    }
  }
  
  .danger-link {
    color: #ff4d4f;
    
    &:hover {
      color: #ff7875;
    }
  }
}
</style>
