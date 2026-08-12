package com.forgex.mobile.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.forgex.mobile.core.database.ForgexDatabase
import com.forgex.mobile.core.database.entity.SyncQueueEntity
import com.forgex.mobile.core.database.entity.SyncStatus
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * SyncQueueDao 集成测试 (Room in-memory + Robolectric)。
 *
 * 测试关键 DAO 操作：
 * - findPending: 返回 PENDING 记录, 按 queuedAt 排序, 支持 limit
 * - markSyncing: 批量将记录状态更新为 SYNCING
 * - incrementRetry: retryCount+1, 若 >= maxRetry → FAILED, 否则 → PENDING
 * - deleteSynced: 删除所有 SYNCED 记录
 * - markFailed: 标记为 FAILED 并记录错误信息
 * - updateStatus / findById / deleteAll
 *
 * 被测接口: [SyncQueueDao]
 * 数据库: [ForgexDatabase] (in-memory, Robolectric 提供应用 Context)
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SyncQueueDaoTest {

    private lateinit var database: ForgexDatabase
    private lateinit var dao: SyncQueueDao

    @Before
    fun setUp() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            ForgexDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.syncQueueDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ==================== Helpers ====================

    /**
     * 创建测试用 SyncQueueEntity。
     */
    private fun createEntity(
        operationType: String = "READ_RECEIPT",
        method: String = "PUT",
        path: String = "sys/message/read",
        payload: String = "{\"messageId\":1}",
        retryCount: Int = 0,
        maxRetry: Int = 5,
        syncStatus: String = SyncStatus.PENDING.name
    ): SyncQueueEntity {
        return SyncQueueEntity(
            operationType = operationType,
            method = method,
            path = path,
            payload = payload,
            retryCount = retryCount,
            maxRetry = maxRetry
        ).apply {
            this.syncStatus = syncStatus
        }
    }

    // ==================== findPending ====================

    @Test
    fun `findPending should return only PENDING records ordered by queuedAt`() = runTest {
        // Arrange
        val entity1 = createEntity(path = "path/1")
        val entity2 = createEntity(path = "path/2")
        val syncedEntity = createEntity(path = "path/3", syncStatus = SyncStatus.SYNCED.name)

        dao.insert(entity1)
        dao.insert(entity2)
        dao.insert(syncedEntity)

        // Act
        val pending = dao.findPending()

        // Assert
        assertEquals(2, pending.size)
        assertEquals("path/1", pending[0].path)
        assertEquals("path/2", pending[1].path)
    }

    @Test
    fun `findPending with limit should return at most limit records`() = runTest {
        // Arrange
        repeat(10) { i ->
            dao.insert(createEntity(path = "path/$i"))
        }

        // Act
        val pending = dao.findPending(limit = 3)

        // Assert
        assertEquals(3, pending.size)
    }

    @Test
    fun `findPending should return empty list when no PENDING records exist`() = runTest {
        // Arrange - only SYNCED records
        dao.insert(createEntity(syncStatus = SyncStatus.SYNCED.name))

        // Act
        val pending = dao.findPending()

        // Assert
        assertEquals(0, pending.size)
    }

    // ==================== markSyncing ====================

    @Test
    fun `markSyncing should update specified records to SYNCING`() = runTest {
        // Arrange
        val id1 = dao.insert(createEntity(path = "path/1"))
        val id2 = dao.insert(createEntity(path = "path/2"))

        // Act
        dao.markSyncing(listOf(id1, id2))

        // Assert
        val syncingRecords = dao.findByStatus(SyncStatus.SYNCING.name)
        assertEquals(2, syncingRecords.size)
        val pendingRecords = dao.findByStatus(SyncStatus.PENDING.name)
        assertEquals(0, pendingRecords.size)
    }

    @Test
    fun `markSyncing should only update specified ids leaving others unchanged`() = runTest {
        // Arrange
        val id1 = dao.insert(createEntity(path = "path/1"))
        val id2 = dao.insert(createEntity(path = "path/2"))
        val id3 = dao.insert(createEntity(path = "path/3"))

        // Act - only mark id1 and id3 as syncing
        dao.markSyncing(listOf(id1, id3))

        // Assert
        assertEquals(2, dao.findByStatus(SyncStatus.SYNCING.name).size)
        assertEquals(1, dao.findByStatus(SyncStatus.PENDING.name).size)
        assertEquals("path/2", dao.findByStatus(SyncStatus.PENDING.name)[0].path)
    }

    // ==================== incrementRetry ====================

    @Test
    fun `incrementRetry with retryCount below maxRetry should set status to PENDING`() = runTest {
        // Arrange: retryCount=2, maxRetry=5 → after increment: 3 < 5 → PENDING
        val id = dao.insert(createEntity(retryCount = 2, maxRetry = 5))

        // Act
        dao.incrementRetry(id, "Network error")

        // Assert
        val updated = dao.findById(id)
        assertNotNull(updated)
        assertEquals(3, updated!!.retryCount)
        assertEquals(SyncStatus.PENDING.name, updated.syncStatus)
        assertEquals("Network error", updated.lastError)
    }

    @Test
    fun `incrementRetry with retryCount reaching maxRetry should set status to FAILED`() = runTest {
        // Arrange: retryCount=4, maxRetry=5 → after increment: 5 >= 5 → FAILED
        val id = dao.insert(createEntity(retryCount = 4, maxRetry = 5))

        // Act
        dao.incrementRetry(id, "Server error 500")

        // Assert
        val updated = dao.findById(id)
        assertNotNull(updated)
        assertEquals(5, updated!!.retryCount)
        assertEquals(SyncStatus.FAILED.name, updated.syncStatus)
        assertEquals("Server error 500", updated.lastError)
    }

    @Test
    fun `incrementRetry with retryCount zero should increment to 1 and keep PENDING`() = runTest {
        // Arrange: retryCount=0, maxRetry=5 → after increment: 1 < 5 → PENDING
        val id = dao.insert(createEntity(retryCount = 0, maxRetry = 5))

        // Act
        dao.incrementRetry(id, "Connection refused")

        // Assert
        val updated = dao.findById(id)
        assertNotNull(updated)
        assertEquals(1, updated!!.retryCount)
        assertEquals(SyncStatus.PENDING.name, updated.syncStatus)
    }

    @Test
    fun `incrementRetry with maxRetry 1 should immediately FAIL on first retry`() = runTest {
        // Arrange: retryCount=0, maxRetry=1 → after increment: 1 >= 1 → FAILED
        val id = dao.insert(createEntity(retryCount = 0, maxRetry = 1))

        // Act
        dao.incrementRetry(id, "First failure")

        // Assert
        val updated = dao.findById(id)
        assertNotNull(updated)
        assertEquals(1, updated!!.retryCount)
        assertEquals(SyncStatus.FAILED.name, updated.syncStatus)
    }

    // ==================== deleteSynced ====================

    @Test
    fun `deleteSynced should remove only SYNCED records`() = runTest {
        // Arrange
        dao.insert(createEntity(path = "pending", syncStatus = SyncStatus.PENDING.name))
        dao.insert(createEntity(path = "synced", syncStatus = SyncStatus.SYNCED.name))
        dao.insert(createEntity(path = "failed", syncStatus = SyncStatus.FAILED.name))

        // Act
        dao.deleteSynced()

        // Assert
        assertEquals(0, dao.findByStatus(SyncStatus.SYNCED.name).size)
        assertEquals(1, dao.findByStatus(SyncStatus.PENDING.name).size)
        assertEquals(1, dao.findByStatus(SyncStatus.FAILED.name).size)
    }

    @Test
    fun `deleteSynced when no SYNCED records should be no-op`() = runTest {
        // Arrange
        dao.insert(createEntity(path = "pending", syncStatus = SyncStatus.PENDING.name))

        // Act
        dao.deleteSynced()

        // Assert
        assertEquals(1, dao.findByStatus(SyncStatus.PENDING.name).size)
    }

    // ==================== markFailed ====================

    @Test
    fun `markFailed should set status to FAILED and record error`() = runTest {
        // Arrange
        val id = dao.insert(createEntity(syncStatus = SyncStatus.SYNCING.name))

        // Act
        dao.markFailed(id, "409 Conflict")

        // Assert
        val updated = dao.findById(id)
        assertNotNull(updated)
        assertEquals(SyncStatus.FAILED.name, updated!!.syncStatus)
        assertEquals("409 Conflict", updated.lastError)
    }

    // ==================== updateStatus ====================

    @Test
    fun `updateStatus should change the syncStatus of a record`() = runTest {
        // Arrange
        val id = dao.insert(createEntity(syncStatus = SyncStatus.PENDING.name))

        // Act
        dao.updateStatus(id, SyncStatus.SYNCED.name)

        // Assert
        val updated = dao.findById(id)
        assertNotNull(updated)
        assertEquals(SyncStatus.SYNCED.name, updated!!.syncStatus)
    }

    // ==================== findById ====================

    @Test
    fun `findById should return null for non-existent id`() = runTest {
        val result = dao.findById(9999L)
        assertNull(result)
    }

    @Test
    fun `findById should return the correct entity`() = runTest {
        // Arrange
        val entity = createEntity(path = "sys/special/path", method = "POST")
        val id = dao.insert(entity)

        // Act
        val found = dao.findById(id)

        // Assert
        assertNotNull(found)
        assertEquals("sys/special/path", found!!.path)
        assertEquals("POST", found.method)
    }

    // ==================== deleteAll ====================

    @Test
    fun `deleteAll should remove all records`() = runTest {
        // Arrange
        dao.insert(createEntity(path = "1"))
        dao.insert(createEntity(path = "2"))
        dao.insert(createEntity(path = "3", syncStatus = SyncStatus.SYNCED.name))

        // Act
        dao.deleteAll()

        // Assert
        assertEquals(0, dao.findByStatus(SyncStatus.PENDING.name).size)
        assertEquals(0, dao.findByStatus(SyncStatus.SYNCED.name).size)
    }

    // ==================== findByStatus ====================

    @Test
    fun `findByStatus should return records matching the given status`() = runTest {
        // Arrange
        dao.insert(createEntity(path = "p1", syncStatus = SyncStatus.PENDING.name))
        dao.insert(createEntity(path = "p2", syncStatus = SyncStatus.PENDING.name))
        dao.insert(createEntity(path = "s1", syncStatus = SyncStatus.SYNCING.name))

        // Act
        val pending = dao.findByStatus(SyncStatus.PENDING.name)
        val syncing = dao.findByStatus(SyncStatus.SYNCING.name)

        // Assert
        assertEquals(2, pending.size)
        assertEquals(1, syncing.size)
    }
}
