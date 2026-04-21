<template>
  <div class="json-tree-viewer">
    <div v-if="!treeData || treeData.length === 0" class="empty-content">
      <a-empty description="暂无数据" />
    </div>
    
    <a-tree
      v-else
      :tree-data="treeData"
      :show-line="showLine"
      :show-icon="showIcon"
      :default-expand-all="defaultExpandAll"
      :expanded-keys="expandedKeys"
      @expand="onExpand"
    >
      <template #title="{ key, title, type, value, description, required, dataType }">
        <div class="tree-node-content">
          <span class="node-title">{{ title }}</span>
          
          <!-- 节点类型标签 -->
          <a-tag :color="getTypeColor(type)" size="small" class="node-type-tag">
            {{ getTypeName(type) }}
          </a-tag>
          
          <!-- 必填标记 -->
          <a-tag v-if="required" color="red" size="small" class="node-required-tag">
            必填
          </a-tag>
          
          <!-- 数据类型 -->
          <a-tag v-if="dataType && type === 'FIELD'" color="blue" size="small" class="node-datatype-tag">
            {{ dataType }}
          </a-tag>
          
          <!-- 字段值预览 -->
          <span v-if="type === 'FIELD' && value !== undefined" class="node-value">
            {{ formatValue(value) }}
          </span>
          
          <!-- 节点描述 -->
          <span v-if="description" class="node-description">
            {{ description }}
          </span>
          
          <!-- 操作按钮 -->
          <div v-if="showActions" class="node-actions">
            <a-button type="link" size="small" @click="handleAddNode(key)">
              <template #icon><PlusOutlined /></template>
            </a-button>
            <a-button type="link" size="small" @click="handleEditNode(key)">
              <template #icon><EditOutlined /></template>
            </a-button>
            <a-popconfirm
              title="确定删除该节点吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleDeleteNode(key)"
            >
              <a-button type="link" size="small" danger>
                <template #icon><DeleteOutlined /></template>
              </a-button>
            </a-popconfirm>
          </div>
        </div>
      </template>
    </a-tree>
  </div>
</template>

<script setup lang="ts">
/**
 * JSON 树形查看器组件
 * 
 * 用于可视化展示 JSON 数据结构，支持展开/收起、节点类型显示、操作按钮等功能
 * 
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */

import { ref, computed, watch } from 'vue';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons-vue';
import type { JsonTreeNode, TreeNodeType } from '@/utils/jsonTree';
import { getFieldTypeName } from '@/utils/jsonTree';

/**
 * 组件属性定义
 */
interface Props {
  /**
   * 树形数据
   */
  treeData?: JsonTreeNode[];
  
  /**
   * 是否显示连接线
   */
  showLine?: boolean;
  
  /**
   * 是否显示图标
   */
  showIcon?: boolean;
  
  /**
   * 是否默认全部展开
   */
  defaultExpandAll?: boolean;
  
  /**
   * 是否显示操作按钮（添加、编辑、删除）
   */
  showActions?: boolean;
  
  /**
   * 受控的展开节点 keys
   */
  expandedKeys?: string[];
}

/**
 * 组件事件定义
 */
interface Emits {
  /**
   * 添加节点事件
   * @param key 父节点 key
   */
  (e: 'add', key: string): void;
  
  /**
   * 编辑节点事件
   * @param key 节点 key
   */
  (e: 'edit', key: string): void;
  
  /**
   * 删除节点事件
   * @param key 节点 key
   */
  (e: 'delete', key: string): void;
  
  /**
   * 展开节点变化事件
   * @param keys 展开的节点 keys
   */
  (e: 'update:expandedKeys', keys: string[]): void;
}

const props = withDefaults(defineProps<Props>(), {
  treeData: () => [],
  showLine: true,
  showIcon: false,
  defaultExpandAll: false,
  showActions: false,
  expandedKeys: () => []
});

const emit = defineEmits<Emits>();

/**
 * 内部展开状态
 */
const internalExpandedKeys = ref<string[]>([]);

/**
 * 获取类型颜色
 * 
 * 根据节点类型返回对应的标签颜色
 * 
 * @param type 节点类型
 * @returns 颜色值
 */
function getTypeColor(type: TreeNodeType | string): string {
  const colorMap: Record<string, string> = {
    'OBJECT': 'blue',
    'ARRAY': 'purple',
    'FIELD': 'green',
    'STRING': 'cyan',
    'NUMBER': 'orange',
    'BOOLEAN': 'pink',
    'NULL': 'gray'
  };
  
  return colorMap[type] || 'default';
}

/**
 * 格式化字段值显示
 * 
 * 将字段值转换为适合显示的字符串格式
 * 
 * @param value 字段值
 * @returns 格式化后的显示字符串
 */
function formatValue(value: any): string {
  if (value === null) {
    return 'null';
  }
  
  if (value === undefined) {
    return 'undefined';
  }
  
  if (typeof value === 'string') {
    return `"${value}"`;
  }
  
  if (typeof value === 'object') {
    return JSON.stringify(value);
  }
  
  return String(value);
}

/**
 * 处理添加节点
 * 
 * 触发添加节点事件，将父节点 key 传递给父组件
 * 
 * @param key 父节点 key
 * @see Props.showActions
 */
function handleAddNode(key: string) {
  emit('add', key);
}

/**
 * 处理编辑节点
 * 
 * 触发编辑节点事件，将节点 key 传递给父组件
 * 
 * @param key 节点 key
 * @see Props.showActions
 */
function handleEditNode(key: string) {
  emit('edit', key);
}

/**
 * 处理删除节点
 * 
 * 触发删除节点事件，将节点 key 传递给父组件
 * 
 * @param key 节点 key
 * @see Props.showActions
 */
function handleDeleteNode(key: string) {
  emit('delete', key);
}

/**
 * 处理展开节点变化
 * 
 * 更新内部展开状态并通知父组件
 * 
 * @param keys 展开的节点 keys 数组
 */
function onExpand(keys: string[]) {
  internalExpandedKeys.value = keys;
  emit('update:expandedKeys', keys);
}

/**
 * 计算实际的展开 keys
 * 
 * 优先使用外部传入的 expandedKeys，否则使用内部状态
 * 
 * @returns 展开的节点 keys 数组
 */
const computedExpandedKeys = computed(() => {
  if (props.expandedKeys && props.expandedKeys.length > 0) {
    return props.expandedKeys;
  }
  return internalExpandedKeys.value;
});
</script>

<style scoped lang="less">
/**
 * JSON 树形查看器样式
 */
.json-tree-viewer {
  width: 100%;
  padding: 12px;
  background-color: #fafafa;
  border-radius: 4px;

  .empty-content {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 200px;
  }

  :deep(.ant-tree) {
    background: transparent;
    
    .ant-tree-treenode {
      padding: 4px 0;
    }
  }

  .tree-node-content {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 4px 8px;
    border-radius: 4px;
    transition: background-color 0.2s;

    &:hover {
      background-color: #f5f5f5;
    }

    .node-title {
      font-weight: 500;
      color: #333;
    }

    .node-type-tag,
    .node-required-tag,
    .node-datatype-tag {
      margin: 0;
    }

    .node-value {
      color: #1890ff;
      font-family: 'Courier New', monospace;
      font-size: 12px;
      max-width: 200px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .node-description {
      color: #999;
      font-size: 12px;
      font-style: italic;
    }

    .node-actions {
      display: flex;
      gap: 4px;
      margin-left: auto;
      opacity: 0;
      transition: opacity 0.2s;

      .tree-node-content:hover & {
        opacity: 1;
      }
    }
  }
}
</style>
