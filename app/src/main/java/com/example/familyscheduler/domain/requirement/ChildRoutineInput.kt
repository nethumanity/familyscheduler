package com.example.familyscheduler.domain.requirement

import com.example.familyscheduler.domain.time.TimeAxis
import com.example.familyscheduler.domain.time.TimeAxis.stepMinutes
import java.time.DayOfWeek
import java.time.LocalTime

data class ChildRoutineInput(
    val name: String,                       // 子どもの名前など（一意）
    val wakeUpTime: LocalTime,
    val sleepTime: LocalTime,
    val daysOfWeek: Set<DayOfWeek>,         // 空のsetも含めて必須項目
    val nurseryStart: LocalTime,
    val nurseryStartEarliest: LocalTime,    // 初期値はnurseryStart
    val nurseryStartLatest: LocalTime,      // 初期値はnurseryStart
    val nurseryEnd: LocalTime,
    val nurseryEndEarliest: LocalTime,      // 初期値はnurseryEnd
    val nurseryEndLatest: LocalTime         // 初期値はnurseryEnd
) {

    init {
        require(wakeUpTime < sleepTime)
        require(nurseryStart <= nurseryEnd)
        require(nurseryStartEarliest <= nurseryStart)
        require(nurseryStart <= nurseryStartLatest)
        require(nurseryEndEarliest <= nurseryEnd)
        require(nurseryEnd <= nurseryEndLatest)
    }

    // ChildCareBlockへの変換ロジック
    // 曜日処理（要確認）
    fun buildChildCareBlocks(inputs: List<ChildRoutineInput>
    ): List<ChildCareBlock> {

        val blocks = mutableListOf<ChildCareBlock>()

        DayOfWeek.values().forEach { day ->

            blocks += buildDayBlocks(day, inputs)

        }

        return mergeSameBlocks(blocks)
    }

    fun buildDayBlocks(
        day: DayOfWeek,
        inputs: List<ChildRoutineInput>
    ): List<ChildCareBlock> {

        val baseUnits = buildBaseUnits(day, inputs)

        val dropEvents = buildDropEvents(day, inputs)
        val pickupEvents = buildPickupEvents(day, inputs)

        val injected = injectEvents(baseUnits, dropEvents, pickupEvents)

        return compressUnits(injected, day)
    }

    private data class BaseUnit(
        val start: LocalTime,
        val end: LocalTime,
        val activeChildrenCount: Int
    )

    private fun buildBaseUnits(
        day: DayOfWeek,
        inputs: List<ChildRoutineInput>
    ): List<BaseUnit> {

        val units = mutableListOf<BaseUnit>()

        for (start in TimeAxis.all) {

            val end = start.plusMinutes(stepMinutes.toLong())

            val activeChildren = inputs.count { child ->

                val needsCareAtThisTime =
                    start >= child.wakeUpTime &&
                            start <= child.sleepTime &&   // 就寝時刻=寝かしつけ時刻と想定
                            (
                                    day !in child.daysOfWeek ||         // その日は登園しない
                                            start <= child.nurseryStart ||       // 登園含む
                                            start >= child.nurseryEnd           // お迎え含む
                                    )

                needsCareAtThisTime
            }

            if (activeChildren > 0) {
                units += BaseUnit(
                    start = start,
                    end = end,
                    activeChildrenCount = activeChildren
                )
            }
        }

        return units
    }

    private data class EventInfo(
        val label: ChildCareLabel,
        val flexEarliest: LocalTime,
        val flexLatest: LocalTime
    )

    private fun buildDropEvents(
        day: DayOfWeek,
        inputs: List<ChildRoutineInput>
    ): Map<LocalTime, EventInfo> {

        return inputs
            .filter { day in it.daysOfWeek }
            .groupBy { it.nurseryStart }
            .mapValues { (_, group) ->

                val earliest = group.maxOf { it.nurseryStartEarliest }
                val latest = group.minOf { it.nurseryStartLatest }

                EventInfo(
                    label = ChildCareLabel.NURSERY_DROP_OFF,
                    flexEarliest = earliest,
                    flexLatest = latest
                )
            }
    }

    private fun buildPickupEvents(
        day: DayOfWeek,
        inputs: List<ChildRoutineInput>
    ): Map<LocalTime, EventInfo> {

        return inputs
            .filter { day in it.daysOfWeek }
            .groupBy { it.nurseryEnd }
            .mapValues { (_, group) ->

                val earliest = group.maxOf { it.nurseryEndEarliest }
                val latest = group.minOf { it.nurseryEndLatest }

                EventInfo(
                    label = ChildCareLabel.NURSERY_PICKUP,
                    flexEarliest = earliest,
                    flexLatest = latest
                )
            }
    }

    private fun injectEvents(
        baseUnits: List<BaseUnit>,
        dropEvents: Map<LocalTime, EventInfo>,
        pickupEvents: Map<LocalTime, EventInfo>
    ): List<ChildCareBlock> {

        return baseUnits.map { unit ->

            val event =
                dropEvents[unit.start]
                    ?: pickupEvents[unit.start]

            if (event != null) {
                ChildCareBlock(
                    daysOfWeek = emptySet(), // ← compress時に設定
                    startTime = unit.start,
                    endTime = unit.end,
                    label = event.label,
                    flexEarliest = event.flexEarliest,
                    flexLatest = event.flexLatest,
                    activeChildrenCount = unit.activeChildrenCount
                )
            } else {
                ChildCareBlock(
                    daysOfWeek = emptySet(),
                    startTime = unit.start,
                    endTime = unit.end,
                    label = null,
                    flexEarliest = null,
                    flexLatest = null,
                    activeChildrenCount = unit.activeChildrenCount
                )
            }
        }
    }

    private fun compressUnits(
        blocks: List<ChildCareBlock>,
        day: DayOfWeek
    ): List<ChildCareBlock> {

        if (blocks.isEmpty()) return emptyList()

        val result = mutableListOf<ChildCareBlock>()
        var current = blocks.first()

        for (next in blocks.drop(1)) {

            val mergeable =
                current.label == next.label &&
                        current.activeChildrenCount == next.activeChildrenCount &&
                        current.flexEarliest == next.flexEarliest &&
                        current.flexLatest == next.flexLatest

            if (mergeable) {
                current = current.copy(
                    endTime = next.endTime
                )
            } else {
                result += current.copy(daysOfWeek = setOf(day))
                current = next
            }
        }

        result += current.copy(daysOfWeek = setOf(day))

        return result
    }

    //最後のマージ関数（要確認）
    private fun mergeSameBlocks(
        blocks: List<ChildCareBlock>
    ): List<ChildCareBlock> {

        return blocks
            .groupBy { Triple(it.startTime, it.endTime, it.label) } //条件足りない？
            .map { (_, group) ->

                group.first().copy(
                    daysOfWeek = group.flatMap { it.daysOfWeek }
                        .toSet()
                )
            }
    }
}