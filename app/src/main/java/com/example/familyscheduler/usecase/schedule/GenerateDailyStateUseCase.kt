package com.example.familyscheduler.usecase.schedule

import com.example.familyscheduler.domain.schedule.DailyTemplate
import com.example.familyscheduler.domain.schedule.generator.DailyStateGenerator
import com.example.familyscheduler.domain.schedule.repository.DailyStateRepository
import java.time.LocalDate
import java.time.LocalTime

interface GenerateDailyStateUseCase {

    suspend operator fun invoke(
        template: DailyTemplate,
        date: LocalDate
    )
}

class GenerateDailyStateUseCaseImpl(
    private val repository: DailyStateRepository,
    private val timeAxis: List<LocalTime>
) : GenerateDailyStateUseCase {

    override suspend fun invoke(
        template: DailyTemplate,
        date: LocalDate
    ) {

        val state = DailyStateGenerator.generate(
            template = template,
            date = date,
            timeAxis = timeAxis
        )

        repository.save(state)
    }
}

