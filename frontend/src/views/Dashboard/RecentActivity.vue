<template>
  <a-card title="Recent Activity" :bordered="false">
    <a-timeline v-if="activities.length > 0">
      <a-timeline-item
        v-for="activity in activities"
        :key="activity.id"
        :color="getTimelineColor(activity.action)"
      >
        <p class="activity-text">
          <strong>{{ activity.action }}</strong> on {{ activity.entityType }}
          #{{ activity.entityId }}
        </p>
        <p class="activity-time">{{ formatTime(activity.timestamp) }}</p>
      </a-timeline-item>
    </a-timeline>
    <a-empty v-else description="No recent activity" />
  </a-card>
</template>

<script setup lang="ts">
import type { AuditLogDTO } from '@/types'

interface Props {
  activities: AuditLogDTO[]
}

defineProps<Props>()

function getTimelineColor(action: string): string {
  const colorMap: Record<string, string> = {
    CREATE: 'green',
    UPDATE: 'blue',
    DELETE: 'red',
    APPROVE: 'orange',
    REJECT: 'gray',
  }
  return colorMap[action.toUpperCase()] || 'blue'
}

function formatTime(timestamp: string): string {
  const date = new Date(timestamp)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)

  if (diffMins < 1) return 'Just now'
  if (diffMins < 60) return `${diffMins}m ago`
  if (diffHours < 24) return `${diffHours}h ago`
  if (diffDays < 7) return `${diffDays}d ago`
  return date.toLocaleDateString()
}
</script>

<style scoped>
.activity-text {
  margin: 0;
  font-size: 14px;
}

.activity-time {
  margin: 4px 0 0;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}
</style>
