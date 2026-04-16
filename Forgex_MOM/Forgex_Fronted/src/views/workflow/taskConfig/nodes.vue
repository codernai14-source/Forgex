<template>
  <div ref="designerRootRef" class="designer-page">
    <section class="designer-topbar">
      <div>
        <a-button type="link" class="designer-topbar__back" @click="navigateBackToList()">
          {{ t('workflow.taskConfig.nodes.backToList') }}
        </a-button>
        <h2>{{ editorContext?.taskName || t('workflow.taskConfig.nodes.title') }}</h2>
        <p>
          {{ t('workflow.taskConfig.nodes.draftVersion', { version: editorContext?.draftVersion || '-' }) }}
          <span v-if="editorContext?.publishedVersion">，{{ t('workflow.taskConfig.nodes.publishedVersion', { version: editorContext.publishedVersion }) }}</span>
        </p>
      </div>

      <a-space>
        <a-button v-if="currentStep === 1" :loading="saving" @click="handleSave">{{ t('workflow.taskConfig.nodes.saveDraft') }}</a-button>
        <a-button v-if="currentStep === 1" type="primary" :loading="publishing" @click="handlePublish">{{ t('workflow.taskConfig.nodes.publishDraft') }}</a-button>
      </a-space>
    </section>

    <section class="designer-steps">
      <a-steps :current="currentStep">
        <a-step :title="t('workflow.taskConfig.nodes.steps.formDesign')" :description="t('workflow.taskConfig.nodes.steps.formDesignDesc')" />
        <a-step :title="t('workflow.taskConfig.nodes.steps.nodeConfig')" :description="t('workflow.taskConfig.nodes.steps.nodeConfigDesc')" />
      </a-steps>
    </section>

    <section v-if="currentStep === 0" class="designer-form-stage">
      <LowCodeFormDesigner
        v-if="editorContext?.formType === 2"
        v-model="lowCodeFormContent"
        @schema-change="handleLowCodeSchemaChange"
      />
      <a-alert
        v-else
        type="info"
        show-icon
        :message="t('workflow.taskConfig.nodes.steps.formDesignDesc')"
        :description="`${t('workflow.taskConfig.formPath')}：${editorContext?.formPath || '-'}`"
      />
      <div class="designer-stage-actions">
        <a-button type="primary" :loading="savingForm" @click="handleSaveFormStep">
          {{ t('workflow.taskConfig.nodes.steps.nextStep') }}
        </a-button>
      </div>
    </section>

    <section v-else class="designer-shell">
      <aside class="palette-panel">
        <div class="panel-title">{{ t('workflow.taskConfig.nodes.paletteTitle') }}</div>
        <p class="panel-desc">{{ t('workflow.taskConfig.nodes.paletteDesc') }}</p>

        <div class="panel-subtitle">{{ t('workflow.taskConfig.nodes.systemNodes') }}</div>
        <button
          type="button"
          class="system-node-item"
          :class="{ 'system-node-item--active': selectedNodeKey === 'start' }"
          @click="focusSystemNode('start')"
        >
          <span class="palette-item__badge palette-item__badge--start">{{ t('workflow.taskConfig.nodes.startBadge') }}</span>
          <div>
            <div class="palette-item__title">{{ t('workflow.taskConfig.nodes.startTitle') }}</div>
            <div class="palette-item__desc">{{ t('workflow.taskConfig.nodes.startDesc') }}</div>
          </div>
        </button>

        <button
          type="button"
          class="system-node-item"
          :class="{ 'system-node-item--active': selectedNodeKey === 'end' }"
          @click="focusSystemNode('end')"
        >
          <span class="palette-item__badge palette-item__badge--end">{{ t('workflow.taskConfig.nodes.endBadge') }}</span>
          <div>
            <div class="palette-item__title">{{ t('workflow.taskConfig.nodes.endTitle') }}</div>
            <div class="palette-item__desc">{{ t('workflow.taskConfig.nodes.endDesc') }}</div>
          </div>
        </button>

        <a-divider />

        <div class="panel-subtitle">{{ t('workflow.taskConfig.nodes.dragToCreate') }}</div>
        <div
          class="palette-item"
          draggable="true"
          @dragstart="handleDragStart($event, NODE_TYPES.APPROVE)"
        >
          <span class="palette-item__badge palette-item__badge--approve">{{ t('workflow.taskConfig.nodes.approveBadge') }}</span>
          <div>
            <div class="palette-item__title">{{ t('workflow.taskConfig.nodes.approveTitle') }}</div>
            <div class="palette-item__desc">{{ t('workflow.taskConfig.nodes.approveDesc') }}</div>
          </div>
        </div>

        <div
          class="palette-item"
          draggable="true"
          @dragstart="handleDragStart($event, NODE_TYPES.BRANCH)"
        >
          <span class="palette-item__badge palette-item__badge--branch">{{ t('workflow.taskConfig.nodes.branchBadge') }}</span>
          <div>
            <div class="palette-item__title">{{ t('workflow.taskConfig.nodes.branchTitle') }}</div>
            <div class="palette-item__desc">{{ t('workflow.taskConfig.nodes.branchDesc') }}</div>
          </div>
        </div>

        <a-divider />

        <a-button block :disabled="!canDeleteNode" @click="handleDeleteSelectedNode">{{ t('workflow.taskConfig.nodes.deleteCurrentNode') }}</a-button>
        <div class="panel-hint">{{ t('workflow.taskConfig.nodes.panelHint') }}</div>
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
        <div class="panel-title">{{ t('workflow.taskConfig.nodes.configTitle') }}</div>

        <template v-if="selectedNodeData">
          <a-form layout="vertical">
            <a-form-item :label="t('workflow.taskConfig.nodes.nodeName')">
              <a-input
                :value="selectedNodeData.nodeName"
                @update:value="updateSelectedNodeData({ nodeName: String($event || '') })"
              />
            </a-form-item>

            <template v-if="selectedNodeData.nodeType === NODE_TYPES.START">
              <div class="field-tip">{{ t('workflow.taskConfig.nodes.startFieldTip') }}</div>
            </template>

            <template v-else-if="selectedNodeData.nodeType === NODE_TYPES.END">
              <div class="field-tip">{{ t('workflow.taskConfig.nodes.endFieldTip') }}</div>
            </template>

            <template v-else-if="selectedNodeData.nodeType === NODE_TYPES.APPROVE">
              <a-form-item :label="t('workflow.taskConfig.formType')">
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

              <a-form-item label="规则类型">
                <a-select
                  :value="ensureNodeRuleConfigs(selectedNodeData)[0]?.ruleType"
                  @update:value="updatePrimaryRule({ ruleType: Number($event), allowInitiatorSelect: Number($event) === RULE_TYPES.INITIATOR_SELECTED })"
                >
                  <a-select-option :value="RULE_TYPES.STATIC">静态审批人</a-select-option>
                  <a-select-option :value="RULE_TYPES.INITIATOR_SELECTED">发起人自选</a-select-option>
                  <a-select-option :value="RULE_TYPES.SUPERIOR">多级上级</a-select-option>
                </a-select>
              </a-form-item>

              <a-row :gutter="12">
                <a-col :span="12">
                  <a-form-item label="超时小时">
                    <a-input-number
                      :value="ensureNodeRuleConfigs(selectedNodeData)[0]?.timeoutHours"
                      :min="1"
                      style="width: 100%"
                      @update:value="updatePrimaryRule({ timeoutHours: $event == null ? undefined : Number($event) })"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="超时动作">
                    <a-select
                      allow-clear
                      :value="ensureNodeRuleConfigs(selectedNodeData)[0]?.timeoutAction"
                      @update:value="updatePrimaryRule({ timeoutAction: $event == null ? undefined : Number($event) })"
                    >
                      <a-select-option v-for="item in TIMEOUT_ACTIONS" :key="item.value" :value="item.value">
                        {{ item.label }}
                      </a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
              </a-row>

              <a-row v-if="ensureNodeRuleConfigs(selectedNodeData)[0]?.ruleType === RULE_TYPES.SUPERIOR" :gutter="12">
                <a-col :span="12">
                  <a-form-item label="上级层级">
                    <a-input-number
                      :value="ensureNodeRuleConfigs(selectedNodeData)[0]?.superiorLevel || 1"
                      :min="1"
                      style="width: 100%"
                      @update:value="updatePrimaryRule({ superiorLevel: $event == null ? 1 : Number($event) })"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="通过阈值">
                    <a-input-number
                      :value="ensureNodeRuleConfigs(selectedNodeData)[0]?.approvalThreshold"
                      :min="0"
                      :step="0.1"
                      style="width: 100%"
                      @update:value="updatePrimaryRule({ approvalThreshold: $event == null ? undefined : Number($event) })"
                    />
                  </a-form-item>
                </a-col>
              </a-row>

              <a-form-item :label="t('workflow.taskConfig.nodes.approverSource')">
                <div class="editor-list">
                  <div
                    v-for="(approver, index) in (ensureNodeRuleConfigs(selectedNodeData)[0]?.approvers || selectedNodeData.approvers)"
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
                          {{ approver.approverIds.length ? t('workflow.taskConfig.nodes.editSelection') : t('workflow.taskConfig.nodes.selectTarget') }}
                        </a-button>
                        <a-button danger @click="removeApprover(index)">{{ t('common.delete') }}</a-button>
                      </a-space>
                    </div>

                    <div class="approver-summary">
                      <template v-if="approver.approverIds.length">
                        <a-tag v-for="label in approverDisplayLabels(approver)" :key="label" color="blue">
                          {{ label }}
                        </a-tag>
                      </template>
                      <span v-else class="approver-summary__empty">{{ t('workflow.taskConfig.nodes.approverEmpty', { type: approverTypeLabel(approver.approverType) }) }}</span>
                    </div>
                  </div>
                </div>
                <a-button type="dashed" block @click="addApprover">{{ t('workflow.taskConfig.nodes.addApproverSource') }}</a-button>
              </a-form-item>

              <a-form-item label="节点能力">
                <a-space wrap>
                  <a-checkbox
                    :checked="Boolean(ensureNodeRuleConfigs(selectedNodeData)[0]?.allowAddSign)"
                    @update:checked="updatePrimaryRule({ allowAddSign: Boolean($event) })"
                  >
                    允许加签
                  </a-checkbox>
                  <a-checkbox
                    :checked="Boolean(ensureNodeRuleConfigs(selectedNodeData)[0]?.allowTransfer)"
                    @update:checked="updatePrimaryRule({ allowTransfer: Boolean($event) })"
                  >
                    允许转交
                  </a-checkbox>
                  <a-checkbox
                    :checked="Boolean(ensureNodeRuleConfigs(selectedNodeData)[0]?.allowDelegate)"
                    @update:checked="updatePrimaryRule({ allowDelegate: Boolean($event) })"
                  >
                    允许委托
                  </a-checkbox>
                  <a-checkbox
                    :checked="Boolean(ensureNodeRuleConfigs(selectedNodeData)[0]?.allowRecall)"
                    @update:checked="updatePrimaryRule({ allowRecall: Boolean($event) })"
                  >
                    允许撤回
                  </a-checkbox>
                </a-space>
              </a-form-item>
            </template>

            <template v-else-if="selectedNodeData.nodeType === NODE_TYPES.BRANCH">
              <a-form-item :label="t('workflow.taskConfig.nodes.defaultBranch')">
                <a-select
                  allow-clear
                  :value="selectedNodeData.defaultBranchNodeKey"
                  :disabled="!branchTargetOptions.length"
                  :placeholder="t('workflow.taskConfig.nodes.defaultBranchPlaceholder')"
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
                <div class="field-tip">{{ t('workflow.taskConfig.nodes.defaultBranchTip') }}</div>
              </a-form-item>

              <a-form-item :label="t('workflow.taskConfig.nodes.branchRules')">
                <div class="editor-list">
                  <div
                    v-for="(rule, index) in selectedNodeData.branchRules"
                    :key="index"
                    class="editor-card editor-card--column"
                  >
                    <a-row :gutter="8">
                      <a-col :span="12">
                        <a-select
                          show-search
                          :value="rule.fieldKey"
                          :placeholder="t('workflow.taskConfig.nodes.branchField')"
                          @update:value="handleBranchFieldChange(index, String($event || ''))"
                        >
                          <a-select-option v-for="field in branchFieldOptions" :key="field.key" :value="field.key">
                            {{ field.label }}
                          </a-select-option>
                        </a-select>
                      </a-col>
                      <a-col :span="12">
                        <a-input
                          :value="rule.fieldLabel"
                          :placeholder="t('workflow.taskConfig.nodes.fieldLabelPlaceholder')"
                          @update:value="updateBranchRule(index, { fieldLabel: String($event || '') })"
                        />
                      </a-col>
                    </a-row>
                    <a-row :gutter="8">
                      <a-col :span="8">
                        <a-select
                          :value="rule.operator"
                          :placeholder="t('workflow.taskConfig.nodes.operatorPlaceholder')"
                          @update:value="updateBranchRule(index, { operator: String($event || '') })"
                        >
                          <a-select-option v-for="item in operatorOptions" :key="item.value" :value="item.value">
                            {{ item.label }}
                          </a-select-option>
                        </a-select>
                      </a-col>
                      <a-col :span="8">
                        <a-input
                          :value="rule.value"
                          :placeholder="t('workflow.taskConfig.nodes.compareValuePlaceholder')"
                          @update:value="updateBranchRule(index, { value: String($event || '') })"
                        />
                      </a-col>
                      <a-col :span="8">
                        <a-select
                          allow-clear
                          :value="rule.nextNodeKey"
                          :disabled="!branchTargetOptions.length"
                          :placeholder="t('workflow.taskConfig.nodes.ruleTargetPlaceholder')"
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
                    <a-button danger @click="removeBranchRule(index)">{{ t('workflow.taskConfig.nodes.deleteRule') }}</a-button>
                  </div>
                </div>
                <a-button type="dashed" block @click="addBranchRule">{{ t('workflow.taskConfig.nodes.addRule') }}</a-button>
              </a-form-item>
            </template>
          </a-form>
        </template>

        <template v-else-if="selectedEdgeData">
          <div class="selected-edge-panel">
            <div class="field-tip">{{ t('workflow.taskConfig.nodes.selectedEdgeTip') }}</div>
            <a-descriptions :column="1" size="small" bordered>
              <a-descriptions-item :label="t('workflow.taskConfig.nodes.edgeSource')">
                {{ edgeNodeName(String(selectedEdgeData.source)) }}
              </a-descriptions-item>
              <a-descriptions-item :label="t('workflow.taskConfig.nodes.edgeTarget')">
                {{ edgeNodeName(String(selectedEdgeData.target)) }}
              </a-descriptions-item>
            </a-descriptions>
            <a-button danger block class="selected-edge-panel__button" @click="handleDeleteSelectedEdge">{{ t('workflow.taskConfig.nodes.deleteCurrentEdge') }}</a-button>
          </div>
        </template>

        <a-empty v-else :description="t('workflow.taskConfig.nodes.emptyConfigTip')" />
      </aside>
    </section>

    <a-modal
      v-model:open="approverDialogOpen"
      :title="t('workflow.taskConfig.nodes.selectApproverSource')"
      destroy-on-close
      @ok="handleApproverDialogOk"
      @cancel="closeApproverDialog"
    >
      <ReceiverSelector v-model:modelValue="approverDialogModel" />
      <div class="field-tip approver-dialog-tip">{{ t('workflow.taskConfig.nodes.approverDialogTip') }}</div>
    </a-modal>

    <Teleport to="body">
      <div
        v-if="contextMenu.visible"
        class="designer-context-menu"
        :style="{ left: `${contextMenu.x}px`, top: `${contextMenu.y}px` }"
      >
        <template v-if="contextMenu.kind === 'pane'">
          <button type="button" class="designer-context-menu__item" @click="handleContextMenuAction('createApprove')">
            {{ t('workflow.taskConfig.nodes.contextMenu.createApprove') }}
          </button>
          <button type="button" class="designer-context-menu__item" @click="handleContextMenuAction('createBranch')">
            {{ t('workflow.taskConfig.nodes.contextMenu.createBranch') }}
          </button>
        </template>

        <template v-else-if="contextMenu.kind === 'node'">
          <button type="button" class="designer-context-menu__item" @click="handleContextMenuAction('editNode')">
            {{ t('workflow.taskConfig.nodes.contextMenu.editNode') }}
          </button>
          <button
            type="button"
            class="designer-context-menu__item"
            :disabled="!contextMenuNodeCanDelete"
            @click="handleContextMenuAction('deleteNode')"
          >
            {{ t('workflow.taskConfig.nodes.contextMenu.deleteNode') }}
          </button>
        </template>

        <template v-else-if="contextMenu.kind === 'edge'">
          <button type="button" class="designer-context-menu__item" @click="handleContextMenuAction('deleteEdge')">
            {{ t('workflow.taskConfig.nodes.contextMenu.deleteEdge') }}
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
import { useDict } from '@/hooks/useDict'
import { getRoleList } from '@/api/system/role'
import { listDepartments } from '@/api/system/department'
import { listPositions } from '@/api/system/position'
import { getUserList } from '@/api/system/user'
import {
  getDraftGraph,
  getOrCreateDraftEditor,
  publishDraft,
  saveDraftBaseInfo,
  saveDraftGraph,
  type WfBranchRuleDTO,
  type WfLowCodeFieldMetaDTO,
  type WfNodeApproverDTO,
  type WfTaskDraftEditorDTO,
  type WfTaskEdgeDTO,
  type WfTaskNodeEditorDTO,
  type WfTaskNodeRuleDTO
} from '@/api/workflow/taskConfig'
import { useI18n } from 'vue-i18n'
import LowCodeFormDesigner from './components/LowCodeFormDesigner.vue'
import { extractLowCodeFieldOptions } from './components/lowCodeSchema'

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
  POSITION: 4,
  INITIATOR_SELECTED: 5,
  SUPERIOR: 6
} as const

