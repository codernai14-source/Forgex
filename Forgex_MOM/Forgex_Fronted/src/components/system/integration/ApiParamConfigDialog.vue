<template>
  <BaseFormDialog
    v-model:visible="dialogVisible"
    :title="title"
    :width="dialogWidth"
    :ok-text="okText"
    :cancel-text="cancelText"
    :ok-button-props="{ loading: submitting }"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <div class="api-param-config-dialog">
      <!-- 导入导出操作区 -->
      <div class="toolbar">
        <a-space>
          <a-button @click="handleImportJson">
            <template #icon><ImportOutlined /></template>
            导入 JSON
          </a-button>
          <a-button @click="handleExportJson">
            <template #icon><ExportOutlined /></template>
            导出 JSON
          </a-button>
          <a-button type="primary" @click="handleAddField">
            <template #icon><PlusOutlined /></template>
            添加字段
          </a-button>
        </a-space>
      </div>

      <!-- 隐藏的文件输入 -->
      <input
        ref="fileInputRef"
        type="file"
        accept=".json"
        style="display: none"
        @change="handleFileChange"
      />

      <!-- Tabs 标签页 -->
      <a-tabs v-model:activeKey="activeTab" class="config-tabs">
        <!-- 本系统参数配置 -->
        <a-tab-pane key="source" tab="本系统参数配置">
          <div class="tab-content">
            <a-alert
              message="提示"
              description="配置本系统（调用方）的参数结构，支持导入 JSON 文件或手动添加字段"
              type="info"
              show-icon
              class="mb-2"
            />
            
            <JsonTreeViewer
              :tree-data="sourceTreeData"
              :show-actions="true"
              :default-expand-all="true"
              @add="handleAddSourceNode"
              @edit="handleEditSourceNode"
              @delete="handleDeleteSourceNode"
            />
          </div>
        </a-tab-pane>

        <!-- 对方系统参数配置 -->
        <a-tab-pane key="target" tab="对方系统参数配置">
          <div class="tab-content">
            <a-alert
              message="提示"
              description="配置对方系统（被调用方）的参数结构，支持导入 API 文档提供的 JSON Schema"
              type="info"
              show-icon
              class="mb-2"
            />
            
            <JsonTreeViewer
              :tree-data="targetTreeData"
              :show-actions="true"
              :default-expand-all="true"
              @add="handleAddTargetNode"
              @edit="handleEditTargetNode"
              @delete="handleDeleteTargetNode"
            />
          </div>
        </a-tab-pane>

        <!-- 参数映射配置 -->
        <a-tab-pane key="mapping" tab="参数映射配置">
          <div class="tab-content">
            <a-alert
              message="提示"
              description="配置本系统参数与对方系统参数之间的映射关系，支持转换规则"
              type="info"
              show-icon
              class="mb-2"
            />

            <div class="mapping-toolbar">
              <a-button type="primary" @click="handleAddMapping">
                <template #icon><PlusOutlined /></template>
                添加映射
              </a-button>
              <a-button @click="handleBatchMapping">
                <template #icon><ThunderboltOutlined /></template>
                自动映射
              </a-button>
            </div>

            <!-- 映射关系表格 -->
            <a-table
              :columns="mappingColumns"
              :data-source="mappingList"
              :pagination="false"
              :scroll="{ y: 400 }"
              row-key="id"
              size="small"
            >
              <template #bodyCell="{ column, record, index }">
                <!-- 源字段 -->
                <template v-if="column.key === 'sourcePath'">
                  <a-tree-select
                    v-model:value="record.sourcePath"
                    :tree-data="sourceTreeSelectData"
                    placeholder="选择源字段"
                    allow-clear
                    tree-default-expand-all
                    style="width: 100%"
                    @change="handleSourcePathChange(record)"
                  />
                </template>

                <!-- 目标字段 -->
                <template v-if="column.key === 'targetPath'">
                  <a-tree-select
                    v-model:value="record.targetPath"
                    :tree-data="targetTreeSelectData"
                    placeholder="选择目标字段"
                    allow-clear
                    tree-default-expand-all
                    style="width: 100%"
                    @change="handleTargetPathChange(record)"
                  />
                </template>

                <!-- 转换类型 -->
                <template v-if="column.key === 'transformType'">
                  <a-select
                    v-model:value="record.transformType"
                    placeholder="选择转换类型"
                    style="width: 100%"
                    @change="handleTransformTypeChange(record)"
                  >
                    <a-select-option value="DIRECT">直接映射</a-select-option>
                    <a-select-option value="FUNCTION">函数转换</a-select-option>
                    <a-select-option value="CONSTANT">常量值</a-select-option>
                  </a-select>
                </template>

                <!-- 转换规则 -->
                <template v-if="column.key === 'transformRule'">
                  <a-input
                    v-model:value="record.transformRule"
                    placeholder="输入转换规则或常量值"
                    v-if="record.transformType"
                  />
                </template>

                <!-- 操作 -->
                <template v-if="column.key === 'action'">
                  <a-button type="link" danger size="small" @click="handleDeleteMapping(index)">
                    <template #icon><DeleteOutlined /></template>
                  </a-button>
                </template>
              </template>
            </a-table>
          </div>
        </a-tab-pane>
      </a-tabs>
    </div>
  </BaseFormDialog>

  <!-- 字段编辑弹窗 -->
  <a-modal
    v-model:visible="fieldDialogVisible"
    :title="fieldDialogTitle"
    width="600px"
    @ok="handleFieldDialogOk"
    @cancel="handleFieldDialogCancel"
  >
    <a-form :model="fieldForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
      <a-form-item label="字段名称" required>
        <a-input v-model:value="fieldForm.title" placeholder="请输入字段名称" />
      </a-form-item>
      
      <a-form-item label="字段类型" required>
        <a-select v-model:value="fieldForm.type" placeholder="请选择字段类型">
          <a-select-option value="FIELD">字段 (FIELD)</a-select-option>
          <a-select-option value="OBJECT">对象 (OBJECT)</a-select-option>
          <a-select-option value="ARRAY">数组 (ARRAY)</a-select-option>
        </a-select>
      </a-form-item>
      
      <a-form-item label="数据类型" v-if="fieldForm.type === 'FIELD'">
        <a-select v-model:value="fieldForm.dataType" placeholder="请选择数据类型">
          <a-select-option value="STRING">字符串</a-select-option>
          <a-select-option value="NUMBER">数字</a-select-option>
          <a-select-option value="BOOLEAN">布尔</a-select-option>
          <a-select-option value="NULL">空值</a-select-option>
        </a-select>
      </a-form-item>
      
      <a-form-item label="字段值" v-if="fieldForm.type === 'FIELD'">
        <a-input v-model:value="fieldForm.value" placeholder="请输入字段值" />
      </a-form-item>
      
      <a-form-item label="是否必填">
        <a-switch v-model:checked="fieldForm.required" />
      </a-form-item>
      
      <a-form-item label="字段描述">
        <a-textarea
          v-model:value="fieldForm.description"
          placeholder="请输入字段描述"
          :rows="3"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
