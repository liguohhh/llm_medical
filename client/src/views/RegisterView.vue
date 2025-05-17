<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/services/api'
import { validationRules } from '@/components/FormValidation'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)

// 注册表单数据
const registerForm = ref({
  username: '',
  password: '',
  realName: '',
  gender: 1, // 默认男性
  age: null
})

// 错误信息
const errorMessage = ref('')
const successMessage = ref('')
const loading = ref(false)

// 处理注册请求
const handleRegister = async () => {
  // 验证表单
  const { valid } = await formRef.value.validate()
  if (!valid) return

  loading.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    const response = await authApi.register(registerForm.value)
    
    if (response.code === 200) {
      // 注册成功
      successMessage.value = '注册成功，即将跳转到登录页面...'
      
      // 3秒后跳转到登录页面
      setTimeout(() => {
        router.push('/login')
      }, 3000)
    } else {
      // 注册失败
      errorMessage.value = response.message || '注册失败'
    }
  } catch (error) {
    console.error('注册出错:', error)
    errorMessage.value = '注册过程中发生错误'
  } finally {
    loading.value = false
  }
}

// 返回登录页面
const goToLogin = () => {
  router.push('/login')
}
</script>

<template>
  <v-container fluid fill-height class="register-container">
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="6" lg="4">
        <v-card class="register-card">
          <div 
            class="pa-5 text-center rounded-t-lg" 
            style="background: linear-gradient(to right, #1976D2, #64B5F6);"
          >
            <h1 class="text-h4 font-weight-bold text-white mb-1">用户注册</h1>
            <p class="text-subtitle-1 text-white text-opacity-90 mb-0">
              欢迎您的注册
            </p>
          </div>
          
          <v-card-text class="pa-6 pt-8">
            <v-form @submit.prevent="handleRegister" ref="formRef">
              <v-text-field
                v-model="registerForm.username"
                label="用户名"
                prepend-icon="mdi-account"
                :rules="validationRules.username"
                hint="4-20个字符"
                variant="outlined"
                density="comfortable"
                bg-color="grey-lighten-5"
                class="mb-3"
                required
              ></v-text-field>
              
              <v-text-field
                v-model="registerForm.password"
                label="密码"
                prepend-icon="mdi-lock"
                :rules="validationRules.password"
                type="password"
                hint="6-20个字符"
                variant="outlined"
                density="comfortable"
                bg-color="grey-lighten-5"
                class="mb-3"
                required
              ></v-text-field>
              
              <v-text-field
                v-model="registerForm.realName"
                label="姓名"
                prepend-icon="mdi-account-circle"
                :rules="validationRules.realName"
                variant="outlined"
                density="comfortable"
                bg-color="grey-lighten-5"
                class="mb-3"
                required
              ></v-text-field>
              
              <v-radio-group
                v-model="registerForm.gender"
                label="性别"
                inline
                class="mb-3"
                required
              >
                <v-radio label="男" :value="1"></v-radio>
                <v-radio label="女" :value="2"></v-radio>
              </v-radio-group>
              
              <v-text-field
                v-model="registerForm.age"
                label="年龄"
                prepend-icon="mdi-calendar"
                :rules="validationRules.age"
                type="number"
                variant="outlined"
                density="comfortable"
                bg-color="grey-lighten-5"
                required
              ></v-text-field>
              
              <v-alert
                v-if="errorMessage"
                type="error"
                class="mt-4"
                density="compact"
                variant="tonal"
                border="start"
              >
                {{ errorMessage }}
              </v-alert>
              
              <v-alert
                v-if="successMessage"
                type="success"
                class="mt-4"
                density="compact"
                variant="tonal"
                border="start"
              >
                {{ successMessage }}
              </v-alert>
              
              <div class="d-flex flex-column gap-4 mt-6">
                <v-btn
                  color="primary"
                  size="large"
                  block
                  type="submit"
                  :loading="loading"
                  elevation="2"
                >
                  注册
                </v-btn>
                
                <v-btn
                  variant="text"
                  @click="goToLogin"
                  class="mt-2"
                >
                  已有账号？返回登录
                </v-btn>
              </div>
            </v-form>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<style scoped>
.register-container {
  background-color: #f5f7fa;
  min-height: 100vh;
  background-image: linear-gradient(135deg, rgba(25, 118, 210, 0.05) 0%, rgba(100, 181, 246, 0.1) 100%);
}

.register-card {
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}
</style> 