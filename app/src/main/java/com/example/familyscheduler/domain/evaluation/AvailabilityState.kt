package com.example.familyscheduler.domain.evaluation

enum class AvailabilityState {
    NONE,       //要求なし
    OK,         //要求あるが充足
    WARN   //要求あるが不足
}