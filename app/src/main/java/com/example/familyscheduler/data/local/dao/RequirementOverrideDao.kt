package com.example.familyscheduler.data.local.dao

import androidx.room.*
import com.example.familyscheduler.data.local.entity.RequirementOverrideEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RequirementOverrideDao {

    @Query("SELECT * FROM requirement_overrides WHERE date = :date")
    fun getByDate(date: String): Flow<List<RequirementOverrideEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RequirementOverrideEntity)

    @Query("DELETE FROM requirement_overrides WHERE ruleId = :ruleId")
    suspend fun deleteByRuleId(ruleId: String)

    @Query("""
        DELETE FROM requirement_overrides 
        WHERE ruleId = :ruleId AND date = :date AND type = :type
    """)
    suspend fun deleteByRuleDateType(
        ruleId: String,
        date: String,
        type: String
    )
}