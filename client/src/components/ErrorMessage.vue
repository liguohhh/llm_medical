<script setup>
defineProps({
  message: {
    type: String,
    required: true
  },
  type: {
    type: String,
    default: 'error',
    validator: (value) => ['error', 'warning', 'info', 'success'].includes(value)
  },
  dense: {
    type: Boolean,
    default: true
  }
})
</script>

<template>
  <v-alert
    :type="type"
    class="my-2"
    :density="dense ? 'compact' : undefined"
  >
    {{ message }}
  </v-alert>
</template> 