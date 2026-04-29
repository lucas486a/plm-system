<template>
  <div class="empty-state">
    <a-empty :description="null">
      <template #image>
        <component :is="iconComponent" :style="{ fontSize: '64px', color: '#bfbfbf' }" />
      </template>
      <h3 class="empty-title">{{ title }}</h3>
      <p class="empty-description">{{ description }}</p>
      <div v-if="showAction" class="empty-action">
        <slot name="action">
          <a-button type="primary" @click="$emit('action')">
            {{ actionText }}
          </a-button>
        </slot>
      </div>
    </a-empty>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  InboxOutlined,
  FileOutlined,
  SearchOutlined,
  FolderOpenOutlined,
} from '@ant-design/icons-vue'

interface Props {
  title?: string
  description?: string
  icon?: 'inbox' | 'file' | 'search' | 'folder'
  showAction?: boolean
  actionText?: string
}

const props = withDefaults(defineProps<Props>(), {
  title: 'No Data',
  description: 'No data available',
  icon: 'inbox',
  showAction: false,
  actionText: 'Create New',
})

defineEmits<{
  (e: 'action'): void
}>()

const iconComponent = computed(() => {
  switch (props.icon) {
    case 'file':
      return FileOutlined
    case 'search':
      return SearchOutlined
    case 'folder':
      return FolderOpenOutlined
    default:
      return InboxOutlined
  }
})
</script>

<style scoped>
.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 20px;
}

.empty-title {
  margin: 16px 0 8px;
  font-size: 16px;
  font-weight: 500;
  color: rgba(0, 0, 0, 0.85);
}

.empty-description {
  margin: 0 0 16px;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.45);
}

.empty-action {
  margin-top: 16px;
}
</style>
