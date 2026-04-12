<template>
  <div ref="designerRootRef" class="designer-page">
    <section class="designer-topbar">
      <div>
        <a-button type="link" class="designer-topbar__back" @click="navigateBackToList()">
          返回流程列表
        </a-button>
        <h2>{{ editorContext?.taskName || '审批节点配置' }}</h2>
        <p>
          草稿版本 v{{ editorContext?.draftVersion || '-' }}
          <span v-if="editorContext?.publishedVersion">，当前已发布 v{{ editorContext.publishedVersion }}</span>
        </p>
      </div>

      <a-space>
        <a-button :loading="saving" @click="handleSave">保存草稿</a-button>
        <a-button type="primary" :loading="publishing" @click="handlePublish">发布草稿</a-button>
      </a-space>
    </section>

    <section class="designer-shell">
      <aside class="palette-panel">
        <div class="panel-title">节点面板</div>
        <p class="panel-desc">开始和结束节点由系统内置且唯一存在，审批节点与条件分支支持拖拽到画布中。</p>

        <div class="panel-subtitle">系统节点</div>
        <button
          type="button"
          class="system-node-item"
          :class="{ 'system-node-item--active': selectedNodeKey === 'start' }"
          @click="focusSystemNode('start')"
        >
          <span class="palette-item__badge palette-item__badge--start">开始</span>
          <div>
            <div class="palette-item__title">开始节点</div>
            <div class="palette-item__desc">流程入口，只能有一条出线，支持修改节点名称。</div>
          </div>
        </button>

        <button
          type="button"
          class="system-node-item"
          :class="{ 'system-node-item--active': selectedNodeKey === 'end' }"
          @click="focusSystemNode('end')"
        >
          <span class="palette-item__badge palette-item__badge--end">缁撴潫</span>
          <div>
            <div class="palette-item__title">缁撴潫鑺傜偣</div>
            <div class="palette-item__desc">流程终点，不能删除，也不允许再向外连线。</div>
          </div>
        </button>

        <a-divider />

        <div class="panel-subtitle">拖拽新增</div>
        <div
          class="palette-item"
          draggable="true"
          @dragstart="handleDragStart($event, NODE_TYPES.APPROVE)"
        >
          <span class="palette-item__badge palette-item__badge--approve">审批</span>
          <div>
            <div class="palette-item__title">审批节点</div>
            <div class="palette-item__desc">配置审批类型、审批人来源，并通过弹窗选择审批对象。</div>
          </div>
        </div>

        <div
          class="palette-item"
          draggable="true"
          @dragstart="handleDragStart($event, NODE_TYPES.BRANCH)"
        >
          <span class="palette-item__badge palette-item__badge--branch">条件</span>
          <div>
            <div class="palette-item__title">条件分支</div>
            <div class="palette-item__desc">为条件规则配置默认去向，规则目标必须来自该分支的出线。</div>
          </div>
        </div>

        <a-divider />

        <a-button block :disabled="!canDeleteNode" @click="handleDeleteSelectedNode">删除当前节点</a-button>
        <div class="panel-hint">仅审批节点和条件分支支持删除。连线可通过右键菜单或 Delete 键删除。</div>
      </aside>

      <div class="canvas-panel" @dragover.prevent @drop="handleDrop">
        <VueFlow
          v-model:nodes="flowNodes"
          v-model:edges="flowEdges"
          class="designer-flow"
          :fit-view-on-init="true"
          :connection-line-style="connectionLineStyle"
          :delete-key-code="null"
          @connect="handleConnect"
          @node-click="handleNodeClick"
          @node-context-menu="handleNodeContextMenu"
          @edge-click="handleEdgeClick"
          @edge-context-menu="handleEdgeContextMenu"
          @pane-click="handlePaneClick"
          @pane-context-menu="handlePaneContextMenu"
        >
          <Background :pattern-color="flowPatternColor" :gap="20" />
          <MiniMap pannable zoomable />
          <Controls />

          <template #node-workflow="{ data, selected }">
            <Handle
              v-if="data.nodeType !== NODE_TYPES.START"
              type="target"
              :position="Position.Top"
              class="node-handle node-handle--target"
            />
            <div
              class="flow-node-card"
              :class="{ 'flow-node-card--selected': selected || selectedNodeKey === data.nodeKey }"
            >
              <div class="flow-node-card__head">
                <span class="flow-node-card__type" :class="`flow-node-card__type--${nodeKind(data.nodeType)}`">
                  {{ nodeTypeLabel(data.nodeType) }}
                </span>
                <strong>{{ data.nodeName }}</strong>
              </div>
              <p>{{ nodeSummary(data) }}</p>
            </div>
            <Handle
              v-if="data.nodeType !== NODE_TYPES.END"
              type="source"
              :position="Position.Bottom"
              class="node-handle node-handle--source"
            />
          </template>
        </VueFlow>
      </div>

      <aside class="config-panel">
        <div class="panel-title">属性配置</div>

        <template v-if="selectedNodeData">
          <a-form layout="vertical">
            <a-form-item label="节点名称">
              <a-input
                :value="selectedNodeData.nodeName"
                @update:value="updateSelectedNodeData({ nodeName: String($event || '') })"
              />
            </a-form-item>

            <template v-if="selectedNodeData.nodeType === NODE_TYPES.START">
              <div class="field-tip">开始节点由系统内置，是流程入口，不能有入线，并且必须保留唯一一条出线。</div>
            </template>

            <template v-else-if="selectedNodeData.nodeType === NODE_TYPES.END">
              <div class="field-tip">结束节点由系统内置，是流程出口，不能删除，也不能继续向外连线。</div>
            </template>

            <template v-else-if="selectedNodeData.nodeType === NODE_TYPES.APPROVE">
              <a-form-item label="审批类型">
                <a-select
                  :value="selectedNodeData.approveType"
                  @update:value="updateSelectedNodeData({ approveType: Number($event) })"
                >
                  <a-select-option v-for="item in approveTypeOptions" :key="item.value" :value="item.value">
                    {{ item.label }}
                  </a-select-option>
                </a-select>
                <div class="field-tip">
                  {{ approveTypeDescription(selectedNodeData.approveType) }}
                </div>
              </a-form-item>

              <a-form-item label="审批人来源">
                <div class="editor-list">
                  <div
                    v-for="(approver, index) in selectedNodeData.approvers"
                    :key="`${approver.approverType}-${index}`"
                    class="editor-card editor-card--column"
                  >
                    <div class="approver-toolbar">
                      <a-select
                        :value="approver.approverType"
                        style="width: 132px"
                        @update:value="updateApproverType(index, Number($event))"
                      >
                        <a-select-option v-for="item in approverTypeOptions" :key="item.value" :value="item.value">
                          {{ item.label }}
                        </a-select-option>
                      </a-select>
                      <a-space>
                        <a-button @click="openApproverDialog(index)">
                          {{ approver.approverIds.length ? '编辑选择' : '选择对象' }}
                        </a-button>
                        <a-button danger @click="removeApprover(index)">删除</a-button>
                      </a-space>
                    </div>

                    <div class="approver-summary">
                      <template v-if="approver.approverIds.length">
                        <a-tag v-for="label in approverDisplayLabels(approver)" :key="label" color="blue">
                          {{ label }}
                        </a-tag>
                      </template>
                      <span v-else class="approver-summary__empty">未选择{{ approverTypeLabel(approver.approverType) }}，请通过弹窗选择。</span>
                    </div>
                  </div>
                </div>
                <a-button type="dashed" block @click="addApprover">新增审批人来源</a-button>
              </a-form-item>
            </template>

            <template v-else-if="selectedNodeData.nodeType === NODE_TYPES.BRANCH">
              <a-form-item label="默认分支">
                <a-select
                  allow-clear
                  :value="selectedNodeData.defaultBranchNodeKey"
                  :disabled="!branchTargetOptions.length"
                  placeholder="请先为当前分支节点连接目标节点"
                  @update:value="updateSelectedNodeData({ defaultBranchNodeKey: normalizeSelectValue($event) })"
                >
                  <a-select-option
                    v-for="node in branchTargetOptions"
                    :key="node.id"
                    :value="String(node.id)"
                  >
                    {{ (node.data as WorkflowNodeData).nodeName }}
                  </a-select-option>
                </a-select>
                <div class="field-tip">默认分支和规则去向都只能从当前分支节点的出线中选择。</div>
              </a-form-item>

              <a-form-item label="分支规则">
                <div class="editor-list">
                  <div
                    v-for="(rule, index) in selectedNodeData.branchRules"
                    :key="index"
                    class="editor-card editor-card--column"
                  >
                    <a-row :gutter="8">
                      <a-col :span="12">
                        <a-input
                          :value="rule.fieldKey"
                          placeholder="字段键"
                          @update:value="updateBranchRule(index, { fieldKey: String($event || '') })"
                        />
                      </a-col>
                      <a-col :span="12">
                        <a-input
                          :value="rule.fieldLabel"
                          placeholder="字段显示名"
                          @update:value="updateBranchRule(index, { fieldLabel: String($event || '') })"
                        />
                      </a-col>
                    </a-row>
                    <a-row :gutter="8">
                      <a-col :span="8">
                        <a-select
                          :value="rule.operator"
                          placeholder="操作符"
                          @update:value="updateBranchRule(index, { operator: String($event || '') })"
                        >
                          <a-select-option v-for="item in operatorOptions" :key="item" :value="item">
                            {{ item }}
                          </a-select-option>
                        </a-select>
                      </a-col>
                      <a-col :span="8">
                        <a-input
                          :value="rule.value"
                          placeholder="比较值"
                          @update:value="updateBranchRule(index, { value: String($event || '') })"
                        />
                      </a-col>
                      <a-col :span="8">
                        <a-select
                          allow-clear
                          :value="rule.nextNodeKey"
                          :disabled="!branchTargetOptions.length"
                          placeholder="瑙勫垯鍘诲悜"
                          @update:value="updateBranchRule(index, { nextNodeKey: normalizeSelectValue($event) || '' })"
                        >
                          <a-select-option
                            v-for="node in branchTargetOptions"
                            :key="node.id"
                            :value="String(node.id)"
                          >
                            {{ (node.data as WorkflowNodeData).nodeName }}
                          </a-select-option>
                        </a-select>
                      </a-col>
                    </a-row>
                    <a-button danger @click="removeBranchRule(index)">删除规则</a-button>
                  </div>
                </div>
                <a-button type="dashed" block @click="addBranchRule">新增规则</a-button>
              </a-form-item>
            </template>
          </a-form>
        </template>

        <template v-else-if="selectedEdgeData">
          <div class="selected-edge-panel">
            <div class="field-tip">已选中一条连线。可以通过 Delete 键、右键菜单或下面的按钮删除它。</div>
            <a-descriptions :column="1" size="small" bordered>
              <a-descriptions-item label="璧风偣">
                {{ edgeNodeName(String(selectedEdgeData.source)) }}
              </a-descriptions-item>
              <a-descriptions-item label="缁堢偣">
                {{ edgeNodeName(String(selectedEdgeData.target)) }}
              </a-descriptions-item>
            </a-descriptions>
            <a-button danger block class="selected-edge-panel__button" @click="handleDeleteSelectedEdge">删除当前连线</a-button>
          </div>
        </template>

        <a-empty v-else description="点击节点后可在这里编辑属性，连线支持点击选中并删除。" />
      </aside>
    </section>

    <a-modal
      v-model:open="approverDialogOpen"
      title="选择审批人来源"
      destroy-on-close
      @ok="handleApproverDialogOk"
      @cancel="closeApproverDialog"
    >
      <ReceiverSelector v-model:modelValue="approverDialogModel" />
      <div class="field-tip approver-dialog-tip">选择结果会回填到当前审批节点，仅提交审批类型和对象 ID，不修改后端接口结构。</div>
    </a-modal>

    <Teleport to="body">
      <div
        v-if="contextMenu.visible"
        class="designer-context-menu"
        :style="{ left: `${contextMenu.x}px`, top: `${contextMenu.y}px` }"
      >
        <template v-if="contextMenu.kind === 'pane'">
          <button type="button" class="designer-context-menu__item" @click="handleContextMenuAction('createApprove')">
            新增审批节点
          </button>
          <button type="button" class="designer-context-menu__item" @click="handleContextMenuAction('createBranch')">
            新增条件分支
          </button>
        </template>

        <template v-else-if="contextMenu.kind === 'node'">
          <button type="button" class="designer-context-menu__item" @click="handleContextMenuAction('editNode')">
            编辑节点
          </button>
          <button
            type="button"
            class="designer-context-menu__item"
            :disabled="!contextMenuNodeCanDelete"
            @click="handleContextMenuAction('deleteNode')"
          >
            删除节点
          </button>
        </template>

        <template v-else-if="contextMenu.kind === 'edge'">
          <button type="button" class="designer-context-menu__item" @click="handleContextMenuAction('deleteEdge')">
            删除连线
          </button>
        </template>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import { MiniMap } from '@vue-flow/minimap'