/**
 * API 参数配置弹窗组件
 * 
 * 用于配置集成接口的参数映射关系，支持本系统参数、对方系统参数的树形结构管理，
 * 以及参数之间的映射转换规则配置
 * 
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 * @see JsonTreeViewer - JSON 树形查看器组件
 * @see BaseFormDialog - 基础表单弹窗组件
 */

import { ref, computed, watch, nextTick } from 'vue';
import { message } from 'ant-design-vue';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ImportOutlined,
  ExportOutlined,
  ThunderboltOutlined
} from '@ant-design/icons-vue';
import type { JsonTreeNode, ParamMapping } from '@/utils/jsonTree';
import {
  parseJsonToTree,
  parseJsonToTreeWithMetadata,
  convertTreeToJson,
  findNodeByPath,
  addNode,
  removeNode,
  updateNode,
  getLeafNodes,
  formatJsonString,
  generateUniqueKey
} from '@/utils/jsonTree';
import BaseFormDialog from '@/components/common/BaseFormDialog.vue';
import JsonTreeViewer from './JsonTreeViewer.vue';

/**
 * 组件属性定义
 */
interface Props {
  /**
   * 弹窗标题
   */
  title?: string;
  
  /**
   * 弹窗宽度
   */
  width?: string | number;
  
  /**
   * 确定按钮文本
   */
  okText?: string;
  
