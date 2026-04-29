<template>
  <a-tag :color="statusColor" class="status-badge">
    {{ statusLabel }}
  </a-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  status: string
  statusMap?: Record<string, { color: string; label: string }>
}

const props = withDefaults(defineProps<Props>(), {
  statusMap: () => ({
    draft: { color: 'default', label: 'Draft' },
    pending: { color: 'processing', label: 'Pending' },
    approved: { color: 'success', label: 'Approved' },
    rejected: { color: 'error', label: 'Rejected' },
    active: { color: 'success', label: 'Active' },
    inactive: { color: 'default', label: 'Inactive' },
    in_progress: { color: 'processing', label: 'In Progress' },
    completed: { color: 'success', label: 'Completed' },
    cancelled: { color: 'error', label: 'Cancelled' },
  }),
})

const statusColor = computed(() => {
  const mapping = props.statusMap[props.status.toLowerCase()]
  return mapping?.color || 'default'
})

const statusLabel = computed(() => {
  const mapping = props.statusMap[props.status.toLowerCase()]
  return mapping?.label || props.status
})
</script>

<style scoped>
.status-badge {
  text-transform: capitalize;
}
</style>
