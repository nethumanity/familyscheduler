package com.example.familyscheduler.domain.requirement

enum class RequirementSemantics {
    TASK,
    STATE,
    EVENT;

    fun canCoexist(existing: RequirementSemantics): Boolean {
        return when (this) {
            TASK ->
                existing == TASK || existing == STATE
            STATE ->
                existing == TASK || existing == STATE
            EVENT ->
                existing == STATE || existing == EVENT
        }
    }

    fun merge(incoming: RequirementSemantics): RequirementSemantics {
        return when {
            this == EVENT || incoming == EVENT -> EVENT
            this == TASK || incoming == TASK -> TASK
            else -> STATE
        }
    }
}