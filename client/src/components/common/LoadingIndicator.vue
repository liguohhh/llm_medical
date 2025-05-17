<script setup>
// 定义props
const props = defineProps({
  size: {
    type: String,
    default: 'medium' // small, medium, large
  },
  color: {
    type: String,
    default: 'primary'
  },
  text: {
    type: String,
    default: '加载中...'
  },
  showText: {
    type: Boolean,
    default: true
  },
  centered: {
    type: Boolean,
    default: true
  },
  loading: {
    type: Boolean,
    default: true
  },
  overlay: {
    type: Boolean,
    default: false
  }
})

// 计算尺寸
const spinnerSize = {
  small: 24,
  medium: 36,
  large: 48
}[props.size] || 36
</script>

<template>
  <div 
    v-if="loading"
    class="loading-indicator" 
    :class="{ 
      'loading-centered': centered,
      'loading-overlay': overlay
    }"
  >
    <v-progress-circular
      :size="spinnerSize"
      :color="color"
      indeterminate
    ></v-progress-circular>
    <span v-if="showText" class="loading-text" :class="`text-${color}`">
      {{ text }}
    </span>
  </div>
</template>

<style scoped>
.loading-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
}

.loading-centered {
  justify-content: center;
  min-height: 120px;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.8);
  z-index: 100;
}

.loading-text {
  margin-top: 12px;
  font-size: 0.875rem;
}
</style> 