const RULE_TYPES = {
  STATIC: 1,
  INITIATOR_SELECTED: 2,
  SUPERIOR: 3
} as const

const TIMEOUT_ACTIONS = [
  { label: '仅提醒', value: 1 },
  { label: '自动通过', value: 2 },
  { label: '自动转交', value: 3 }
]

const { dictItems: approveTypeDictItems } = useDict('wf_approve_type')
const { dictItems: approverTypeDictItems } = useDict('wf_approver_type')
const { dictItems: branchOperatorDictItems } = useDict('wf_branch_operator')

const approveTypeDescriptionMap: Record<number, string> = {
  1: 'workflow.taskConfig.nodes.approveTypeDescription.1',
  2: 'workflow.taskConfig.nodes.approveTypeDescription.2',
  3: 'workflow.taskConfig.nodes.approveTypeDescription.3',
  4: 'workflow.taskConfig.nodes.approveTypeDescription.4',
  5: 'workflow.taskConfig.nodes.approveTypeDescription.5',
}

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
const { t } = useI18n({ useScope: 'global' })

const designerRootRef = ref<HTMLElement | null>(null)
const editorContext = ref<WfTaskDraftEditorDTO | null>(null)
const flowNodes = ref<FlowNode[]>([])
const flowEdges = ref<FlowEdge[]>([])
const selectedNodeKey = ref('')
const selectedEdgeId = ref('')
const currentStep = ref(0)
const saving = ref(false)
const savingForm = ref(false)
const publishing = ref(false)
const lowCodeFormContent = ref('')
const availableFormFields = ref<WfLowCodeFieldMetaDTO[]>([])
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
const approveTypeOptions = computed(() =>
  (approveTypeDictItems.value || []).map((item: { label: string; value: string | number }) => ({
    label: item.label,
    value: Number(item.value),
  })),
)
const approverTypeOptions = computed(() =>
  (approverTypeDictItems.value || []).map((item: { label: string; value: string | number }) => ({
    label: item.label,
    value: Number(item.value),
  })),
)
const operatorOptions = computed(() =>
  (branchOperatorDictItems.value || []).map((item: { label: string; value: string | number }) => ({
    label: item.label,
    value: String(item.value),
  })),
)
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
const branchFieldOptions = computed(() => availableFormFields.value)
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
  if (nodeType === NODE_TYPES.START) return t('workflow.taskConfig.nodes.startBadge')
  if (nodeType === NODE_TYPES.END) return t('workflow.taskConfig.nodes.endBadge')
  if (nodeType === NODE_TYPES.APPROVE) return t('workflow.taskConfig.nodes.approveBadge')
  return t('workflow.taskConfig.nodes.branchBadge')
}

