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
        <p class="panel-desc">开始和结束节点默认存在，审批节点与条件分支从左侧拖入画布。</p>

        <div
          class="palette-item"
          draggable="true"
          @dragstart="handleDragStart($event, NODE_TYPES.APPROVE)"
        >
          <span class="palette-item__badge palette-item__badge--approve">审批</span>
          <div>
            <div class="palette-item__title">审批节点</div>
            <div class="palette-item__desc">配置审批类型与审批人来源</div>
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
            <div class="palette-item__desc">配置规则去向和默认分支</div>
          </div>
        </div>

        <a-divider />

        <a-button block :disabled="!canDeleteNode" @click="handleDeleteSelected">删除当前节点</a-button>
      </aside>

      <div class="canvas-panel" @dragover.prevent @drop="handleDrop">
        <VueFlow
          v-model:nodes="flowNodes"
          v-model:edges="flowEdges"
          class="designer-flow"
          :fit-view-on-init="true"
          :connection-line-style="connectionLineStyle"
          @connect="handleConnect"
          @node-click="handleNodeClick"
          @pane-click="selectedNodeKey = ''"
        >
          <Background :pattern-color="flowPatternColor" :gap="20" />
          <MiniMap pannable zoomable />
          <Controls />

          <template #node-workflow="{ data, selected }">
            <Handle
              v-if="data.nodeType !== NODE_TYPES.START"
              type="target"
              :position="Position.Left"
              class="node-handle"
            />
            <div class="flow-node-card" :class="{ 'flow-node-card--selected': selected }">
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
              :position="Position.Right"
              class="node-handle"
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

            <template v-if="selectedNodeData.nodeType === NODE_TYPES.APPROVE">
              <a-form-item label="审批类型">
                <a-select
                  :value="selectedNodeData.approveType"
                  @update:value="updateSelectedNodeData({ approveType: Number($event) })"
                >
                  <a-select-option v-for="item in approveTypeOptions" :key="item.value" :value="item.value">
                    {{ item.label }}
                  </a-select-option>
                </a-select>
              </a-form-item>

              <a-form-item label="审批人来源">
                <div class="editor-list">
                  <div v-for="(approver, index) in selectedNodeData.approvers" :key="index" class="editor-card">
                    <a-select
                      :value="approver.approverType"
                      style="width: 120px"
                      @update:value="updateApprover(index, { approverType: Number($event) })"
                    >
                      <a-select-option v-for="item in approverTypeOptions" :key="item.value" :value="item.value">
                        {{ item.label }}
                      </a-select-option>
                    </a-select>
                    <a-input
                      :value="approver.approverIds.join(',')"
                      placeholder="请输入ID，多个用逗号分隔"
                      @update:value="updateApproverIds(index, String($event || ''))"
                    />
                    <a-button danger @click="removeApprover(index)">删除</a-button>
                  </div>
                </div>
                <a-button type="dashed" block @click="addApprover">新增审批人来源</a-button>
              </a-form-item>
            </template>

            <template v-if="selectedNodeData.nodeType === NODE_TYPES.BRANCH">
              <a-form-item label="默认分支">
                <a-select
                  allow-clear
                  :value="selectedNodeData.defaultBranchNodeKey"
                  @update:value="updateSelectedNodeData({ defaultBranchNodeKey: String($event || '') || undefined })"
                >
                  <a-select-option
                    v-for="node in branchTargetOptions"
                    :key="node.id"
                    :value="String(node.id)"
                  >
                    {{ (node.data as WorkflowNodeData).nodeName }}
                  </a-select-option>
                </a-select>
              </a-form-item>

              <a-form-item label="分支规则">
                <div class="editor-list">
                  <div v-for="(rule, index) in selectedNodeData.branchRules" :key="index" class="editor-card editor-card--column">
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
                          :value="rule.nextNodeKey"
                          placeholder="规则去向"
                          @update:value="updateBranchRule(index, { nextNodeKey: String($event || '') })"
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

        <a-empty v-else description="点击画布中的节点后在这里编辑属性" />
      </aside>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import { MiniMap } from '@vue-flow/minimap'
