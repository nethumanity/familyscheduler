package com.example.familyscheduler.data.local.dao

import androidx.room.*
import com.example.familyscheduler.data.local.entity.ChildRoutineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChildRoutineDao {

    @Query("SELECT * FROM child_routines")
    fun getAll(): Flow<List<ChildRoutineEntity>>

    @Query("SELECT * FROM child_routines WHERE childId = :childId LIMIT 1")
    fun getByChildId(childId: String): Flow<ChildRoutineEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ChildRoutineEntity)

    @Query("DELETE FROM child_routines WHERE childId = :childId")
    suspend fun delete(childId: String)
}