function approverTypeLabel(approverType?: number) {
  return approverTypeOptions.value.find(item => item.value === Number(approverType))?.label || t('workflow.taskConfig.nodes.approverObject')
}

function approveTypeDescription(approveType?: number) {
  return t(approveTypeDescriptionMap[Number(approveType)] || 'workflow.taskConfig.nodes.selectApproveTypeTip')
}

function nodeSummary(data: WorkflowNodeData) {
  if (data.nodeType === NODE_TYPES.START) {
    return t('workflow.taskConfig.nodes.startSummary')
  }
  if (data.nodeType === NODE_TYPES.END) {
    return t('workflow.taskConfig.nodes.endSummary')
  }
  if (data.nodeType === NODE_TYPES.APPROVE) {
    const approveType = approveTypeOptions.value.find(item => item.value === Number(data.approveType))?.label || t('workflow.taskConfig.nodes.notConfigured')
    const approverCount = data.approvers.filter(item => item.approverType != null && item.approverIds?.length).length
    return t('workflow.taskConfig.nodes.approveSummary', { approveType, approverCount })
  }
  return t('workflow.taskConfig.nodes.branchSummary', {
    ruleCount: data.branchRules.length,
    target: data.defaultBranchNodeKey || t('workflow.taskConfig.nodes.toBeConfigured')
  })
}

