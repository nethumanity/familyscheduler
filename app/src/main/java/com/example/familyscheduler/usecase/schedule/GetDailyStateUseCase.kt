package com.example.familyscheduler.usecase.schedule

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.schedule.DailyState
import com.example.familyscheduler.domain.schedule.repository.DailyStateRepository
import java.time.LocalDate

interface GetDailyStateUseCase {

    suspend operator fun invoke(
        date: LocalDate,
        person: Person
    ): DailyState?
}

class GetDailyStateUseCaseImpl(
    private val repository: DailyStateRepository
) : GetDailyStateUseCase {

    override suspend fun invoke(
        date: LocalDate,
        person: Person
    ): DailyState? {
        return repository.get(date, person)
    }
}

