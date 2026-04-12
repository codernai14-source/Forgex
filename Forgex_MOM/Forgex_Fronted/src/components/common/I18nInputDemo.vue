<template>
  <div class="i18n-input-demo">
    <a-card title="澶氳瑷€杈撳叆缁勪欢浣跨敤绀轰緥" :bordered="false">
      <a-space direction="vertical" :size="24" style="width: 100%">
        
        <!-- 绀轰緥1锛氱畝鍗曟ā寮忥紙閫傚悎琛ㄥ崟锛?-->
        <div class="demo-section">
          <h3>绀轰緥1锛氱畝鍗曟ā寮忥紙Simple Mode锛?/h3>
          <p class="description">閫傚悎鍦ㄨ〃鍗曚腑浣跨敤锛屾彁渚涗竴涓緭鍏ユ鍜屽璇█閰嶇疆鎸夐挳</p>
          
          <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 16 }">
            <a-form-item label="鑿滃崟鍚嶇О">
              <I18nInput 
                v-model="form1.nameI18nJson" 
                mode="simple" 
                placeholder="璇疯緭鍏ヨ彍鍗曞悕绉?
              />
            </a-form-item>
            
            <a-form-item label="褰撳墠JSON鍊?>
              <a-textarea 
                :value="form1.nameI18nJson" 
                :rows="3" 
                disabled
                placeholder="杩欓噷浼氭樉绀虹敓鎴愮殑JSON"
              />
            </a-form-item>
          </a-form>
        </div>

        <a-divider />

        <!-- 绀轰緥2锛氳〃鏍兼ā寮忥紙閫傚悎鐙珛閰嶇疆椤甸潰锛?-->
        <div class="demo-section">
          <h3>绀轰緥2锛氳〃鏍兼ā寮忥紙Table Mode锛?/h3>
          <p class="description">鐩存帴鏄剧ず琛ㄦ牸锛岄€傚悎鍦ㄧ嫭绔嬬殑澶氳瑷€閰嶇疆椤甸潰浣跨敤</p>
          
          <I18nInput 
            v-model="form2.dictValueI18nJson" 
            mode="table"
          />
          
          <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 16 }" style="margin-top: 16px">
            <a-form-item label="褰撳墠JSON鍊?>
              <a-textarea 
                :value="form2.dictValueI18nJson" 
                :rows="3" 
                disabled
                placeholder="杩欓噷浼氭樉绀虹敓鎴愮殑JSON"
              />
            </a-form-item>
          </a-form>
        </div>

        <a-divider />

        <!-- 绀轰緥3锛氬疄闄呭簲鐢ㄥ満鏅?-->
        <div class="demo-section">
          <h3>绀轰緥3锛氬疄闄呭簲鐢ㄥ満鏅?/h3>
          <p class="description">妯℃嫙鑿滃崟绠＄悊琛ㄥ崟</p>
          
          <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 16 }">
            <a-form-item label="鑿滃崟鍚嶇О" required>
              <I18nInput 
                v-model="menu表单.nameI18nJson" 
                mode="simple" 
                placeholder="璇疯緭鍏ヨ彍鍗曞悕绉?
              />
            </a-form-item>
            
            <a-form-item label="鑿滃崟璺緞">
              <a-input v-model:value="menu表单.path" placeholder="璇疯緭鍏ヨ彍鍗曡矾寰? />
            </a-form-item>
            
            <a-form-item label="鑿滃崟鍥炬爣">
              <a-input v-model:value="menu表单.icon" placeholder="璇疯緭鍏ュ浘鏍囧悕绉? />
            </a-form-item>
            
            <a-form-item label="鎺掑簭鍙?>
              <a-input-number v-model:value="menu表单.orderNum" :min="0" style="width: 100%" />
            </a-form-item>
            
            <a-form-item :wrapper-col="{ offset: 4, span: 16 }">
              <a-space>
                <a-button type="primary" @click="handleSubmit">鎻愪氦</a-button>
                <a-button @click="handleReset">閲嶇疆</a-button>
                <a-button @click="handleViewJson">鏌ョ湅JSON</a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </div>

      </a-space>
    </a-card>

    <!-- JSON棰勮寮圭獥 -->
    <a-modal
      v-model:open="jsonModalVisible"
      title="JSON鏁版嵁棰勮"
      width="600px"
      :footer="null"
    >
      <a-descriptions bordered :column="1">
        <a-descriptions-item label="鑿滃崟鍚嶇О澶氳瑷€">
          <pre>{{ menu表单.nameI18nJson || '鏈厤缃? }}</pre>
        </a-descriptions-item>
        <a-descriptions-item label="瀹屾暣琛ㄥ崟鏁版嵁">
          <pre>{{ JSON.stringify(menu表单, null, 2) }}</pre>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import I18nInput from './I18nInput.vue'

// 绀轰緥1鏁版嵁
const form1 = reactive({
  nameI18nJson: ''
})

// 绀轰緥2鏁版嵁
const form2 = reactive({
  dictValueI18nJson: ''
})

// 绀轰緥3鏁版嵁锛堣彍鍗曡〃鍗曪級
const menu表单 = reactive({
  nameI18nJson: '',
  path: '',
  icon: '',
  orderNum: 0
})

const jsonModalVisible = ref(false)

/**
 * 鎻愪氦琛ㄥ崟
 */
const handleSubmit = () => {
  if (!menu表单.nameI18nJson) {
    message.warning('璇烽厤缃彍鍗曞悕绉扮殑澶氳瑷€')
    return
  }
  
  message.success('鎻愪氦鎴愬姛锛?)
  console.log('鎻愪氦鐨勬暟鎹細', menu表单)
}

/**
 * 閲嶇疆琛ㄥ崟
 */
const handleReset = () => {
  menu表单.nameI18nJson = ''
  menu表单.path = ''
  menu表单.icon = ''
  menu表单.orderNum = 0
  message.info('琛ㄥ崟宸查噸缃?)
}

/**
 * 鏌ョ湅JSON
 */
const handleViewJson = () => {
  jsonModalVisible.value = true
}
</script>

<style scoped lang="less">
.i18n-input-demo {
  padding: 24px;
  
  .demo-section {
    h3 {
      margin-bottom: 8px;
      color: #1890ff;
    }
    
    .description {
      margin-bottom: 16px;
      color: #666;
      font-size: 14px;
    }
  }
  
  pre {
    background: #f5f5f5;
    padding: 12px;
    border-radius: 4px;
    overflow-x: auto;
    margin: 0;
  }
}
</style>

