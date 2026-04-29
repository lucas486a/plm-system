<template>
  <div class="loading-spinner" :class="{ 'full-screen': fullScreen }">
    <a-spin :size="size" :tip="tip">
      <template #indicator>
        <LoadingOutlined :style="{ fontSize: iconSize }" spin />
      </template>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { LoadingOutlined } from '@ant-design/icons-vue'

interface Props {
  size?: 'small' | 'default' | 'large'
  tip?: string
  fullScreen?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  size: 'default',
  tip: '',
  fullScreen: false,
})

const iconSize = computed(() => {
  switch (props.size) {
    case 'small':
      return '24px'
    case 'large':
      return '48px'
    default:
      return '32px'
  }
})
</script>

<style scoped>
.loading-spinner {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px;
}

.loading-spinner.full-screen {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  z-index: 1000;
}
</style>
