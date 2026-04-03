package com.example.familyscheduler.data.repository

import com.example.familyscheduler.domain.routine.ChildRoutineInput
import com.example.familyscheduler.domain.routine.repository.ChildRoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class InMemoryChildRoutineRepository : ChildRoutineRepository {

    private val _routines = MutableStateFlow<List<ChildRoutineInput>>(emptyList())

    override fun getAllFlow(): Flow<List<ChildRoutineInput>> {
        return _routines
    }

    // 編集画面用
    override fun getByChildName(childName: String): Flow<ChildRoutineInput?> {
        return _routines
            .map { list ->
                list.firstOrNull { it.name == childName }
            }
    }

    override suspend fun save(input: ChildRoutineInput) {
        _routines.update { old ->
            val filtered = old.filterNot {
                it.name == input.name
            }
            filtered + input
        }
    }

    override suspend fun delete(name: String) {
        _routines.update { old ->
            val filtered = old.filterNot {
                it.name == name
            }
            filtered
        }
    }
}