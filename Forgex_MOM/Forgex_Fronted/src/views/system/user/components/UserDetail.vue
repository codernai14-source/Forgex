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
          {{ userDetail?.username || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="账号">
          {{ userDetail?.account || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="邮箱">
          {{ userDetail?.email || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="手机号">
          {{ userDetail?.phone || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="关联员工ID">
          {{ userDetail?.employeeId || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="用户来源">
          {{ userDetail?.userSourceText || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="性别">
          {{ userDetail?.genderText || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="入职时间">
          {{ userDetail?.entryDate || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="所属部门">
          {{ userDetail?.departmentName || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="岗位">
          {{ userDetail?.positionName || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          {{ userDetail?.statusText || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{ userDetail?.createTime || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="最后登录时间">
          {{ userDetail?.lastLoginTime || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="最后登录IP">
          {{ userDetail?.lastLoginIp || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="最后登录地区">
          {{ userDetail?.lastLoginRegion || '-' }}
        </a-descriptions-item>
      </a-descriptions>

      <a-divider orientation="left">关联租户</a-divider>

      <a-table
        :columns="tenantColumns"
        :data-source="userDetail?.tenantList || []"
        :pagination="false"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'isDefault'">
            <a-tag v-if="record.isDefault" color="blue">默认</a-tag>
            <a-tag v-else>非默认</a-tag>
          </template>
          <template v-else-if="column.key === 'lastUsed'">
            {{ record.lastUsed || '-' }}
          </template>
        </template>
      </a-table>

      <a-divider orientation="left">附属信息</a-divider>

      <a-descriptions :column="2" bordered v-if="userDetail?.profile">
        <a-descriptions-item label="政治面貌">
          {{ userDetail.profile.politicalStatus || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="学历">
          {{ userDetail.profile.education || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="籍贯">
          {{ userDetail.profile.birthPlace || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="个人简介">
          {{ userDetail.profile.intro || '-' }}
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
        <a-descriptions-item label="介绍人" :span="2">
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
            <p style="color: #999">{{ history.startDate }} ~ {{ history.endDate }}</p>
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
import type { User } from '../types'

interface Props {
  open: boolean
  userId?: string
}

const props = withDefaults(defineProps<Props>(), {
  open: false,
  userId: undefined,
})

const emit = defineEmits<{
  'update:open': [value: boolean]
}>()

const visible = ref(props.open)
const loading = ref(false)
const userDetail = ref<User | null>(null)

const tenantColumns = [
  { title: '租户 ID', dataIndex: 'tenantId', key: 'tenantId', width: 180 },
  { title: '是否默认', dataIndex: 'isDefault', key: 'isDefault', width: 100 },
  { title: '最后使用时间', dataIndex: 'lastUsed', key: 'lastUsed', width: 160 },
]

watch(() => props.open, async (val) => {
  visible.value = val
  if (val && props.userId) {
    await loadUserDetail()
  }
})

watch(visible, (val) => {
  emit('update:open', val)
})

async function loadUserDetail() {
  if (!props.userId) return
  loading.value = true
  try {
    userDetail.value = await userApi.getUserDetail(props.userId)
  } catch {
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
