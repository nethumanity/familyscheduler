package com.example.familyscheduler.domain.requirement

enum class RequirementSource(
    val semantics: RequirementSemantics = RequirementSemantics.TASK
) {
    USER(RequirementSemantics.TASK),
    CHILD_ROUTINE(RequirementSemantics.STATE),
    NURSERY_DROP_OFF(RequirementSemantics.EVENT),
    NURSERY_PICKUP(RequirementSemantics.EVENT),
    BEDTIME(RequirementSemantics.EVENT)
}