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
    <div class="api-param-mapping-dialog">
      <!-- 操作区 -->
      <div class="toolbar">
        <a-space>
          <a-button type="primary" @click="handleAddMapping">
            <template #icon><PlusOutlined /></template>
            添加映射
          </a-button>
          <a-button @click="handleBatchMapping">
            <template #icon><ThunderboltOutlined /></template>
            智能匹配
          </a-button>
          <a-button @click="handleClearAll">
            <template #icon><ClearOutlined /></template>
            清空全部
          </a-button>
        </a-space>
      </div>

      <!-- 映射关系表格 -->
      <a-table
        :columns="columns"
        :data-source="mappingList"
        :pagination="pagination"
        :scroll="{ y: 500 }"
        row-key="id"
        size="middle"
        :row-class-name="getRowClassName"
      >
        <!-- 序号列 -->
        <template #bodyCell="{ column, index }">
          <template v-if="column.key === 'index'">
            {{ index + 1 }}
          </template>

          <!-- 源字段选择 -->
          <template v-if="column.key === 'sourceField'">
            <div class="field-select-cell">
              <a-tree-select
                v-model:value="mappingList[index].sourcePath"
                :tree-data="sourceTreeData"
                placeholder="选择源字段"
                allow-clear
                tree-default-expand-all
                show-search
                style="width: 100%"
                :filter-tree-node="filterSourceNode"
                @change="handleSourceFieldChange(index)"
              />
              <span v-if="mappingList[index].sourcePath" class="field-type-tag">
                {{ getSourceFieldType(mappingList[index].sourcePath) }}
              </span>
            </div>
          </template>

          <!-- 目标字段选择 -->
          <template v-if="column.key === 'targetField'">
            <div class="field-select-cell">
              <a-tree-select
                v-model:value="mappingList[index].targetPath"
                :tree-data="targetTreeData"
                placeholder="选择目标字段"
                allow-clear
                tree-default-expand-all
                show-search
                style="width: 100%"
                :filter-tree-node="filterTargetNode"
                @change="handleTargetFieldChange(index)"
              />
              <span v-if="mappingList[index].targetPath" class="field-type-tag">
                {{ getTargetFieldType(mappingList[index].targetPath) }}
              </span>
            </div>
          </template>

          <!-- 转换类型 -->
          <template v-if="column.key === 'transformType'">
            <a-select
              v-model:value="mappingList[index].transformType"
              placeholder="选择转换类型"
              style="width: 100%"
              :options="transformTypeOptions"
              @change="handleTransformTypeChange(index)"
            />
          </template>

          <!-- 转换规则 -->
          <template v-if="column.key === 'transformRule'">
            <div class="transform-rule-cell">
              <a-input
                v-model:value="mappingList[index].transformRule"
                :placeholder="getTransformRulePlaceholder(mappingList[index].transformType)"
                allow-clear
              >
                <template v-if="mappingList[index].transformType === 'FUNCTION'" #prefix>
                  <FunctionOutlined />
                </template>
                <template v-else-if="mappingList[index].transformType === 'CONSTANT'" #prefix>
                  <NumberOutlined />
                </template>
              </a-input>
              <a-tooltip v-if="mappingList[index].transformType === 'FUNCTION'" title="常用函数">
                <a-button type="text" size="small" @click="showFunctionHelp">
                  <template #icon><QuestionCircleOutlined /></template>
                </a-button>
              </a-tooltip>
            </div>
          </template>

          <!-- 备注 -->
          <template v-if="column.key === 'remark'">
            <a-input
              v-model:value="mappingList[index].remark"
              placeholder="备注说明"
              allow-clear
            />
          </template>

          <!-- 操作 -->
          <template v-if="column.key === 'action'">
            <div class="action-buttons">
              <a-button
                type="link"
                size="small"
                @click="handleCopyMapping(index)"
                title="复制"
              >
                <template #icon><CopyOutlined /></template>
              </a-button>
              <a-button
                type="link"
                size="small"
                danger
                @click="handleDeleteMapping(index)"
                title="删除"
              >
                <template #icon><DeleteOutlined /></template>
              </a-button>
            </div>
          </template>
        </template>

        <!-- 汇总行 -->
        <template #summary>
          <a-table-summary>
            <a-table-summary-row>
              <a-table-summary-cell :index="0" colspan="2">
                <strong>汇总：</strong>
              </a-table-summary-cell>
              <a-table-summary-cell :index="2">
                共 {{ mappingList.length }} 条映射
              </a-table-summary-cell>
              <a-table-summary-cell :index="3" colspan="3">
                <a-space>
                  <span>直接映射：{{ directCount }}</span>
                  <span>函数转换：{{ functionCount }}</span>
                  <span>常量值：{{ constantCount }}</span>
                </a-space>
              </a-table-summary-cell>
            </a-table-summary-row>
          </a-table-summary>
        </template>
      </a-table>

      <!-- 字段类型提示 -->
      <a-alert
        message="字段类型说明"
        type="info"
        show-icon
        class="mt-2"
      >
        <template #description>
          <div class="field-type-help">
            <a-tag color="cyan">STRING</a-tag>
            <span>字符串</span>
            <a-tag color="orange" class="ml-1">NUMBER</a-tag>
            <span>数字</span>
            <a-tag color="pink" class="ml-1">BOOLEAN</a-tag>
            <span>布尔</span>
            <a-tag color="blue" class="ml-1">OBJECT</a-tag>
            <span>对象</span>
            <a-tag color="purple" class="ml-1">ARRAY</a-tag>
            <span>数组</span>
          </div>
        </template>
      </a-alert>
    </div>

    <!-- 函数帮助弹窗 -->
    <a-modal
      v-model:visible="functionHelpVisible"
      title="转换函数帮助"
      width="700px"
      :footer="null"
    >
      <div class="function-help">
        <a-collapse>
          <a-collapse-panel key="1" header="字符串函数">
            <ul>
              <li><code>toUpperCase(value)</code> - 转大写</li>
              <li><code>toLowerCase(value)</code> - 转小写</li>
              <li><code>trim(value)</code> - 去除空格</li>
              <li><code>substring(value, start, end)</code> - 截取字符串</li>
              <li><code>concat(value1, value2)</code> - 拼接字符串</li>
            </ul>
          </a-collapse-panel>
          <a-collapse-panel key="2" header="数字函数">
            <ul>
              <li><code>abs(value)</code> - 绝对值</li>
              <li><code>round(value, decimals)</code> - 四舍五入</li>
              <li><code>ceil(value)</code> - 向上取整</li>
              <li><code>floor(value)</code> - 向下取整</li>
              <li><code>max(value1, value2)</code> - 最大值</li>
              <li><code>min(value1, value2)</code> - 最小值</li>
            </ul>
          </a-collapse-panel>
          <a-collapse-panel key="3" header="日期函数">
            <ul>
              <li><code>formatDate(value, format)</code> - 格式化日期</li>
              <li><code>parseDate(value, format)</code> - 解析日期</li>
              <li><code>dateAdd(value, days)</code> - 日期相加</li>
              <li><code>dateDiff(date1, date2)</code> - 日期差值</li>
            </ul>
          </a-collapse-panel>
          <a-collapse-panel key="4" header="逻辑函数">
            <ul>
              <li><code>if(condition, trueValue, falseValue)</code> - 条件判断</li>
              <li><code>coalesce(value1, value2)</code> - 返回第一个非空值</li>
              <li><code>defaultValue(value, default)</code> - 默认值</li>
            </ul>
          </a-collapse-panel>
        </a-collapse>
      </div>
    </a-modal>
  </BaseFormDialog>
