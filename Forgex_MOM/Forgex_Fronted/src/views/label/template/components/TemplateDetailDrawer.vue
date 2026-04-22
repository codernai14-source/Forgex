
<template>
  <a-drawer
      v-model:open="drawerVisible"
      title="模板详情"
      width="700px"
      :footer-style="{ textAlign: 'right' }"
  >
    <template #extra>
      <a-button @click="drawerVisible = false">关闭</a-button>
    </template>

    <a-descriptions bordered :column="2">
      <a-descriptions-item label="模板编码">
        {{ templateData?.templateCode }}
      </a-descriptions-item>
      <a-descriptions-item label="模板名称">
        {{ templateData?.templateName }}
      </a-descriptions-item>
      <a-descriptions-item label="模板类型">
        <a-tag :color="getTypeColor(templateData?.templateType)">
          {{ getTypeName(templateData?.templateType) }}
        </a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="模板版本">
        v{{ templateData?.templateVersion || '1.0' }}
      </a-descriptions-item>
      <a-descriptions-item label="是否默认">
        <a-tag v-if="templateData?.isDefault" color="green">是</a-tag>
        <a-tag v-else color="default">否</a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="状态">
        <a-tag v-if="templateData?.status === 1" color="green">启用</a-tag>
        <a-tag v-else-if="templateData?.status === 0" color="red">禁用</a-tag>
        <a-tag v-else color="default">未知</a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="描述" :span="2">
        {{ templateData?.description || '-' }}
      </a-descriptions-item>
      <a-descriptions-item label="创建人">
        {{ templateData?.createBy }}
      </a-descriptions-item>
      <a-descriptions-item label="创建时间">
        {{ templateData?.createTime }}
      </a-descriptions-item>
      <a-descriptions-item label="更新人">
        {{ templateData?.updateBy }}
      </a-descriptions-item>
      <a-descriptions-item label="更新时间">
        {{ templateData?.updateTime }}
      </a-descriptions-item>
    </a-descriptions>
  </a-drawer>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  visible: boolean
  templateData?: any
}>()

const emit = defineEmits(['update:visible'])

const drawerVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

function getTypeName(type: string) {
  const typeMap: Record<string, string> = {
    PRODUCT: '产品标签',
    MATERIAL: '物料标签',
    BATCH: '批次标签',
    SUPPLIER: '供应商标签',
    CUSTOMER_MARK: '客户唛头',
    WORKSTATION: '工位标签',
    EQUIPMENT: '设备标签',
    LOCATION: '库位标签'
  }
  return typeMap[type] || type
}

function getTypeColor(type: string) {
  const colorMap: Record<string, string> = {
    PRODUCT: 'blue',
    MATERIAL: 'orange',
    BATCH: 'purple',
    SUPPLIER: 'cyan',
    CUSTOMER_MARK: 'pink',
    WORKSTATION: 'green',
    EQUIPMENT: 'volcano',
    LOCATION: 'geekblue'
  }
  return colorMap[type] || 'default'
}
</script>
