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

<style scoped lang="less" src="@/styles/views/integrationPlatform/home/index.less"></style>
