<template>
  <a-config-provider>
    <ErrorBoundary>
      <router-view />
    </ErrorBoundary>
  </a-config-provider>
</template>

<script setup lang="ts">
import ErrorBoundary from '@/components/ErrorBoundary.vue'
</script>
