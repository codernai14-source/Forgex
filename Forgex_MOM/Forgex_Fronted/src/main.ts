import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import i18n from './locales'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import * as Icons from '@ant-design/icons-vue'
import { permission } from './directives/permission'

const app = createApp(App)
const pinia = createPinia()

// 注册权限指令
app.directive('permission', permission)

// 全局注册常用图标
const iconComponents = [
  'UserOutlined',
  'UserAddOutlined',
  'TeamOutlined',
  'SettingOutlined',
  'DashboardOutlined',
  'MenuOutlined',
  'MenuFoldOutlined',
  'MenuUnfoldOutlined',
  'EditOutlined',
  'DeleteOutlined',
  'PlusOutlined',
  'SearchOutlined',
  'CheckOutlined',
  'CloseOutlined',
  'WarningOutlined',
  'InfoCircleOutlined',
  'HomeOutlined',
  'FolderOutlined',
  'FileOutlined',
  'LockOutlined',
  'UnlockOutlined',
  'EyeOutlined',
  'EyeInvisibleOutlined',
  'LogoutOutlined',
  'KeyOutlined',
  'SafetyOutlined',
  'AppstoreOutlined',
  'UnorderedListOutlined',
  'DownOutlined',
  'RightOutlined',
  'LeftOutlined',
  'UpOutlined',
  'ReloadOutlined'
]

iconComponents.forEach(name => {
  const icon = Icons[name as keyof typeof Icons]
  if (icon) {
    app.component(name, icon)
  }
})

app.use(pinia).use(router).use(Antd).use(i18n).mount('#app')
