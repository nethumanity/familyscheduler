package com.example.familyscheduler.data.repository

import com.example.familyscheduler.data.local.dao.HouseholdRequirementDao
import com.example.familyscheduler.data.mapper.HouseholdRequirementMapper
import com.example.familyscheduler.domain.requirement.HouseholdRequirementRule
import com.example.familyscheduler.domain.requirement.repository.HouseholdRequirementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class RoomHouseholdRequirementRepository(
    private val dao: HouseholdRequirementDao
) : HouseholdRequirementRepository {

    override fun getByDate(
        date: LocalDate
    ): Flow<List<HouseholdRequirementRule>> {

        val dateStr = date.toString()
        val dayStr = date.dayOfWeek.name

        return dao.getByDate(dateStr, dayStr).map { list ->
            list.map { HouseholdRequirementMapper.toDomain(it) }
        }
    }

    override fun getById(
        id: String
    ): Flow<HouseholdRequirementRule?> {
        return dao.getById(id).map {
            it?.let { HouseholdRequirementMapper.toDomain(it) }
        }
    }

    override suspend fun save(
        rule: HouseholdRequirementRule
    ) {
        dao.insert(HouseholdRequirementMapper.toEntity(rule))
    }

    override suspend fun delete(id: String) {
        dao.delete(id)
    }
}