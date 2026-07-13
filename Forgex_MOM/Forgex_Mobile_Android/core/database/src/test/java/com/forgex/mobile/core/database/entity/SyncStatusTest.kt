package com.forgex.mobile.core.database.entity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * SyncStatus 枚举单元测试。
 *
 * 测试 canRetry 方法的状态判断逻辑：
 * - PENDING → true (可重试, 只有 PENDING 状态的记录才能被重新入队重试)
 * - SYNCING/SYNCED/FAILED → false (不可重试)
 *
 * 设计说明：
 * SyncStatus.canRetry(maxRetry) 仅判断当前状态是否为 PENDING,
 * 实际的重试次数上限检查 (retryCount >= maxRetry → FAILED) 由
 * SyncQueueDao.incrementRetry 的 SQL CASE WHEN 语句实现。
 * 因此 maxRetry 参数在枚举方法中未被使用, 这是有意设计。
 *
 * 被测类: [SyncStatus]
 * 被测方法: [SyncStatus.canRetry]
 */
class SyncStatusTest {

    // ==================== canRetry: 各状态判断 ====================

    @Test
    fun `PENDING canRetry should return true`() {
        assertTrue(SyncStatus.PENDING.canRetry(5))
    }

    @Test
    fun `SYNCING canRetry should return false`() {
        assertFalse(SyncStatus.SYNCING.canRetry(5))
    }

    @Test
    fun `SYNCED canRetry should return false`() {
        assertFalse(SyncStatus.SYNCED.canRetry(5))
    }

    @Test
    fun `FAILED canRetry should return false`() {
        assertFalse(SyncStatus.FAILED.canRetry(5))
    }

    // ==================== canRetry: maxRetry 参数不影响结果 ====================

    @Test
    fun `PENDING canRetry should return true regardless of maxRetry value`() {
        assertTrue(SyncStatus.PENDING.canRetry(0))
        assertTrue(SyncStatus.PENDING.canRetry(1))
        assertTrue(SyncStatus.PENDING.canRetry(100))
    }

    @Test
    fun `SYNCING canRetry should return false regardless of maxRetry value`() {
        assertFalse(SyncStatus.SYNCING.canRetry(0))
        assertFalse(SyncStatus.SYNCING.canRetry(100))
    }

    @Test
    fun `SYNCED canRetry should return false regardless of maxRetry value`() {
        assertFalse(SyncStatus.SYNCED.canRetry(0))
        assertFalse(SyncStatus.SYNCED.canRetry(100))
    }

    @Test
    fun `FAILED canRetry should return false regardless of maxRetry value`() {
        assertFalse(SyncStatus.FAILED.canRetry(0))
        assertFalse(SyncStatus.FAILED.canRetry(100))
    }

    // ==================== 枚举完整性 ====================

    @Test
    fun `enum should have exactly four values`() {
        val values = SyncStatus.values()
        assertEquals(4, values.size)
        assertTrue(values.contains(SyncStatus.PENDING))
        assertTrue(values.contains(SyncStatus.SYNCING))
        assertTrue(values.contains(SyncStatus.SYNCED))
        assertTrue(values.contains(SyncStatus.FAILED))
    }

    @Test
    fun `enum name should match expected string values`() {
        assertEquals("PENDING", SyncStatus.PENDING.name)
        assertEquals("SYNCING", SyncStatus.SYNCING.name)
        assertEquals("SYNCED", SyncStatus.SYNCED.name)
        assertEquals("FAILED", SyncStatus.FAILED.name)
    }
}
