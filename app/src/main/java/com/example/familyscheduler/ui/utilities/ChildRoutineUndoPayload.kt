package com.example.familyscheduler.ui.utilities

import com.example.familyscheduler.domain.routine.ChildRoutineInput
import com.example.familyscheduler.domain.routine.RoutineOverrideSnapshot

data class ChildRoutineUndoPayload(
    val routine: ChildRoutineInput,
    val snapshot: RoutineOverrideSnapshot
)