  /**
   * 取消按钮文本
   */
  cancelText?: string;
  
  /**
   * 是否可见
   */
  visible?: boolean;
  
  /**
   * 本系统参数树形数据
   */
  sourceData?: JsonTreeNode[];
  
  /**
   * 对方系统参数树形数据
   */
  targetData?: JsonTreeNode[];
  
  /**
   * 参数映射列表
   */
  mappingData?: ParamMapping[];
}

/**
 * 组件事件定义
 */
interface Emits {
  /**
   * 可见性变化事件
   * @param visible 是否可见
   */
  (e: 'update:visible', visible: boolean): void;
  
  /**
   * 提交事件
   * @param data 提交的数据（包含 sourceData, targetData, mappingData）
   */
  (e: 'submit', data: {
    sourceData: JsonTreeNode[];
    targetData: JsonTreeNode[];
    mappingData: ParamMapping[];
  }): void;
}

const props = withDefaults(defineProps<Props>(), {
  title: 'API 参数配置',
  width: '1200px',
  okText: '保存',
  cancelText: '取消',
  visible: false,
  sourceData: () => [],
  targetData: () => [],
  mappingData: () => []
});

const emit = defineEmits<Emits>();

/**
 * 弹窗可见状态
 */
const dialogVisible = ref(false);

/**
 * 文件输入引用
 */
const fileInputRef = ref<HTMLInputElement>();

/**
 * 当前激活的标签页
 */
const activeTab = ref('source');

/**
 * 提交中状态
 */
const submitting = ref(false);

/**
 * 本系统参数树形数据
 */
const sourceTreeData = ref<JsonTreeNode[]>([]);

/**
 * 对方系统参数树形数据
 */
const targetTreeData = ref<JsonTreeNode[]>([]);

/**
 * 参数映射列表
 */
const mappingList = ref<ParamMapping[]>([]);

/**
 * 字段编辑弹窗可见状态
 */
const fieldDialogVisible = ref(false);

/**
 * 字段表单数据
 */
const fieldForm = ref({
  title: '',
  type: 'FIELD' as TreeNodeType,
  dataType: 'STRING',
  value: '',
  required: false,
  description: '',
  parentKey: '',
  editNodeKey: ''
});

/**
 * 映射表格列配置
 */
const mappingColumns = [
  {
    title: '源字段',
    dataIndex: 'sourcePath',
    key: 'sourcePath',
    width: 250
  },
  {
    title: '目标字段',
    dataIndex: 'targetPath',
    key: 'targetPath',
    width: 250
  },
  {
    title: '转换类型',
    dataIndex: 'transformType',
    key: 'transformType',
    width: 120
  },
  {
    title: '转换规则',
    dataIndex: 'transformRule',
    key: 'transformRule',
    width: 200
  },
  {
    title: '操作',
    key: 'action',
    width: 80,
    fixed: 'right' as const
  }
];

/**
 * 计算字段编辑弹窗标题
 */
const fieldDialogTitle = computed(() => {
  return fieldForm.value.editNodeKey ? '编辑字段' : '添加字段';
});

/**
 * 监听 visible 变化
 */
watch(() => props.visible, (val) => {
  dialogVisible.value = val;
  if (val) {
    // 初始化数据
    sourceTreeData.value = JSON.parse(JSON.stringify(props.sourceData || []));
    targetTreeData.value = JSON.parse(JSON.stringify(props.targetData || []));
    mappingList.value = JSON.parse(JSON.stringify(props.mappingData || []));
  }
});

/**
 * 监听内部 visible 变化
 */
watch(dialogVisible, (val) => {
  emit('update:visible', val);
});

/**
 * 获取树形选择器数据
 * 
 * 将树形数据转换为 a-tree-select 可用的格式，仅显示字段节点
 */
const sourceTreeSelectData = computed(() => {
  return buildTreeSelectData(sourceTreeData.value);
});

const targetTreeSelectData = computed(() => {
  return buildTreeSelectData(targetTreeData.value);
});

