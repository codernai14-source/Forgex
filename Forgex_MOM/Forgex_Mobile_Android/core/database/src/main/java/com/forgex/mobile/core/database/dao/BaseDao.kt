package com.forgex.mobile.core.database.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

/**
 * 泛型 DAO 基接口, 提供通用 CRUD 操作。
 *
 * Room 不支持动态表名, 因此查询方法需在各子 Dao 中定义。
 * 此接口仅提供 insert/update/delete 抽象。
 *
 * @param T Entity 类型
 */
interface BaseDao<T> {

    /**
     * 插入一条记录, 冲突时替换。
     *
     * @param entity 待插入的 Entity
     * @return 插入记录的主键 ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: T): Long

    /**
     * 批量插入记录, 冲突时替换。
     *
     * @param entities 待插入的 Entity 列表
     * @return 插入记录的主键 ID 列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<T>): List<Long>

    /**
     * 更新一条记录。
     *
     * @param entity 待更新的 Entity
     */
    @Update
    suspend fun update(entity: T)

    /**
     * 删除一条记录。
     *
     * @param entity 待删除的 Entity
     */
    @Delete
    suspend fun delete(entity: T)
}
