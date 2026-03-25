package com.example.familyscheduler.domain.requirement

import java.time.LocalDate

data class RequirementOverride(     //未実装（RequirementRuleからの差分）
    val date: LocalDate,
    val ruleId: String,
    val disabled: Boolean = true,    //ユーザーがキャンセルした時（ReqToday: 自動アサイン↔キャンセル↔逆転）
    val deltaSteps: Int,             //FlexResolveProposal実行時（仮）
    val splitIndex: Int              //Block分割を伴う移動時（仮）
)