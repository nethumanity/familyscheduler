package com.example.familyscheduler.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.familyscheduler.data.local.entity.TemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {
    @Query("SELECT * FROM templates")
    fun getAll(): Flow<List<TemplateEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: TemplateEntity)
    @Query("DELETE FROM templates WHERE id = :id")
    suspend fun delete(id: String)
    @Query("SELECT * FROM templates WHERE id = :id")
    fun getById(id: String): Flow<TemplateEntity?>
    
}
