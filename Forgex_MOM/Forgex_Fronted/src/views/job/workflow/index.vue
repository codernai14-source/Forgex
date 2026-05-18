<template>
  <div class="job-workflow-page">
    <div class="workflow-list">
      <FxDynamicTable ref="tableRef" table-code="JobWorkflowTable" :request="handleRequest" row-key="id">
        <template #toolbar>
          <a-button v-permission="'job:workflow:add'" type="primary" @click="openWorkflow()">
            {{ t('job.actions.addWorkflow') }}
          </a-button>
        </template>
        <template #status="{ record }">
          <a-tag :color="record.status === 1 ? 'green' : 'default'">
            {{ record.status === 1 ? t('job.status.published') : t('job.status.draft') }}
          </a-tag>
        </template>
        <template #action="{ record }">
          <a-space>
            <a v-permission="'job:workflow:edit'" @click="openWorkflow(record.id)">{{ t('common.edit') }}</a>
            <a v-permission="'job:workflow:publish'" @click="handlePublish(record.id)">{{ t('job.actions.publish') }}</a>
            <a v-permission="'job:workflow:execute'" @click="handleExecute(record.id)">{{ t('job.actions.execute') }}</a>
          </a-space>
        </template>
      </FxDynamicTable>
    </div>

    <BaseFormDialog
      v-model:open="dialogVisible"
      :title="currentId ? t('job.actions.editWorkflow') : t('job.actions.addWorkflow')"
      mode="drawer"
      width="900px"
      :loading="saving"
      @submit="handleSave"
    >
      <a-form :model="formState" layout="vertical">
        <a-row :gutter="12">
          <a-col :span="8"><a-form-item :label="t('job.fields.workflowCode')" required><a-input v-model:value="formState.workflowCode" :disabled="!!currentId" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item :label="t('job.fields.workflowName')" required><a-input v-model:value="formState.workflowName" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item :label="t('job.fields.status')"><a-select v-model:value="formState.status" :options="workflowStatusOptions" /></a-form-item></a-col>
        </a-row>
      </a-form>

      <div class="flow-toolbar">
        <a-space>
          <a-button size="small" @click="addNode">{{ t('job.actions.addNode') }}</a-button>
          <a-button size="small" @click="validateDag">{{ t('job.actions.validateDag') }}</a-button>
        </a-space>
      </div>

      <div class="flow-canvas">
        <VueFlow v-model:nodes="nodes" v-model:edges="edges" fit-view-on-init>
          <Background />
          <Controls />
          <MiniMap />
        </VueFlow>
      </div>
    </BaseFormDialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import { VueFlow, useVueFlow, type Edge, type Node } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import { MiniMap } from '@vue-flow/minimap'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import { executeJobWorkflow, getJobWorkflowDetail, getJobWorkflowPage, publishJobWorkflow, saveJobWorkflow } from '@/api/job/workflow'
import type { JobWorkflow } from '@/api/job/types'

const { t } = useI18n({ useScope: 'global' })
const tableRef = ref()
const dialogVisible = ref(false)
const saving = ref(false)
const currentId = ref<number>()
const formState = reactive<JobWorkflow>({})
const nodes = ref<Node[]>([])
const edges = ref<Edge[]>([])
const { addEdges, onConnect } = useVueFlow()

onConnect(params => addEdges([params]))

const workflowStatusOptions = [
  { label: 'Draft', value: 0 },
  { label: 'Published', value: 1 },
]

async function handleRequest(payload: { page: { current: number; pageSize: number }; query: Record<string, any> }) {
  const result = await getJobWorkflowPage({ ...payload.query, pageNum: payload.page.current, pageSize: payload.page.pageSize })
  return { records: result.records || [], total: result.total || 0 }
}

async function openWorkflow(id?: number) {
  currentId.value = id
  Object.keys(formState).forEach(key => delete (formState as any)[key])
  if (id) {
    Object.assign(formState, await getJobWorkflowDetail(id))
    loadGraph(formState.graphJson)
  } else {
    Object.assign(formState, { status: 0 })
    nodes.value = []
    edges.value = []
  }
  dialogVisible.value = true
}

function addNode() {
  const index = nodes.value.length + 1
  nodes.value = [
    ...nodes.value,
    {
      id: `node_${index}`,
      label: `${t('job.fields.node')} ${index}`,
      position: { x: 80 + index * 30, y: 80 + index * 24 },
    },
  ]
}

function validateDag() {
  const graph = new Map<string, string[]>()
  nodes.value.forEach(node => graph.set(node.id, []))
  edges.value.forEach(edge => graph.get(String(edge.source))?.push(String(edge.target)))
  const visiting = new Set<string>()
  const visited = new Set<string>()
  const dfs = (id: string): boolean => {
    if (visiting.has(id)) return true
    if (visited.has(id)) return false
    visiting.add(id)
    for (const next of graph.get(id) || []) {
      if (dfs(next)) return true
    }
    visiting.delete(id)
    visited.add(id)
    return false
  }
  const hasCycle = nodes.value.some(node => dfs(node.id))
  if (hasCycle) {
    message.error(t('job.messages.dagCycle'))
    return false
  }
  message.success(t('job.messages.dagValid'))
  return true
}

async function handleSave() {
  if (!validateDag()) return
  saving.value = true
  try {
    await saveJobWorkflow({
      ...formState,
      id: currentId.value,
      graphJson: JSON.stringify({ nodes: nodes.value, edges: edges.value }),
    })
    dialogVisible.value = false
    tableRef.value?.refresh?.()
  } finally {
    saving.value = false
  }
}

async function handlePublish(id?: number) {
  if (!id) return
  await publishJobWorkflow(id)
  tableRef.value?.refresh?.()
}

async function handleExecute(id?: number) {
  if (!id) return
  await executeJobWorkflow(id)
}

function loadGraph(raw?: string) {
  if (!raw) {
    nodes.value = []
    edges.value = []
    return
  }
  try {
    const graph = JSON.parse(raw)
    nodes.value = Array.isArray(graph.nodes) ? graph.nodes : []
    edges.value = Array.isArray(graph.edges) ? graph.edges : []
  } catch (e) {
    nodes.value = []
    edges.value = []
  }
}
</script>

<style scoped lang="less" src="@/styles/views/job/workflow/index.less"></style>
