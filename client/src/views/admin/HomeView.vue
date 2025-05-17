<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import SystemStatsPanel from '@/components/admin/SystemStatsPanel.vue'

const userStore = useUserStore()
const user = ref(userStore.user)

onMounted(() => {
  // 可以在这里加载管理员相关的初始数据
})
</script>

<template>
  <v-container>
    <v-row>
      <v-col cols="12">
        <v-card class="mb-6" flat>
          <div 
            class="pa-5 text-center rounded-t-lg" 
            style="background: linear-gradient(to right, #1976D2, #64B5F6);"
          >
            <h1 class="text-h3 font-weight-bold text-white mb-1">欢迎回来，{{ user?.realName }}管理员</h1>
            <p class="text-subtitle-1 text-white text-opacity-90 mb-0">
              医疗辅助问答系统管理后台
            </p>
          </div>
        </v-card>
      </v-col>
    </v-row>
    
    <system-stats-panel />
    
    <v-row>
      <v-col cols="12">
        <v-card class="navigation-card">
          <v-card-title class="text-h5 d-flex align-center py-4 px-6">
            <v-icon icon="mdi-compass" size="large" class="me-2 text-primary"></v-icon>
            快速导航
          </v-card-title>
          <v-card-text class="pa-6">
            <v-row>
              <v-col cols="12" sm="6" md="3">
                <v-btn
                  block
                  size="large"
                  color="primary"
                  variant="elevated"
                  prepend-icon="mdi-account-group"
                  class="mb-2 navigation-btn"
                  to="/admin/users"
                >
                  用户管理
                </v-btn>
              </v-col>
              
              <v-col cols="12" sm="6" md="3">
                <v-btn
                  block
                  size="large"
                  color="success"
                  variant="elevated"
                  prepend-icon="mdi-hospital-building"
                  class="mb-2 navigation-btn"
                  to="/admin/directions"
                >
                  医疗方向管理
                </v-btn>
              </v-col>
              
              <v-col cols="12" sm="6" md="3">
                <v-btn
                  block
                  size="large"
                  color="info"
                  variant="elevated"
                  prepend-icon="mdi-robot"
                  class="mb-2 navigation-btn"
                  to="/admin/agents"
                >
                  智能体管理
                </v-btn>
              </v-col>
              
              <v-col cols="12" sm="6" md="3">
                <v-btn
                  block
                  size="large"
                  color="deep-purple"
                  variant="elevated"
                  prepend-icon="mdi-file-document-outline"
                  class="mb-2 navigation-btn"
                  to="/admin/templates"
                >
                  提示词模板
                </v-btn>
              </v-col>
            </v-row>
            
            <v-row>
              
              <v-col cols="12" sm="6" md="3">
                <v-btn
                  block
                  size="large"
                  color="indigo"
                  variant="elevated"
                  prepend-icon="mdi-database"
                  class="mb-2 navigation-btn"
                  to="/admin/vector"
                >
                  向量数据库
                </v-btn>
              </v-col>
              
              <v-col cols="12" sm="6" md="3">
                <v-btn
                  block
                  size="large"
                  color="teal"
                  variant="elevated"
                  prepend-icon="mdi-database-search"
                  class="mb-2 navigation-btn"
                  to="/admin/precise"
                >
                  精确查找数据库
                </v-btn>
              </v-col>
            </v-row>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<style scoped>
.v-container {
  max-width: 1400px;
  margin: 0 auto;
}

.navigation-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.navigation-btn {
  transition: transform 0.2s, box-shadow 0.2s;
}

.navigation-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}
</style> 