/**
 * 构建树形选择器数据
 * 
 * 递归遍历树形节点，仅保留 FIELD 类型的节点用于选择
 * 
 * @param treeData 树形数据
 * @param result 结果数组
 * @returns 树形选择器数据
 */
function buildTreeSelectData(treeData: JsonTreeNode[], result: any[] = []): any[] {
  for (const node of treeData) {
    if (node.type === 'FIELD') {
      result.push({
        key: node.key,
        title: node.title,
        value: node.key,
        children: node.children ? buildTreeSelectData(node.children) : []
      });
    } else if (node.children) {
      const nodeData: any = {
        key: node.key,
        title: node.title,
        value: node.key,
        selectable: false,
        children: buildTreeSelectData(node.children)
      };
      result.push(nodeData);
    }
  }
  return result;
}

/**
 * 处理导入 JSON
 */
function handleImportJson() {
  fileInputRef.value?.click();
}

/**
 * 处理文件选择变化
 * 
 * 读取用户上传的 JSON 文件并解析为树形结构
 * 
 * @param event 文件输入事件
 */
function handleFileChange(event: Event) {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];
  
  if (!file) {
    return;
  }
  
  const reader = new FileReader();
  reader.onload = (e) => {
    try {
      const jsonStr = e.target?.result as string;
      const treeData = parseJsonToTree(jsonStr, 'root', file.name);
      
      if (activeTab.value === 'source') {
        sourceTreeData.value = treeData;
      } else {
        targetTreeData.value = treeData;
      }
      
      message.success('JSON 导入成功');
    } catch (error) {
      message.error('JSON 解析失败，请检查文件格式');
      console.error('JSON 导入失败:', error);
    }
    
    // 清空文件输入
    target.value = '';
  };
  
  reader.readAsText(file);
}

/**
 * 处理导出 JSON
 */
