package com.example.familyscheduler.data.mapper

import com.example.familyscheduler.data.local.entity.RequirementOverrideEntity
import com.example.familyscheduler.domain.requirement.RequirementModeToday
import com.example.familyscheduler.domain.requirement.RequirementOverride
import com.example.familyscheduler.domain.requirement.RequirementShiftOverride
import com.example.familyscheduler.domain.requirement.RequirementToggleOverride
import java.time.LocalDate

object RequirementOverrideMapper {

    fun toEntity(domain: RequirementOverride): RequirementOverrideEntity {
        return when (domain) {

            is RequirementToggleOverride -> {
                RequirementOverrideEntity(
                    ruleId = domain.ruleId,
                    date = domain.date.toString(),
                    type = "TOGGLE",
                    mode = domain.mode.name,
                    deltaSteps = null
                )
            }

            is RequirementShiftOverride -> {
                RequirementOverrideEntity(
                    ruleId = domain.ruleId,
                    date = domain.date.toString(),
                    type = "SHIFT",
                    mode = null,
                    deltaSteps = domain.deltaSteps
                )
            }
        }
    }

    fun toDomain(entity: RequirementOverrideEntity): RequirementOverride {

        return when (entity.type) {

            "TOGGLE" -> RequirementToggleOverride(
                ruleId = entity.ruleId,
                date = LocalDate.parse(entity.date),
                mode = RequirementModeToday.valueOf(entity.mode!!)
            )

            "SHIFT" -> RequirementShiftOverride(
                ruleId = entity.ruleId,
                date = LocalDate.parse(entity.date),
                deltaSteps = entity.deltaSteps ?: 0
            )

            else -> error("Unknown override type: ${entity.type}")
        }
    }
}