import {
  Handle,
  MarkerType,
  Position,
  VueFlow,
  useVueFlow,
  type Connection,
  type Edge as FlowEdge,
  type EdgeMouseEvent,
  type Node as FlowNode,
  type NodeMouseEvent
} from '@vue-flow/core'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'
import '@vue-flow/controls/dist/style.css'
import '@vue-flow/minimap/dist/style.css'
import { approvalRoutePaths, normalizeApprovalRoutePath } from '@/router/approvalRoutePaths'
import ReceiverSelector from '@/components/common/ReceiverSelector.vue'
import { getRoleList } from '@/api/system/role'
import { listDepartments } from '@/api/system/department'
import { listPositions } from '@/api/system/position'
import { getUserList } from '@/api/system/user'
import {
  getDraftGraph,
  getOrCreateDraftEditor,
  publishDraft,
  saveDraftGraph,
  type WfBranchRuleDTO,
  type WfNodeApproverDTO,
  type WfTaskDraftEditorDTO,
  type WfTaskEdgeDTO,
  type WfTaskNodeEditorDTO
} from '@/api/workflow/taskConfig'

interface WorkflowNodeData extends WfTaskNodeEditorDTO {
  nodeKey: string
}

interface ReceiverModel {
  receiverType?: string
  receiverIds: string[]
}

