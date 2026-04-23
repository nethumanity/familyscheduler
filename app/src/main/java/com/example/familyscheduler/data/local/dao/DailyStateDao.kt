package com.example.familyscheduler.data.local.dao

import androidx.room.*
import com.example.familyscheduler.data.local.entity.DailyStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStateDao {

//    @Query("SELECT * FROM daily_states")
//    fun getAll(): Flow<List<DailyStateEntity>>

    @Query("SELECT * FROM daily_states WHERE date = :date")
    fun getByDate(date: String): Flow<List<DailyStateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(state: DailyStateEntity)
}