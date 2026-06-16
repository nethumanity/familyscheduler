package com.example.familyscheduler.ui.presentation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.requirement.RequirementModeToday
import com.example.familyscheduler.ui.projection.StatusUiModel

object StateTextPresentation {

    fun baseColor(
        mode: RequirementModeToday,
        isProposalApplied: Boolean = false
    ): Color =
        when {
            mode == RequirementModeToday.CANCELED ->  Color.LightGray
            isProposalApplied && mode != RequirementModeToday.CANCELED -> Color(0xFF1976D2)
            else -> Color.Unspecified
        }

    fun stateColor(
        status: StatusUiModel
    ): Color =
        when (status) {
            is StatusUiModel.Warning -> Color.Red
            is StatusUiModel.Assigned -> Color.Green
            is StatusUiModel.Reverse -> Color.Green
            is StatusUiModel.Solo -> Color.Green
            is StatusUiModel.Canceled -> Color.LightGray
        }

    fun stateText(status: StatusUiModel): AnnotatedString =
        when (status) {

            is StatusUiModel.Warning -> {
                buildAnnotatedString {
                    append("⚠ ${status.assignableCount}/${status.requiredCount}")
                }
            }

            is StatusUiModel.Assigned -> {
                when {
                    status.requiredCount > 1 -> {
                        buildAnnotatedString {
                            append(
                                "✔ ${status.persons.joinToString(" ") { it.label }}"
                            )
                        }
                    }

                    else -> {
                        when {
                            status.persons.size == 2 -> {
                                buildReverseAssignableText (
                                    status.persons.first(),
                                    status.persons.last()
                                )
                            }
                            status.reverseAssignable -> {
                                buildReverseAssignableText (
                                    status.persons.single(),
                                    (Person.entries - status.persons).single()
                                )
                            }
                            else -> {
                                buildAnnotatedString {
                                    append("✔ ${status.persons.first().label}")
                                }
                            }
                        }
                    }
                }
            }

            is StatusUiModel.Reverse -> {
                buildReverseAssignableText(
                    status.mainPerson,
                    status.anotherPerson
                )
            }

            is StatusUiModel.Solo -> {
                buildSoloAppliedText(
                    status.assignedPerson,
                    status.blockedPerson
                )
            }

            is StatusUiModel.Canceled -> {
                buildAnnotatedString {
                    append("キャンセル")
                }
            }
        }

    private fun buildReverseAssignableText(
        mainPerson: Person,
        anotherPerson: Person
    ): AnnotatedString =

        buildAnnotatedString {

            append("✔ ${mainPerson.label}")

            withStyle(
                style = SpanStyle(
                    color = Color.LightGray,
                    fontSize = 10.sp
                )
            ) {
                append(" ⇄ ${anotherPerson.label}")
            }
        }

    private fun buildSoloAppliedText(
        assignedPerson: Person,
        blockedPerson: Person
    ): AnnotatedString =

        buildAnnotatedString {

            append("✔ ${assignedPerson.label}")

            withStyle(
                style = SpanStyle(
                    color = Color.Red,
                    fontSize = 10.sp
                )
            ) {
                append(" × ${blockedPerson.label}")
            }
        }
}