<template>
  <a-card class="stat-card" :bordered="false">
    <a-statistic
      :title="title"
      :value="value"
      :value-style="{ color: color }"
    >
      <template #prefix>
        <component :is="icon" />
      </template>
    </a-statistic>
  </a-card>
</template>

<script setup lang="ts">
import type { Component } from 'vue'

interface Props {
  title: string
  value: number
  icon: Component
  color?: string
}

withDefaults(defineProps<Props>(), {
  color: '#1890ff',
})
</script>

<style scoped>
.stat-card {
  text-align: center;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
</style>
