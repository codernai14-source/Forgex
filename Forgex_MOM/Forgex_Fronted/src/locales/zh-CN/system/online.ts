export default {
  action: {
    kickout: '强制下线',
    batchKickout: '批量强制下线',
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
  },
}
