<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  value: {
    type: [Number, String],
    required: true
  },
  icon: {
    type: String,
    default: 'mdi-chart-bar'
  },
  color: {
    type: String,
    default: 'primary'
  }
})
</script>

<template>
  <v-card :color="color" variant="tonal" class="stat-card">
    <v-card-text>
      <div class="d-flex justify-space-between align-center">
        <div>
          <div class="text-overline mb-1">{{ title }}</div>
          <div class="text-h4">{{ value }}</div>
        </div>
        <v-icon :icon="icon" size="x-large"></v-icon>
      </div>
    </v-card-text>
  </v-card>
</template>

<style scoped>
.stat-card {
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}
</style> 