interface ContextMenuState {
  visible: boolean
  kind: 'pane' | 'node' | 'edge'
  x: number
  y: number
  nodeId?: string
  edgeId?: string
  flowX?: number
  flowY?: number
}

const NODE_TYPES = {
  START: 1,
  END: 2,
  APPROVE: 3,
  BRANCH: 5
} as const

const APPROVER_TYPES = {
  USER: 1,
  DEPT: 2,
  ROLE: 3,
  POSITION: 4
} as const

const approveTypeOptions = [
  { label: '会签', value: 1, description: '所有审批人都同意后才通过。' },
  { label: '或签', value: 2, description: '任一审批人同意即可通过。' },
  { label: '抄送', value: 3, description: '仅抄送通知，不要求审批动作。' },
  { label: '投票', value: 4, description: '超过半数同意后通过。' },
  { label: '顺序审批', value: 5, description: '按配置顺序依次审批。' }
] as const

const approverTypeOptions = [
  { label: '用户', value: APPROVER_TYPES.USER },
  { label: '部门', value: APPROVER_TYPES.DEPT },
  { label: '角色', value: APPROVER_TYPES.ROLE },
  { label: '岗位', value: APPROVER_TYPES.POSITION }
] as const

const operatorOptions = ['=', '!=', '>', '>=', '<', '<=', 'contains']

const APPROVER_TYPE_TO_RECEIVER_TYPE: Record<number, string> = {
  [APPROVER_TYPES.USER]: 'USER',
  [APPROVER_TYPES.DEPT]: 'DEPT',
  [APPROVER_TYPES.ROLE]: 'ROLE',
  [APPROVER_TYPES.POSITION]: 'POSITION'
}

const RECEIVER_TYPE_TO_APPROVER_TYPE: Record<string, number> = {
  USER: APPROVER_TYPES.USER,
  DEPT: APPROVER_TYPES.DEPT,
  ROLE: APPROVER_TYPES.ROLE,
  POSITION: APPROVER_TYPES.POSITION
}

const route = useRoute()
const router = useRouter()
const { screenToFlowCoordinate, setCenter } = useVueFlow()

const designerRootRef = ref<HTMLElement | null>(null)
const editorContext = ref<WfTaskDraftEditorDTO | null>(null)
const flowNodes = ref<FlowNode[]>([])
const flowEdges = ref<FlowEdge[]>([])
const selectedNodeKey = ref('')
const selectedEdgeId = ref('')
const saving = ref(false)
const publishing = ref(false)
const approverDialogOpen = ref(false)
const approverDialogIndex = ref<number | null>(null)
const approverDialogModel = ref<ReceiverModel>({ receiverType: undefined, receiverIds: [] })
const silentErrorConfig = { silentError: true }
const flowConnectionColor = ref('#2563eb')
const flowPatternColor = ref('#d7deeb')
const contextMenu = reactive<ContextMenuState>({
  visible: false,
  kind: 'pane',
  x: 0,
  y: 0
})
const approverLabelMaps = reactive<Record<number, Record<number, string>>>({
  [APPROVER_TYPES.USER]: {},
  [APPROVER_TYPES.DEPT]: {},
  [APPROVER_TYPES.ROLE]: {},
  [APPROVER_TYPES.POSITION]: {}
})
const approverOptionsLoaded = reactive<Record<number, boolean>>({
  [APPROVER_TYPES.USER]: false,
  [APPROVER_TYPES.DEPT]: false,
  [APPROVER_TYPES.ROLE]: false,
  [APPROVER_TYPES.POSITION]: false
})
const approverOptionsLoading = reactive<Record<number, boolean>>({
  [APPROVER_TYPES.USER]: false,
  [APPROVER_TYPES.DEPT]: false,
  [APPROVER_TYPES.ROLE]: false,
  [APPROVER_TYPES.POSITION]: false
})

let themeObserver: MutationObserver | null = null

const connectionLineStyle = computed(() => ({
  stroke: flowConnectionColor.value,
  strokeWidth: 2
}))
const selectedNode = computed(() => flowNodes.value.find(node => String(node.id) === selectedNodeKey.value) || null)
const selectedNodeData = computed(() => (selectedNode.value?.data as WorkflowNodeData | undefined) || null)
const selectedEdgeData = computed(() => flowEdges.value.find(edge => String(edge.id) === selectedEdgeId.value) || null)
const canDeleteNode = computed(() =>
  Boolean(selectedNodeData.value && [NODE_TYPES.APPROVE, NODE_TYPES.BRANCH].includes(selectedNodeData.value.nodeType))
)
const selectedOutgoingNodeKeys = computed(() => {
  if (!selectedNodeKey.value) {
    return []
  }
  return flowEdges.value
    .filter(edge => String(edge.source) === selectedNodeKey.value)
    .map(edge => String(edge.target))
})
const branchTargetOptions = computed(() => {
  const allowedTargets = new Set(selectedOutgoingNodeKeys.value)
  return flowNodes.value.filter(node =>
    allowedTargets.has(String(node.id)) && (node.data as WorkflowNodeData).nodeType !== NODE_TYPES.START
  )
})
const contextMenuNodeCanDelete = computed(() => {
  if (contextMenu.kind !== 'node' || !contextMenu.nodeId) {
    return false
  }
  const node = flowNodes.value.find(item => String(item.id) === contextMenu.nodeId)
  const nodeType = (node?.data as WorkflowNodeData | undefined)?.nodeType
  return nodeType === NODE_TYPES.APPROVE || nodeType === NODE_TYPES.BRANCH
})

function taskCode() {
  return String(route.params.taskCode || '').trim()
}

function listPagePath() {
  const from = route.query.from
  if (Array.isArray(from)) {
    return normalizeApprovalRoutePath(from[0], approvalRoutePaths.taskConfigList)
  }
  return normalizeApprovalRoutePath(from, approvalRoutePaths.taskConfigList)
}

function navigateBackToList() {
  router.push(listPagePath())
}

function clone<T>(value: T): T {
  return JSON.parse(JSON.stringify(value))
}

function normalizeSelectValue(value: unknown) {
  const nextValue = String(value || '').trim()
  return nextValue || undefined
}

function createEdgeId(sourceNodeKey: string, targetNodeKey: string) {
  return `${sourceNodeKey}->${targetNodeKey}`
}

function nodeKind(nodeType: number) {
  if (nodeType === NODE_TYPES.START) return 'start'
  if (nodeType === NODE_TYPES.END) return 'end'
  if (nodeType === NODE_TYPES.APPROVE) return 'approve'
  return 'branch'
}

function nodeTypeLabel(nodeType: number) {
  if (nodeType === NODE_TYPES.START) return '开始'
  if (nodeType === NODE_TYPES.END) return '结束'
  if (nodeType === NODE_TYPES.APPROVE) return '审批'
  return '条件分支'
}

