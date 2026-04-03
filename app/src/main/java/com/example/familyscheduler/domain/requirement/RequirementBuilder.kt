package com.example.familyscheduler.domain.requirement

import com.example.familyscheduler.domain.slot.FlexWindowParameters
import com.example.familyscheduler.domain.time.TimeAxis

class RequirementBuilder {

    fun build(
        rules: List<HouseholdRequirementRule>,
        overrides: List<RequirementOverride>
    ): List<HouseholdRequirement> {

        val activeRules =
            applyToggleOverrides(rules, overrides)

        return activeRules
            .map { it.toRequirement() }
            .let { applyShiftOverrides(it, overrides) }
    }

    private fun applyToggleOverrides(
        rules: List<HouseholdRequirementRule>,
        overrides: List<RequirementOverride>
    ): List<HouseholdRequirementRule> {

        val toggleMap = overrides
            .filterIsInstance<RequirementToggleOverride>()
            .associateBy { it.ruleId }

        return rules.mapNotNull { rule ->

            val mode = toggleMap[rule.id]?.mode ?: RequirementModeToday.AUTO

            when (mode) {

                RequirementModeToday.CANCELED -> null

                RequirementModeToday.REVERSE ->  rule
                // 適用条件はVMのToggle関数で管理
                // ロジックはSolverで処理

                RequirementModeToday.AUTO -> rule
            }
        }
    }

    private fun applyShiftOverrides(
        requirements: List<HouseholdRequirement>,
        overrides: List<RequirementOverride>
    ): List<HouseholdRequirement> {

        val shiftMap = overrides
            .filterIsInstance<RequirementShiftOverride>()
            .associateBy { it.ruleId }

        return requirements.map { req ->

            if (req !is TimeRangeHouseholdRequirement) return@map req

            val ruleId = req.sourceRuleId ?: return@map req

            val shift = shiftMap[ruleId] ?: return@map req

            val delta = shift.deltaSteps

            // ★ 範囲外ガードとして、現案はclamp処理（UI都合）
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
