export default {
  action: {
    kickout: '强制下线',
    batchKickout: '批量强制下线',
    detail: '详情',
  },
  terminal: {
    all: '全部',
    b: 'B端',
    c: 'C端',
    thirdParty: '第三方',
  },
  ttl: {
    longTerm: '长期',
    second: '{count}s',
    minuteSecond: '{minute}m {second}s',
  },
  confirm: {
    kickoutTitle: '确认强制下线',
    kickoutContent: '该操作会使对应会话立即失效。',
    batchKickoutTitle: '确认批量强制下线',
    batchKickoutContent: '将对选中的 {count} 个会话执行强制下线。',
    onlyKickout: '仅下线，保留用户启用状态',
    kickoutAndDisable: '下线并停用用户',
    batchTarget: '选中的 {count} 个用户',
  },
  detail: {
    title: '在线会话详情',
    account: '账号',
    username: '用户名称',
    tenant: '租户',
    sessionCount: '有效会话数',
    token: 'Token',
    terminal: '登录端',
    region: '登录地区',
    ttl: '剩余会话时间',
  },
}
