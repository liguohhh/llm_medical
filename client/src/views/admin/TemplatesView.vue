<script setup>
import { ref } from 'vue'
import TemplateList from '@/components/admin/TemplateList.vue'
import TemplateEditDialog from '@/components/admin/TemplateEditDialog.vue'

// 编辑对话框状态
const editDialog = ref(false)
const editTemplateId = ref(null)
const editTemplateData = ref(null)

// 模板列表引用
const templateListRef = ref(null)

// 打开编辑对话框
const handleEdit = (templateId, templateData) => {
  editTemplateId.value = templateId
  editTemplateData.value = templateData
  editDialog.value = true
}

// 模板保存成功
const handleSaved = () => {
  // 关闭对话框
  editDialog.value = false
  // 刷新整个页面以确保数据更新
  window.location.reload()
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
              <v-icon icon="mdi-file-document-outline" size="large" color="white" class="me-3"></v-icon>
              <div>
                <h1 class="text-h4 font-weight-bold text-white mb-1">提示词模板管理</h1>
                <p class="text-subtitle-1 text-white text-opacity-90 mb-0">
                  管理系统中的LLM提示词模板
                </p>
              </div>
            </div>
          </div>
        </v-card>
      </v-col>
    </v-row>
    
    <template-list @edit="handleEdit" ref="templateListRef" />
    
    <template-edit-dialog
      v-model="editDialog"
      :template-id="editTemplateId"
      :template-data="editTemplateData"
      @saved="handleSaved"
    />
  </v-container>
</template>

<style scoped>
.v-container {
  max-width: 1400px;
  margin: 0 auto;
}
</style> 