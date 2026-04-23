package com.example.familyscheduler.data.repository

import com.example.familyscheduler.domain.routine.RoutineShiftOverride
import com.example.familyscheduler.domain.routine.repository.RoutineShiftOverrideRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalDate
/*
class InMemoryRoutineShiftOverrideRepository: RoutineShiftOverrideRepository {

    private val _overrides = MutableStateFlow<List<RoutineShiftOverride>>(emptyList())

    override fun getAllFlow(): Flow<List<RoutineShiftOverride>> {
        return _overrides
    }

    //使い方は↓
    //val overridesForDate = currentDate.flatMapLatest { date -> repository.getByDate(date) }
    override fun getByDate(date: LocalDate): Flow<List<RoutineShiftOverride>> {
        return _overrides
            .map { list ->
                list.filter { it.date == date }
            }
            .distinctUntilChanged()
    }

    override suspend fun saveOverride(override: RoutineShiftOverride) {
        _overrides.update { old ->
            val filtered = old.filterNot {
                it.childId == override.childId &&
                        it.date == override.date &&
                        it.eventType == override.eventType
            }
            filtered + override
        }
    }

    override suspend fun deleteByChildId(childId: String) {
        _overrides.update { old ->
            val filtered = old.filterNot {
                it.childId == childId
            }
            filtered
        }
    }
}*/