function handleLowCodeSchemaChange() {
  availableFormFields.value = extractLowCodeFieldOptions(lowCodeFormContent.value)
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
    ruleConfigs: clone(node.ruleConfigs || []),
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

function readThemeVar(name: string, fallbackValue: string) {
  const value = getComputedStyle(resolveThemeHost()).getPropertyValue(name).trim()
  return value || fallbackValue
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
      nodeName: t('workflow.taskConfig.nodes.startBadge'),
      canvasX: 220,
      canvasY: 120,
      approvers: [],
      ruleConfigs: [],
      branchRules: []
    }),
    createFlowNode({
      nodeKey: 'end',
      nodeType: NODE_TYPES.END,
      nodeName: t('workflow.taskConfig.nodes.endBadge'),
      canvasX: 220,
      canvasY: 420,
      approvers: [],
      ruleConfigs: [],
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
          labelMap[id] = item.username ? `${item.username}${item.account ? `(${item.account})` : ''}` : t('workflow.taskConfig.nodes.approverFallback.user', { id })
        }
      })
    } else if (approverType === APPROVER_TYPES.DEPT) {
      const result = await listDepartments({})
      const records = Array.isArray(result) ? result : []
      records.forEach((item: any) => {
        const id = Number(item.id)
        if (Number.isFinite(id)) {
          labelMap[id] = item.deptName || t('workflow.taskConfig.nodes.approverFallback.department', { id })
        }
      })
    } else if (approverType === APPROVER_TYPES.ROLE) {
      const result = await getRoleList({})
      const records = Array.isArray(result) ? result : []
      records.forEach((item: any) => {
        const id = Number(item.id)
        if (Number.isFinite(id)) {
          labelMap[id] = item.roleName || t('workflow.taskConfig.nodes.approverFallback.role', { id })
        }
      })
    } else if (approverType === APPROVER_TYPES.POSITION) {
      const result = await listPositions({})
      const records = Array.isArray(result) ? result : []
      records.forEach((item: any) => {
        const id = Number(item.id)
        if (Number.isFinite(id)) {
          labelMap[id] = item.positionName || t('workflow.taskConfig.nodes.approverFallback.position', { id })
        }
      })
    }

    approverLabelMaps[approverType] = labelMap
    approverOptionsLoaded[approverType] = true
  } catch (error) {
    console.error(t('workflow.taskConfig.nodes.loadApproverNamesFailedLog'), error)
    message.warning(t('workflow.taskConfig.nodes.loadApproverNamesFailed', { type: approverTypeLabel(approverType) }))
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
  return [...names.slice(0, 3), t('workflow.taskConfig.nodes.moreCount', { count: names.length - 3 })]
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
    message.error(t('workflow.taskConfig.nodes.missingTaskCode'))
    navigateBackToList()
    return
  }

  try {
    editorContext.value = await getOrCreateDraftEditor({ taskCode: currentTaskCode }, silentErrorConfig)
    lowCodeFormContent.value = editorContext.value.formContent || ''
    const graph = await getDraftGraph({ taskCode: currentTaskCode }, silentErrorConfig)
    if (graph.nodes?.length) {
      flowNodes.value = graph.nodes.map(createFlowNode)
      flowEdges.value = graph.edges.map(createFlowEdge)
      availableFormFields.value = graph.availableFormFields || extractLowCodeFieldOptions(lowCodeFormContent.value)
      currentStep.value = editorContext.value.formType === 2 && lowCodeFormContent.value ? 1 : 0
      selectNode(graph.nodes[0]?.nodeKey || '')
      await warmUpApproverLabels(graph.nodes)
    } else {
      availableFormFields.value = graph.availableFormFields || extractLowCodeFieldOptions(lowCodeFormContent.value)
      buildDefaultGraph()
    }
    syncEdgePresentation()
  } catch (error: any) {
    message.error(error.message || t('workflow.taskConfig.nodes.loadDesignerFailed'))
    navigateBackToList()
  }
}