function handleExportJson() {
  const treeData = activeTab.value === 'source' ? sourceTreeData.value : targetTreeData.value;
  
  if (!treeData || treeData.length === 0) {
    message.warning('暂无数据可导出');
    return;
  }
  
  const jsonData = convertTreeToJson(treeData);
  const jsonStr = formatJsonString(jsonData);
  
  // 创建下载链接
  const blob = new Blob([jsonStr], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `${activeTab.value === 'source' ? '本系统' : '对方系统'}_参数配置_${new Date().getTime()}.json`;
  link.click();
  
  URL.revokeObjectURL(url);
  message.success('JSON 导出成功');
}

/**
 * 处理添加字段
 */
function handleAddField() {
  resetFieldForm();
  fieldForm.value.parentKey = '';
  fieldDialogVisible.value = true;
}

/**
 * 处理添加源系统节点
 * 
 * @param parentKey 父节点 key
 */
function handleAddSourceNode(parentKey: string) {
  resetFieldForm();
  fieldForm.value.parentKey = parentKey;
  fieldDialogVisible.value = true;
}

/**
 * 处理编辑源系统节点
 * 
 * @param key 节点 key
 */
function handleEditSourceNode(key: string) {
  const node = findNodeByPath(sourceTreeData.value, key);
  if (!node) {
    message.error('节点不存在');
    return;
  }
  
  fieldForm.value = {
    title: node.title,
    type: node.type,
    dataType: node.dataType || 'STRING',
    value: node.value !== undefined ? String(node.value) : '',
    required: node.required || false,
    description: node.description || '',
    parentKey: node.parentKey || '',
    editNodeKey: key
  };
  
  fieldDialogVisible.value = true;
}

/**
 * 处理删除源系统节点
 * 
 * @param key 节点 key
 */
function handleDeleteSourceNode(key: string) {
  const success = removeNode(sourceTreeData.value, key);
  if (success) {
    message.success('删除成功');
  } else {
    message.error('删除失败');
  }
}

/**
 * 处理添加目标系统节点
 * 
 * @param parentKey 父节点 key
 */
function handleAddTargetNode(parentKey: string) {
  resetFieldForm();
  fieldForm.value.parentKey = parentKey;
  fieldDialogVisible.value = true;
}

/**
 * 处理编辑目标系统节点
 * 
 * @param key 节点 key
 */
function handleEditTargetNode(key: string) {
  const node = findNodeByPath(targetTreeData.value, key);
  if (!node) {
    message.error('节点不存在');
    return;
  }
  
  fieldForm.value = {
    title: node.title,
    type: node.type,
    dataType: node.dataType || 'STRING',
    value: node.value !== undefined ? String(node.value) : '',
    required: node.required || false,
    description: node.description || '',
    parentKey: node.parentKey || '',
    editNodeKey: key
  };
  
  fieldDialogVisible.value = true;
}

/**
 * 处理删除目标系统节点
 * 
 * @param key 节点 key
 */
function handleDeleteTargetNode(key: string) {
  const success = removeNode(targetTreeData.value, key);
  if (success) {
    message.success('删除成功');
  } else {
    message.error('删除失败');
  }
}

/**
 * 处理添加映射关系
 */
function handleAddMapping() {
  const newMapping: ParamMapping = {
    id: `mapping_${Date.now()}`,
    sourcePath: '',
    sourceName: '',
    targetPath: '',
    targetName: '',
    transformType: 'DIRECT',
    transformRule: '',
    remark: ''
  };
  
  mappingList.value.push(newMapping);
}

/**
 * 处理批量自动映射
 * 
 * 根据字段名称自动匹配源字段和目标字段
 */
function handleBatchMapping() {
  const sourceFields = getLeafNodes(sourceTreeData.value);
  const targetFields = getLeafNodes(targetTreeData.value);
  
  if (sourceFields.length === 0 || targetFields.length === 0) {
    message.warning('请先配置源字段和目标字段');
    return;
  }
  
  let matchCount = 0;
  
  // 根据字段名称自动匹配
  sourceFields.forEach(sourceField => {
    const matchedTarget = targetFields.find(target => {
      return target.title.toLowerCase() === sourceField.title.toLowerCase();
    });
    
    if (matchedTarget) {
      const mapping: ParamMapping = {
        id: `mapping_${Date.now()}_${matchCount}`,
        sourcePath: sourceField.key,
        sourceName: sourceField.title,
        targetPath: matchedTarget.key,
        targetName: matchedTarget.title,
        transformType: 'DIRECT',
        transformRule: '',
        remark: '自动映射'
      };
      
      mappingList.value.push(mapping);
      matchCount++;
    }
  });
  
  if (matchCount > 0) {
    message.success(`自动映射成功，共匹配 ${matchCount} 个字段`);
  } else {
    message.info('未找到匹配的字段，请手动添加映射');
  }
}

/**
 * 处理删除映射关系
 * 
 * @param index 映射索引
 */
function handleDeleteMapping(index: number) {
  mappingList.value.splice(index, 1);
  message.success('删除成功');
}

/**
 * 处理源字段路径变化
 * 
 * @param record 映射记录
 */
function handleSourcePathChange(record: ParamMapping) {
  if (record.sourcePath) {
    const node = findNodeByPath(sourceTreeData.value, record.sourcePath);
    if (node) {
      record.sourceName = node.title;
    }
  }
}

/**
 * 处理目标字段路径变化
 * 
 * @param record 映射记录
 */
function handleTargetPathChange(record: ParamMapping) {
  if (record.targetPath) {
    const node = findNodeByPath(targetTreeData.value, record.targetPath);
    if (node) {
      record.targetName = node.title;
    }
  }
}

/**
 * 处理转换类型变化
 * 
 * @param record 映射记录
 */
function handleTransformTypeChange(record: ParamMapping) {
  if (record.transformType === 'CONSTANT') {
    record.transformRule = '';
    message.info('请在转换规则中输入常量值');
  } else if (record.transformType === 'FUNCTION') {
    record.transformRule = '';
    message.info('请在转换规则中输入函数表达式');
  }
}

/**
 * 处理字段弹窗确认
 */
function handleFieldDialogOk() {
  // 校验必填字段
  if (!fieldForm.value.title) {
    message.warning('请输入字段名称');
    return;
  }
  
  if (!fieldForm.value.type) {
    message.warning('请选择字段类型');
    return;
  }
  
  const newNode: JsonTreeNode = {
    key: '',
    title: fieldForm.value.title,
    type: fieldForm.value.type,
    dataType: fieldForm.value.type === 'FIELD' ? fieldForm.value.dataType : undefined,
    value: fieldForm.value.type === 'FIELD' ? fieldForm.value.value : undefined,
    required: fieldForm.value.required,
    description: fieldForm.value.description,
    editable: true,
    parentKey: fieldForm.value.parentKey || undefined
  };
  
  // 生成唯一 key
  const existingKeys = getAllKeys(
    fieldForm.value.parentKey 
      ? (findNodeByPath(sourceTreeData.value, fieldForm.value.parentKey)?.children || [])
      : sourceTreeData.value
  );
  newNode.key = generateUniqueKey(
    fieldForm.value.parentKey || '',
    fieldForm.value.title,
    existingKeys
  );
  
  // 判断是添加到哪个树
  const targetTree = activeTab.value === 'source' ? sourceTreeData.value : targetTreeData.value;
  
  if (fieldForm.value.editNodeKey) {
    // 编辑模式
    const success = updateNode(targetTree, fieldForm.value.editNodeKey, newNode);
    if (success) {
      message.success('更新成功');
    } else {
      message.error('更新失败');
    }
  } else {
    // 新增模式
    const success = addNode(targetTree, fieldForm.value.parentKey || 'root', newNode);
    if (success) {
      message.success('添加成功');
    } else {
      message.error('添加失败，父节点不存在');
    }
  }
  
  fieldDialogVisible.value = false;
}

/**
 * 处理字段弹窗取消
 */
function handleFieldDialogCancel() {
  fieldDialogVisible.value = false;
}

/**
 * 处理提交
 */
async function handleSubmit() {
  submitting.value = true;
  
  try {
    // 校验映射关系
    if (mappingList.value.length === 0) {
      message.warning('请至少添加一条映射关系');
      submitting.value = false;
      return;
    }
    
    // 校验必填字段
    for (const mapping of mappingList.value) {
      if (!mapping.sourcePath || !mapping.targetPath) {
        message.warning('请完善所有映射关系');
        submitting.value = false;
        return;
      }
    }
    
    // 提交数据
    emit('submit', {
      sourceData: sourceTreeData.value,
      targetData: targetTreeData.value,
      mappingData: mappingList.value
    });
    
    message.success('保存成功');
    dialogVisible.value = false;
  } catch (error) {
    message.error('保存失败');
    console.error('保存失败:', error);
  } finally {
    submitting.value = false;
  }
}

/**
 * 处理取消
 */
function handleCancel() {
  dialogVisible.value = false;
}

/**
 * 重置字段表单
 */
function resetFieldForm() {
  fieldForm.value = {
    title: '',
    type: 'FIELD',
    dataType: 'STRING',
    value: '',
    required: false,
    description: '',
    parentKey: '',
    editNodeKey: ''
  };
}

/**
 * 获取所有节点的 keys
 * 
 * @param nodes 节点数组
 * @returns 所有 key 的数组
 */
function getAllKeys(nodes: JsonTreeNode[]): string[] {
  const keys: string[] = [];
  
  for (const node of nodes) {
    keys.push(node.key);
    if (node.children) {
      keys.push(...getAllKeys(node.children));
    }
  }
  
  return keys;
}
</script>

<style scoped lang="less">
/**
 * API 参数配置弹窗样式
 */
.api-param-config-dialog {
  .toolbar {
    margin-bottom: 16px;
    padding: 12px;
    background-color: #fafafa;
    border-radius: 4px;
  }

  .config-tabs {
    :deep(.ant-tabs-content) {
      background-color: #fff;
    }

    .tab-content {
      padding: 16px;

      .mb-2 {
        margin-bottom: 16px;
      }

      .mapping-toolbar {
        margin-bottom: 16px;
        display: flex;
        gap: 8px;
      }

      :deep(.ant-table) {
        font-size: 13px;
        
        .ant-select {
          font-size: 13px;
        }
        
        .ant-input {
          font-size: 13px;
        }
      }
    }
  }
}
</style>
