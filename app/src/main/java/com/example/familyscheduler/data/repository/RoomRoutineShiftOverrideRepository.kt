package com.example.familyscheduler.data.repository

import com.example.familyscheduler.data.local.dao.RoutineShiftOverrideDao
import com.example.familyscheduler.data.mapper.RoutineShiftOverrideMapper
import com.example.familyscheduler.domain.routine.RoutineShiftOverride
import com.example.familyscheduler.domain.routine.repository.RoutineShiftOverrideRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import kotlin.collections.map

class RoomRoutineShiftOverrideRepository(
    private val dao: RoutineShiftOverrideDao
) : RoutineShiftOverrideRepository {

    override fun getAllFlow(): Flow<List<RoutineShiftOverride>> {
        return dao.getAll().map { list ->
            list.map { RoutineShiftOverrideMapper.toDomain(it) }
        }
    }

    override fun getByDate(date: LocalDate): Flow<List<RoutineShiftOverride>> {
        return dao.getByDate(date.toString()).map { list ->
            list.map { RoutineShiftOverrideMapper.toDomain(it) }
        }
    }

    override suspend fun replace(override: RoutineShiftOverride) {
        dao.insert(RoutineShiftOverrideMapper.toEntity(override))
    }

    override suspend fun deleteAllByChildId(childId: String) {
        dao.deleteByChildId(childId)
    }

    override suspend fun delete(override: RoutineShiftOverride) {
        dao.deleteByKey(
            override.childId,
            override.date.toString(),
            override.eventType.name
        )
    }
}