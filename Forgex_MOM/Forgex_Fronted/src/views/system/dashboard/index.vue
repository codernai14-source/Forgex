<template>
  <div class="system-dashboard">
    <a-card title="系统管理" :bordered="false">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-statistic title="用户总数" :value="112893" />
        </a-col>
        <a-col :span="6">
          <a-statistic title="角色总数" :value="8" />
        </a-col>
        <a-col :span="6">
          <a-statistic title="菜单总数" :value="32" />
        </a-col>
        <a-col :span="6">
          <a-statistic title="在线用户" :value="93" suffix="/ 100" />
        </a-col>
      </a-row>
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
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const navigateTo = (path: string) => {
  router.push(path)
}
</script>

<style scoped lang="less">
.system-dashboard {
  padding: 16px;
}
</style>
