<template>
  <div class="tree-container">
    <div class="tree-header">
      <span class="tree-title">{{ $t('system.department.title') }}</span>
      <a-button
        type="primary"
        size="small"
        @click="$emit('add')"
        v-if="showAdd"
      >
        <template #icon><PlusOutlined /></template>
        {{ $t('common.add') }}
      </a-button>
    </div>
    <a-spin :spinning="loading">
      <a-tree
        v-if="treeData.length > 0"
        :tree-data="treeData"
        :field-names="fieldNames"
        :selected-keys="selectedKeys"
        :default-expand-all="true"
        show-icon
        @select="onSelect"
      >
        <template #icon="{ dataRef }">
          <GlobalOutlined v-if="dataRef.orgType === 'group'" />
          <BankOutlined v-else-if="dataRef.orgType === 'company'" />
          <ClusterOutlined v-else-if="dataRef.orgType === 'subsidiary'" />
          <ApartmentOutlined v-else-if="dataRef.orgType === 'department'" />
          <TeamOutlined v-else-if="dataRef.orgType === 'team'" />
          <ApartmentOutlined v-else />
        </template>
        <template #title="node">
          <span :class="{ 'disabled-node': (node.dataRef || node).status === false }">
            {{ (node.dataRef || node).deptName }}
          </span>
        </template>
      </a-tree>
      <a-empty v-else :description="$t('common.noData')" />
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import { 
  PlusOutlined,
  GlobalOutlined,
  BankOutlined,
  ClusterOutlined,
  ApartmentOutlined,
  TeamOutlined
} from '@ant-design/icons-vue'
import { getDepartmentTree } from '@/api/system/department'
import type { Department } from '@/views/system/department/types'

const { t } = useI18n()

const props = defineProps<{
  /** 是否显示新增按钮，默认 true */
  showAdd?: boolean
}>()

const emit = defineEmits<{
  /**
   * 节点选择事件
   * 触发时机：用户点击树节点时触发
   * @param keys 选中的节点 ID 数组
   * @param node 选中的节点数据对象
   */
  (e: 'select', keys: string[], node: any): void
  /**
   * 新增按钮点击事件
   * 触发时机：点击新增按钮时触发
   */
  (e: 'add'): void
}>()

const loading = ref(false)
const treeData = ref<any[]>([])
const selectedKeys = ref<string[]>([])
const currentTenantId = ref<string | null>(null)

const fieldNames = {
  key: 'id',
  title: 'deptName',
  children: 'children'
}

async function loadTree() {
  const tid = sessionStorage.getItem('tenantId')
  if (!tid) return
  
  currentTenantId.value = tid
  try {
    loading.value = true
    const data = await getDepartmentTree({ tenantId: tid })
    treeData.value = data || []
  } catch (e) {
    message.error(t('system.department.message.loadTreeFailed'))
  } finally {
    loading.value = false
  }
}

function onSelect(keys: string[], info: any) {
  selectedKeys.value = keys
  // Pass the full node data
  emit('select', keys, info.node.dataRef || info.node)
}

// Expose refresh method
defineExpose({
  loadTree,
  selectedKeys
})

onMounted(() => {
  loadTree()
})
</script>

<style scoped lang="less" src="@/styles/components/system/dept-tree.less"></style>