function approverTypeLabel(approverType?: number) {
  return approverTypeOptions.find(item => item.value === approverType)?.label || '审批对象'
}

function approveTypeDescription(approveType?: number) {
  return approveTypeOptions.find(item => item.value === approveType)?.description || '请选择审批类型。'
}

function nodeSummary(data: WorkflowNodeData) {
  if (data.nodeType === NODE_TYPES.START) {
    return '流程入口，不能有入线。'
  }
  if (data.nodeType === NODE_TYPES.END) {
    return '流程结束节点，不能有出线。'
  }
  if (data.nodeType === NODE_TYPES.APPROVE) {
    const approveType = approveTypeOptions.find(item => item.value === data.approveType)?.label || '未配置'
    const approverCount = data.approvers.filter(item => item.approverType != null && item.approverIds?.length).length
    return `审批类型：${approveType}，审批人来源：${approverCount} 组`
  }
  return `规则 ${data.branchRules.length} 条，默认去向：${data.defaultBranchNodeKey || '待配置'}`
}

function createNodeData(node: Partial<WfTaskNodeEditorDTO> & { nodeType: number; nodeKey: string }): WorkflowNodeData {
  return {
    nodeKey: node.nodeKey,
    nodeType: node.nodeType,
    nodeName: node.nodeName || nodeTypeLabel(node.nodeType),
    approveType: node.approveType,
    canvasX: node.canvasX ?? 0,
    canvasY: node.canvasY ?? 0,
    defaultBranchNodeKey: node.defaultBranchNodeKey,
    approvers: clone(node.approvers || []),
    branchRules: clone(node.branchRules || [])
  }
}

function createFlowNode(node: WfTaskNodeEditorDTO): FlowNode {
  return {
    id: node.nodeKey,
    type: 'workflow',
    position: { x: node.canvasX || 0, y: node.canvasY || 0 },
    selected: false,
    data: createNodeData(node)
  }
}

function createFlowEdge(edge: WfTaskEdgeDTO): FlowEdge {
  return {
    id: edge.id || createEdgeId(edge.sourceNodeKey, edge.targetNodeKey),
    source: edge.sourceNodeKey,
    target: edge.targetNodeKey,
    type: 'smoothstep',
    selectable: true,
    focusable: true,
    markerEnd: MarkerType.ArrowClosed,
    style: {
      stroke: flowConnectionColor.value,
      strokeWidth: 2
    }
  }
}

function syncNodeSelection() {
  flowNodes.value = flowNodes.value.map(node => ({
    ...node,
    selected: String(node.id) === selectedNodeKey.value
  }))
}

function syncEdgePresentation() {
  flowEdges.value = flowEdges.value.map(edge => {
    const isSelected = String(edge.id) === selectedEdgeId.value
    return {
      ...edge,
      type: 'smoothstep',
      selectable: true,
      focusable: true,
      markerEnd: MarkerType.ArrowClosed,
      selected: isSelected,
      style: {
        ...(edge.style || {}),
        stroke: flowConnectionColor.value,
        strokeWidth: isSelected ? 3 : 2,
        opacity: isSelected ? 1 : 0.92
      }
    }
  })
}

function selectNode(nodeKey: string) {
  selectedNodeKey.value = nodeKey
  selectedEdgeId.value = ''
  syncNodeSelection()
  syncEdgePresentation()
}

function selectEdge(edgeId: string) {
  selectedEdgeId.value = edgeId
  selectedNodeKey.value = ''
  syncNodeSelection()
  syncEdgePresentation()
}

function clearSelection() {
  selectedNodeKey.value = ''
  selectedEdgeId.value = ''
  syncNodeSelection()
  syncEdgePresentation()
}

function resolveThemeHost() {
  return (
    designerRootRef.value?.closest('.fx-main-layout') ||
    designerRootRef.value ||
    document.documentElement
  ) as HTMLElement
}

function readThemeVar(name: string, 降级方案: string) {
  const value = getComputedStyle(resolveThemeHost()).getPropertyValue(name).trim()
  return value || 降级方案
}

function refreshFlowThemeColors() {
  flowConnectionColor.value = readThemeVar('--fx-primary', '#2563eb')
  flowPatternColor.value = readThemeVar('--fx-border-color', '#d7deeb')
  syncEdgePresentation()
}

function observeThemeChanges() {
  refreshFlowThemeColors()
  if (typeof MutationObserver === 'undefined') {
    return
  }

  themeObserver?.disconnect()
  themeObserver = new MutationObserver(() => {
    refreshFlowThemeColors()
  })

  const themeHost = resolveThemeHost()
  themeObserver.observe(themeHost, {
    attributes: true,
    attributeFilter: ['style', 'class', 'data-theme']
  })

  if (themeHost !== document.documentElement) {
    themeObserver.observe(document.documentElement, {
      attributes: true,
      attributeFilter: ['style', 'class', 'data-theme']
    })
  }
}

function buildDefaultGraph() {
  flowNodes.value = [
    createFlowNode({
      nodeKey: 'start',
      nodeType: NODE_TYPES.START,
      nodeName: '开始',
      canvasX: 220,
      canvasY: 120,
      approvers: [],
      branchRules: []
    }),
    createFlowNode({
      nodeKey: 'end',
      nodeType: NODE_TYPES.END,
      nodeName: '结束',
      canvasX: 220,
      canvasY: 420,
      approvers: [],
      branchRules: []
    })
  ]
  flowEdges.value = [createFlowEdge({ sourceNodeKey: 'start', targetNodeKey: 'end' })]
  selectNode('start')
}

async function ensureApproverOptionsLoaded(approverType?: number) {
  if (
    !approverType ||
    approverOptionsLoaded[approverType] ||
    approverOptionsLoading[approverType]
  ) {
    return
  }

  approverOptionsLoading[approverType] = true
  try {
    const labelMap: Record<number, string> = {}

    if (approverType === APPROVER_TYPES.USER) {
      const result = await getUserList({ pageNum: 1, pageSize: 1000 })
      const records = Array.isArray(result?.records) ? result.records : []
      records.forEach((item: any) => {
        const id = Number(item.id)
        if (Number.isFinite(id)) {
          labelMap[id] = item.username ? `${item.username}${item.account ? `(${item.account})` : ''}` : `用户 ${id}`
        }
      })
    } else if (approverType === APPROVER_TYPES.DEPT) {
      const result = await listDepartments({})
      const records = Array.isArray(result) ? result : []
      records.forEach((item: any) => {
        const id = Number(item.id)
        if (Number.isFinite(id)) {
          labelMap[id] = item.deptName || `部门 ${id}`
        }
      })
    } else if (approverType === APPROVER_TYPES.ROLE) {
      const result = await getRoleList({})
      const records = Array.isArray(result) ? result : []
      records.forEach((item: any) => {
        const id = Number(item.id)
        if (Number.isFinite(id)) {
          labelMap[id] = item.roleName || `角色 ${id}`
        }
      })
    } else if (approverType === APPROVER_TYPES.POSITION) {
      const result = await listPositions({})
      const records = Array.isArray(result) ? result : []
      records.forEach((item: any) => {
        const id = Number(item.id)
        if (Number.isFinite(id)) {
          labelMap[id] = item.positionName || `岗位 ${id}`
        }
      })
    }

    approverLabelMaps[approverType] = labelMap
    approverOptionsLoaded[approverType] = true
  } catch (error) {
    console.error('加载审批对象名称失败:', error)
    message.warning(`加载${approverTypeLabel(approverType)}名称失败，将先显示 ID。`)
  } finally {
    approverOptionsLoading[approverType] = false
  }
}

