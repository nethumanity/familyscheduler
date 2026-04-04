package com.example.familyscheduler.data.repository

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.schedule.DailyState
import com.example.familyscheduler.domain.schedule.repository.DailyStateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class InMemoryDailyStateRepository : DailyStateRepository {

    private val _states = MutableStateFlow<Map<Pair<LocalDate, Person>, DailyState>>(emptyMap())

    override fun getAllFlow(): Flow<Map<Pair<LocalDate, Person>, DailyState>> {
        return _states
    }

    // いらない？使い方は↓
    //currentDate.flatMapLatest { date ->
    //dailyStateRepository.getByDate(date)
    override fun getByDate(date: LocalDate): Flow<List<DailyState>> {
        return _states
            .map { map ->
                map
                    .filterKeys { (d, _) -> d == date }
                    .values
                    .toList()
            }
    }

    override suspend fun save(state: DailyState) {
        _states.update { old ->
            old + ((state.date to state.person) to state)
        }
    }
}