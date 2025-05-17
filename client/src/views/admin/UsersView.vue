<script setup>
import { ref, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import UserTable from '@/components/admin/UserTable.vue'

const userStore = useUserStore()
const user = computed(() => userStore.user)

// 当前选中的用户类型：0-病人，1-医生
const activeTab = ref(0)

// 切换标签
const switchTab = (tab) => {
  activeTab.value = tab
}
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
              <div>
                <div class="d-flex align-center">
                  <v-icon icon="mdi-account-group" size="large" color="white" class="me-3"></v-icon>
                  <h1 class="text-h4 font-weight-bold text-white mb-1">用户管理</h1>
                </div>
                <p class="text-subtitle-1 text-white text-opacity-90 mb-0">
                  管理系统中的病人和医生用户
                </p>
              </div>
              <v-spacer></v-spacer>
              <v-tabs
                :model-value="activeTab"
                @update:model-value="activeTab = $event"
                color="white"
                align-tabs="center"
                class="user-tabs"
                bg-color="transparent"
              >
                <v-tab :value="0" @click="switchTab(0)" class="text-white">
                  <v-icon start icon="mdi-account-multiple"></v-icon>
                  病人管理
                </v-tab>
                <v-tab :value="1" @click="switchTab(1)" class="text-white">
                  <v-icon start icon="mdi-doctor"></v-icon>
                  医生管理
                </v-tab>
              </v-tabs>
            </div>
          </div>
        </v-card>
      </v-col>
    </v-row>
    
    <v-row>
      <v-col cols="12">
        <v-window :model-value="activeTab" @update:model-value="activeTab = $event" class="user-window">
          <v-window-item :value="0">
            <user-table :user-type="0" />
          </v-window-item>
          
          <v-window-item :value="1">
            <user-table :user-type="1" />
          </v-window-item>
        </v-window>
      </v-col>
    </v-row>
  </v-container>
</template>

<style scoped>
.v-container {
  max-width: 1400px;
  margin: 0 auto;
}

.user-tabs {
  min-width: 300px;
}

.user-window {
  min-height: 600px;
}

:deep(.v-tab) {
  opacity: 0.8;
}

:deep(.v-tab--selected) {
  opacity: 1;
}

:deep(.v-tab:hover) {
  opacity: 1;
}
</style> 