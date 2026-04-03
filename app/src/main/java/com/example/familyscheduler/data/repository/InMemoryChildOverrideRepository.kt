package com.example.familyscheduler.data.repository

import com.example.familyscheduler.domain.routine.ChildTodayRoutine
import com.example.familyscheduler.domain.routine.repository.ChildOverrideRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class InMemoryChildOverrideRepository: ChildOverrideRepository {

    private val _childOverrides =
        MutableStateFlow<Map<Pair<String, LocalDate>, ChildTodayRoutine>>(emptyMap())

    override fun getAllFlow(): Flow<Map<Pair<String, LocalDate>, ChildTodayRoutine>> {
        return _childOverrides
    }

    // 編集画面用（いらない？）
    override fun getOverride(
        childName: String,
        date: LocalDate
    ): Flow<ChildTodayRoutine?> {
        return _childOverrides
            .map { map ->
                map[childName to date]
            }
    }

    override suspend fun saveOverride(
        childName: String,
        date: LocalDate,
        routine: ChildTodayRoutine
    ) {
        _childOverrides.update { old ->
            old + ((childName to date) to routine)
        }
    }

    override suspend fun deleteByChildName(childName: String) {
        _childOverrides.update { old ->
            old.filterKeys { (name, _) -> name != childName }
        }
    }
}