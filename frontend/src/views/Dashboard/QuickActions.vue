<template>
  <a-card title="Quick Actions" :bordered="false">
    <div class="actions-grid">
      <a-button
        v-for="action in actions"
        :key="action.label"
        type="default"
        size="large"
        class="action-btn"
        @click="handleAction(action.route)"
      >
        <template #icon>
          <component :is="action.icon" />
        </template>
        {{ action.label }}
      </a-button>
    </div>
  </a-card>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import {
  PlusOutlined,
  FileTextOutlined,
  SwapOutlined,
  SearchOutlined,
} from '@ant-design/icons-vue'
import type { Component } from 'vue'

interface ActionItem {
  label: string
  icon: Component
  route: string
}

const router = useRouter()

const actions: ActionItem[] = [
  { label: 'New Part', icon: PlusOutlined, route: '/parts' },
  { label: 'New ECR', icon: FileTextOutlined, route: '/ecrs' },
  { label: 'New ECO', icon: SwapOutlined, route: '/ecos' },
  { label: 'Search', icon: SearchOutlined, route: '/parts' },
]

function handleAction(route: string): void {
  router.push(route)
}
</script>

<style scoped>
.actions-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.action-btn {
  height: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.action-btn:hover {
  border-color: #1890ff;
  color: #1890ff;
}
</style>
