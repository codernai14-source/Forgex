/**
 * 图标工具函数
 * 用于动态加载和使用 Ant Design Vue Icons
 */
import * as Icons from '@ant-design/icons-vue';
const legacyIconMap = {
    table: 'TableOutlined',
    cluster: 'ClusterOutlined',
    setting: 'SettingOutlined',
    appstore: 'AppstoreOutlined',
};
/**
 * 根据图标名称获取图标组件
 * @param iconName 图标名称，直接使用 Ant Design 的完整组件名（如 'UserOutlined'）
 * @returns 图标组件或 null
 */
export function getIcon(iconName) {
    if (!iconName) {
        return null;
    }
    let resolvedName = iconName;
    if (!(resolvedName in Icons) && legacyIconMap[iconName]) {
        resolvedName = legacyIconMap[iconName];
    }
    const icon = Icons[resolvedName];
    if (!icon) {
        console.warn(`图标 "${iconName}" 对应的组件不存在`);
        return null;
    }
    return icon;
}
/**
 * 检查图标是否存在
 * @param iconName 图标名称
 * @returns 是否存在
 */
export function hasIcon(iconName) {
    if (!iconName) {
        return false;
    }
    return iconName in Icons;
}
/**
 * 获取所有可用的图标名称列表
 * @returns 图标名称数组
 */
export function getAllIconNames() {
    return Object.keys(Icons);
}
/**
 * 常用图标映射
 * 用于快速访问常用图标
 */
export const CommonIcons = {
    // 用户相关
    user: 'UserOutlined',
    userAdd: 'UserAddOutlined',
    team: 'TeamOutlined',
    // 系统相关
    setting: 'SettingOutlined',
    dashboard: 'DashboardOutlined',
    menu: 'MenuOutlined',
    // 操作相关
    edit: 'EditOutlined',
    delete: 'DeleteOutlined',
    plus: 'PlusOutlined',
    search: 'SearchOutlined',
    // 状态相关
    check: 'CheckOutlined',
    close: 'CloseOutlined',
    warning: 'WarningOutlined',
    info: 'InfoCircleOutlined',
    // 导航相关
    home: 'HomeOutlined',
    folder: 'FolderOutlined',
    file: 'FileOutlined',
    // 其他
    lock: 'LockOutlined',
    unlock: 'UnlockOutlined',
    eye: 'EyeOutlined',
    eyeInvisible: 'EyeInvisibleOutlined',
};
