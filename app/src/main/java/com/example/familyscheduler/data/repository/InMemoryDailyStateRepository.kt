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

/*
class InMemoryDailyStateRepository : DailyStateRepository {

    private val states = mutableListOf<DailyState>()

    override suspend fun get(date: LocalDate, person: Person): DailyState? {
        return states.find {
            it.date == date && it.person == person
        }
    }

    override suspend fun save(state: DailyState) {
        states.removeIf {
            it.date == state.date && it.person == state.person
        }
        states.add(state)
    }
}

 */

