<template>
  <div class="menu-container">
    <!-- 搜索区域 -->
    <a-card :bordered="false" class="search-card">
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
    </a-card>
    
    <!-- 主内容区 -->
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
              :key="String(module.id)"
              :tab="module.name"
            />
          </a-tabs>
        </div>
        
        <!-- 右侧内容区 -->
        <div class="content-area">
          <!-- 操作按钮 -->
          <div class="table-toolbar">
            <a-space>
              <a-button
                v-permission="'sys:menu:create'"
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
          <div class="table-wrapper">
            <a-table
              :columns="columns"
              :data-source="menuList"
              :loading="loading"
              :row-selection="{
                selectedRowKeys,
                onChange: handleSelectionChange
              }"
              :pagination="false"
              :scroll="{ y: 'calc(100vh - 380px)' }"
              row-key="id"
              :default-expand-all-rows="true"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'type'">
                  <a-tag v-if="record.type === 'catalog'" color="blue">目录</a-tag>
                  <a-tag v-else-if="record.type === 'menu'" color="green">菜单</a-tag>
                  <a-tag v-else-if="record.type === 'button'" color="orange">按钮</a-tag>
                </template>
                
                <template v-else-if="column.key === 'menuMode'">
                  <a-tag v-if="record.menuMode === 'embedded'" color="blue">内嵌</a-tag>
                  <a-tag v-else-if="record.menuMode === 'external'" color="purple">外联</a-tag>
                  <span v-else>-</span>
                </template>
                
                <template v-else-if="column.key === 'icon'">
                  <component v-if="record.icon" :is="getIcon(record.icon)" />
                  <span v-else>-</span>
                </template>
                
                <template v-else-if="column.key === 'visible'">
                  <a-tag v-if="record.visible === true || record.visible === 1" color="success">显示</a-tag>
                  <a-tag v-else-if="record.visible === false || record.visible === 0" color="default">隐藏</a-tag>
                  <span v-else>-</span>
                </template>
                
                <template v-else-if="column.key === 'status'">
                  <a-tag v-if="record.status === true || record.status === 1" color="success">启用</a-tag>
                  <a-tag v-else-if="record.status === false || record.status === 0" color="error">禁用</a-tag>
                  <span v-else>-</span>
                </template>
                
                <template v-else-if="column.key === 'action'">
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
              </template>
            </a-table>
          </div>
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
                <a-select-option
                  v-for="module in modules"
                  :key="String(module.id)"
                  :value="String(module.id)"
                >
                  {{ module.name }}
                </a-select-option>
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
                  v-for="item in menuTypeOptions"
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
                  v-for="item in menuModeOptions"
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
                <a-radio :value="true">显示</a-radio>
                <a-radio :value="false">隐藏</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
          
          <a-col :span="12">
            <a-form-item label="状态" name="status">
              <a-radio-group v-model:value="formData.status">
                <a-radio :value="true">启用</a-radio>
                <a-radio :value="false">禁用</a-radio>
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
import { listModules } from '@/api/system/module'
import { getIcon } from '@/utils/icon'
import { useDict } from '@/hooks/useDict'

// 模块列表
const modules = ref<any[]>([])
const activeModuleId = ref<string>('')

// 字典数据
const { dictItems: menuTypeOptions } = useDict('menu_type')
const { dictItems: menuModeOptions } = useDict('menu_mode')

// 表格列定义
const columns = [
  { title: '菜单名称', dataIndex: 'name', key: 'name', width: 200 },
  { title: '菜单类型', dataIndex: 'type', key: 'type', width: 100 },
  { title: '路径', dataIndex: 'path', key: 'path', ellipsis: true },
  { title: '图标', dataIndex: 'icon', key: 'icon', width: 80 },
  { title: '菜单模式', dataIndex: 'menuMode', key: 'menuMode', width: 100 },
  { title: '权限标识', dataIndex: 'permKey', key: 'permKey', ellipsis: true },
  { title: '排序', dataIndex: 'orderNum', key: 'orderNum', width: 80 },
  { title: '可见', dataIndex: 'visible', key: 'visible', width: 80 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '创建人', dataIndex: 'createBy', key: 'createBy' },
  { title: '修改时间', dataIndex: 'updateTime', key: 'updateTime', width: 180 },
  { title: '修改人', dataIndex: 'updateBy', key: 'updateBy' },
  { title: '操作', key: 'action', width: 200 }
]

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
  menuTreeData,
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
  openEdit({
    ...record,
    moduleId: String(record.moduleId ?? activeModuleId.value)
  })
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
  
  .main-card {
    :deep(.ant-card-body) {
      padding: 0;
    }
  }
  
  .menu-layout {
    display: flex;
    width: 100%;
    
    .module-tabs {
      width: 140px;
      border-right: 1px solid var(--fx-border-color);
      background: var(--fx-bg-container);
      
      :deep(.ant-tabs) {
        height: 100%;
        
        .ant-tabs-nav {
          margin: 0;
          width: 100%;
        }
        
        .ant-tabs-nav-list {
          width: 100%;
        }
        
        .ant-tabs-tab {
          padding: 12px 24px;
          margin: 0;
          width: 100%;
          justify-content: flex-start;
          transition: all 0.3s;
          
          &:hover {
            background: var(--fx-tab-hover-bg);
          }
          
          &.ant-tabs-tab-active {
            background: var(--fx-tab-bg);
            
            .ant-tabs-tab-btn {
              color: var(--fx-theme-color, #1677ff);
            }
          }
        }
      }
    }
    
    .content-area {
      flex: 1;
      padding: 12px 16px 8px;
      background: var(--fx-bg-container);
      
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