import { Handle, MarkerType, Position, VueFlow, useVueFlow, type Connection, type Edge, type Node } from '@vue-flow/core'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'
import '@vue-flow/controls/dist/style.css'
import '@vue-flow/minimap/dist/style.css'
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

interface WorkflowNodeData extends WfTaskNodeEditorDTO {}

const NODE_TYPES = {
  START: 1,
  END: 2,
  APPROVE: 3,
  BRANCH: 5
} as const

const approveTypeOptions = [
  { label: '会签', value: 1 },
  { label: '或签', value: 2 },
  { label: '抄送', value: 3 },
  { label: '投票', value: 4 },
  { label: '顺序审批', value: 5 }
]

const approverTypeOptions = [
  { label: '用户', value: 1 },
  { label: '部门', value: 2 },
  { label: '角色', value: 3 },
  { label: '岗位', value: 4 }
]

const operatorOptions = ['=', '!=', '>', '>=', '<', '<=', 'contains']

const route = useRoute()
const router = useRouter()
const { screenToFlowCoordinate } = useVueFlow()

const designerRootRef = ref<HTMLElement | null>(null)
const editorContext = ref<WfTaskDraftEditorDTO | null>(null)
const flowNodes = ref<Node[]>([])
const flowEdges = ref<Edge[]>([])
const selectedNodeKey = ref('')
const saving = ref(false)
const publishing = ref(false)
const silentErrorConfig = { silentError: true }
const flowConnectionColor = ref('#2563eb')
const flowPatternColor = ref('#d7deeb')
const connectionLineStyle = computed(() => ({
  stroke: flowConnectionColor.value,
  strokeWidth: 2
}))

let themeObserver: MutationObserver | null = null

const selectedNode = computed(() => flowNodes.value.find(node => node.id === selectedNodeKey.value) || null)
const selectedNodeData = computed(() => (selectedNode.value?.data as WorkflowNodeData | undefined) || null)
const nodeTypeCounts = computed(() =>
  flowNodes.value.reduce<Record<number, number>>((acc, node) => {
    const nodeType = (node.data as WorkflowNodeData).nodeType
    acc[nodeType] = (acc[nodeType] || 0) + 1
    return acc
  }, {})
)
const branchTargetOptions = computed(() =>
  flowNodes.value.filter(node =>
    node.id !== selectedNodeKey.value && (node.data as WorkflowNodeData).nodeType !== NODE_TYPES.START
  )
)
const canDeleteNode = computed(() => {
  const nodeType = selectedNodeData.value?.nodeType
  if (nodeType === NODE_TYPES.APPROVE || nodeType === NODE_TYPES.BRANCH) {
    return true
  }
  if (nodeType === NODE_TYPES.START) {
    return (nodeTypeCounts.value[NODE_TYPES.START] || 0) > 1
  }
  if (nodeType === NODE_TYPES.END) {
    return (nodeTypeCounts.value[NODE_TYPES.END] || 0) > 1
  }
  return false
})

function taskCode() {
  return String(route.params.taskCode || '').trim()
}

function listPagePath() {
  const from = route.query.from
  if (Array.isArray(from)) {
    return String(from[0] || '/workflow/taskConfig')
  }
  return String(from || '/workflow/taskConfig')
}

function navigateBackToList() {
  router.push(listPagePath())
}

