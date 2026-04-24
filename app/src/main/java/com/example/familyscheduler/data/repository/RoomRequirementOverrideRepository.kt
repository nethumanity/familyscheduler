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

    override fun getByDate(date: LocalDate): Flow<List<RequirementOverride>> {
        return dao.getByDate(date.toString()).map { list ->
            list.map { RequirementOverrideMapper.toDomain(it) }
        }
    }

    override suspend fun replace(override: RequirementOverride) {

        val entity = RequirementOverrideMapper.toEntity(override)

        // 同種override置き換え（重要）
        dao.deleteByRuleDateType(
            ruleId = entity.ruleId,
            date = entity.date,
            type = entity.type
        )

        dao.insert(entity)
    }

    override suspend fun deleteAllByRuleId(ruleId: String) {
        dao.deleteByRuleId(ruleId)
    }

    override suspend fun delete(override: RequirementOverride) {
        dao.deleteByRuleDateType(
            override.ruleId,
            override.date.toString(),
            override.type.name
        )
    }
}