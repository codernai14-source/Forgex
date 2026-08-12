export const pendingActionPermissions = {
  approve: 'wf:execution:approve',
  reject: 'wf:execution:reject',
  addSign: 'wf:execution:addSign',
  transfer: 'wf:execution:transfer',
  delegate: 'wf:execution:delegate',
}

const nodeCapabilityKeys = {
  addSign: 'allowAddSign',
  transfer: 'allowTransfer',
  delegate: 'allowDelegate',
}

export function idsEqual(left, right) {
  if (left === undefined || left === null || right === undefined || right === null) {
    return false
  }
  return String(left) === String(right)
}

export function resolveReceiverId(receiverIds) {
  const receiverId = receiverIds?.[0]
  return receiverId === undefined || receiverId === null || receiverId === ''
    ? undefined
    : receiverId
}

export function resolvePendingActions(instance, hasPermission) {
  return Object.entries(pendingActionPermissions)
    .filter(([action, permission]) => {
      if (!hasPermission(permission)) return false
      const capabilityKey = nodeCapabilityKeys[action]
      return capabilityKey ? instance?.[capabilityKey] === true : true
    })
    .map(([action]) => action)
}
