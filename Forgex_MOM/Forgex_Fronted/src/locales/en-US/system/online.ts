export default {
  action: {
    kickout: 'Force Logout',
    batchKickout: 'Batch Force Logout',
  },
  terminal: {
    all: 'All',
    b: 'B-End',
    c: 'C-End',
    thirdParty: 'Third Party',
  },
  ttl: {
    longTerm: 'Permanent',
    second: '{count}s',
    minuteSecond: '{minute}m {second}s',
  },
  confirm: {
    kickoutTitle: 'Confirm Force Logout',
    kickoutContent: 'This operation will invalidate the selected session immediately.',
    batchKickoutTitle: 'Confirm Batch Force Logout',
    batchKickoutContent: 'Force logout will be executed for {count} selected sessions.',
  },
}
