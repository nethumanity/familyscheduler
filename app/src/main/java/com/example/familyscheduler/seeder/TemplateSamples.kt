package com.example.familyscheduler.seeder

import com.example.familyscheduler.domain.person.Person
import com.example.familyscheduler.domain.schedule.DailyTemplate
import com.example.familyscheduler.domain.schedule.RepeatRule
import com.example.familyscheduler.domain.schedule.ScheduleTemplate
import com.example.familyscheduler.domain.schedule.ScheduleType
import com.example.familyscheduler.domain.schedule.ScheduleTypes
import com.example.familyscheduler.domain.schedule.StateCategory
import com.example.familyscheduler.domain.time.TimeRange
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.UUID

object TemplateSamples {

    fun defaultTemplates(): List<DailyTemplate> {

        return listOf(

            // ===============================
            // FATHER OFFICE
            // ===============================

            DailyTemplate(
                id = UUID.randomUUID(),
                person = Person.FATHER,
                name = "OFFICE",
                schedules = listOf(

                    ScheduleTemplate(
                        type = ScheduleTypes.SLEEP,
                        timeRange = TimeRange(
                            LocalTime.of(0, 0),
                            LocalTime.of(6, 0)
                        )
                    ),

                    ScheduleTemplate(
                        type = ScheduleTypes.COMMUTE_GO,
                        timeRange = TimeRange(
                            LocalTime.of(6,30),
                            LocalTime.of(9,0)
                        )
                    ),

                    ScheduleTemplate(
                        type = ScheduleTypes.WORK,
                        timeRange = TimeRange(
                            LocalTime.of(9,0),
                            LocalTime.of(17,30)
                        )
                    ),

                    ScheduleTemplate(
                        type = ScheduleTypes.COMMUTE_BACK,
                        timeRange = TimeRange(
                            LocalTime.of(17,30),
                            LocalTime.of(20,0)
                        )
                    ),

                    ScheduleTemplate(
                        type = ScheduleTypes.SLEEP,
                        timeRange = TimeRange(
                            LocalTime.of(22,0),
                            LocalTime.of(0,0)
                        )
                    )

                ),
                repeatRule = RepeatRule.Weekly(
                    setOf(
                        DayOfWeek.MONDAY,
                        DayOfWeek.TUESDAY,
                        DayOfWeek.FRIDAY
                    )
                )
            ),

            // ===============================
            // MOTHER OFFICE
            // ===============================

            DailyTemplate(
                id = UUID.randomUUID(),
                person = Person.MOTHER,
                name = "OFFICE",
                schedules = listOf(

                    ScheduleTemplate(
                        type = ScheduleTypes.SLEEP,
                        timeRange = TimeRange(
                            LocalTime.of(0,0),
                            LocalTime.of(5,30)
                        )
                    ),

                    ScheduleTemplate(
                        type = ScheduleTypes.COMMUTE_GO,
                        timeRange = TimeRange(
                            LocalTime.of(7,30),
                            LocalTime.of(9,0)
                        )
                    ),

                    ScheduleTemplate(
                        type = ScheduleTypes.WORK,
                        timeRange = TimeRange(
                            LocalTime.of(9,0),
                            LocalTime.of(18,0)
                        )
                    ),

                    ScheduleTemplate(
                        type = ScheduleTypes.COMMUTE_BACK,
                        timeRange = TimeRange(
                            LocalTime.of(18,0),
                            LocalTime.of(19,0)
                        )
                    ),

                    ScheduleTemplate(
                        type = ScheduleTypes.SLEEP,
                        timeRange = TimeRange(
                            LocalTime.of(21,30),
                            LocalTime.of(0,0)
                        )
                    )

                ),
                repeatRule = RepeatRule.Weekly(
                    setOf(
                        DayOfWeek.MONDAY,
                        DayOfWeek.WEDNESDAY
                    )
                )
            ),

            // ===============================
            // FATHER REMOTE
            // ===============================

            DailyTemplate(
                id = UUID.randomUUID(),
                person = Person.FATHER,
                name = "REMOTE",
                schedules = listOf(

                    ScheduleTemplate(
                        type = ScheduleTypes.SLEEP,
                        timeRange = TimeRange(
                            LocalTime.of(0,0),
                            LocalTime.of(6,0)
                        )
                    ),

                    ScheduleTemplate(
                        type = ScheduleTypes.WORK,
                        timeRange = TimeRange(
                            LocalTime.of(8,30),
                            LocalTime.of(17,30)
                        )
                    ),

                    ScheduleTemplate(
                        type = ScheduleTypes.SLEEP,
                        timeRange = TimeRange(
                            LocalTime.of(22,0),
                            LocalTime.of(0,0)
                        )
                    )

                ),
                repeatRule = RepeatRule.Weekly(
                    setOf(
                        DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY
                    )
                )
            ),

            // ===============================
            // MOTHER REMOTE
            // ===============================

            DailyTemplate(
                id = UUID.randomUUID(),
                person = Person.MOTHER,
                name = "REMOTE",
                schedules = listOf(

                    ScheduleTemplate(
                        type = ScheduleTypes.SLEEP,
                        timeRange = TimeRange(
                            LocalTime.of(0,0),
                            LocalTime.of(5,30)
                        )
                    ),

                    ScheduleTemplate(
                        type = ScheduleTypes.COMMUTE_GO,
                        timeRange = TimeRange(
                            LocalTime.of(8,30),
                            LocalTime.of(9,0)
                        )
                    ),

                    ScheduleTemplate(
                        type = ScheduleTypes.WORK,
                        timeRange = TimeRange(
                            LocalTime.of(9,0),
                            LocalTime.of(18,0)
                        )
                    ),

                    ScheduleTemplate(
                        type = ScheduleTypes.SLEEP,
                        timeRange = TimeRange(
                            LocalTime.of(21,30),
                            LocalTime.of(0,0)
                        )
                    )

                ),
                repeatRule = RepeatRule.Weekly(
                    setOf(
                        DayOfWeek.TUESDAY
                    )
                )
            ),

            // ===============================
            // FATHER OVERWORK
            // ===============================

            DailyTemplate(
                id = UUID.randomUUID(),
                person = Person.FATHER,
                name = "OVERWORK",
                schedules = listOf(

                    ScheduleTemplate(
                        type = ScheduleTypes.SLEEP,
                        timeRange = TimeRange(
                            LocalTime.of(0,0),
                            LocalTime.of(6,0)
                        )
                    ),

                    ScheduleTemplate(
                        type = ScheduleTypes.COMMUTE_GO,
                        timeRange = TimeRange(
                            LocalTime.of(6,30),
                            LocalTime.of(9,0)
                        )
                    ),

                    ScheduleTemplate(
                        type = ScheduleTypes.WORK,
                        timeRange = TimeRange(
                            LocalTime.of(9,0),
                            LocalTime.of(18,30)
                        )
                    ),

                    ScheduleTemplate(
                        type = ScheduleTypes.COMMUTE_BACK,
                        timeRange = TimeRange(
                            LocalTime.of(18,30),
                            LocalTime.of(21,0)
                        )
                    ),

                    ScheduleTemplate(
                        type = ScheduleTypes.SLEEP,
                        timeRange = TimeRange(
                            LocalTime.of(22,0),
                            LocalTime.of(0,0)
                        )
                    )

                ),
                repeatRule = RepeatRule.None
            ),

            // ===============================
            // MOTHER OVERWORK
            // ===============================

            DailyTemplate(
                id = UUID.randomUUID(),
                person = Person.MOTHER,
                name = "OVERWORK",
                schedules = listOf(

                    ScheduleTemplate(
                        type = ScheduleTypes.SLEEP,
                        timeRange = TimeRange(
                            LocalTime.of(0,0),
                            LocalTime.of(5,30)
                        )
                    ),

                    ScheduleTemplate(
                        type = ScheduleTypes.COMMUTE_GO,
                        timeRange = TimeRange(
                            LocalTime.of(7,30),
                            LocalTime.of(9,0)
                        )
                    ),

                    ScheduleTemplate(
                        type = ScheduleTypes.WORK,
                        timeRange = TimeRange(
                            LocalTime.of(9,0),
                            LocalTime.of(20,0)
                        )
                    ),

                    ScheduleTemplate(
                        type = ScheduleTypes.COMMUTE_BACK,
                        timeRange = TimeRange(
                            LocalTime.of(20,0),
                            LocalTime.of(21,0)
                        )
                    ),

                    ScheduleTemplate(
                        type = ScheduleTypes.SLEEP,
                        timeRange = TimeRange(
                            LocalTime.of(21,30),
                            LocalTime.of(0,0)
                        )
                    )

                ),
                repeatRule = RepeatRule.Weekly(
                    setOf(
                        DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY
                    )
                )
            ),

            // ===============================
            // FATHER HOLIDAY
            // ===============================

            DailyTemplate(
                id = UUID.randomUUID(),
                person = Person.FATHER,
                name = "HOLIDAY",
                schedules = listOf(

                    ScheduleTemplate(
                        type = ScheduleTypes.SLEEP,
                        timeRange = TimeRange(
                            LocalTime.of(0,0),
                            LocalTime.of(6,0)
                        )
                    ),

                    ScheduleTemplate(
                        type = ScheduleType(
                            "趣味",
                            StateCategory.BLOCKED,
                            3
                        ),
                        timeRange = TimeRange(
                            LocalTime.of(10,0),
                            LocalTime.of(14,0)
                        )
                    ),

                    ScheduleTemplate(
                        type = ScheduleType(
                            "趣味",
                            StateCategory.BLOCKED,
                            3
                        ),
                        timeRange = TimeRange(
                            LocalTime.of(20,0),
                            LocalTime.of(22,0)
                        )
                    ),

                    ScheduleTemplate(
                        type = ScheduleTypes.SLEEP,
                        timeRange = TimeRange(
                            LocalTime.of(22,0),
                            LocalTime.of(0,0)
                        )
                    )

                ),
                repeatRule = RepeatRule.Weekly(
                    setOf(
                        DayOfWeek.SATURDAY,
                        DayOfWeek.SUNDAY
                    )
                )
            ),

            // ===============================
            // MOTHER HOLIDAY
            // ===============================

            DailyTemplate(
                id = UUID.randomUUID(),
                person = Person.MOTHER,
                name = "HOLIDAY",
                schedules = listOf(

                    ScheduleTemplate(
                        type = ScheduleTypes.SLEEP,
                        timeRange = TimeRange(
                            LocalTime.of(0,0),
                            LocalTime.of(5,30)
                        )
                    ),

                    ScheduleTemplate(
                        type = ScheduleType(
                            "家事全般",
                            StateCategory.BLOCKED,
                            3
                        ),
                        timeRange = TimeRange(
                            LocalTime.of(13,30),
                            LocalTime.of(15,0)
                        )
                    ),

                    ScheduleTemplate(
                        type = ScheduleType(
                            "自由時間",
                            StateCategory.BLOCKED,
                            3
                        ),
                        timeRange = TimeRange(
                            LocalTime.of(15,0),
                            LocalTime.of(17,0)
                        )
                    ),

                    ScheduleTemplate(
                        type = ScheduleTypes.SLEEP,
                        timeRange = TimeRange(
                            LocalTime.of(21,30),
                            LocalTime.of(0,0)
                        )
                    )

                ),
                repeatRule = RepeatRule.Weekly(
                    setOf(
                        DayOfWeek.SATURDAY,
                        DayOfWeek.SUNDAY
                    )
                )
            )
        )
    }
}