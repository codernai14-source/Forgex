<template>
  <div class="page-wrap">
    <a-row :gutter="16">
      <a-col :span="6">
        <a-card>
          <div class="metric-title">用户数</div>
          <div class="metric-value">{{ metrics.users }}</div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <div class="metric-title">角色数</div>
          <div class="metric-value">{{ metrics.roles }}</div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <div class="metric-title">租户数</div>
          <div class="metric-value">{{ metrics.tenants }}</div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <div class="metric-title">在线用户</div>
          <div class="metric-value">{{ metrics.online }}</div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue'

const metrics = reactive({
  users: 0,
  roles: 0,
  tenants: 0,
  online: 0
})

onMounted(() => {
  metrics.users = 0
  metrics.roles = 0
  metrics.tenants = 0
  metrics.online = 0
})
</script>

<style scoped>
.page-wrap {
  padding: 16px;
}
.metric-title {
  font-size: 13px;
  color: #9ca3af;
}
.metric-value {
  margin-top: 6px;
  font-size: 24px;
  font-weight: 600;
}
</style>

