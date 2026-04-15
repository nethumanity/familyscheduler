package com.example.familyscheduler.domain.requirement

enum class RequirementModeToday {
    AUTO,            // 自動で割り当て
    CANCELED,        // その日の予定はキャンセル
    //SOLO,            // 2人でやる予定を1人で実行
    REVERSE;         // 割り当てを配偶者に変更（条件あり）

    fun next(
        reverseAssignable: Boolean
    ): RequirementModeToday {

        return if (reverseAssignable) {
            when (this) {
                AUTO -> REVERSE
                REVERSE -> CANCELED
                CANCELED -> AUTO
            }
        } else {
            when (this) {
                AUTO -> CANCELED
                CANCELED -> AUTO
                REVERSE -> AUTO
            }
        }
    }
}