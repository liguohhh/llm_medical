<script setup>
import { defineProps, defineEmits, computed, ref, watch } from 'vue'

const props = defineProps({
  settings: {
    type: Object,
    default: () => ({
      vectorResults: 3,
      preciseResults: 3,
      searchDepth: 2,
      vectorHistoryCount: 1,
      preciseHistoryCount: 1,
      templateParams: {}
    })
  },
  agentSettings: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:settings'])

// 本地state，避免直接修改props
const localSettings = ref({ ...props.settings })

// 更新设置
const updateSettings = () => {
  emit('update:settings', { ...localSettings.value })
}

// 监听props变化
watch(() => props.settings, (newVal) => {
  localSettings.value = { ...newVal }
}, { deep: true })

// 重置设置
const resetSettings = () => {
  localSettings.value = {
    vectorResults: 3,
    preciseResults: 3,
    searchDepth: 2,
    vectorHistoryCount: 1,
    preciseHistoryCount: 1,
    templateParams: {}
  }
  updateSettings()
}

// 是否有模板参数
const hasTemplateParams = computed(() => {
  return props.agentSettings?.templateParams && 
         Array.isArray(props.agentSettings.templateParams) && 
         props.agentSettings.templateParams.length > 0
})

// 确保templateParams存在
watch(() => localSettings.value, (newVal) => {
  if (!newVal.templateParams) {
    localSettings.value.templateParams = {}
    updateSettings()
  }
}, { deep: true, immediate: true })
</script>

<template>
  <div class="settings-drawer">
    <v-toolbar color="primary" dark>
      <v-toolbar-title>高级设置</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-btn icon @click="resetSettings">
        <v-icon>mdi-refresh</v-icon>
      </v-btn>
    </v-toolbar>
    
    <v-card-text>
      <v-alert
        color="info"
        variant="tonal"
        icon="mdi-information"
        class="mb-4"
      >
        这些设置将影响AI回答的质量和响应速度。
      </v-alert>
      
      <div class="text-h6 mb-2">向量检索设置</div>
      <v-slider
        v-model="localSettings.vectorResults"
        label="检索结果数量"
        min="1"
        max="10"
        step="1"
        thumb-label
        @update:modelValue="updateSettings"
      ></v-slider>
      
      <v-slider
        v-model="localSettings.vectorHistoryCount"
        label="历史消息数量"
        min="1"
        max="5"
        step="1"
        thumb-label
        @update:modelValue="updateSettings"
      ></v-slider>
      
      <v-divider class="my-4"></v-divider>
      
      <div class="text-h6 mb-2">精确查询设置</div>
      <v-slider
        v-model="localSettings.preciseResults"
        label="查询结果数量"
        min="1"
        max="10"
        step="1"
        thumb-label
        @update:modelValue="updateSettings"
      ></v-slider>
      
      <v-slider
        v-model="localSettings.searchDepth"
        label="搜索深度"
        min="1"
        max="10"
        step="1"
        thumb-label
        @update:modelValue="updateSettings"
      ></v-slider>
      
      <v-slider
        v-model="localSettings.preciseHistoryCount"
        label="历史消息数量"
        min="1"
        max="5"
        step="1"
        thumb-label
        @update:modelValue="updateSettings"
      ></v-slider>
      
      <template v-if="hasTemplateParams">
        <v-divider class="my-4"></v-divider>
        <div class="text-h6 mb-2">模板参数</div>
        
        <div v-for="param in agentSettings.templateParams" :key="param.name || 'param'" class="mb-4">
          <template v-if="param.options && Array.isArray(param.options) && param.options.length > 0">
            <!-- 下拉选择 -->
            <v-select
              v-model="localSettings.templateParams[param.name]"
              :label="param.description || param.name || '参数'"
              :items="param.options"
              variant="outlined"
              density="comfortable"
              :required="param.required"
              @update:modelValue="updateSettings"
            ></v-select>
          </template>
          <template v-else-if="param.name">
            <!-- 文本输入 -->
            <v-text-field
              v-model="localSettings.templateParams[param.name]"
              :label="param.description || param.name || '参数'"
              variant="outlined"
              density="comfortable"
              :required="param.required"
              @update:modelValue="updateSettings"
            ></v-text-field>
          </template>
        </div>
      </template>
      
      <v-divider class="my-4"></v-divider>
      
      <div class="text-body-2 text-medium-emphasis">
        <p><strong>向量检索</strong>：按语义相似度查找相关内容</p>
        <p><strong>精确查询</strong>：通过关键词精确匹配内容</p>
        <p><strong>搜索深度</strong>：1级仅进行关键词匹配，2级增加二次关联匹配</p>
        <p><strong>历史消息数量</strong>：用于检索的历史消息数量</p>
      </div>
    </v-card-text>
  </div>
</template>

<style scoped>
.settings-drawer {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.v-card-text {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 24px;
}
</style> 