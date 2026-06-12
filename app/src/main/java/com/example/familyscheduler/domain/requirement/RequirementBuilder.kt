package com.example.familyscheduler.domain.requirement

import com.example.familyscheduler.domain.slot.FlexWindowParameters
import com.example.familyscheduler.domain.time.TimeAxis

class RequirementBuilder {

    fun build(
        rules: List<HouseholdRequirementRule>,
        modeMap: Map<String, RequirementToggleOverride>,
        requirementShiftMap: Map<String, RequirementShiftOverride>
    ): List<HouseholdRequirement> {

        val activeRules =
            applyToggleOverrides(rules, modeMap)

        return activeRules
            .map { rule ->
                rule.toRequirement(
                    requiredCount =
                        effectiveRequiredCount(rule, modeMap)
                )
            }
            .let { applyShiftOverrides(it, requirementShiftMap) }
    }

    private fun applyToggleOverrides(
        rules: List<HouseholdRequirementRule>,
        modeMap: Map<String, RequirementToggleOverride>
    ): List<HouseholdRequirementRule> {

        return rules.mapNotNull { rule ->

            val mode = modeMap[rule.id]?.mode ?: RequirementModeToday.AUTO

            when (mode) {

                RequirementModeToday.CANCELED -> null

                RequirementModeToday.SOLO -> rule

                RequirementModeToday.REVERSE -> rule

                RequirementModeToday.AUTO -> rule
            }
        }
    }

    private fun effectiveRequiredCount(
        rule: HouseholdRequirementRule,
        modeMap: Map<String, RequirementToggleOverride>
    ): Int =
        when (modeMap[rule.id]?.mode) {
            RequirementModeToday.SOLO -> 1
            else -> rule.requiredCount
        }

    private fun applyShiftOverrides(
        requirements: List<HouseholdRequirement>,
        requirementShiftMap: Map<String, RequirementShiftOverride>
    ): List<HouseholdRequirement> {

        return requirements.map { req ->

            if (req !is TimeRangeHouseholdRequirement) return@map req

            val ruleId = req.sourceRuleId

            val shift = requirementShiftMap[ruleId] ?: return@map req

            val delta = shift.deltaSteps

            // 範囲外ガードとして、現在はclamp処理（UI都合）
            val newStart = (req.startIndex + delta).coerceIn(0, TimeAxis.indices.last)
            val newEnd = (req.endIndex + delta).coerceIn(1, TimeAxis.indices.last + 1)

            req.copy(
                flexWindowSlots =
                    FlexWindowParameters(
                        req.flexWindowSlots.backward + delta,
                        req.flexWindowSlots.forward- delta
                    ),
                startIndex = newStart,
                endIndex = newEnd
            )
        }
    }
}
