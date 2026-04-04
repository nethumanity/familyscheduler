package com.example.familyscheduler.data.repository

import com.example.familyscheduler.data.local.dao.RequirementOverrideDao
import com.example.familyscheduler.data.mapper.RequirementOverrideMapper
import com.example.familyscheduler.domain.requirement.RequirementOverride
import com.example.familyscheduler.domain.requirement.repository.RequirementOverrideRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class RoomRequirementOverrideRepository(
    private val dao: RequirementOverrideDao
) : RequirementOverrideRepository {

    override fun getAllFlow(): Flow<List<RequirementOverride>> {
        return dao.getAll().map { list ->
            list.map { RequirementOverrideMapper.toDomain(it) }
        }
    }

    //使い方は↓
    //val overridesForDate = currentDate.flatMapLatest { date -> repository.getByDate(date) }
    override fun getByDate(date: LocalDate): Flow<List<RequirementOverride>> {
        return dao.getByDate(date.toString()).map { list ->
            list.map { RequirementOverrideMapper.toDomain(it) }
        }
    }

    //いらない？
    override fun getOverrides(ruleId: String, date: LocalDate): Flow<List<RequirementOverride>> {
        return dao.getByRuleAndDate(ruleId, date.toString()).map { list ->
            list.map { RequirementOverrideMapper.toDomain(it) }
        }
    }

    override suspend fun saveOverride(override: RequirementOverride) {

        val entity = RequirementOverrideMapper.toEntity(override)

        // 同種override置き換え（重要）
        dao.deleteSameType(
            ruleId = entity.ruleId,
            date = entity.date,
            type = entity.type
        )

        dao.insert(entity)
    }

    override suspend fun deleteByRuleId(ruleId: String) {
        dao.deleteByRuleId(ruleId)
    }
}