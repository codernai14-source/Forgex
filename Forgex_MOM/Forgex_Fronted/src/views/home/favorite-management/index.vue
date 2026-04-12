<!--
  - Copyright 2026 coder_nai@163.com
  -
  - Licensed under the Apache License, Version 2.0 (the "License");
  - you may not use this file except in compliance with the License.
  - You may obtain a copy of the License at
  -
  - http://www.apache.org/licenses/LICENSE-2.0
  -
  - Unless required by applicable law or agreed to in writing, software
  - distributed under the License is distributed on an "AS IS" BASIS,
  - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  - See the License for the specific language governing permissions and
  - limitations under the License.
  -->

<template>
  <div class="favorite-management-page">
	<section class="favorite-management-page__hero">
	  <div>
		<h2 class="favorite-management-page__title">{{ t('personalHomepage.management.title') }}</h2>
		<p class="favorite-management-page__desc">{{ t('personalHomepage.management.desc') }}</p>
	  </div>
	  <a-tag color="blue">{{ t('personalHomepage.management.stats.count', { count: favoriteMenus.length }) }}</a-tag>
	</section>

	<a-alert class="favorite-management-page__alert" type="info" show-icon :message="t('personalHomepage.management.alert')" />

	<section class="favorite-management-page__toolbar">
	  <a-space wrap>
		<a-button :loading="loading" @click="loadFavoriteMenus">
		  <template #icon><ReloadOutlined /></template>
		  {{ t('personalHomepage.management.action.refresh') }}
		</a-button>
		<a-button danger :disabled="selectedRowKeys.length === 0" :loading="batchCanceling" @click="handleBatchCancel">
		  <template #icon><DeleteOutlined /></template>
		  {{ t('personalHomepage.management.action.batchCancel') }}
		</a-button>
		<a-button type="primary" :disabled="!sortChanged" :loading="savingSort" @click="handleSaveSort">
		  <template #icon><SaveOutlined /></template>
		  {{ t('personalHomepage.management.action.saveSort') }}
		</a-button>
	  </a-space>
	</section>

	<section class="favorite-management-page__table-wrap">
	  <a-table :loading="loading" :data-source="favoriteMenus" :pagination="false" :row-selection="rowSelection" row-key="path" size="middle">
		<a-table-column :title="t('personalHomepage.management.table.order')" key="order" width="90">
		  <template #default="{ index }">{{ index + 1 }}</template>
		</a-table-column>

		<a-table-column :title="t('personalHomepage.management.table.menu')" key="menu" min-width="280">
		  <template #default="{ record }">
			<div class="favorite-menu-cell">
			  <span class="favorite-menu-cell__icon"><component :is="getMenuIcon(record.icon)" /></span>
			  <div class="favorite-menu-cell__content">
				<button type="button" class="favorite-menu-cell__title" @click="openMenu(record.path)">
				  {{ record.title }}
				</button>
				<span class="favorite-menu-cell__module">{{ record.moduleName }}</span>
			  </div>
			</div>
		  </template>
		</a-table-column>

		<a-table-column :title="t('personalHomepage.management.table.path')" data-index="path" key="path" ellipsis />

		<a-table-column :title="t('personalHomepage.management.table.action')" key="action" width="260">
		  <template #default="{ record, index }">
			<a-space :size="4" wrap>
			  <a-button size="small" :disabled="index === 0" @click="moveItem(index, -1)">
				<template #icon><ArrowUpOutlined /></template>
				{{ t('personalHomepage.management.action.moveUp') }}
			  </a-button>
			  <a-button size="small" :disabled="index === favoriteMenus.length - 1" @click="moveItem(index, 1)">
				<template #icon><ArrowDownOutlined /></template>
				{{ t('personalHomepage.management.action.moveDown') }}
			  </a-button>
			  <a-button size="small" type="link" @click="openMenu(record.path)">{{ t('personalHomepage.management.action.open') }}</a-button>
			  <a-button size="small" type="link" danger @click="handleSingleCancel(record)">{{ t('personalHomepage.management.action.remove') }}</a-button>
			</a-space>
		  </template>
		</a-table-column>

		<template #emptyText>
		  <a-empty :description="t('personalHomepage.management.empty')" />
		</template>
	  </a-table>
	</section>
  </div>
</template>

<script setup lang="ts">
/**
 * 收藏管理页面。
 * <p>
 * 提供用户收藏菜单的集中管理能力，包括查看全部收藏、调整顺序、单条或批量取消收藏。
 * </p>
 */
import { computed, onMounted, ref } from 'vue'
import type { Component } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { message, Modal } from 'ant-design-vue'
import { ArrowDownOutlined, ArrowUpOutlined, DeleteOutlined, ReloadOutlined, SaveOutlined, StarFilled } from '@ant-design/icons-vue'
import { batchCancelUserFavoriteMenus, getUserFavoriteManageMenus, sortUserFavoriteMenus, type PersonalMenuEntry } from '@/api/system/personalHomepage'
import { PERSONAL_HOME_PATH } from '@/router'
import { getIcon } from '@/utils/icon'

const { t } = useI18n()
const router = useRouter()

const loading = ref(false)
const batchCanceling = ref(false)
const savingSort = ref(false)
const sortChanged = ref(false)
const favoriteMenus = ref<PersonalMenuEntry[]>([])
const selectedRowKeys = ref<string[]>([])

/** 表格勾选配置，用于给批量取消收藏提供路径列表。 */
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: Array<string | number>) => {
	selectedRowKeys.value = keys.map(item => String(item))
  },
}))

/**
 * 解析菜单图标。
 *
 * @param iconName 后端返回的图标名称
 * @returns 可直接渲染的图标组件
 */
