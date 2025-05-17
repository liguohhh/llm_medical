<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'
import AgentTable from '@/components/admin/AgentTable.vue'

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
              <v-icon icon="mdi-robot" size="large" color="white" class="me-3"></v-icon>
              <div>
                <h1 class="text-h4 font-weight-bold text-white mb-1">智能体管理</h1>
                <p class="text-subtitle-1 text-white text-opacity-90 mb-0">
                  管理系统中的智能体配置和关联
                </p>
              </div>
            </div>
          </div>
        </v-card>
      </v-col>
    </v-row>
    
    <v-row>
      <v-col cols="12">
        <agent-table />
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