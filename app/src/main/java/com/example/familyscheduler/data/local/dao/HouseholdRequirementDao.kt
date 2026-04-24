package com.example.familyscheduler.data.local.dao

import androidx.room.*
import com.example.familyscheduler.data.local.entity.HouseholdRequirementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HouseholdRequirementDao {

    @Query("""
        SELECT * FROM household_requirements
        WHERE
            (date IS NOT NULL AND date = :date)
            OR
            (date IS NULL AND daysOfWeek IS NOT NULL AND instr(daysOfWeek, :dayOfWeek) > 0)
    """)
    fun getByDate(
        date: String,
        dayOfWeek: String
    ): Flow<List<HouseholdRequirementEntity>>

    @Query("SELECT * FROM household_requirements WHERE id = :id LIMIT 1")
    fun getById(id: String): Flow<HouseholdRequirementEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HouseholdRequirementEntity)

    @Query("DELETE FROM household_requirements WHERE id = :id")
    suspend fun delete(id: String)
}