<template>
  <router-view />
  <div v-if="loaderOpen" style="position:fixed;inset:0;z-index:2000;background:rgba(255,255,255,0.12);display:flex;align-items:center;justify-content:center;backdrop-filter:blur(2px)">
    <video src="/loading.mp4" autoplay muted loop style="max-width:70%;max-height:70%;opacity:0.85;border-radius:12px;box-shadow:0 8px 32px rgba(0,0,0,0.45)"></video>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
const loaderOpen = ref(false)
onMounted(() => {
  (window as any).__globalLoader = {
    show: () => { loaderOpen.value = true },
    hide: () => { loaderOpen.value = false }
  }
})
</script>

<style>
html, body, #app {
  height: 100%;
  overflow: hidden;
}
</style>
