<template>
  <div class="component-config-page">
    <section class="component-config-page__hero">
      <div>
        <h2 class="component-config-page__title">{{ t('personalHomepage.componentConfig.title') }}</h2>
        <p class="component-config-page__desc">{{ t('personalHomepage.componentConfig.desc') }}</p>
      </div>
      <a-tag color="blue">{{ t('personalHomepage.componentConfig.stats.count', { count: componentGroups.length }) }}</a-tag>
    </section>

    <section class="component-config-page__toolbar">
      <a-space wrap>
        <a-input-search
          v-model:value="keyword"
          allow-clear
          style="width: 320px"
          :placeholder="t('personalHomepage.library.searchPlaceholder')"
          @search="loadComponents"
        />
        <a-radio-group v-model:value="scopeFilter" button-style="solid" @change="loadComponents">
          <a-radio-button value="ALL">{{ t('personalHomepage.library.scopeAll') }}</a-radio-button>
          <a-radio-button value="PUBLIC">{{ t('personalHomepage.library.scopePublic') }}</a-radio-button>
          <a-radio-button value="TENANT">{{ t('personalHomepage.library.scopeTenant') }}</a-radio-button>
          <a-radio-button value="USER">{{ t('personalHomepage.library.scopeUser') }}</a-radio-button>
        </a-radio-group>
        <a-button :loading="loading" @click="loadComponents">
          <template #icon><ReloadOutlined /></template>
          {{ t('common.refresh') }}
        </a-button>
      </a-space>
    </section>

    <section class="component-config-page__body">
      <a-spin :spinning="loading">
        <a-empty v-if="componentGroups.length === 0" :description="t('personalHomepage.componentConfig.empty')" />
        <a-collapse v-else ghost>
          <a-collapse-panel v-for="group in componentGroups" :key="group.key">
            <template #header>
              <div class="component-config-page__group-header">
                <h3>{{ group.label }}</h3>
                <span>{{ group.items.length }}</span>
              </div>
            </template>
            <div class="component-config-page__grid">
              <article
                v-for="item in group.items"
                :key="item.componentCode"
                class="component-config-card"
                :class="{ 'component-config-card--removed': item.removed }"
              >
                <header class="component-config-card__header">
                  <div class="component-config-card__icon">
                    <FxIcon :name="item.icon" :size="18" />
                  </div>
                  <div class="component-config-card__title">
                    <strong>{{ item.componentName }}</strong>
                    <span>{{ item.componentCode }}</span>
                  </div>
                </header>
                <p class="component-config-card__desc">{{ item.useDesc || item.remark || '-' }}</p>
                <div class="component-config-card__meta">
                  <a-tag v-if="item.scopeLevel">{{ item.scopeLevel }}</a-tag>
                  <a-tag v-if="item.favorite" color="gold">{{ t('personalHomepage.library.favorite') }}</a-tag>
                  <a-tag v-if="item.selected && !item.removed" color="green">{{ t('personalHomepage.library.selected') }}</a-tag>
                  <a-tag v-if="item.removed" color="red">{{ t('personalHomepage.library.removed') }}</a-tag>
                </div>
                <div class="component-config-card__actions">
                  <a-button size="small" @click="toggleFavorite(item)">
                    <template #icon>
                      <StarFilled v-if="item.favorite" />
                      <StarOutlined v-else />
                    </template>
                  </a-button>
                  <a-button
                    size="small"
                    type="primary"
                    :disabled="item.selected && !item.removed"
                    @click="addToHomepage(item)"
                  >
                    <template #icon><PlusOutlined /></template>
                  </a-button>
                  <a-button
                    size="small"
                    danger
                    :disabled="item.removed"
                    @click="removeFromHomepage(item)"
                  >
                    <template #icon><DeleteOutlined /></template>
                  </a-button>
                </div>
              </article>
            </div>
          </a-collapse-panel>
        </a-collapse>
      </a-spin>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { DeleteOutlined, PlusOutlined, ReloadOutlined, StarFilled, StarOutlined } from '@ant-design/icons-vue'
import { useI18n } from 'vue-i18n'
import FxIcon from '@/components/common/FxIcon.vue'
import {
  addHomepageComponent,
  favoriteHomepageComponent,
  listPersonalHomepageComponents,
  removeHomepageComponent,
  type HomepageComponentVO,
} from '@/api/system/personalHomepage'

const { t } = useI18n()
const loading = ref(false)
const keyword = ref('')
const scopeFilter = ref<'ALL' | 'PUBLIC' | 'TENANT' | 'USER'>('ALL')
const componentList = ref<HomepageComponentVO[]>([])

const componentGroups = computed(() => {
  const groups = new Map<string, { key: string; label: string; items: HomepageComponentVO[] }>()
  for (const item of componentList.value) {
    if (scopeFilter.value !== 'ALL' && item.scopeLevel !== scopeFilter.value) {
      continue
    }
    const text = keyword.value.trim().toLowerCase()
    if (text) {
      const matched = [item.componentName, item.componentCode, item.categoryName, item.categoryCode]
        .filter(Boolean)
        .some(value => String(value).toLowerCase().includes(text))
      if (!matched) {
        continue
      }
    }
    const key = item.categoryCode || item.categoryName || 'default'
    if (!groups.has(key)) {
      groups.set(key, {
        key,
        label: item.categoryName || item.categoryCode || t('personalHomepage.library.defaultGroup'),
        items: [],
      })
    }
    groups.get(key)!.items.push(item)
  }
  return Array.from(groups.values()).map(group => ({
    ...group,
    items: group.items.sort((left, right) => (Number(left.orderNum || 0) - Number(right.orderNum || 0)) || left.componentCode.localeCompare(right.componentCode)),
  }))
})

async function loadComponents() {
  loading.value = true
  try {
    const list = await listPersonalHomepageComponents({
      keyword: keyword.value || undefined,
      scopeLevel: scopeFilter.value === 'ALL' ? undefined : scopeFilter.value,
    })
    componentList.value = Array.isArray(list) ? list : []
  } finally {
    loading.value = false
  }
}

async function toggleFavorite(item: HomepageComponentVO) {
  await favoriteHomepageComponent({
    componentCode: item.componentCode,
    favorite: !item.favorite,
  })
  await loadComponents()
}

async function addToHomepage(item: HomepageComponentVO) {
  await addHomepageComponent({ componentCode: item.componentCode })
  message.success(t('personalHomepage.componentConfig.addSuccess'))
  await loadComponents()
}

async function removeFromHomepage(item: HomepageComponentVO) {
  await removeHomepageComponent({ componentCode: item.componentCode })
  message.success(t('personalHomepage.componentConfig.removeSuccess'))
  await loadComponents()
}

onMounted(loadComponents)
</script>

<style scoped lang="less" src="@/styles/views/home/component-config/index.less"></style>
