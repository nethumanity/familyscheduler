package com.example.familyscheduler.data.repository

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.schedule.DailyState
import com.example.familyscheduler.domain.schedule.repository.DailyStateRepository
import java.time.LocalDate

class InMemoryDailyStateRepository : DailyStateRepository {

    private val storage =
        mutableMapOf<Pair<LocalDate, Person>, DailyState>()

    override suspend fun save(state: DailyState) {
        storage[state.date to state.person] = state
    }

    override suspend fun get(date: LocalDate): List<DailyState> {
        return storage
            .filterKeys { it.first == date }
            .values
            .toList()
    }

    override suspend fun get(date: LocalDate, person: Person): DailyState? {
        return storage[date to person]
    }
}