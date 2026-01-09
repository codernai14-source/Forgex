<template>
  <div class="system-dashboard">
    <a-card title="系统管理" :bordered="false">
      <a-spin :spinning="loading">
        <a-row :gutter="16">
          <a-col :span="6">
            <a-statistic title="用户总数" :value="statistics.userCount || 0" />
          </a-col>
          <a-col :span="6">
            <a-statistic title="角色总数" :value="statistics.roleCount || 0" />
          </a-col>
          <a-col :span="6">
            <a-statistic title="菜单总数" :value="statistics.menuCount || 0" />
          </a-col>
          <a-col :span="6">
            <a-statistic title="在线用户" :value="statistics.onlineUsers || 0" />
          </a-col>
        </a-row>
      </a-spin>
    </a-card>

    <a-row :gutter="16" style="margin-top: 16px;">
      <a-col :span="12">
        <a-card title="快速入口" :bordered="false">
          <a-space direction="vertical" style="width: 100%;">
            <a-button type="primary" block @click="navigateTo('/workspace/sys/user')">
              用户管理
            </a-button>
            <a-button block @click="navigateTo('/workspace/sys/role')">
              角色管理
            </a-button>
            <a-button block @click="navigateTo('/workspace/sys/menu')">
              菜单管理
            </a-button>
            <a-button block @click="navigateTo('/workspace/sys/module')">
              模块管理
            </a-button>
          </a-space>
        </a-card>
      </a-col>
      
      <a-col :span="12">
        <a-card title="系统信息" :bordered="false">
          <a-descriptions :column="1">
            <a-descriptions-item label="系统名称">
              Forgex MOM
            </a-descriptions-item>
            <a-descriptions-item label="系统版本">
              v1.0.0
            </a-descriptions-item>
            <a-descriptions-item label="当前租户">
              {{ userStore.userInfo?.tenantName || '默认租户' }}
            </a-descriptions-item>
            <a-descriptions-item label="当前用户">
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
import { getDashboardStatistics } from '@/api/sys/dashboard'

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
