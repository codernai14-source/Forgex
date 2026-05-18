/**
 * 缺省页面翻译 - 中文
 */
export default {
  actions: {
    home: '返回首页',
    back: '返回上一页',
    retry: '重新连接',
  },
  panel: {
    title: '状态检查',
  },
  '403': {
    eyebrow: '权限校验未通过',
    title: '访问被拒绝',
    description: '当前账号没有访问该功能的权限。请确认角色授权、租户范围或联系管理员开通对应菜单与按钮权限。',
    status: '访问链路已拦截',
    checks: {
      permission: '检查菜单与按钮权限是否已授权',
      role: '确认当前角色是否包含目标功能',
      tenant: '确认当前租户是否允许访问该模块',
    },
  },
  '404': {
    eyebrow: '路由未匹配',
    title: '页面不存在',
    description: '未找到您要访问的页面。链接可能已变更、菜单尚未发布，或当前模块路由没有完成配置。',
    status: '未发现有效页面',
    checks: {
      route: '检查访问地址是否输入正确',
      menu: '确认菜单配置是否已发布并启用',
      link: '从首页或模块菜单重新进入目标页面',
    },
  },
  offline: {
    eyebrow: '网络连接异常',
    title: '暂时无法连接服务',
    description: '当前客户端无法连接到 Forgex 服务。请检查网络、网关或 VPN 状态，恢复后重新连接即可继续操作。',
    status: '连接等待恢复',
    checks: {
      network: '检查本机网络与企业内网连接',
      gateway: '确认接口网关或后端服务是否可用',
      retry: '恢复连接后点击重新连接刷新页面',
    },
  },
}
