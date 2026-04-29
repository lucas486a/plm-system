<template>
  <div class="page-header">
    <div class="header-left">
      <a-breadcrumb v-if="breadcrumbs.length > 0" class="breadcrumbs">
        <a-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="index">
          <router-link v-if="item.path" :to="item.path">{{ item.label }}</router-link>
          <span v-else>{{ item.label }}</span>
        </a-breadcrumb-item>
      </a-breadcrumb>
      <h2 class="page-title">{{ title }}</h2>
      <p v-if="subtitle" class="page-subtitle">{{ subtitle }}</p>
    </div>
    <div class="header-right">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup lang="ts">
interface Breadcrumb {
  label: string
  path?: string
}

interface Props {
  title: string
  subtitle?: string
  breadcrumbs?: Breadcrumb[]
}

withDefaults(defineProps<Props>(), {
  subtitle: '',
  breadcrumbs: () => [],
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.header-left {
  flex: 1;
}

.breadcrumbs {
  margin-bottom: 8px;
}

.page-title {
  margin: 0 0 4px 0;
  font-size: 20px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.85);
}

.page-subtitle {
  margin: 0;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.45);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
