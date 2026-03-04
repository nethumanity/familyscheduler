package com.example.familyscheduler.data.repository

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.schedule.DailyState
import java.time.LocalDate

object InMemoryDailyStateRepository {

    private val storage =
        mutableMapOf<Pair<LocalDate, Person>, DailyState>()

    fun save(state: DailyState) {
        storage[state.date to state.person] = state
    }

    fun get(date: LocalDate): List<DailyState> {
        return storage
            .filterKeys { it.first == date }
            .values
            .toList()
    }
}