function clone<T>(value: T): T {
  return JSON.parse(JSON.stringify(value))
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

function nodeSummary(data: WorkflowNodeData) {
  if (data.nodeType === NODE_TYPES.START) return '流程入口，不能有入边。'
  if (data.nodeType === NODE_TYPES.END) return '流程结束节点，不能有出边。'
  if (data.nodeType === NODE_TYPES.APPROVE) {
    const approveType = approveTypeOptions.find(item => item.value === data.approveType)?.label || '未配置'
    const approverCount = data.approvers.filter(item => item.approverIds?.length).length
    return `审批类型：${approveType}，审批人来源：${approverCount} 组`
  }
  return `规则 ${data.branchRules.length} 条，默认去向：${data.defaultBranchNodeKey || '未配置'}`
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

function createFlowNode(node: WfTaskNodeEditorDTO): Node {
  return {
    id: node.nodeKey,
    type: 'workflow',
    position: { x: node.canvasX || 0, y: node.canvasY || 0 },
    data: createNodeData(node)
  }
}

function createFlowEdge(edge: WfTaskEdgeDTO): Edge {
  return {
    id: edge.id || `${edge.sourceNodeKey}-${edge.targetNodeKey}`,
    source: edge.sourceNodeKey,
    target: edge.targetNodeKey,
    markerEnd: MarkerType.ArrowClosed,
    style: { stroke: flowConnectionColor.value, strokeWidth: 2 }
  }
}

function resolveThemeHost() {
  return (
    designerRootRef.value?.closest('.fx-main-layout') ||
    designerRootRef.value ||
    document.documentElement
  ) as HTMLElement
}

function readThemeVar(name: string, fallback: string) {
  const value = getComputedStyle(resolveThemeHost()).getPropertyValue(name).trim()
  return value || fallback
}

function refreshFlowThemeColors() {
  flowConnectionColor.value = readThemeVar('--fx-primary', '#2563eb')
  flowPatternColor.value = readThemeVar('--fx-border-color', '#d7deeb')
  flowEdges.value = flowEdges.value.map(edge => ({
    ...edge,
    style: {
      ...(edge.style || {}),
      stroke: flowConnectionColor.value,
      strokeWidth: 2
    }
  }))
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
    createFlowNode({ nodeKey: 'start', nodeType: NODE_TYPES.START, nodeName: '开始', canvasX: 120, canvasY: 220, approvers: [], branchRules: [] }),
    createFlowNode({ nodeKey: 'end', nodeType: NODE_TYPES.END, nodeName: '结束', canvasX: 700, canvasY: 220, approvers: [], branchRules: [] })
  ]
  flowEdges.value = [createFlowEdge({ sourceNodeKey: 'start', targetNodeKey: 'end' })]
  selectedNodeKey.value = 'start'
}

async function loadDesigner() {
  const currentTaskCode = taskCode()
  if (!currentTaskCode) {
    message.error('缺少任务编码，无法加载设计器')
    navigateBackToList()
    return
  }

  try {
    editorContext.value = await getOrCreateDraftEditor({ taskCode: currentTaskCode }, silentErrorConfig)
    const graph = await getDraftGraph({ taskCode: currentTaskCode }, silentErrorConfig)
    if (graph.nodes?.length) {
      flowNodes.value = graph.nodes.map(createFlowNode)
      flowEdges.value = graph.edges.map(createFlowEdge)
      selectedNodeKey.value = graph.nodes[0]?.nodeKey || ''
    } else {
      buildDefaultGraph()
    }
  } catch (error: any) {
    message.error(error.message || '加载设计器失败')
    navigateBackToList()
  }
}

function handleDragStart(event: DragEvent, nodeType: number) {
  event.dataTransfer?.setData('application/workflow-node-type', String(nodeType))
  event.dataTransfer!.effectAllowed = 'move'
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
  const nodeKey = `${nodeType === NODE_TYPES.APPROVE ? 'approve' : 'branch'}_${Date.now()}`
  const node = createFlowNode({
    nodeKey,
    nodeType,
    nodeName: nodeTypeLabel(nodeType),
    canvasX: position.x,
    canvasY: position.y,
    approveType: nodeType === NODE_TYPES.APPROVE ? 2 : undefined,
    approvers: nodeType === NODE_TYPES.APPROVE ? [{ approverType: 1, approverIds: [] }] : [],
    branchRules: nodeType === NODE_TYPES.BRANCH ? [] : []
  })
  flowNodes.value = [...flowNodes.value, node]
  selectedNodeKey.value = node.id
}

function handleConnect(connection: Connection) {
  if (!connection.source || !connection.target || connection.source === connection.target) {
    return
  }
  if (flowEdges.value.some(edge => edge.source === connection.source && edge.target === connection.target)) {
    return
  }
  if (connection.target === 'start') {
    message.warning('开始节点不能有入边')
    return
  }
  if (connection.source === 'end') {
    message.warning('结束节点不能有出边')
    return
  }

  flowEdges.value = [...flowEdges.value, createFlowEdge({
    sourceNodeKey: connection.source,
    targetNodeKey: connection.target
  })]
}

function handleNodeClick(_: MouseEvent, node: Node) {
  selectedNodeKey.value = String(node.id)
}

function updateSelectedNodeData(patch: Partial<WorkflowNodeData>) {
  if (!selectedNode.value) {
    return
  }
  flowNodes.value = flowNodes.value.map(node =>
    node.id === selectedNode.value!.id
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
    approvers: [...current.approvers, { approverType: 1, approverIds: [] }]
  })
}

