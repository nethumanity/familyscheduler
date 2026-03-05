package com.example.familyscheduler.domain.requirement

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.slot.FlexWindowParameters
import com.example.familyscheduler.domain.slot.SlotState
import com.example.familyscheduler.domain.time.TimeRange
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID

data class HouseholdRequirementRule(
    val id: UUID = UUID.randomUUID(),
    val taskName: String,
    val targetState: SlotState,
    val requiredCount: Int,
    val allowedPersons: Set<Person>,
    val flexWindowSlots: FlexWindowParameters,
    val date: LocalDate?,               // 入力系①日付指定パターン用
    val daysOfWeek: Set<DayOfWeek>?,    // 入力系②毎日/曜日指定パターン、③子どもルーティン用
    val timeRange: TimeRange
)