<template>
  <div class="receiver-selector">
    <a-form-item label="接收类型" required>
      <a-select 
        v-model:value="localReceiver.receiverType" 
        placeholder="请选择接收类型"
        @change="handleTypeChange"
      >
        <a-select-option value="USER">指定用户</a-select-option>
        <a-select-option value="ROLE">角色</a-select-option>
        <a-select-option value="DEPT">部门</a-select-option>
        <a-select-option value="POSITION">职位</a-select-option>
      </a-select>
    </a-form-item>
    
    <a-form-item :label="getReceiverLabel()" required>
      <a-select
        v-model:value="localReceiver.receiverIds"
        mode="multiple"
        :placeholder="`请选择${getReceiverLabel()}`"
        :options="receiverOptions"
        :loading="loading"
        :filter-option="filterOption"
        show-search
        allow-clear
        @change="handleReceiverChange"
      >
        <template #notFoundContent>
          <a-empty :description="loading ? '加载中...' : '暂无数据'" />
        </template>
      </a-select>
    </a-form-item>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { getRoleList } from '@/api/system/role'
import { listDepartments } from '@/api/system/department'
import { listPositions } from '@/api/system/position'
import { getUserList } from '@/api/system/user'

interface Receiver {
  receiverType?: string
  receiverIds: string[]
}

interface Props {
  modelValue: Receiver
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:modelValue': [value: Receiver]
}>()

const localReceiver = ref<Receiver>({ ...props.modelValue })
const loading = ref(false)
const receiverOptions = ref<Array<{ label: string; value: string }>>([])

// 获取接收人标签
const getReceiverLabel = () => {
  const labelMap: Record<string, string> = {
    USER: '用户',
    ROLE: '角色',
    DEPT: '部门',
    POSITION: '职位'
  }
  return labelMap[localReceiver.value.receiverType || ''] || '接收人'
}

// 过滤选项
const filterOption = (input: string, option: any) => {
  return option.label.toLowerCase().includes(input.toLowerCase())
}

// 加载接收人选项
const loadReceiverOptions = async () => {
  if (!localReceiver.value.receiverType) {
    receiverOptions.value = []
    return
  }
  
  loading.value = true
  try {
    let data: any[] = []
    
    switch (localReceiver.value.receiverType) {
      case 'USER':
        const userRes = await getUserList({ pageNum: 1, pageSize: 1000 })
        data = (userRes.records || []).map((item: any) => ({
          label: `${item.username}(${item.account})`,
          value: String(item.id)
        }))
        break
        
      case 'ROLE':
        const roleRes = await getRoleList({})
        data = (roleRes || []).map((item: any) => ({
          label: item.roleName,
          value: String(item.id)
        }))
        break
        
      case 'DEPT':
        const deptRes = await listDepartments({})
        data = (deptRes || []).map((item: any) => ({
          label: item.deptName,
          value: String(item.id)
        }))
        break
        
      case 'POSITION':
        const posRes = await listPositions({})
        data = (posRes || []).map((item: any) => ({
          label: item.positionName,
          value: String(item.id)
        }))
        break
    }
    
    receiverOptions.value = data
  } catch (error) {
    console.error('加载接收人选项失败:', error)
    message.error('加载接收人选项失败')
  } finally {
    loading.value = false
  }
}

// 接收类型变化
const handleTypeChange = () => {
  localReceiver.value.receiverIds = []
  loadReceiverOptions()
  emitChange()
}

// 接收人变化
const handleReceiverChange = () => {
  emitChange()
}

// 触发变化
const emitChange = () => {
  emit('update:modelValue', { ...localReceiver.value })
}

// 监听外部值变化
watch(() => props.modelValue, (newVal) => {
  localReceiver.value = { ...newVal }
  if (newVal.receiverType) {
    loadReceiverOptions()
  }
}, { deep: true })

// 初始化
onMounted(() => {
  if (localReceiver.value.receiverType) {
    loadReceiverOptions()
  }
})
</script>

<style scoped lang="less">
.receiver-selector {
  // 样式可以根据需要添加
}
</style>

