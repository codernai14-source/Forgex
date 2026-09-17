<template>
  <div class="page-wrap">
    <a-space style="margin-bottom: 16px">
      <a-input v-model:value="alias" :placeholder="t('system.kms.alias')" />
      <a-button type="primary" @click="load">{{ t('common.search') }}</a-button>
      <a-button v-permission="'sys:kms:add'" @click="create">{{ t('system.kms.create') }}</a-button>
    </a-space>
    <a-table :data-source="rows" :columns="columns" row-key="id" :pagination="false" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { createKms, disableKms, pageKms, rotateKms } from '@/api/system/kms'

const { t } = useI18n()
const alias = ref('')
const rows = ref<any[]>([])
const columns = [
  { title: t('system.kms.alias'), dataIndex: 'alias' },
  { title: t('system.kms.keyType'), dataIndex: 'keyType' },
  { title: t('system.kms.status'), dataIndex: 'status' },
  { title: t('system.kms.version'), dataIndex: 'keyVersion' },
]

async function load() {
  const page = await pageKms({ current: 1, size: 50, alias: alias.value })
  rows.value = page?.records || []
}

async function create() {
  await createKms({ alias: alias.value || 'field-encrypt-sm4', keyType: 'SM4', keySize: 128 })
  await load()
}

onMounted(load)
</script>
