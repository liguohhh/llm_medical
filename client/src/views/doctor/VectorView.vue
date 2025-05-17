<script setup>
import { ref } from 'vue'
import NamespaceList from '@/components/doctor/NamespaceList.vue'
import DocumentList from '@/components/doctor/DocumentList.vue'
import DocumentUpload from '@/components/doctor/DocumentUpload.vue'

// 状态变量
const selectedNamespace = ref(null)
const namespaceListRef = ref(null)
const documentListRef = ref(null)

// 处理命名空间选择
const handleSelectNamespace = (namespace) => {
  selectedNamespace.value = namespace
}

// 处理文档上传成功
const handleUploadSuccess = () => {
  // 刷新文档列表
  if (documentListRef.value) {
    documentListRef.value.loadDocuments()
  }
}

// 处理刷新文档列表
const handleRefreshDocuments = () => {
  if (documentListRef.value) {
    documentListRef.value.loadDocuments()
  }
}
</script>

<template>
  <v-container>
    <v-card class="mb-6" flat>
      <div 
        class="pa-5 rounded-t-lg" 
        style="background: linear-gradient(to right, #1976D2, #64B5F6);"
      >
        <div class="d-flex align-center">
          <v-icon icon="mdi-database" size="large" color="white" class="me-3"></v-icon>
          <div>
            <h1 class="text-h4 font-weight-bold text-white mb-1">向量数据库管理</h1>
            <p class="text-subtitle-1 text-white text-opacity-90 mb-0">
              管理向量数据库中的命名空间和文档
            </p>
          </div>
        </div>
      </div>
    </v-card>
    
    <v-row>
      <!-- 命名空间列表 -->
      <v-col cols="12" md="4">
        <namespace-list
          ref="namespaceListRef"
          @select-namespace="handleSelectNamespace"
          @refresh-documents="handleRefreshDocuments"
        />
      </v-col>
      
      <v-col cols="12" md="8">
        <v-row>
          <!-- 文档上传 -->
          <v-col cols="12">
            <document-upload
              :namespace="selectedNamespace"
              @upload-success="handleUploadSuccess"
            />
          </v-col>
          
          <!-- 文档列表 -->
          <v-col cols="12">
            <document-list
              ref="documentListRef"
              :namespace="selectedNamespace"
            />
          </v-col>
        </v-row>
      </v-col>
    </v-row>
  </v-container>
</template>

<style scoped>
.v-container {
  max-width: 1400px;
}

.v-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}
</style> 