async function warmUpApproverLabels(nodes: WfTaskNodeEditorDTO[]) {
  const approverTypes = new Set<number>()
  nodes.forEach(node => {
    node.approvers?.forEach(approver => {
      if (approver.approverType != null && approver.approverIds?.length) {
        approverTypes.add(approver.approverType)
      }
    })
  })
  await Promise.all(Array.from(approverTypes).map(item => ensureApproverOptionsLoaded(item)))
}

function approverDisplayLabels(approver: WfNodeApproverDTO) {
  const labelMap = approverLabelMaps[approver.approverType || APPROVER_TYPES.USER] || {}
  const names = approver.approverIds.map(id => labelMap[id] || `ID:${id}`)
  if (names.length <= 3) {
    return names
  }
  return [...names.slice(0, 3), `+${names.length - 3}`]
}

function edgeNodeName(nodeKey: string) {
  return (flowNodes.value.find(node => String(node.id) === nodeKey)?.data as WorkflowNodeData | undefined)?.nodeName || nodeKey
}

function buildApproverDialogModel(approver: WfNodeApproverDTO): ReceiverModel {
  return {
    receiverType: APPROVER_TYPE_TO_RECEIVER_TYPE[approver.approverType || APPROVER_TYPES.USER],
    receiverIds: approver.approverIds.map(id => String(id))
  }
}

function sanitizeBranchReferences() {
  const outgoingTargets = flowEdges.value.reduce<Record<string, Set<string>>>((acc, edge) => {
    const source = String(edge.source)
    const target = String(edge.target)
    if (!acc[source]) {
      acc[source] = new Set<string>()
    }
    acc[source].add(target)
    return acc
  }, {})

  flowNodes.value = flowNodes.value.map(node => {
    const data = clone(node.data as WorkflowNodeData)
    if (data.nodeType !== NODE_TYPES.BRANCH) {
      return node
    }

    const allowedTargets = outgoingTargets[String(node.id)] || new Set<string>()
    if (data.defaultBranchNodeKey && !allowedTargets.has(data.defaultBranchNodeKey)) {
      data.defaultBranchNodeKey = undefined
    }
    data.branchRules = data.branchRules.map(rule => ({
      ...rule,
      nextNodeKey: rule.nextNodeKey && allowedTargets.has(rule.nextNodeKey) ? rule.nextNodeKey : ''
    }))
    return { ...node, data }
  })
}

async function loadDesigner() {
  const currentTaskCode = taskCode()
  if (!currentTaskCode) {
    message.error('缺少任务编码，无法加载设计器。')
    navigateBackToList()
    return
  }

  try {
    editorContext.value = await getOrCreateDraftEditor({ taskCode: currentTaskCode }, silentErrorConfig)
    const graph = await getDraftGraph({ taskCode: currentTaskCode }, silentErrorConfig)
    if (graph.nodes?.length) {
      flowNodes.value = graph.nodes.map(createFlowNode)
      flowEdges.value = graph.edges.map(createFlowEdge)
      selectNode(graph.nodes[0]?.nodeKey || '')
      await warmUpApproverLabels(graph.nodes)
    } else {
      buildDefaultGraph()
    }
    syncEdgePresentation()
  } catch (error: any) {
    message.error(error.message || '加载设计器失败。')
    navigateBackToList()
  }
}

function handleDragStart(event: DragEvent, nodeType: number) {
  event.dataTransfer?.setData('application/workflow-node-type', String(nodeType))
  event.dataTransfer!.effectAllowed = 'move'
}

function createNodeKey(prefix: string) {
  return `${prefix}_${Date.now()}_${Math.random().toString(36).slice(2, 6)}`
}

function buildNewNode(nodeType: number, x: number, y: number) {
  const nodeKey = nodeType === NODE_TYPES.APPROVE ? createNodeKey('approve') : createNodeKey('branch')
  return createFlowNode({
    nodeKey,
    nodeType,
    nodeName: nodeType === NODE_TYPES.APPROVE ? '审批节点' : '条件分支',
    canvasX: x,
    canvasY: y,
    approveType: nodeType === NODE_TYPES.APPROVE ? 2 : undefined,
    approvers: nodeType === NODE_TYPES.APPROVE ? [{ approverType: APPROVER_TYPES.USER, approverIds: [] }] : [],
    branchRules: nodeType === NODE_TYPES.BRANCH ? [] : []
  })
}

async function appendNewNode(nodeType: number, x: number, y: number) {
  const node = buildNewNode(nodeType, x, y)
  flowNodes.value = [...flowNodes.value, node]
  selectNode(String(node.id))
  await nextTick()
  await focusNode(String(node.id))
}

function handleDrop(event: DragEvent) {
  const nodeType = Number(event.dataTransfer?.getData('application/workflow-node-type'))
  if (![NODE_TYPES.APPROVE, NODE_TYPES.BRANCH].includes(nodeType)) {
    return
  }

  const position = screenToFlowCoordinate({
    x: event.clientX,
    y: event.clientY
  })
  void appendNewNode(nodeType, position.x, position.y)
}

function handleConnect(connection: Connection) {
  if (!connection.source || !connection.target || connection.source === connection.target) {
    return
  }
  if (flowEdges.value.some(edge => edge.source === connection.source && edge.target === connection.target)) {
    return
  }
  if (connection.target === 'start') {
    message.warning('开始节点不能有入线。')
    return
  }
  if (connection.source === 'end') {
    message.warning('结束节点不能有出线。')
    return
  }

  const sourceNodeData = flowNodes.value.find(node => String(node.id) === connection.source)?.data as WorkflowNodeData | undefined
  const sourceOutgoingCount = flowEdges.value.filter(edge => String(edge.source) === connection.source).length
  if (sourceNodeData && sourceNodeData.nodeType !== NODE_TYPES.BRANCH && sourceOutgoingCount >= 1) {
    message.warning('当前节点只能保留一条出线。只有条件分支节点才允许多条出线。')
    return
  }

  flowEdges.value = [
    ...flowEdges.value,
    createFlowEdge({
      sourceNodeKey: connection.source,
      targetNodeKey: connection.target
    })
  ]
  syncEdgePresentation()
}

function handlePaneClick() {
  closeContextMenu()
  clearSelection()
}

function handleNodeClick(payload: NodeMouseEvent) {
  closeContextMenu()
  selectNode(String(payload.node.id))
}

function handleEdgeClick(payload: EdgeMouseEvent) {
  closeContextMenu()
  selectEdge(String(payload.edge.id))
}

function resolveMouseEvent(event: MouseEvent | TouchEvent) {
  return event instanceof MouseEvent ? event : null
}

