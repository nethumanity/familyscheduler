package com.example.familyscheduler.data.repository

import com.example.familyscheduler.data.local.dao.ChildRoutineDao
import com.example.familyscheduler.data.mapper.ChildRoutineMapper
import com.example.familyscheduler.domain.routine.ChildRoutineInput
import com.example.familyscheduler.domain.routine.repository.ChildRoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomChildRoutineRepository(
    private val dao: ChildRoutineDao
) : ChildRoutineRepository {

    override fun getAllFlow(): Flow<List<ChildRoutineInput>> {
        return dao.getAll().map { list ->
            list.map { ChildRoutineMapper.toDomain(it) }
        }
    }

    override fun getByChildId(childId: String): Flow<ChildRoutineInput?> {
        return dao.getByChildId(childId).map {
            it?.let { ChildRoutineMapper.toDomain(it) }
        }
    }

    override suspend fun save(input: ChildRoutineInput) {
        dao.insert(ChildRoutineMapper.toEntity(input))
    }

    override suspend fun delete(childId: String) {
        dao.delete(childId)
    }
}