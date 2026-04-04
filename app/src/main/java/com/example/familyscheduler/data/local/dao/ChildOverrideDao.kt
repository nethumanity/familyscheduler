package com.example.familyscheduler.data.local.dao

import androidx.room.*
import com.example.familyscheduler.data.local.entity.ChildOverrideEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChildOverrideDao {

    @Query("SELECT * FROM child_overrides")
    fun getAll(): Flow<List<ChildOverrideEntity>>

    @Query("""
        SELECT * FROM child_overrides 
        WHERE childName = :childName AND date = :date
        LIMIT 1
    """)
    fun getByChildAndDate(
        childName: String,
        date: String
    ): Flow<ChildOverrideEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ChildOverrideEntity)

    @Query("DELETE FROM child_overrides WHERE childName = :childName")
    suspend fun deleteByChildName(childName: String)
}