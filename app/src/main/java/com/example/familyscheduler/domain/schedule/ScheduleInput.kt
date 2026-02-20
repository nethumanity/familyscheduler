package com.example.familyscheduler.domain.schedule

import com.example.familyscheduler.domain.person.Person
import java.time.LocalTime
import java.util.UUID

data class ScheduleInput(    //まだ信頼しない・保存しないデータ
    val person: Person,
    val title: String,        //デフォルトは「出勤」、このほか「在宅」などユーザーが（別UIで？）任意に追加していく
    val scheduleTypeId: UUID,    //ScheduleTypeはResolverで取得、UI入力中にScheduleTypeが変更されても壊れない
    val startTime: LocalTime,
    val endTime: LocalTime,
    val repeatRule: RepeatRule    //Daily / Weekly  ←UIでは曜日から選択（在宅は水金にチェック、など）してもらう
)
