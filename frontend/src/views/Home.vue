<template>
  <div class="home">
    <a-typography-title>PLM System</a-typography-title>
    <a-typography-paragraph>
      Welcome to the Product Lifecycle Management System.
    </a-typography-paragraph>
    <HelloWorld msg="PLM System" />
  </div>
</template>

<script setup lang="ts">
import HelloWorld from '@/components/HelloWorld.vue'
</script>

<style scoped>
.home {
  padding: 24px;
  text-align: center;
}
</style>
