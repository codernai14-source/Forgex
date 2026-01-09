<template>
  <a-drawer
    v-model:open="visible"
    title="用户详情"
    width="720"
    :body-style="{ paddingBottom: '80px' }"
  >
    <a-spin :spinning="loading">
      <a-descriptions :column="2" bordered>
        <a-descriptions-item label="用户名">
          {{ userDetail?.username }}
        </a-descriptions-item>
        <a-descriptions-item label="邮箱">
          {{ userDetail?.email }}
        </a-descriptions-item>
        <a-descriptions-item label="手机号">
          {{ userDetail?.phone }}
        </a-descriptions-item>
        <a-descriptions-item label="性别">
          <a-tag v-if="userDetail?.gender === 1" color="blue">男</a-tag>
          <a-tag v-else-if="userDetail?.gender === 2" color="pink">女</a-tag>
          <a-tag v-else color="default">未知</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="入职时间">
          {{ userDetail?.entryDate }}
        </a-descriptions-item>
        <a-descriptions-item label="所属部门">
          {{ userDetail?.departmentName }}
        </a-descriptions-item>
        <a-descriptions-item label="职位">
          {{ userDetail?.positionName }}
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag v-if="userDetail?.status === 1" color="success">启用</a-tag>
          <a-tag v-else color="error">禁用</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{ userDetail?.createTime }}
        </a-descriptions-item>
      </a-descriptions>
      
      <a-divider orientation="left">附属信息</a-divider>
      
      <a-descriptions :column="2" bordered v-if="userDetail?.profile">
        <a-descriptions-item label="政治面貌">
          {{ userDetail.profile.politicalStatus || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="学历">
          {{ userDetail.profile.education || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="家庭住址" :span="2">
          {{ userDetail.profile.homeAddress || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="紧急联系人">
          {{ userDetail.profile.emergencyContact || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="紧急联系电话">
          {{ userDetail.profile.emergencyPhone || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="引荐人" :span="2">
          {{ userDetail.profile.referrer || '-' }}
        </a-descriptions-item>
      </a-descriptions>
      
      <template v-if="userDetail?.profile?.workHistory && userDetail.profile.workHistory.length > 0">
        <a-divider orientation="left">工作经历</a-divider>
        
        <a-timeline>
          <a-timeline-item
            v-for="(history, index) in userDetail.profile.workHistory"
            :key="index"
          >
            <p><strong>{{ history.company }}</strong> - {{ history.position }}</p>
            <p style="color: #999;">{{ history.startDate }} ~ {{ history.endDate }}</p>
            <p v-if="history.description">{{ history.description }}</p>
          </a-timeline-item>
        </a-timeline>
      </template>
    </a-spin>
    
    <template #footer>
      <a-button @click="visible = false">关闭</a-button>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { userApi } from '@/api/system/user'
import type { User, UserProfile } from '../types'

// Props
interface Props {
  open: boolean
  userId?: string
}

const props = withDefaults(defineProps<Props>(), {
  open: false,
  userId: undefined,
})

// Emits
const emit = defineEmits<{
  'update:open': [value: boolean]
}>()

// 响应式数据
const visible = ref(props.open)
const loading = ref(false)
const userDetail = ref<(User & { profile?: UserProfile }) | null>(null)

// 监听 props.open 变化
watch(() => props.open, (val) => {
  visible.value = val
  if (val && props.userId) {
    loadUserDetail()
  }
})

// 监听 visible 变化
watch(visible, (val) => {
  emit('update:open', val)
})

/**
 * 加载用户详情
 */
async function loadUserDetail() {
  if (!props.userId) return
  
  loading.value = true
  try {
    const data = await userApi.getUserDetail(props.userId)
    if (data) {
      userDetail.value = data
    } else {
      message.error('加载失败')
    }
  } catch (error) {
    console.error('加载用户详情失败:', error)
    message.error('加载用户详情失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="less">
:deep(.ant-descriptions-item-label) {
  width: 120px;
  font-weight: 500;
}
</style>
