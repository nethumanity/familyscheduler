package com.example.familyscheduler.data.repository

import com.example.familyscheduler.data.local.dao.RoutineToggleOverrideDao
import com.example.familyscheduler.data.mapper.RoutineToggleOverrideMapper
import com.example.familyscheduler.domain.routine.ChildTodayRoutine
import com.example.familyscheduler.domain.routine.repository.RoutineToggleOverrideRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import kotlin.collections.map

class RoomRoutineToggleOverrideRepository(
    private val dao: RoutineToggleOverrideDao
): RoutineToggleOverrideRepository {

    override fun getAllFlow(): Flow<Map<Pair<String, LocalDate>, ChildTodayRoutine>> {
        return dao.getAll().map { list ->
            list.map { RoutineToggleOverrideMapper.toDomain(it) }
                .toMap()
        }
    }

    override fun getByDate(date: LocalDate): Flow<Map<Pair<String, LocalDate>, ChildTodayRoutine>> {
        return dao.getByDate(date.toString()).map { list ->
            list.map { RoutineToggleOverrideMapper.toDomain(it) }
                .toMap()
        }
    }

    override suspend fun replace(
        childId: String,
        date: LocalDate,
        routine: ChildTodayRoutine
    ) {
        dao.insert(RoutineToggleOverrideMapper.toEntity(childId, date, routine))
    }

    override suspend fun deleteAllByChildId(childId: String) {
        dao.deleteByChildId(childId)
    }
}