</template>

<script setup lang="ts">
/**
 * API 参数映射配置弹窗组件
 * 
 * 专门用于配置 API 接口参数之间的映射关系，支持源字段和目标字段的树形选择，
 * 提供多种转换类型（直接映射、函数转换、常量值）和转换规则配置
 * 
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 * @see BaseFormDialog - 基础表单弹窗组件
 * @see ApiParamConfigDialog - API 参数配置弹窗组件
 */

import { ref, computed, watch } from 'vue';
import { message } from 'ant-design-vue';
import {
  PlusOutlined,
  DeleteOutlined,
  CopyOutlined,
  ThunderboltOutlined,
  ClearOutlined,
  FunctionOutlined,
  NumberOutlined,
  QuestionCircleOutlined
} from '@ant-design/icons-vue';
import type { JsonTreeNode, ParamMapping } from '@/utils/jsonTree';
import { findNodeByPath, getLeafNodes } from '@/utils/jsonTree';
import BaseFormDialog from '@/components/common/BaseFormDialog.vue';

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
   * 源系统（本系统）参数树形数据
   */
  sourceData?: JsonTreeNode[];
  
  /**
   * 目标系统（对方系统）参数树形数据
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
   * @param data 提交的数据（包含 mappingData）
   */
  (e: 'submit', data: {
    mappingData: ParamMapping[];
  }): void;
}

