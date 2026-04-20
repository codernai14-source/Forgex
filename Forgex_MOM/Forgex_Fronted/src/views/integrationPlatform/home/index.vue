<template>
  <div class="integration-home">
    <a-card :bordered="false" class="integration-home__card">
      <a-empty :description="t('integration.home.title')" />
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n({ useScope: 'global' })
</script>

<style scoped lang="less">
.integration-home {
  min-height: 100%;
}

.integration-home__card {
  min-height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.integration-home__card :deep(.ant-card-body) {
  width: 100%;
}
</style>
