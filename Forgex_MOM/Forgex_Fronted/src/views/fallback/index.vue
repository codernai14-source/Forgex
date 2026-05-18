<template>
  <section class="fx-fallback-page" :class="`fx-fallback-page--${pageKind}`">
    <div class="fx-fallback-page__shell">
      <div class="fx-fallback-page__visual" aria-hidden="true">
        <div class="fx-fallback-orbit">
          <span class="fx-fallback-orbit__dot fx-fallback-orbit__dot--primary"></span>
          <span class="fx-fallback-orbit__dot"></span>
          <span class="fx-fallback-orbit__dot"></span>
        </div>
        <div class="fx-fallback-console">
          <div class="fx-fallback-console__bar">
            <span></span>
            <span></span>
            <span></span>
          </div>
          <div class="fx-fallback-console__grid">
            <div
              v-for="cell in consoleCells"
              :key="cell"
              class="fx-fallback-console__cell"
              :class="{ 'fx-fallback-console__cell--active': activeCells.includes(cell) }"
            ></div>
          </div>
          <div class="fx-fallback-console__status">
            <span class="fx-fallback-console__pulse"></span>
            {{ t(`fallback.${pageKind}.status`) }}
          </div>
        </div>
        <div class="fx-fallback-code">{{ pageConfig.code }}</div>
      </div>

      <div class="fx-fallback-page__content">
        <div class="fx-fallback-page__eyebrow">
          <span class="fx-fallback-page__indicator"></span>
          {{ t(`fallback.${pageKind}.eyebrow`) }}
        </div>
        <h1>{{ t(`fallback.${pageKind}.title`) }}</h1>
        <p>{{ t(`fallback.${pageKind}.description`) }}</p>

        <div class="fx-fallback-actions">
          <a-button v-if="pageKind === 'offline'" type="primary" @click="reloadPage">
            <template #icon><ReloadOutlined /></template>
            {{ t('fallback.actions.retry') }}
          </a-button>
          <a-button v-else type="primary" @click="goHome">
            <template #icon><HomeOutlined /></template>
            {{ t('fallback.actions.home') }}
          </a-button>
          <a-button @click="goBack">
            <template #icon><LeftOutlined /></template>
            {{ t('fallback.actions.back') }}
          </a-button>
        </div>
      </div>

      <aside class="fx-fallback-page__panel">
        <div class="fx-fallback-panel__header">
          <span>{{ t('fallback.panel.title') }}</span>
          <strong>{{ pageConfig.code }}</strong>
        </div>
        <div class="fx-fallback-panel__list">
          <div
            v-for="item in pageConfig.checks"
            :key="item"
            class="fx-fallback-panel__item"
          >
            <span class="fx-fallback-panel__mark"></span>
            <span>{{ t(`fallback.${pageKind}.checks.${item}`) }}</span>
          </div>
        </div>
      </aside>
    </div>
  </section>
</template>

<!--
 * Forgex 缺省状态页
 *
 * 功能描述：
 * 1. 统一展示 403、404、离线等系统缺省状态
 * 2. 提供返回首页、返回上一页、重新连接等恢复操作
 * 3. 保持与 Forgex 工作台深色/浅色主题变量一致
 *
 * @author Forgex
 * @version 1.0
 * @since 2026-05-17
-->
<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

type FallbackPageKind = '403' | '404' | 'offline'

interface FallbackPageConfig {
  code: string
  checks: string[]
}

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const consoleCells = Array.from({ length: 18 }, (_, index) => index + 1)

const pageConfigs: Record<FallbackPageKind, FallbackPageConfig> = {
  '403': {
    code: '403',
    checks: ['permission', 'role', 'tenant'],
  },
  '404': {
    code: '404',
    checks: ['route', 'menu', 'link'],
  },
  offline: {
    code: 'NET',
    checks: ['network', 'gateway', 'retry'],
  },
}

const pageKind = computed<FallbackPageKind>(() => {
  const kind = String(route.params.kind || route.meta.fallbackKind || '404')
  return kind === '403' || kind === 'offline' ? kind : '404'
})

const pageConfig = computed(() => pageConfigs[pageKind.value])

const activeCells = computed(() => {
  if (pageKind.value === '403') return [2, 4, 8, 11, 15]
  if (pageKind.value === 'offline') return [1, 5, 6, 12, 17]
  return [3, 7, 9, 13, 18]
})

function goHome() {
  router.push('/workspace/home').catch(() => {})
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
    return
  }
  goHome()
}

function reloadPage() {
  window.location.reload()
}
</script>

<style scoped lang="less" src="@/styles/views/fallback/index.less"></style>
