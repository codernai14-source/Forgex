<template>
  <div class="system-dashboard">
    <a-card :title="$t('system.dashboard.title')" :bordered="false">
      <a-spin :spinning="loading">
        <a-row :gutter="16">
          <a-col :span="6">
            <a-statistic :title="$t('system.dashboard.userCount')" :value="statistics.userCount || 0" />
          </a-col>
          <a-col :span="6">
            <a-statistic :title="$t('system.dashboard.roleCount')" :value="statistics.roleCount || 0" />
          </a-col>
          <a-col :span="6">
            <a-statistic :title="$t('system.dashboard.menuCount')" :value="statistics.menuCount || 0" />
          </a-col>
          <a-col :span="6">
            <a-statistic :title="$t('system.dashboard.onlineUsers')" :value="statistics.onlineUsers || 0" />
          </a-col>
        </a-row>
      </a-spin>
    </a-card>

    <a-row :gutter="16" style="margin-top: 16px;">
      <a-col :span="12">
        <a-card :title="$t('system.dashboard.quickEntry')" :bordered="false">
          <a-space direction="vertical" style="width: 100%;">
            <a-button type="primary" block @click="navigateTo('/workspace/sys/user')">
              {{ $t('system.dashboard.userManagement') }}
            </a-button>
            <a-button block @click="navigateTo('/workspace/sys/role')">
              {{ $t('system.dashboard.roleManagement') }}
            </a-button>
            <a-button block @click="navigateTo('/workspace/sys/menu')">
              {{ $t('system.dashboard.menuManagement') }}
            </a-button>
            <a-button block @click="navigateTo('/workspace/sys/module')">
              {{ $t('system.dashboard.moduleManagement') }}
            </a-button>
          </a-space>
        </a-card>
      </a-col>
      
      <a-col :span="12">
        <a-card :title="$t('system.dashboard.systemInfo')" :bordered="false">
          <a-descriptions :column="1">
            <a-descriptions-item :label="$t('system.dashboard.systemName')">
              Forgex MOM
            </a-descriptions-item>
            <a-descriptions-item :label="$t('system.dashboard.systemVersion')">
              v1.0.0
            </a-descriptions-item>
            <a-descriptions-item :label="$t('system.dashboard.currentTenant')">
              {{ userStore.userInfo?.tenantName || $t('system.dashboard.defaultTenant') }}
            </a-descriptions-item>
            <a-descriptions-item :label="$t('system.dashboard.currentUser')">
              {{ userStore.account }}
            </a-descriptions-item>
          </a-descriptions>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/stores/user'
import { getDashboardStatistics } from '@/api/system/dashboard'
import {
  UserOutlined,
  TeamOutlined,
} from '@ant-design/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const statistics = ref({
  userCount: 0,
  roleCount: 0,
  menuCount: 0,
  onlineUsers: 0
})

const navigateTo = (path: string) => {
  router.push(path)
}

const loadStatistics = async () => {
  const tenantId = sessionStorage.getItem('tenantId')
  if (!tenantId) {
    message.warning('租户信息缺失')
    return
  }
  
  try {
    loading.value = true
    const data = await getDashboardStatistics({ tenantId })
    statistics.value = data || statistics.value
  } catch (error) {
    console.error('加载统计数据失败:', error)
    message.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped lang="less">
.system-dashboard {
  padding: 16px;
}
</style>