async function handleSaveFormStep() {
  if (!editorContext.value) {
    return
  }

  if (editorContext.value.formType === 2 && !availableFormFields.value.length) {
    message.warning(t('workflow.taskConfig.nodes.missingLowCodeSchema'))
    return
  }

  try {
    savingForm.value = true
    editorContext.value = await saveDraftBaseInfo({
      id: editorContext.value.draftId,
      taskName: editorContext.value.taskName,
      taskCode: editorContext.value.taskCode,
      interpreterBean: editorContext.value.interpreterBean,
      formType: editorContext.value.formType,
      formPath: editorContext.value.formPath,
      formContent: editorContext.value.formType === 2 ? lowCodeFormContent.value : editorContext.value.formContent,
      status: editorContext.value.status,
      remark: editorContext.value.remark
    }, silentErrorConfig)
    message.success(t('workflow.taskConfig.nodes.steps.formSaved'))
    currentStep.value = 1
  } catch (error: any) {
    message.error(error.message || t('workflow.taskConfig.list.saveDraftBaseInfoFailed'))
  } finally {
    savingForm.value = false
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
    nodeName: nodeType === NODE_TYPES.APPROVE ? t('workflow.taskConfig.nodes.approveTitle') : t('workflow.taskConfig.nodes.branchTitle'),
    canvasX: x,
    canvasY: y,
    approveType: nodeType === NODE_TYPES.APPROVE ? 2 : undefined,
    approvers: nodeType === NODE_TYPES.APPROVE ? [{ approverType: APPROVER_TYPES.USER, approverIds: [] }] : [],
    ruleConfigs: nodeType === NODE_TYPES.APPROVE ? [buildDefaultRuleConfig()] : [],
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
    message.warning(t('workflow.taskConfig.nodes.startNoIncoming'))
    return
  }
  if (connection.source === 'end') {
    message.warning(t('workflow.taskConfig.nodes.endNoOutgoing'))
    return
  }

  const sourceNodeData = flowNodes.value.find(node => String(node.id) === connection.source)?.data as WorkflowNodeData | undefined
  const sourceOutgoingCount = flowEdges.value.filter(edge => String(edge.source) === connection.source).length
  if (sourceNodeData && sourceNodeData.nodeType !== NODE_TYPES.BRANCH && sourceOutgoingCount >= 1) {
    message.warning(t('workflow.taskConfig.nodes.singleOutgoingOnly'))
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
  const approvers = [...(ensureNodeRuleConfigs(current)[0]?.approvers || current.approvers), { approverType: APPROVER_TYPES.USER, approverIds: [] }]
  updateSelectedNodeData({ approvers })
  updatePrimaryRuleApprovers(approvers)
}

function updateApprover(index: number, patch: Partial<WfNodeApproverDTO>) {
  const current = selectedNodeData.value
  if (!current) {
    return
  }

  const approvers = clone(ensureNodeRuleConfigs(current)[0]?.approvers || current.approvers)
  approvers[index] = {
    ...approvers[index],
    ...patch
  }
  updateSelectedNodeData({ approvers })
  updatePrimaryRuleApprovers(approvers)
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
  const approvers = (ensureNodeRuleConfigs(current)[0]?.approvers || current.approvers).filter((_, itemIndex) => itemIndex !== index)
  updateSelectedNodeData({ approvers })
  updatePrimaryRuleApprovers(approvers)
}

function openApproverDialog(index: number) {
  const current = selectedNodeData.value
  if (!current) {
    return
  }
  const approver = (ensureNodeRuleConfigs(current)[0]?.approvers || current.approvers)[index]
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
    message.warning(t('workflow.taskConfig.nodes.selectApproverTypeFirst'))
    return
  }
  if (!approverIds.length) {
    message.warning(t('workflow.taskConfig.nodes.selectAtLeastOneApprover'))
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

function buildDefaultRuleConfig(): WfTaskNodeRuleDTO {
  return {
    ruleName: '默认规则',
    ruleType: RULE_TYPES.STATIC,
    approveMode: 2,
    approvalThreshold: undefined,
    sortOrder: 1,
    timeoutHours: undefined,
    timeoutAction: undefined,
    allowInitiatorSelect: false,
    superiorLevel: 1,
    allowAddSign: false,
    allowTransfer: false,
    allowDelegate: false,
    allowRecall: true,
    fallbackApproverIds: [],
    approvers: [{ approverType: APPROVER_TYPES.USER, approverIds: [] }],
    extraConfig: ''
  }
}

function ensureNodeRuleConfigs(current?: WorkflowNodeData | null) {
  if (!current) {
    return []
  }
  if (current.ruleConfigs?.length) {
    return current.ruleConfigs
  }
  if (current.approvers?.length) {
    return [{
      ...buildDefaultRuleConfig(),
      approvers: clone(current.approvers)
    }]
  }
  return [buildDefaultRuleConfig()]
}

function updateRuleConfigs(ruleConfigs: WfTaskNodeRuleDTO[]) {
  updateSelectedNodeData({ ruleConfigs, approvers: clone(ruleConfigs[0]?.approvers || []) })
}

function updatePrimaryRule(patch: Partial<WfTaskNodeRuleDTO>) {
  const current = selectedNodeData.value
  if (!current) {
    return
  }
  const [firstRule, ...rest] = ensureNodeRuleConfigs(current)
  updateRuleConfigs([{ ...firstRule, ...patch }, ...rest])
}

function updatePrimaryRuleApprovers(approvers: WfNodeApproverDTO[]) {
  updatePrimaryRule({ approvers })
}

function handleBranchFieldChange(index: number, fieldKey: string) {
  const field = branchFieldOptions.value.find(item => item.key === fieldKey)
  updateBranchRule(index, {
    fieldKey,
    fieldLabel: field?.label || fieldKey
  })
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
    message.warning(t('workflow.taskConfig.nodes.systemNodesCannotDelete'))
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
    console.warn(t('workflow.taskConfig.nodes.focusNodeFailedLog'), error)
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
      ruleConfigs: clone(data.ruleConfigs || []),
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
    console.error(t('workflow.taskConfig.nodes.saveDraftFailedLog'), error)
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
    console.error(t('workflow.taskConfig.nodes.publishFailedLog'), error)
    message.error(error.message || t('workflow.taskConfig.nodes.publishFailed'))
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
.designer-steps,
.designer-form-stage,
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

.designer-steps {
  padding: 20px 24px;
}

.designer-form-stage {
  padding: 24px;
}

.designer-stage-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
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
