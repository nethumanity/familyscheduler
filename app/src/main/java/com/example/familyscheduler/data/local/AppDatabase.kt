package com.example.familyscheduler.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.familyscheduler.data.local.dao.RoutineToggleOverrideDao
import com.example.familyscheduler.data.local.dao.ChildRoutineDao
import com.example.familyscheduler.data.local.dao.DailyStateDao
import com.example.familyscheduler.data.local.dao.HouseholdRequirementDao
import com.example.familyscheduler.data.local.dao.RequirementOverrideDao
import com.example.familyscheduler.data.local.dao.RoutineShiftOverrideDao
import com.example.familyscheduler.data.local.dao.TemplateDao
import com.example.familyscheduler.data.local.entity.RoutineToggleOverrideEntity
import com.example.familyscheduler.data.local.entity.ChildRoutineEntity
import com.example.familyscheduler.data.local.entity.DailyStateEntity
import com.example.familyscheduler.data.local.entity.HouseholdRequirementEntity
import com.example.familyscheduler.data.local.entity.RequirementOverrideEntity
import com.example.familyscheduler.data.local.entity.RoutineShiftOverrideEntity
import com.example.familyscheduler.data.local.entity.TemplateEntity

@Database(
    entities = [
        TemplateEntity::class,
        HouseholdRequirementEntity::class,
        ChildRoutineEntity::class,
        DailyStateEntity::class,
        RequirementOverrideEntity::class,
        RoutineToggleOverrideEntity::class,
        RoutineShiftOverrideEntity::class
               ],
    version = 10,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun templateDao(): TemplateDao
    abstract fun householdRequirementDao(): HouseholdRequirementDao
    abstract fun childRoutineDao(): ChildRoutineDao
    abstract fun dailyStateDao(): DailyStateDao
    abstract fun requirementOverrideDao(): RequirementOverrideDao
    abstract fun routineToggleOverrideDao(): RoutineToggleOverrideDao
    abstract fun routineShiftOverrideDao(): RoutineShiftOverrideDao

}
