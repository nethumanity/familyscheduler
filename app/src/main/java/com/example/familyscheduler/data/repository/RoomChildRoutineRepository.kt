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

    // 編集画面用
    override fun getByChildName(childName: String): Flow<ChildRoutineInput?> {
        return dao.getByName(childName).map {
            it?.let { ChildRoutineMapper.toDomain(it) }
        }
    }

    override suspend fun save(input: ChildRoutineInput) {
        dao.insert(ChildRoutineMapper.toEntity(input))
    }

    override suspend fun delete(name: String) {
        dao.delete(name)
    }
}