import type { WfApprovalInstanceDTO } from '@/api/workflow/execution'
import type { WorkflowId } from '@/api/workflow/execution'

export type PendingAction = 'approve' | 'reject' | 'addSign' | 'transfer' | 'delegate'

export const pendingActionPermissions: Record<PendingAction, string>

export function idsEqual(
  left: WorkflowId | null | undefined,
  right: WorkflowId | null | undefined,
): boolean

export function resolveReceiverId(receiverIds: WorkflowId[]): WorkflowId | undefined

export function resolvePendingActions(
  instance: Partial<WfApprovalInstanceDTO>,
  hasPermission: (permission: string) => boolean,
): PendingAction[]