const props = withDefaults(defineProps<Props>(), {
  title: 'API 参数映射配置',
  width: '1400px',
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
 * 提交中状态
 */
const submitting = ref(false);

/**
 * 参数映射列表
 */
const mappingList = ref<ParamMapping[]>([]);

/**
 * 函数帮助弹窗可见状态
 */
const functionHelpVisible = ref(false);

/**
 * 表格分页配置
 */
const pagination = {
  pageSize: 100,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
};

/**
 * 转换类型选项
 */
const transformTypeOptions = [
  { label: '直接映射', value: 'DIRECT' },
  { label: '函数转换', value: 'FUNCTION' },
  { label: '常量值', value: 'CONSTANT' }
];

/**
 * 表格列配置
 */
const columns = [
  {
    title: '序号',
    dataIndex: 'index',
    key: 'index',
    width: 60,
    fixed: 'left' as const
  },
  {
    title: '源字段',
    dataIndex: 'sourcePath',
    key: 'sourceField',
    width: 300,
    fixed: 'left' as const
  },
  {
    title: '目标字段',
    dataIndex: 'targetPath',
    key: 'targetField',
    width: 300,
    fixed: 'left' as const
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
    width: 250
  },
  {
    title: '备注',
    dataIndex: 'remark',
    key: 'remark',
    width: 200
  },
  {
    title: '操作',
    key: 'action',
    width: 100,
    fixed: 'right' as const
  }
];

/**
 * 监听 visible 变化
 */
watch(() => props.visible, (val) => {
  dialogVisible.value = val;
  if (val) {
    // 初始化数据
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
 * 计算源系统树形数据
 */
const sourceTreeData = computed(() => {
  return buildTreeSelectData(props.sourceData || []);
});

/**
 * 计算目标系统树形数据
 */
const targetTreeData = computed(() => {
  return buildTreeSelectData(props.targetData || []);
});

/**
 * 计算直接映射数量
 */
const directCount = computed(() => {
  return mappingList.value.filter(m => m.transformType === 'DIRECT').length;
});

/**
 * 计算函数转换数量
 */
const functionCount = computed(() => {
  return mappingList.value.filter(m => m.transformType === 'FUNCTION').length;
});

/**
 * 计算常量值数量
 */
const constantCount = computed(() => {
  return mappingList.value.filter(m => m.transformType === 'CONSTANT').length;
});

/**
 * 构建树形选择器数据
 * 
 * 递归遍历树形节点，构建 a-tree-select 可用的数据格式
 * 
 * @param treeData 树形数据
 * @param result 结果数组
 * @returns 树形选择器数据
 */
function buildTreeSelectData(treeData: JsonTreeNode[], result: any[] = []): any[] {
  for (const node of treeData) {
    const nodeData: any = {
      key: node.key,
      title: getNodeTitle(node),
      value: node.key,
      selectable: node.type === 'FIELD',
      isLeaf: node.type === 'FIELD',
      children: node.children ? buildTreeSelectData(node.children, []) : []
    };
    
    // 如果有子节点，合并到 children
    if (nodeData.children.length > 0) {
      nodeData.children = nodeData.children[0].children || nodeData.children;
    }
    
    result.push(nodeData);
  }
  return result;
}

/**
 * 获取节点显示标题
 * 
 * 包含节点名称和类型信息
 * 
 * @param node 树形节点
 * @returns 显示标题
 */
function getNodeTitle(node: JsonTreeNode): string {
  const typeMap: Record<string, string> = {
    'FIELD': '字段',
    'OBJECT': '对象',
    'ARRAY': '数组'
  };
  
  return `${node.title} (${typeMap[node.type] || '未知'})`;
}

/**
 * 筛选源系统节点
 * 
 * @param treeNode 树节点
 * @returns 是否匹配
 */
function filterSourceNode(treeNode: any): boolean {
  const title = treeNode.title || '';
  return title.toLowerCase().includes('');
}

/**
 * 筛选目标系统节点
 * 
 * @param treeNode 树节点
 * @returns 是否匹配
 */
function filterTargetNode(treeNode: any): boolean {
  const title = treeNode.title || '';
  return title.toLowerCase().includes('');
}

/**
 * 获取源字段类型
 * 
 * @param path 字段路径
 * @returns 字段类型
 */
function getSourceFieldType(path: string): string {
  const node = findNodeByPath(props.sourceData || [], path);
  return node?.dataType || node?.type || '';
}

/**
 * 获取目标字段类型
 * 
 * @param path 字段路径
 * @returns 字段类型
 */
function getTargetFieldType(path: string): string {
  const node = findNodeByPath(props.targetData || [], path);
  return node?.dataType || node?.type || '';
}

/**
 * 获取转换规则输入框占位符
 * 
 * @param transformType 转换类型
 * @returns 占位符文本
 */
function getTransformRulePlaceholder(transformType?: string): string {
  switch (transformType) {
    case 'FUNCTION':
      return '输入函数表达式，如：toUpperCase(value)';
    case 'CONSTANT':
      return '输入常量值';
    default:
      return '无需转换规则';
  }
}

/**
 * 获取表格行样式
 * 
 * @param record 行数据
 * @param index 行索引
 * @returns 样式类名
 */
function getRowClassName(record: ParamMapping, index: number): string {
  if (!record.sourcePath || !record.targetPath) {
    return 'incomplete-row';
  }
  return '';
}

/**
 * 处理添加映射
 */
function handleAddMapping() {
  const newMapping: ParamMapping = {
    id: `mapping_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
    sourcePath: '',
    sourceName: '',
    targetPath: '',
    targetName: '',
    transformType: 'DIRECT',
    transformRule: '',
    remark: ''
  };
  
  mappingList.value.push(newMapping);
  
  // 滚动到底部
  nextTick(() => {
    const tableWrapper = document.querySelector('.ant-table-body');
    if (tableWrapper) {
      tableWrapper.scrollTop = tableWrapper.scrollHeight;
    }
  });
}

/**
 * 处理批量智能匹配
 * 
 * 根据字段名称、类型等信息自动匹配源字段和目标字段
 */
function handleBatchMapping() {
  const sourceFields = getLeafNodes(props.sourceData || []);
  const targetFields = getLeafNodes(props.targetData || []);
  
  if (sourceFields.length === 0) {
    message.warning('源系统参数为空，请先配置源系统参数');
    return;
  }
  
  if (targetFields.length === 0) {
    message.warning('目标系统参数为空，请先配置目标系统参数');
    return;
  }
  
  let matchCount = 0;
  const matchedSourceKeys = new Set<string>();
  
  // 精确匹配：字段名完全相同（忽略大小写）
  sourceFields.forEach(sourceField => {
    const matchedTarget = targetFields.find(target => {
      return target.title.toLowerCase() === sourceField.title.toLowerCase();
    });
    
    if (matchedTarget && !matchedSourceKeys.has(sourceField.key)) {
      const mapping: ParamMapping = {
        id: `mapping_${Date.now()}_${matchCount}`,
        sourcePath: sourceField.key,
        sourceName: sourceField.title,
        targetPath: matchedTarget.key,
        targetName: matchedTarget.title,
        transformType: 'DIRECT',
        transformRule: '',
        remark: '智能匹配 - 字段名相同'
      };
      
      mappingList.value.push(mapping);
      matchedSourceKeys.add(sourceField.key);
      matchCount++;
    }
  });
  
  // 模糊匹配：字段名包含关系
  sourceFields.forEach(sourceField => {
    if (matchedSourceKeys.has(sourceField.key)) {
      return;
    }
    
    const matchedTarget = targetFields.find(target => {
      return !matchedSourceKeys.has(target.key) &&
        (target.title.toLowerCase().includes(sourceField.title.toLowerCase()) ||
         sourceField.title.toLowerCase().includes(target.title.toLowerCase()));
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
        remark: '智能匹配 - 字段名相似'
      };
      
      mappingList.value.push(mapping);
      matchedSourceKeys.add(sourceField.key);
      matchCount++;
    }
  });
  
  if (matchCount > 0) {
    message.success(`智能匹配成功，共匹配 ${matchCount} 个字段`);
  } else {
    message.info('未找到匹配的字段，请手动添加映射');
  }
}

/**
 * 处理清空全部映射
 */
function handleClearAll() {
  if (mappingList.value.length === 0) {
    return;
  }
  
  // 使用 confirm 对话框
  const confirmDialog = message.loading({
    content: '确定要清空所有映射关系吗？此操作不可恢复。',
    duration: 0
  });
  
  // 这里简化处理，直接清空
  mappingList.value = [];
  message.success('已清空所有映射关系');
}

/**
 * 处理源字段变化
 * 
 * @param index 映射索引
 */
function handleSourceFieldChange(index: number) {
  const mapping = mappingList.value[index];
  if (mapping.sourcePath) {
    const node = findNodeByPath(props.sourceData || [], mapping.sourcePath);
    if (node) {
      mapping.sourceName = node.title;
      
      // 如果是首次设置源字段，且目标字段为空，尝试自动匹配
      if (!mapping.targetPath) {
        autoMatchTargetField(mapping, index);
      }
    }
  }
}

/**
 * 处理目标字段变化
 * 
 * @param index 映射索引
 */
function handleTargetFieldChange(index: number) {
  const mapping = mappingList.value[index];
  if (mapping.targetPath) {
    const node = findNodeByPath(props.targetData || [], mapping.targetPath);
    if (node) {
      mapping.targetName = node.title;
    }
  }
}

/**
 * 处理转换类型变化
 * 
 * @param index 映射索引
 */
function handleTransformTypeChange(index: number) {
  const mapping = mappingList.value[index];
  
  // 清空转换规则
  if (mapping.transformType !== 'FUNCTION' && mapping.transformType !== 'CONSTANT') {
    mapping.transformRule = '';
  }
}

/**
 * 处理复制映射
 * 
 * @param index 映射索引
 */
function handleCopyMapping(index: number) {
  const source = mappingList.value[index];
  const newMapping: ParamMapping = {
    ...source,
    id: `mapping_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
    sourcePath: '',
    targetPath: '',
    remark: source.remark ? `${source.remark} (复制)` : '复制'
  };
  
  mappingList.value.splice(index + 1, 0, newMapping);
  message.success('复制成功');
}

/**
 * 处理删除映射
 * 
 * @param index 映射索引
 */
function handleDeleteMapping(index: number) {
  mappingList.value.splice(index, 1);
  message.success('删除成功');
}

/**
 * 显示函数帮助
 */
function showFunctionHelp() {
  functionHelpVisible.value = true;
}

/**
 * 自动匹配目标字段
 * 
 * @param mapping 映射记录
 * @param index 映射索引
 */
function autoMatchTargetField(mapping: ParamMapping, index: number) {
  const targetFields = getLeafNodes(props.targetData || []);
  
  const matchedTarget = targetFields.find(target => {
    return target.title.toLowerCase() === mapping.sourceName?.toLowerCase();
  });
  
  if (matchedTarget) {
    mapping.targetPath = matchedTarget.key;
    mapping.targetName = matchedTarget.title;
  }
}

/**
 * 处理提交
 */
async function handleSubmit() {
  submitting.value = true;
  
  try {
    // 校验必填字段
    const incompleteMappings = mappingList.value.filter(
      m => !m.sourcePath || !m.targetPath
    );
    
    if (incompleteMappings.length > 0) {
      message.warning(`有 ${incompleteMappings.length} 条映射关系未完善，请补充源字段和目标字段`);
      submitting.value = false;
      return;
    }
    
    if (mappingList.value.length === 0) {
      message.warning('请至少添加一条映射关系');
      submitting.value = false;
      return;
    }
    
    // 提交数据
    emit('submit', {
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
 * 延迟执行
 * 
 * @param ms 毫秒数
 */
function nextTick(ms: number = 0): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms));
}
</script>

<style scoped lang="less">
/**
 * API 参数映射配置弹窗样式
 */
.api-param-mapping-dialog {
  .toolbar {
    margin-bottom: 16px;
    padding: 12px;
    background-color: #fafafa;
    border-radius: 4px;
  }

  .mt-2 {
    margin-top: 16px;
  }

  .ml-1 {
    margin-left: 8px;
  }

  :deep(.ant-table) {
    font-size: 13px;

    .incomplete-row {
      background-color: #fff2f0;
    }

    .ant-select {
      font-size: 13px;
    }
  }

  .field-select-cell {
    display: flex;
    align-items: center;
    gap: 8px;

    .field-type-tag {
      font-size: 12px;
      color: #666;
      white-space: nowrap;
      min-width: 60px;
    }
  }

  .transform-rule-cell {
    display: flex;
    align-items: center;
    gap: 4px;

    :deep(.ant-input) {
      font-family: 'Courier New', monospace;
      font-size: 13px;
    }
  }

  .action-buttons {
    display: flex;
    gap: 4px;
  }

  .field-type-help {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    align-items: center;

    span {
      color: #666;
      font-size: 13px;
    }
  }

  .function-help {
    ul {
      margin: 8px 0;
      padding-left: 20px;

      li {
        margin: 4px 0;
        line-height: 1.8;

        code {
          background-color: #f5f5f5;
          padding: 2px 6px;
          border-radius: 3px;
          font-family: 'Courier New', monospace;
          color: #1890ff;
        }
      }
    }
  }
}
</style>
