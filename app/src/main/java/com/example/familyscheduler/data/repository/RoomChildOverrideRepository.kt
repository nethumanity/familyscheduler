package com.example.familyscheduler.data.repository

import com.example.familyscheduler.data.local.dao.ChildOverrideDao
import com.example.familyscheduler.data.mapper.ChildOverrideMapper
import com.example.familyscheduler.domain.routine.ChildTodayRoutine
import com.example.familyscheduler.domain.routine.repository.ChildOverrideRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class RoomChildOverrideRepository(
    private val dao: ChildOverrideDao
): ChildOverrideRepository {

    override fun getAllFlow(): Flow<Map<Pair<String, LocalDate>, ChildTodayRoutine>> {
        return dao.getAll().map { list ->
            list.map { ChildOverrideMapper.toDomain(it) }
                .toMap()
        }
    }

    // いらない？
    override fun getOverride(
        childName: String,
        date: LocalDate
    ): Flow<ChildTodayRoutine?> {
        return dao.getByChildAndDate(childName, date.toString())
            .map { entity ->
                entity?.let {
                    ChildTodayRoutine.valueOf(it.routine)
                }
            }
    }

    override suspend fun saveOverride(
        childName: String,
        date: LocalDate,
        routine: ChildTodayRoutine
    ) {
        dao.insert(ChildOverrideMapper.toEntity(childName, date, routine))
    }

    override suspend fun deleteByChildName(childName: String) {
        dao.deleteByChildName(childName)
    }
}