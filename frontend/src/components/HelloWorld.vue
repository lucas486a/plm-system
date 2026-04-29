<template>
  <div class="hello">
    <a-card :title="msg">
      <p>This is the PLM System frontend built with Vue 3 + TypeScript + Vite.</p>
      <a-space>
        <a-button type="primary">Get Started</a-button>
        <a-button>Learn More</a-button>
      </a-space>
    </a-card>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  msg: string
}>()
</script>

<style scoped>
.hello {
  max-width: 600px;
  margin: 0 auto;
}
</style>