function openContextMenu(nextState: Partial<ContextMenuState> & { kind: ContextMenuState['kind']; x: number; y: number }) {
  const menuWidth = 220
  const menuHeight = nextState.kind === 'pane' ? 108 : 84
  contextMenu.visible = true
  contextMenu.kind = nextState.kind
  contextMenu.x = Math.min(nextState.x, window.innerWidth - menuWidth)
  contextMenu.y = Math.min(nextState.y, window.innerHeight - menuHeight)
  contextMenu.nodeId = nextState.nodeId
  contextMenu.edgeId = nextState.edgeId
  contextMenu.flowX = nextState.flowX
  contextMenu.flowY = nextState.flowY
}

function closeContextMenu() {
  contextMenu.visible = false
  contextMenu.nodeId = undefined
  contextMenu.edgeId = undefined
  contextMenu.flowX = undefined
  contextMenu.flowY = undefined
}

function handleNodeContextMenu(payload: NodeMouseEvent) {
  const mouseEvent = resolveMouseEvent(payload.event)
  if (!mouseEvent) {
    return
  }
  mouseEvent.preventDefault()
  selectNode(String(payload.node.id))
  openContextMenu({
    kind: 'node',
    nodeId: String(payload.node.id),
    x: mouseEvent.clientX,
    y: mouseEvent.clientY
  })
}

function handleEdgeContextMenu(payload: EdgeMouseEvent) {
  const mouseEvent = resolveMouseEvent(payload.event)
  if (!mouseEvent) {
    return
  }
  mouseEvent.preventDefault()
  selectEdge(String(payload.edge.id))
  openContextMenu({
    kind: 'edge',
    edgeId: String(payload.edge.id),
    x: mouseEvent.clientX,
    y: mouseEvent.clientY
  })
}

function handlePaneContextMenu(event: MouseEvent) {
  event.preventDefault()
  clearSelection()
  const position = screenToFlowCoordinate({
    x: event.clientX,
    y: event.clientY
  })
  openContextMenu({
    kind: 'pane',
    x: event.clientX,
    y: event.clientY,
    flowX: position.x,
    flowY: position.y
  })
}

function updateSelectedNodeData(patch: Partial<WorkflowNodeData>) {
  if (!selectedNode.value) {
    return
  }

  flowNodes.value = flowNodes.value.map(node =>
    String(node.id) === String(selectedNode.value!.id)
      ? { ...node, data: { ...(node.data as WorkflowNodeData), ...patch } }
      : node
  )
}

function addApprover() {
  const current = selectedNodeData.value
  if (!current) {
    return
  }
  updateSelectedNodeData({
    approvers: [...current.approvers, { approverType: APPROVER_TYPES.USER, approverIds: [] }]
  })
}

function updateApprover(index: number, patch: Partial<WfNodeApproverDTO>) {
  const current = selectedNodeData.value
  if (!current) {
    return
  }

  const approvers = clone(current.approvers)
  approvers[index] = {
    ...approvers[index],
    ...patch
  }
  updateSelectedNodeData({ approvers })
}

function updateApproverType(index: number, approverType: number) {
  updateApprover(index, { approverType, approverIds: [] })
  void ensureApproverOptionsLoaded(approverType)
}

function removeApprover(index: number) {
  const current = selectedNodeData.value
  if (!current) {
    return
  }
  updateSelectedNodeData({
    approvers: current.approvers.filter((_, itemIndex) => itemIndex !== index)
  })
}

function openApproverDialog(index: number) {
  const current = selectedNodeData.value
  if (!current) {
    return
  }
  const approver = current.approvers[index]
  approverDialogIndex.value = index
  approverDialogModel.value = buildApproverDialogModel(approver)
  approverDialogOpen.value = true
  void ensureApproverOptionsLoaded(approver.approverType)
}

function closeApproverDialog() {
  approverDialogOpen.value = false
  approverDialogIndex.value = null
  approverDialogModel.value = { receiverType: undefined, receiverIds: [] }
}

function handleApproverDialogOk() {
  if (approverDialogIndex.value == null) {
    closeApproverDialog()
    return
  }

  const approverType = RECEIVER_TYPE_TO_APPROVER_TYPE[approverDialogModel.value.receiverType || '']
  const approverIds = approverDialogModel.value.receiverIds
    .map(item => Number(item))
    .filter(item => Number.isFinite(item) && item > 0)

  if (!approverType) {
    message.warning('请先选择审批人来源类型。')
    return
  }
  if (!approverIds.length) {
    message.warning('请至少选择一个审批对象。')
    return
  }

  updateApprover(approverDialogIndex.value, {
    approverType,
    approverIds
  })
  void ensureApproverOptionsLoaded(approverType)
  closeApproverDialog()
}

function addBranchRule() {
  const current = selectedNodeData.value
  if (!current) {
    return
  }
  updateSelectedNodeData({
    branchRules: [
      ...current.branchRules,
      { fieldKey: '', fieldLabel: '', operator: '=', value: '', nextNodeKey: '' }
    ]
  })
}

function updateBranchRule(index: number, patch: Partial<WfBranchRuleDTO>) {
  const current = selectedNodeData.value
  if (!current) {
    return
  }
  const branchRules = clone(current.branchRules)
  branchRules[index] = { ...branchRules[index], ...patch }
  updateSelectedNodeData({ branchRules })
}

function removeBranchRule(index: number) {
  const current = selectedNodeData.value
  if (!current) {
    return
  }
  updateSelectedNodeData({
    branchRules: current.branchRules.filter((_, itemIndex) => itemIndex !== index)
  })
}

function deleteNode(nodeKey: string) {
  const node = flowNodes.value.find(item => String(item.id) === nodeKey)
  const nodeType = (node?.data as WorkflowNodeData | undefined)?.nodeType
  if (!node || ![NODE_TYPES.APPROVE, NODE_TYPES.BRANCH].includes(nodeType || -1)) {
    message.warning('开始和结束节点为系统内置节点，不能删除。')
    return
  }

  flowNodes.value = flowNodes.value.filter(item => String(item.id) !== nodeKey)
  flowEdges.value = flowEdges.value.filter(edge => String(edge.source) !== nodeKey && String(edge.target) !== nodeKey)
  sanitizeBranchReferences()
  clearSelection()
  closeContextMenu()
}

function deleteEdge(edgeId: string) {
  flowEdges.value = flowEdges.value.filter(edge => String(edge.id) !== edgeId)
  sanitizeBranchReferences()
  clearSelection()
  closeContextMenu()
}

function handleDeleteSelectedNode() {
  if (!selectedNodeKey.value) {
    return
  }
  deleteNode(selectedNodeKey.value)
}

function handleDeleteSelectedEdge() {
  if (!selectedEdgeId.value) {
    return
  }
  deleteEdge(selectedEdgeId.value)
}

async function focusNode(nodeKey: string) {
  const node = flowNodes.value.find(item => String(item.id) === nodeKey)
  if (!node) {
    return
  }
  selectNode(nodeKey)
  const width = (node as any).dimensions?.width || 220
  const height = (node as any).dimensions?.height || 120
  try {
    await setCenter(node.position.x + width / 2, node.position.y + height / 2, { duration: 260, zoom: 1 })
  } catch (error) {
    console.warn('聚焦节点失败:', error)
  }
}

