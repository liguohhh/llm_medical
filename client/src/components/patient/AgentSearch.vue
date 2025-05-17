<script setup>
import { ref, defineEmits, defineProps } from 'vue'

const props = defineProps({
  directions: {
    type: Array,
    default: () => []
  },
  showIntro: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['search', 'filter-direction'])

const searchQuery = ref('')
const showFilterMenu = ref(false)
const selectedDirections = ref([])

const search = () => {
  emit('search', searchQuery.value)
}

const toggleDirection = (directionId) => {
  const index = selectedDirections.value.indexOf(directionId)
  if (index === -1) {
    selectedDirections.value.push(directionId)
  } else {
    selectedDirections.value.splice(index, 1)
  }
  emit('filter-direction', selectedDirections.value)
}

const clearSearch = () => {
  searchQuery.value = ''
  emit('search', '')
}

const clearFilters = () => {
  selectedDirections.value = []
  emit('filter-direction', [])
}
</script>

<template>
  <div class="agent-search mb-4">
    <v-card class="search-card" variant="flat" elevation="2">
      <v-card-text class="pa-4">
        <v-text-field
          v-model="searchQuery"
          clearable
          hide-details
          density="comfortable"
          placeholder="输入智能体名称或症状关键词..."
          prepend-inner-icon="mdi-magnify"
          @keyup.enter="search"
          @click:clear="clearSearch"
          variant="solo-filled"
          class="search-input rounded-lg mb-3"
          bg-color="surface"
        >
          <template v-slot:append-inner>
            <v-btn
              color="primary"
              variant="tonal"
              class="search-btn"
              @click="search"
              size="small"
            >
              搜索
            </v-btn>
          </template>
        </v-text-field>
        
        <div class="d-flex flex-wrap align-center gap-2">
          <div class="filter-button-group d-flex align-center">
            <v-btn-group variant="outlined" rounded="pill" class="filter-group">
              <v-btn
                color="primary"
                :variant="selectedDirections.length ? 'elevated' : 'outlined'"
                prepend-icon="mdi-filter-variant"
                @click="showFilterMenu = !showFilterMenu"
                class="filter-btn"
                :class="{'filter-active': selectedDirections.length}"
              >
                筛选方向
                <span v-if="selectedDirections.length" class="ml-2 badge-container">
                  <v-badge
                    :content="selectedDirections.length.toString()"
                    color="primary"
                    inline
                  ></v-badge>
                </span>
              </v-btn>
              
              <v-btn
                v-if="selectedDirections.length"
                color="error"
                variant="outlined"
                icon="mdi-close"
                @click="clearFilters"
                size="default"
              ></v-btn>
            </v-btn-group>
            
            <v-menu
              v-model="showFilterMenu"
              :close-on-content-click="false"
              location="bottom"
              transition="slide-y-transition"
              min-width="300"
            >
              <v-card class="filter-menu" elevation="4">
                <v-card-title class="d-flex align-center pa-4">
                  <v-icon icon="mdi-hospital-building" color="primary" class="mr-2"></v-icon>
                  筛选医疗方向
                  <v-spacer></v-spacer>
                  <v-btn
                    variant="text"
                    size="small"
                    color="primary"
                    @click="clearFilters"
                    :disabled="!selectedDirections.length"
                  >
                    清除全部
                  </v-btn>
                </v-card-title>
                
                <v-divider></v-divider>
                
                <v-card-text class="filter-content pa-4">
                  <template v-if="props.directions.length">
                    <v-row>
                      <v-col 
                        v-for="direction in props.directions" 
                        :key="direction.id"
                        cols="12" sm="6"
                      >
                        <v-checkbox
                          :label="direction.name"
                          :model-value="selectedDirections.includes(direction.id)"
                          @change="toggleDirection(direction.id)"
                          hide-details
                          density="comfortable"
                          color="primary"
                          class="mb-2 direction-checkbox"
                        >
                          <template v-slot:label>
                            <div class="d-flex align-center">
                              <span>{{ direction.name }}</span>
                              <v-chip
                                size="x-small"
                                color="primary"
                                variant="flat"
                                class="ml-2"
                                v-if="selectedDirections.includes(direction.id)"
                              >
                                已选
                              </v-chip>
                            </div>
                          </template>
                        </v-checkbox>
                      </v-col>
                    </v-row>
                  </template>
                  <p v-else class="text-center text-medium-emphasis my-4">
                    暂无医疗方向数据
                  </p>
                </v-card-text>
                
                <v-card-actions class="pa-4 pt-0">
                  <v-spacer></v-spacer>
                  <v-btn
                    color="primary"
                    variant="tonal"
                    @click="showFilterMenu = false"
                    prepend-icon="mdi-check"
                  >
                    应用筛选
                  </v-btn>
                </v-card-actions>
              </v-card>
            </v-menu>
          </div>
          
          <div class="direction-chips flex-grow-1">
            <v-chip
              v-for="dirId in selectedDirections"
              :key="dirId"
              size="small"
              closable
              @click:close="toggleDirection(dirId)"
              class="ma-1"
              color="primary"
              variant="outlined"
            >
              {{ props.directions.find(d => d.id === dirId)?.name || `方向 ${dirId}` }}
            </v-chip>
          </div>
        </div>
        
        <div v-if="searchQuery" class="search-status mt-3">
          <v-chip
            size="small"
            closable
            @click:close="clearSearch"
            class="ma-1"
            color="primary"
            variant="flat"
            prepend-icon="mdi-magnify"
          >
            搜索: {{ searchQuery }}
          </v-chip>
          
          <v-btn
            size="small"
            variant="text"
            color="primary"
            class="ms-2"
            @click="clearSearch"
            density="comfortable"
          >
            清除搜索
          </v-btn>
        </div>
        
        <v-expand-transition>
          <div v-if="props.showIntro" class="help-content mt-4">
            <v-divider class="mb-4"></v-divider>
            
            <div class="d-flex align-items-center mb-3">
              <v-icon icon="mdi-lightbulb-outline" color="warning" class="me-2"></v-icon>
              <span class="text-subtitle-1 font-weight-medium">如何选择合适的智能医疗助手？</span>
            </div>
            
            <v-row dense class="help-tips">
              <v-col cols="12" md="4">
                <div class="tip-item d-flex align-center">
                  <v-avatar color="primary" size="32" class="me-3">
                    <v-icon size="16" color="white">mdi-hospital-building</v-icon>
                  </v-avatar>
                  <div>
                    <div class="font-weight-medium">按医疗专科选择</div>
                    <div class="text-caption text-medium-emphasis">根据症状选择相应医疗专科方向</div>
                  </div>
                </div>
              </v-col>
              
              <v-col cols="12" md="4">
                <div class="tip-item d-flex align-center">
                  <v-avatar color="info" size="32" class="me-3">
                    <v-icon size="16" color="white">mdi-magnify</v-icon>
                  </v-avatar>
                  <div>
                    <div class="font-weight-medium">搜索症状或关键词</div>
                    <div class="text-caption text-medium-emphasis">使用搜索框查找相关智能助手</div>
                  </div>
                </div>
              </v-col>
              
              <v-col cols="12" md="4">
                <div class="tip-item d-flex align-center">
                  <v-avatar color="success" size="32" class="me-3">
                    <v-icon size="16" color="white">mdi-filter-variant</v-icon>
                  </v-avatar>
                  <div>
                    <div class="font-weight-medium">筛选医疗方向</div>
                    <div class="text-caption text-medium-emphasis">通过筛选缩小智能助手范围</div>
                  </div>
                </div>
              </v-col>
            </v-row>
          </div>
        </v-expand-transition>
      </v-card-text>
    </v-card>
  </div>
</template>

<style scoped>
.agent-search {
  position: relative;
}

.search-card {
  border-radius: 16px;
  background: linear-gradient(to bottom, rgba(255,255,255,0.95), rgba(255,255,255,0.98));
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.search-card:hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
}

.search-input {
  border-radius: 12px;
  transition: all 0.3s ease;
}

.search-input:focus-within {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.05);
}

.filter-button-group {
  margin-right: 8px;
}

.filter-group {
  border-radius: 20px;
  overflow: hidden;
}

.filter-btn {
  transition: all 0.3s ease;
  border-top-right-radius: 20px !important;
  border-bottom-right-radius: 20px !important;
  padding-right: 16px;
  min-width: 110px;
}

.filter-active {
  background-color: rgba(var(--v-theme-primary), 0.1);
}

.filter-menu {
  border-radius: 12px;
  overflow: hidden;
}

.filter-content {
  max-height: 300px;
  overflow-y: auto;
}

.direction-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.search-status {
  display: flex;
  align-items: center;
}

.help-content {
  animation: fadeIn 0.5s ease;
}

.tip-item {
  padding: 8px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.tip-item:hover {
  background-color: rgba(var(--v-theme-primary), 0.05);
}

.direction-checkbox:hover {
  background-color: rgba(var(--v-theme-primary), 0.05);
  border-radius: 4px;
}

.gap-2 {
  gap: 8px;
}

.badge-container {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-left: 4px;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style> 