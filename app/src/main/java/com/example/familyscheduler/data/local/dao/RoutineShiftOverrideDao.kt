package com.example.familyscheduler.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.familyscheduler.data.local.entity.RoutineShiftOverrideEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineShiftOverrideDao {

    @Query("SELECT * FROM routine_shift_overrides")
    fun getAll(): Flow<List<RoutineShiftOverrideEntity>>

    @Query("""
        SELECT * FROM routine_shift_overrides
        WHERE date = :date
    """)
    fun getByDate(date: String): Flow<List<RoutineShiftOverrideEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RoutineShiftOverrideEntity)

    @Query("""
        DELETE FROM routine_shift_overrides
        WHERE childId = :childId
    """)
    suspend fun deleteByChildId(childId: String)
}