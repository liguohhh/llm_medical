<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'
import DirectionTable from '@/components/admin/DirectionTable.vue'

const userStore = useUserStore()
const user = computed(() => userStore.user)
</script>

<template>
  <v-container>
    <v-row>
      <v-col cols="12">
        <v-card class="mb-6" flat>
          <div 
            class="pa-5 rounded-t-lg" 
            style="background: linear-gradient(to right, #1976D2, #64B5F6);"
          >
            <div class="d-flex align-center">
              <v-icon icon="mdi-hospital-building" size="large" color="white" class="me-3"></v-icon>
              <div>
                <h1 class="text-h4 font-weight-bold text-white mb-1">医疗方向管理</h1>
                <p class="text-subtitle-1 text-white text-opacity-90 mb-0">
                  管理系统中的医疗专科方向
                </p>
              </div>
            </div>
          </div>
        </v-card>
      </v-col>
    </v-row>
    
    <v-row>
      <v-col cols="12">
        <direction-table />
      </v-col>
    </v-row>
  </v-container>
</template>

<style scoped>
.v-container {
  max-width: 1400px;
  margin: 0 auto;
}
</style> 