<template>
  <div class="page-wrap">
    <a-button v-permission="'sys:backup:run'" type="primary" style="margin-bottom: 16px" @click="run">{{ t('system.backup.run') }}</a-button>
    <a-table :data-source="rows" :columns="columns" row-key="id" :pagination="false" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { pageBackup, runBackup } from '@/api/system/backup'

const { t } = useI18n()
const rows = ref<any[]>([])
const columns = [
  { title: t('system.backup.name'), dataIndex: 'backupName' },
  { title: t('system.backup.type'), dataIndex: 'backupType' },
  { title: t('system.backup.status'), dataIndex: 'backupStatus' },
  { title: t('system.backup.time'), dataIndex: 'backupTime' },
]

async function load() {
  const page = await pageBackup({ current: 1, size: 50 })
  rows.value = page?.records || []
}

async function run() {
  await runBackup()
  await load()
}

onMounted(load)
</script>
