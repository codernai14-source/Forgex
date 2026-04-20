<template>
  <div class="personal-home-view">
    <PersonalHomepageDesigner mode="current" />
  </div>
</template>

<script setup lang="ts">
/**
 * 首页入口页。
 * 当前页面仅负责承载并渲染用户个人主页设计器。
 */
import PersonalHomepageDesigner from '@/components/personal-homepage/PersonalHomepageDesigner.vue'
</script>

<style scoped>
.personal-home-view {
  /* 让个人主页设计器在主布局内容区内撑满高度。 */
  min-height: 100%;
}
</style>