function focusSystemNode(nodeKey: 'start' | 'end') {
  void focusNode(nodeKey)
}

function handleContextMenuAction(action: 'createApprove' | 'createBranch' | 'editNode' | 'deleteNode' | 'deleteEdge') {
  if (action === 'createApprove' || action === 'createBranch') {
    if (contextMenu.flowX == null || contextMenu.flowY == null) {
      closeContextMenu()
      return
    }
    const nodeType = action === 'createApprove' ? NODE_TYPES.APPROVE : NODE_TYPES.BRANCH
    void appendNewNode(nodeType, contextMenu.flowX, contextMenu.flowY)
    closeContextMenu()
    return
  }

  if (action === 'editNode') {
    if (contextMenu.nodeId) {
      void focusNode(contextMenu.nodeId)
    }
    closeContextMenu()
    return
  }

  if (action === 'deleteNode' && contextMenu.nodeId) {
    deleteNode(contextMenu.nodeId)
    return
  }

  if (action === 'deleteEdge' && contextMenu.edgeId) {
    deleteEdge(contextMenu.edgeId)
  }
}

function buildSavePayload(): WfTaskNodeEditorDTO[] {
  return flowNodes.value.map(node => {
    const data = node.data as WorkflowNodeData
    return {
      nodeKey: String(node.id),
      nodeType: data.nodeType,
      nodeName: data.nodeName,
      approveType: data.approveType,
      canvasX: Number(node.position.x),
      canvasY: Number(node.position.y),
      defaultBranchNodeKey: data.defaultBranchNodeKey,
      approvers: clone(data.approvers),
      branchRules: clone(data.branchRules)
    }
  })
}

async function handleSave(showSuccessMessage = true) {
  if (!editorContext.value) {
    return false
  }
  try {
    saving.value = true
    await saveDraftGraph({
      draftId: editorContext.value.draftId,
      nodes: buildSavePayload(),
      edges: flowEdges.value.map(edge => ({
        id: String(edge.id),
        sourceNodeKey: String(edge.source),
        targetNodeKey: String(edge.target)
      }))
    }, { showSuccessMessage })
    return true
  } catch (error: any) {
    console.error('保存草稿失败。', error)
    return false
  } finally {
    saving.value = false
  }
}

async function handlePublish() {
  if (!editorContext.value) {
    return
  }
  try {
    publishing.value = true
    const saved = await handleSave(false)
    if (!saved) {
      return
    }
    await publishDraft({ taskCode: editorContext.value.taskCode })
    navigateBackToList()
  } catch (error: any) {
    console.error('发布失败。', error)
  } finally {
    publishing.value = false
  }
}

function isEditableTarget(target: EventTarget | null) {
  if (!(target instanceof HTMLElement)) {
    return false
  }
  if (target.isContentEditable) {
    return true
  }
  const tagName = target.tagName
  if (['INPUT', 'TEXTAREA', 'SELECT'].includes(tagName)) {
    return true
  }
  return Boolean(
    target.closest(
      '.ant-input, .ant-input-affix-wrapper, .ant-select, .ant-select-dropdown, .ant-picker, .ant-modal, .ant-form-item'
    )
  )
}

function handleWindowKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    closeContextMenu()
    return
  }

  if (!['Delete', 'Backspace'].includes(event.key) || isEditableTarget(event.target)) {
    return
  }

  if (selectedEdgeId.value) {
    event.preventDefault()
    handleDeleteSelectedEdge()
    return
  }

  if (selectedNodeKey.value) {
    event.preventDefault()
    handleDeleteSelectedNode()
  }
}

function handleWindowClick() {
  closeContextMenu()
}

function handleWindowContextMenu() {
  closeContextMenu()
}

function handleWindowResize() {
  closeContextMenu()
}

function handleWindowWheel() {
  closeContextMenu()
}

onMounted(async () => {
  await nextTick()
  observeThemeChanges()
  await loadDesigner()
  window.addEventListener('keydown', handleWindowKeydown)
  window.addEventListener('click', handleWindowClick)
  window.addEventListener('resize', handleWindowResize)
  window.addEventListener('wheel', handleWindowWheel, { passive: true })
  document.addEventListener('contextmenu', handleWindowContextMenu, true)
})

onBeforeUnmount(() => {
  themeObserver?.disconnect()
  themeObserver = null
  window.removeEventListener('keydown', handleWindowKeydown)
  window.removeEventListener('click', handleWindowClick)
  window.removeEventListener('resize', handleWindowResize)
  window.removeEventListener('wheel', handleWindowWheel)
  document.removeEventListener('contextmenu', handleWindowContextMenu, true)
})
</script>

<style scoped>
.designer-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: calc(100vh - 120px);
  padding: 16px;
  color: var(--fx-text-primary, #172033);
}

