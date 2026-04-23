package com.example.familyscheduler.data.local.dao

import androidx.room.*
import com.example.familyscheduler.data.local.entity.RoutineToggleOverrideEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineToggleOverrideDao {

    @Query("SELECT * FROM child_overrides")
    fun getAll(): Flow<List<RoutineToggleOverrideEntity>>

    @Query("""
        SELECT * FROM child_overrides
        WHERE date = :date
    """)
    fun getByDate(date: String): Flow<List<RoutineToggleOverrideEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RoutineToggleOverrideEntity)

    @Query("DELETE FROM child_overrides WHERE childId = :childId")
    suspend fun deleteByChildId(childId: String)
}