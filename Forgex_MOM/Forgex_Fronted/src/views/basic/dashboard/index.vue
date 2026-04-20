<template>
  <div class="basic-dashboard">
    <section class="hero-panel">
      <div class="hero-panel__meta">
        <a-tag color="processing">Basic</a-tag>
        <span>基础信息管理</span>
      </div>
      <h1 class="hero-panel__title">基础信息管理首页</h1>
      <p class="hero-panel__desc">
        当前页面作为基础信息管理模块的默认落地页，先把模块入口稳定接通，后续可以继续承接物料、主数据和其他基础资料能力。
      </p>
    </section>

    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :md="8">
        <a-card :bordered="false" class="status-card">
          <template #title>
            <span class="status-card__title">
              <CheckCircleOutlined />
              页面状态
            </span>
          </template>
          <div class="status-card__value">首页已可访问</div>
          <p class="status-card__desc">顶部模块切换会优先进入当前首页，不再落到不可渲染的目录路由。</p>
        </a-card>
      </a-col>

      <a-col :xs="24" :md="8">
        <a-card :bordered="false" class="status-card">
          <template #title>
            <span class="status-card__title">
              <AppstoreOutlined />
              模块定位
            </span>
          </template>
          <div class="status-card__value">基础数据入口</div>
          <p class="status-card__desc">适合作为物料、编码、主数据和字典类页面的统一导航与状态说明页。</p>
        </a-card>
      </a-col>

      <a-col :xs="24" :md="8">
        <a-card :bordered="false" class="status-card">
          <template #title>
            <span class="status-card__title">
              <FlagOutlined />
              当前阶段
            </span>
          </template>
          <div class="status-card__value">空白占位主页</div>
          <p class="status-card__desc">先保证模块从菜单到页面链路打通，后续可以按业务逐步补充卡片、统计和快捷入口。</p>
        </a-card>
      </a-col>
    </a-row>

    <a-card :bordered="false" class="guide-card">
      <div class="guide-card__header">
        <ContainerOutlined class="guide-card__icon" />
        <div>
          <h2 class="guide-card__title">下一步建议</h2>
          <p class="guide-card__subtitle">如果后面继续补基础信息模块，可以优先沿着这条链路往下接。</p>
        </div>
      </div>

      <ul class="guide-list">
        <li>菜单 `component_key` 尽量和前端组件目录保持一一对应，例如 `BasicDashboard`、`BasicMaterial`。</li>
        <li>模块首页建议固定使用 `dashboard` 路径，方便顶部模块切换时统一回落到默认入口。</li>
        <li>后续新增业务页时，直接补到 `src/views/basic/...` 下，现有动态路由解析就能自动接住。</li>
      </ul>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import {
  AppstoreOutlined,
  CheckCircleOutlined,
  ContainerOutlined,
  FlagOutlined
} from '@ant-design/icons-vue'
</script>

<style scoped>
.basic-dashboard {
  min-height: 100%;
  padding: 20px;
  box-sizing: border-box;
  background:
    radial-gradient(circle at top left, color-mix(in srgb, var(--fx-primary, #1677ff) 12%, transparent), transparent 34%),
    linear-gradient(180deg, var(--fx-bg-layout, #f8fafc) 0%, color-mix(in srgb, var(--fx-bg-layout, #f8fafc) 88%, var(--fx-bg-container, #ffffff)) 100%);
}

.hero-panel {
  padding: 24px 28px;
  border-radius: 18px;
  margin-bottom: 18px;
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--fx-primary, #1677ff) 14%, var(--fx-bg-container, #ffffff)) 0%, var(--fx-bg-container, #ffffff) 68%),
    var(--fx-bg-container, #ffffff);
  border: 1px solid color-mix(in srgb, var(--fx-border-color, #e5e7eb) 84%, transparent);
  box-shadow: var(--fx-shadow-secondary, 0 12px 28px rgba(15, 23, 42, 0.08));
}

.hero-panel__meta {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: var(--fx-text-secondary, #6b7280);
  margin-bottom: 14px;
}

.hero-panel__title {
  margin: 0 0 10px;
  font-size: 28px;
  line-height: 1.2;
  color: var(--fx-text-primary, #111827);
}

.hero-panel__desc {
  margin: 0;
  max-width: 760px;
  color: var(--fx-text-secondary, #6b7280);
  font-size: 14px;
  line-height: 1.8;
}

.status-card {
  height: 100%;
  border-radius: 16px;
  border: 1px solid var(--fx-border-color, #e5e7eb);
  box-shadow: var(--fx-shadow-secondary, 0 10px 24px rgba(15, 23, 42, 0.06));
  background: var(--fx-bg-container, #ffffff);
}

.status-card__title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.status-card__value {
  font-size: 22px;
  font-weight: 600;
  color: var(--fx-text-primary, #111827);
  margin-bottom: 8px;
}

.status-card__desc {
  margin: 0;
  color: var(--fx-text-secondary, #6b7280);
  line-height: 1.7;
}

.guide-card {
  margin-top: 18px;
  border-radius: 18px;
  border: 1px solid var(--fx-border-color, #e5e7eb);
  box-shadow: var(--fx-shadow-secondary, 0 10px 24px rgba(15, 23, 42, 0.06));
  background: var(--fx-bg-container, #ffffff);
}

.guide-card__header {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  margin-bottom: 18px;
}

.guide-card__icon {
  margin-top: 2px;
  font-size: 22px;
  color: var(--fx-primary, #1677ff);
}

.guide-card__title {
  margin: 0 0 6px;
  font-size: 18px;
  color: var(--fx-text-primary, #111827);
}

.guide-card__subtitle {
  margin: 0;
  color: var(--fx-text-secondary, #6b7280);
}

.guide-list {
  margin: 0;
  padding-left: 18px;
  color: var(--fx-text-secondary, #4b5563);
  line-height: 1.9;
}

@media (max-width: 768px) {
  .basic-dashboard {
    padding: 16px;
  }

  .hero-panel {
    padding: 20px;
  }

  .hero-panel__title {
    font-size: 24px;
  }
}
</style>
