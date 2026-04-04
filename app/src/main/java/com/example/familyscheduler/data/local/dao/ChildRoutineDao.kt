package com.example.familyscheduler.data.local.dao

import androidx.room.*
import com.example.familyscheduler.data.local.entity.ChildRoutineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChildRoutineDao {

    @Query("SELECT * FROM child_routines")
    fun getAll(): Flow<List<ChildRoutineEntity>>

    @Query("SELECT * FROM child_routines WHERE name = :name LIMIT 1")
    fun getByName(name: String): Flow<ChildRoutineEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ChildRoutineEntity)

    @Query("DELETE FROM child_routines WHERE name = :name")
    suspend fun delete(name: String)
}