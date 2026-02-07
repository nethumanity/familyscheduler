package com.example.familyscheduler.domain.model

enum class AvailabilityState {
    NONE,       //要求なし
    OK,         //要求あるが充足
    WARN;   //要求あるが不足

    val shouldWarn: Boolean
        get() = this == WARN
}