function getMenuIcon(iconName?: string): Component {
  return getIcon(iconName) || StarFilled
}

/**
 * 加载收藏菜单列表。
 * <p>
 * 1. 调用后端获取全部收藏菜单；2. 用最新结果覆盖本地列表；3. 过滤无效勾选项。
 * </p>
 */
async function loadFavoriteMenus() {
  loading.value = true
  try {
	const list = await getUserFavoriteManageMenus()
	favoriteMenus.value = Array.isArray(list) ? list : []
	const validPathSet = new Set(favoriteMenus.value.map((item: PersonalMenuEntry) => String(item.path || '')))
	selectedRowKeys.value = selectedRowKeys.value.filter(path => validPathSet.has(path))
	sortChanged.value = false
  } catch (error) {
	console.error('加载收藏管理列表失败:', error)
	message.error(t('personalHomepage.management.message.loadFailed'))
  } finally {
	loading.value = false
  }
}

/**
 * 打开菜单页面。
 *
 * @param path 菜单完整路由路径
 */
function openMenu(path: string) {
  if (!path || path === PERSONAL_HOME_PATH) {
	return
  }
  router.push(path).catch(() => {})
}

/**
 * 调整本地列表顺序。
 *
 * @param index 当前项索引
 * @param step 位移步长，-1 表示上移，1 表示下移
 */
function moveItem(index: number, step: number) {
  const targetIndex = index + step
  if (index < 0 || targetIndex < 0 || index >= favoriteMenus.value.length || targetIndex >= favoriteMenus.value.length) {
	return
  }
  const nextList = favoriteMenus.value.slice()
  const current = nextList[index]
  nextList[index] = nextList[targetIndex]
  nextList[targetIndex] = current
  favoriteMenus.value = nextList
  sortChanged.value = true
}

/** 批量取消收藏。 */
function handleBatchCancel() {
  if (selectedRowKeys.value.length === 0) {
	return
  }
  Modal.confirm({
	title: t('personalHomepage.management.confirm.batchCancelTitle'),
	content: t('personalHomepage.management.confirm.batchCancelContent', { count: selectedRowKeys.value.length }),
	okButtonProps: { danger: true },
	onOk: async () => {
	  batchCanceling.value = true
	  try {
		await batchCancelUserFavoriteMenus(selectedRowKeys.value)
		message.success(t('personalHomepage.management.message.batchCancelSuccess'))
		selectedRowKeys.value = []
		await loadFavoriteMenus()
	  } catch (error) {
		console.error('批量取消收藏失败:', error)
		message.error(t('personalHomepage.management.message.batchCancelFailed'))
	  } finally {
		batchCanceling.value = false
	  }
	},
  })
}

/**
 * 取消单条收藏。
 *
 * @param record 当前收藏菜单记录
 */
function handleSingleCancel(record: PersonalMenuEntry) {
  const path = String(record.path || '')
  if (!path) {
	return
  }
  Modal.confirm({
	title: t('personalHomepage.management.confirm.singleCancelTitle'),
	content: t('personalHomepage.management.confirm.singleCancelContent', { title: record.title || path }),
	okButtonProps: { danger: true },
	onOk: async () => {
	  try {
		await batchCancelUserFavoriteMenus([path])
		message.success(t('personalHomepage.management.message.singleCancelSuccess'))
		await loadFavoriteMenus()
	  } catch (error) {
		console.error('取消单条收藏失败:', error)
		message.error(t('personalHomepage.management.message.singleCancelFailed'))
	  }
	},
  })
}

/** 保存当前排序。 */
async function handleSaveSort() {
  savingSort.value = true
  try {
	await sortUserFavoriteMenus(favoriteMenus.value.map((item: PersonalMenuEntry) => String(item.path || '')))
	message.success(t('personalHomepage.management.message.sortSaveSuccess'))
	sortChanged.value = false
	await loadFavoriteMenus()
  } catch (error) {
	console.error('保存收藏排序失败:', error)
	message.error(t('personalHomepage.management.message.sortSaveFailed'))
  } finally {
	savingSort.value = false
  }
}

onMounted(() => {
  loadFavoriteMenus()
})
</script>

<style scoped lang="less">
.favorite-management-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.favorite-management-page__hero,
.favorite-management-page__toolbar,
.favorite-management-page__table-wrap {
  border-radius: 20px;
  border: 1px solid var(--fx-border-color, #e5e7eb);
  background: var(--fx-bg-container, #ffffff);
  box-shadow: var(--fx-shadow-secondary, 0 12px 32px rgba(15, 23, 42, 0.08));
}

.favorite-management-page__hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 24px;
}

.favorite-management-page__title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.favorite-management-page__desc {
  margin: 8px 0 0;
  color: var(--fx-text-secondary, #6b7280);
}

.favorite-management-page__toolbar,
.favorite-management-page__table-wrap {
  padding: 16px 20px;
}

.favorite-menu-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.favorite-menu-cell__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 12px;
  color: var(--fx-primary, #1677ff);
  background: color-mix(in srgb, var(--fx-primary-bg, #eff6ff) 72%, var(--fx-bg-elevated, #f8fafc));
}

.favorite-menu-cell__content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.favorite-menu-cell__title {
  width: fit-content;
  max-width: 100%;
  padding: 0;
  border: none;
  background: transparent;
  color: var(--fx-text-primary, #111827);
  font-weight: 600;
  cursor: pointer;
  text-align: left;
}

.favorite-menu-cell__title:hover {
  color: var(--fx-primary, #1677ff);
}

.favorite-menu-cell__module {
  color: var(--fx-text-secondary, #6b7280);
  font-size: 12px;
}

@media (max-width: 768px) {
  .favorite-management-page__hero {
	flex-direction: column;
  }
}
</style>

