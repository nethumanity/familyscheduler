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

    override fun getAllFlow(): Flow<List<HouseholdRequirementRule>> {
        return dao.getAll().map { list ->
            list.map { HouseholdRequirementMapper.toDomain(it) }
        }
    }

    // いらない？
    override fun getByDate(
        date: LocalDate
    ): Flow<List<HouseholdRequirementRule>> {
        return getAllFlow().map { list ->
            list.filter { it.isActiveOn(date) }
        }
    }

    // 編集画面用
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