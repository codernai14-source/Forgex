package com.forgex.workflow.common;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowConstantsTest {

    @Test
    void approvalActionTypesShouldKeepReminderAndRecallDistinct() {
        assertEquals(1, WorkflowConstants.ApprovalActionType.APPROVE);
        assertEquals(2, WorkflowConstants.ApprovalActionType.REJECT);
        assertEquals(3, WorkflowConstants.ApprovalActionType.TRANSFER);
        assertEquals(4, WorkflowConstants.ApprovalActionType.ADD_SIGN);
        assertEquals(5, WorkflowConstants.ApprovalActionType.DELEGATE);
        assertEquals(6, WorkflowConstants.ApprovalActionType.TIMEOUT_PASS);
        assertEquals(7, WorkflowConstants.ApprovalActionType.TIMEOUT_TRANSFER);
        assertEquals(8, WorkflowConstants.ApprovalActionType.SYSTEM_CLOSE);
        assertEquals(9, WorkflowConstants.ApprovalActionType.REMIND);
        assertEquals(10, WorkflowConstants.ApprovalActionType.RECALL);

        Set<Integer> actionTypes = Set.of(
                WorkflowConstants.ApprovalActionType.APPROVE,
                WorkflowConstants.ApprovalActionType.REJECT,
                WorkflowConstants.ApprovalActionType.TRANSFER,
                WorkflowConstants.ApprovalActionType.ADD_SIGN,
                WorkflowConstants.ApprovalActionType.DELEGATE,
                WorkflowConstants.ApprovalActionType.TIMEOUT_PASS,
                WorkflowConstants.ApprovalActionType.TIMEOUT_TRANSFER,
                WorkflowConstants.ApprovalActionType.SYSTEM_CLOSE,
                WorkflowConstants.ApprovalActionType.REMIND,
                WorkflowConstants.ApprovalActionType.RECALL);
        assertEquals(10, actionTypes.size());
        assertTrue(actionTypes.contains(WorkflowConstants.ApprovalActionType.REMIND));
        assertTrue(actionTypes.contains(WorkflowConstants.ApprovalActionType.RECALL));
    }
}
