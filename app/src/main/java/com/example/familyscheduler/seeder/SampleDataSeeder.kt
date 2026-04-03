package com.example.familyscheduler.seeder

import com.example.familyscheduler.domain.requirement.repository.HouseholdRequirementRepository
import com.example.familyscheduler.domain.routine.repository.ChildRoutineRepository
import com.example.familyscheduler.domain.schedule.repository.TemplateRepository

object SampleDataSeeder {

    suspend fun seed(
        templateRepository: TemplateRepository,
        requirementRepository: HouseholdRequirementRepository,
        childRoutineRepository: ChildRoutineRepository
    ) {

        seedRequirements(requirementRepository)
        seedRoutines(childRoutineRepository)
        seedTemplates(templateRepository)

    }

    private suspend fun seedTemplates(
        repo: TemplateRepository
    ) {
        TemplateSamples.defaultTemplates()
            .forEach { repo.save(it) }
    }

    private suspend fun seedRequirements(
        repo: HouseholdRequirementRepository
    ) {

        RequirementSamples.defaultRequirements()
            .forEach { repo.save(it) }
    }

    private suspend fun seedRoutines(
        repo: ChildRoutineRepository
    ) {

        RoutineSamples.defaultRoutine()
            .forEach { repo.save(it) }
    }

}