function updateApprover(index: number, patch: Partial<WfNodeApproverDTO>) {
  const current = selectedNodeData.value
  if (!current) {
    return
  }
  const approvers = clone(current.approvers)
  approvers[index] = { ...approvers[index], ...patch }
  updateSelectedNodeData({ approvers })
}

function updateApproverIds(index: number, value: string) {
  const approverIds = value
    .split(',')
    .map(item => Number(item.trim()))
    .filter(item => Number.isFinite(item) && item > 0)
  updateApprover(index, { approverIds })
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

function handleDeleteSelected() {
  if (!selectedNode.value || !canDeleteNode.value) {
    return
  }
  const targetNodeKey = String(selectedNode.value.id)
  flowNodes.value = flowNodes.value.filter(node => node.id !== targetNodeKey)
  flowEdges.value = flowEdges.value.filter(edge => edge.source !== targetNodeKey && edge.target !== targetNodeKey)
  flowNodes.value = flowNodes.value.map(node => {
    const data = clone(node.data as WorkflowNodeData)
    data.branchRules = data.branchRules.filter(rule => rule.nextNodeKey !== targetNodeKey)
    if (data.defaultBranchNodeKey === targetNodeKey) {
      data.defaultBranchNodeKey = undefined
    }
    return { ...node, data }
  })
  selectedNodeKey.value = ''
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

async function handleSave() {
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
    }, silentErrorConfig)
    message.success('草稿已保存')
    return true
  } catch (error: any) {
    message.error(error.message || '保存草稿失败')
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
    const saved = await handleSave()
    if (!saved) {
      return
    }
    await publishDraft({ taskCode: editorContext.value.taskCode }, silentErrorConfig)
    message.success('发布成功')
    navigateBackToList()
  } catch (error: any) {
    message.error(error.message || '发布失败')
  } finally {
    publishing.value = false
  }
}

onMounted(async () => {
  await nextTick()
  observeThemeChanges()
  loadDesigner()
})

onBeforeUnmount(() => {
  themeObserver?.disconnect()
  themeObserver = null
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
  grid-template-columns: 260px minmax(0, 1fr) 360px;
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

.panel-desc {
  margin: 10px 0 16px;
  line-height: 1.6;
  color: var(--fx-text-secondary, #6b7280);
}

.palette-item {
  display: flex;
  gap: 14px;
  width: 100%;
  padding: 16px;
  margin-bottom: 12px;
  border: 1px dashed var(--fx-border-color, #d4dcf0);
  border-radius: 16px;
  background: linear-gradient(180deg, var(--fx-bg-elevated, #fbfcff), var(--fx-fill-secondary, #f3f7fd));
  cursor: grab;
  transition: border-color 0.2s ease, transform 0.2s ease, box-shadow 0.2s ease;
}

.palette-item:hover {
  border-color: var(--fx-primary, #1677ff);
  box-shadow: var(--fx-shadow-secondary, 0 12px 24px rgba(15, 23, 42, 0.08));
  transform: translateY(-1px);
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
  min-width: 180px;
  padding: 14px 16px;
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

.flow-node-card__type--start { background: var(--fx-success, #16a34a); }
.flow-node-card__type--end { background: var(--fx-text-secondary, #64748b); }
.flow-node-card__type--approve { background: var(--fx-primary, #2563eb); }
.flow-node-card__type--branch { background: var(--fx-warning, #f97316); }

.flow-node-card strong {
  color: var(--fx-text-primary, #172033);
}

.flow-node-card p {
  margin: 10px 0 0;
  line-height: 1.6;
  color: var(--fx-text-secondary, #6b7280);
}

.node-handle {
  width: 10px;
  height: 10px;
  background: var(--fx-primary, #2563eb);
  border: 2px solid var(--fx-bg-container, #ffffff);
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

@media (max-width: 1320px) {
  .designer-shell {
    grid-template-columns: 1fr;
  }
}
</style>