.designer-topbar,
.palette-panel,
.config-panel,
.canvas-panel {
  border-radius: var(--fx-radius-lg, 22px);
  border: 1px solid var(--fx-border-color, #eceef5);
  background: var(--fx-bg-container, #ffffff);
  box-shadow: var(--fx-shadow-secondary, 0 18px 40px rgba(15, 23, 42, 0.06));
}

.designer-topbar {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  padding: 22px 24px;
}

.designer-topbar__back {
  padding: 0;
}

.designer-topbar h2 {
  margin: 8px 0 4px;
  font-size: 28px;
  color: var(--fx-text-primary, #172033);
}

.designer-topbar p {
  margin: 0;
  color: var(--fx-text-secondary, #6b7280);
}

.designer-shell {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr) 380px;
  gap: 16px;
  min-height: 0;
  flex: 1;
}

.palette-panel,
.config-panel {
  padding: 18px;
  overflow: auto;
}

.canvas-panel {
  min-height: 640px;
  overflow: hidden;
  background:
    radial-gradient(circle at top left, color-mix(in srgb, var(--fx-primary, #1677ff) 10%, transparent), transparent 38%),
    linear-gradient(180deg, var(--fx-bg-elevated, #ffffff), var(--fx-fill-secondary, #f7f9fc));
}

.designer-flow {
  width: 100%;
  height: 100%;
  min-height: 640px;
}

.panel-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--fx-text-primary, #172033);
}

.panel-subtitle {
  margin-bottom: 12px;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.04em;
  color: var(--fx-text-secondary, #64748b);
  text-transform: uppercase;
}

.panel-desc {
  margin: 10px 0 16px;
  line-height: 1.6;
  color: var(--fx-text-secondary, #6b7280);
}

.panel-hint {
  margin-top: 10px;
  line-height: 1.6;
  color: var(--fx-text-secondary, #6b7280);
}

.system-node-item,
.palette-item {
  display: flex;
  gap: 14px;
  width: 100%;
  padding: 16px;
  margin-bottom: 12px;
  border: 1px dashed var(--fx-border-color, #d4dcf0);
  border-radius: 16px;
  background: linear-gradient(180deg, var(--fx-bg-elevated, #fbfcff), var(--fx-fill-secondary, #f3f7fd));
  transition: border-color 0.2s ease, transform 0.2s ease, box-shadow 0.2s ease;
}

.system-node-item {
  cursor: pointer;
  text-align: left;
}

.palette-item {
  cursor: grab;
}

.system-node-item:hover,
.palette-item:hover {
  border-color: var(--fx-primary, #1677ff);
  box-shadow: var(--fx-shadow-secondary, 0 12px 24px rgba(15, 23, 42, 0.08));
  transform: translateY(-1px);
}

.system-node-item--active {
  border-color: var(--fx-primary, #1677ff);
  box-shadow: 0 0 0 1px color-mix(in srgb, var(--fx-primary, #1677ff) 25%, transparent);
}

.palette-item__badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 48px;
  height: 48px;
  border-radius: 14px;
  color: #fff;
  font-weight: 700;
}

.palette-item__badge--start {
  background: linear-gradient(135deg, var(--fx-success, #16a34a), color-mix(in srgb, var(--fx-success, #16a34a) 65%, #4ade80));
}

.palette-item__badge--end {
  background: linear-gradient(135deg, var(--fx-text-secondary, #64748b), color-mix(in srgb, var(--fx-text-secondary, #64748b) 55%, #cbd5e1));
}

.palette-item__badge--approve {
  background: linear-gradient(135deg, var(--fx-primary, #2563eb), color-mix(in srgb, var(--fx-primary, #2563eb) 65%, #22d3ee));
}

.palette-item__badge--branch {
  background: linear-gradient(135deg, var(--fx-warning, #f97316), color-mix(in srgb, var(--fx-warning, #f97316) 70%, #facc15));
}

.palette-item__title {
  font-size: 15px;
  font-weight: 700;
  color: var(--fx-text-primary, #172033);
}

.palette-item__desc {
  margin-top: 4px;
  color: var(--fx-text-secondary, #6b7280);
  line-height: 1.5;
}

.flow-node-card {
  min-width: 196px;
  padding: 18px 18px 20px;
  border: 1px solid var(--fx-border-color, #dbe4f0);
  border-radius: 18px;
  background: var(--fx-bg-container, #ffffff);
  box-shadow: var(--fx-shadow-secondary, 0 12px 24px rgba(15, 23, 42, 0.08));
}

.flow-node-card--selected {
  border-color: var(--fx-primary, #2563eb);
  box-shadow: 0 0 0 1px color-mix(in srgb, var(--fx-primary, #2563eb) 45%, transparent), var(--fx-shadow-secondary, 0 14px 28px rgba(37, 99, 235, 0.18));
}

.flow-node-card__head {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.flow-node-card__type {
  display: inline-flex;
  width: fit-content;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
}

.flow-node-card__type--start {
  background: var(--fx-success, #16a34a);
}

.flow-node-card__type--end {
  background: var(--fx-text-secondary, #64748b);
}

.flow-node-card__type--approve {
  background: var(--fx-primary, #2563eb);
}

.flow-node-card__type--branch {
  background: var(--fx-warning, #f97316);
}

.flow-node-card strong {
  color: var(--fx-text-primary, #172033);
}

.flow-node-card p {
  margin: 12px 0 0;
  line-height: 1.6;
  color: var(--fx-text-secondary, #6b7280);
}

.node-handle {
  width: 12px;
  height: 12px;
  background: var(--fx-primary, #2563eb);
  border: 2px solid var(--fx-bg-container, #ffffff);
}

.node-handle--target {
  transform: translateY(-1px);
}

.node-handle--source {
  transform: translateY(1px);
}

.field-tip {
  line-height: 1.6;
  color: var(--fx-text-secondary, #6b7280);
}

.editor-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.editor-card {
  display: flex;
  gap: 8px;
  align-items: center;
  padding: 12px;
  border-radius: 14px;
  border: 1px solid var(--fx-border-secondary, var(--fx-border-color, #e8e8e8));
  background: var(--fx-fill-secondary, #f7f9fc);
}

.editor-card--column {
  align-items: stretch;
  flex-direction: column;
}

.approver-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.approver-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.approver-summary__empty {
  color: var(--fx-text-secondary, #6b7280);
}

.selected-edge-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.selected-edge-panel__button {
  margin-top: 4px;
}

.approver-dialog-tip {
  margin-top: 12px;
}

.designer-context-menu {
  position: fixed;
  z-index: 2000;
  min-width: 200px;
  padding: 8px;
  border: 1px solid var(--fx-border-color, #dbe4f0);
  border-radius: 14px;
  background: var(--fx-bg-container, #ffffff);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.18);
}

.designer-context-menu__item {
  width: 100%;
  padding: 10px 12px;
  border: 0;
  border-radius: 10px;
  background: transparent;
  color: var(--fx-text-primary, #172033);
  text-align: left;
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease;
}

.designer-context-menu__item:hover:not(:disabled) {
  background: var(--fx-fill-secondary, #f3f7fd);
  color: var(--fx-primary, #2563eb);
}

.designer-context-menu__item:disabled {
  cursor: not-allowed;
  color: var(--fx-text-tertiary, #94a3b8);
}

.designer-page :deep(.vue-flow__background) {
  background: transparent;
}

.designer-page :deep(.vue-flow__background-pattern.dots),
.designer-page :deep(.vue-flow__background-pattern.lines),
.designer-page :deep(.vue-flow__background-pattern.cross) {
  color: color-mix(in srgb, var(--fx-border-color, #d7deeb) 75%, transparent);
}

.designer-page :deep(.vue-flow__controls) {
  box-shadow: var(--fx-shadow-secondary, 0 8px 18px rgba(15, 23, 42, 0.08));
  border-radius: var(--fx-radius, 12px);
  overflow: hidden;
}

.designer-page :deep(.vue-flow__controls-button) {
  background: var(--fx-bg-elevated, #ffffff);
  border-bottom: 1px solid var(--fx-border-color, #e8e8e8);
  color: var(--fx-text-primary, #172033);
}

.designer-page :deep(.vue-flow__controls-button:hover) {
  background: var(--fx-fill, #f3f4f6);
}

.designer-page :deep(.vue-flow__minimap) {
  background: color-mix(in srgb, var(--fx-bg-elevated, #ffffff) 92%, transparent);
  border: 1px solid var(--fx-border-color, #e8e8e8);
  border-radius: var(--fx-radius, 12px);
  box-shadow: var(--fx-shadow-secondary, 0 8px 18px rgba(15, 23, 42, 0.08));
}

.designer-page :deep(.vue-flow__minimap-mask) {
  fill: color-mix(in srgb, var(--fx-primary, #1677ff) 10%, transparent);
}

.designer-page :deep(.vue-flow__edge-path) {
  stroke: var(--fx-primary, #2563eb);
}

.designer-page :deep(.vue-flow__edge.selected .vue-flow__edge-path) {
  stroke-width: 3;
}

.designer-page :deep(.vue-flow__edge-interaction) {
  stroke-width: 28;
}

@media (max-width: 1360px) {
  .designer-shell {
    grid-template-columns: 1fr;
  }
}
</style>
