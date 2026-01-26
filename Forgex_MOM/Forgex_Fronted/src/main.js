/**
 * 应用入口文件
 * 负责创建和配置 Vue 应用实例，注册全局组件、指令和插件
 * @author Forgex Team
 * @version 1.0.0
 */
import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import router from './router';
import i18n from './locales';
import Antd from 'ant-design-vue';
import 'ant-design-vue/dist/reset.css';
import * as Icons from '@ant-design/icons-vue';
import { permission } from './directives/permission';
import FxDynamicTable from './components/common/FxDynamicTable.vue';
import { useUserStore } from './stores/user';
/**
 * 创建 Vue 应用实例
 */
const app = createApp(App);
/**
 * 创建 Pinia 状态管理实例
 */
const pinia = createPinia();
// 注册权限指令
app.directive('permission', permission);
/**
 * 全局注册常用图标组件列表
 * 从 Ant Design Icons 中选择常用图标进行全局注册
 */
const iconComponents = [
    'UserOutlined', // 用户图标
    'UserAddOutlined', // 添加用户图标
    'TeamOutlined', // 团队图标
    'SettingOutlined', // 设置图标
    'DashboardOutlined', // 仪表盘图标
    'MenuOutlined', // 菜单图标
    'MenuFoldOutlined', // 折叠菜单图标
    'MenuUnfoldOutlined', // 展开菜单图标
    'EditOutlined', // 编辑图标
    'DeleteOutlined', // 删除图标
    'PlusOutlined', // 加号图标
    'SearchOutlined', // 搜索图标
    'CheckOutlined', // 检查图标
    'CloseOutlined', // 关闭图标
    'WarningOutlined', // 警告图标
    'InfoCircleOutlined', // 信息圆图标
    'HomeOutlined', // 首页图标
    'FolderOutlined', // 文件夹图标
    'FileOutlined', // 文件图标
    'LockOutlined', // 锁定图标
    'UnlockOutlined', // 解锁图标
    'EyeOutlined', // 眼睛图标
    'EyeInvisibleOutlined', // 眼睛隐藏图标
    'LogoutOutlined', // 登出图标
    'KeyOutlined', // 钥匙图标
    'SafetyOutlined', // 安全图标
    'AppstoreOutlined', // 应用商店图标
    'UnorderedListOutlined', // 无序列表图标
    'DownOutlined', // 向下箭头图标
    'RightOutlined', // 向右箭头图标
    'LeftOutlined', // 向左箭头图标
    'UpOutlined', // 向上箭头图标
    'ReloadOutlined' // 刷新图标
];
/**
 * 注册全局组件
 */
app.component('FxDynamicTable', FxDynamicTable);
/**
 * 遍历图标列表，将图标注册为全局组件
 * @param name 图标组件名称
 */
iconComponents.forEach(name => {
    // 根据名称从 Icons 对象中获取对应的图标组件
    const icon = Icons[name];
    // 如果图标组件存在，则注册为全局组件
    if (icon) {
        app.component(name, icon);
    }
});
/**
 * 配置并挂载 Vue 应用实例
 * 依次使用：
 * - pinia: 状态管理
 * - router: 路由管理
 * - Antd: Ant Design Vue 组件库
 * - i18n: 国际化支持
 * 最后将应用挂载到 id 为 'app' 的 DOM 元素上
 */
app.use(pinia).use(router).use(Antd).use(i18n).mount('#app');
/**
 * 页面关闭或刷新时调用登出接口
 * 使用 sendBeacon 确保请求能够发送
 */
window.addEventListener('beforeunload', (event) => {
    const userStore = useUserStore();
    if (userStore.isLoggedIn) {
        // 使用 sendBeacon 发送登出请求，确保在页面关闭时也能发送
        navigator.sendBeacon('/auth/logout', JSON.stringify({}));
    }
});
