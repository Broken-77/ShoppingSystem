<template>
  <div class="app-shell">
    <AppNav />
    <main>
      <RouterView v-slot="{ Component }">
        <keep-alive :include="['ProductListView']">
          <component :is="Component" />
        </keep-alive>
      </RouterView>
    </main>
  </div>
</template>

<script setup>
import { RouterView } from 'vue-router'
import AppNav from './components/AppNav.vue'
</script>
