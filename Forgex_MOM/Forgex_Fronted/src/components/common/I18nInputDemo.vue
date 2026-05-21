<template>
  <div class="i18n-input-demo">
    <a-card :title="t('common.i18nInputDemo.title')" :bordered="false">
      <a-space direction="vertical" :size="24" style="width: 100%">
        <div class="demo-section">
          <h3>{{ t('common.i18nInputDemo.simpleTitle') }}</h3>
          <p class="description">{{ t('common.i18nInputDemo.simpleDesc') }}</p>

          <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 16 }">
            <a-form-item :label="t('common.i18nInputDemo.menuName')">
              <I18nInput
                v-model="form1.nameI18nJson"
                mode="simple"
                :placeholder="t('common.i18nInputDemo.menuNamePlaceholder')"
              />
            </a-form-item>

            <a-form-item :label="t('common.i18nInputDemo.currentJson')">
              <a-textarea
                :value="form1.nameI18nJson"
                :rows="3"
                disabled
                :placeholder="t('common.i18nInputDemo.jsonPlaceholder')"
              />
            </a-form-item>
          </a-form>
        </div>

        <a-divider />

        <div class="demo-section">
          <h3>{{ t('common.i18nInputDemo.tableTitle') }}</h3>
          <p class="description">{{ t('common.i18nInputDemo.tableDesc') }}</p>

          <I18nInput v-model="form2.dictValueI18nJson" mode="table" />

          <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 16 }" style="margin-top: 16px">
            <a-form-item :label="t('common.i18nInputDemo.currentJson')">
              <a-textarea
                :value="form2.dictValueI18nJson"
                :rows="3"
                disabled
                :placeholder="t('common.i18nInputDemo.jsonPlaceholder')"
              />
            </a-form-item>
          </a-form>
        </div>

        <a-divider />

        <div class="demo-section">
          <h3>{{ t('common.i18nInputDemo.formTitle') }}</h3>
          <p class="description">{{ t('common.i18nInputDemo.formDesc') }}</p>

          <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 16 }">
            <a-form-item :label="t('common.i18nInputDemo.menuName')" required>
              <I18nInput
                v-model="menuForm.nameI18nJson"
                mode="simple"
                :placeholder="t('common.i18nInputDemo.menuNamePlaceholder')"
              />
            </a-form-item>

            <a-form-item :label="t('common.i18nInputDemo.menuPath')">
              <a-input v-model:value="menuForm.path" :placeholder="t('common.i18nInputDemo.menuPathPlaceholder')" />
            </a-form-item>

            <a-form-item :label="t('common.i18nInputDemo.menuIcon')">
              <a-input v-model:value="menuForm.icon" :placeholder="t('common.i18nInputDemo.menuIconPlaceholder')" />
            </a-form-item>

            <a-form-item :label="t('common.i18nInputDemo.orderNum')">
              <a-input-number v-model:value="menuForm.orderNum" :min="0" style="width: 100%" />
            </a-form-item>

            <a-form-item :wrapper-col="{ offset: 4, span: 16 }">
              <a-space>
                <a-button type="primary" @click="handleSubmit">{{ t('common.submit') }}</a-button>
                <a-button @click="handleReset">{{ t('common.reset') }}</a-button>
                <a-button @click="handleViewJson">{{ t('common.i18nInputDemo.viewJson') }}</a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </div>
      </a-space>
    </a-card>

    <a-modal
      v-model:open="jsonModalVisible"
      :title="t('common.i18nInputDemo.jsonPreview')"
      width="600px"
      :footer="null"
    >
      <a-descriptions bordered :column="1">
        <a-descriptions-item :label="t('common.i18nInputDemo.menuNameJson')">
          <pre>{{ menuForm.nameI18nJson || t('common.i18nInputDemo.notConfigured') }}</pre>
        </a-descriptions-item>
        <a-descriptions-item :label="t('common.i18nInputDemo.formData')">
          <pre>{{ JSON.stringify(menuForm, null, 2) }}</pre>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import I18nInput from './I18nInput.vue'

const { t } = useI18n()

const form1 = reactive({
  nameI18nJson: '',
})

const form2 = reactive({
  dictValueI18nJson: '',
})

const menuForm = reactive({
  nameI18nJson: '',
  path: '',
  icon: '',
  orderNum: 0,
})

const jsonModalVisible = ref(false)

const handleSubmit = () => {
  if (!menuForm.nameI18nJson) {
    message.warning(t('common.i18nInputDemo.needConfig'))
    return
  }

  message.success(t('common.submit'))
  console.log('menu form data:', menuForm)
}

const handleReset = () => {
  menuForm.nameI18nJson = ''
  menuForm.path = ''
  menuForm.icon = ''
  menuForm.orderNum = 0
  message.info(t('common.reset'))
}

const handleViewJson = () => {
  jsonModalVisible.value = true
}
</script>

<style scoped lang="less" src="@/styles/components/common/i18n-input-demo.less"></style>
