<template>
  <div class="bom-tree-view">
    <div class="toolbar">
      <a-button @click="$emit('refresh')">
        <template #icon><ReloadOutlined /></template>
        Refresh
      </a-button>
      <a-switch
        v-model:checked="showQuantity"
        checked-children="Show Quantity"
        un-checked-children="Hide Quantity"
        style="margin-left: 8px"
      />
    </div>

    <a-spin :spinning="loading">
      <a-empty v-if="!treeData || treeData.length === 0" description="No BOM tree data available" />
      <a-tree
        v-else
        :tree-data="treeData"
        :field-names="{ key: 'bomItemId', title: 'partNumber', children: 'children' }"
        default-expand-all
        show-line
      >
        <template #title="{ data }">
          <div class="tree-node">
            <span class="node-part-number">{{ data.partNumber }}</span>
            <span class="node-part-name">{{ data.partName }}</span>
            <a-tag v-if="data.revision" color="blue" size="small">Rev {{ data.revision }}</a-tag>
            <span v-if="showQuantity" class="node-quantity">Qty: {{ data.quantity }}</span>
            <a-tag v-if="data.isMounted === false" color="orange" size="small">Not Mounted</a-tag>
            <span v-if="data.designator" class="node-designator">{{ data.designator }}</span>
          </div>
        </template>
      </a-tree>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ReloadOutlined } from '@ant-design/icons-vue'
import type { BOMExplodeNode } from '@/types/bom'

interface Props {
  bomId: number
  treeData: BOMExplodeNode[]
  loading?: boolean
}

defineProps<Props>()

defineEmits<{
  (e: 'refresh'): void
}>()

const showQuantity = ref(true)
</script>

<style scoped>
.bom-tree-view {
  width: 100%;
}

.toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.tree-node {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
}

.node-part-number {
  font-weight: 600;
  color: rgba(0, 0, 0, 0.85);
}

.node-part-name {
  color: rgba(0, 0, 0, 0.65);
}

.node-quantity {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}

.node-designator {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
  font-style: italic;
}
</style>
