package com.example.familyscheduler.domain.requirement

enum class RequirementModeToday {
    AUTO,            // 自動で割り当て
    CANCELED,        // その日の予定はキャンセル
    SOLO,            // 2人でやる予定を1人で実行
    REVERSE;         // 割り当てを配偶者に変更

    fun next(
        cancelApplicable: Boolean,
        soloApplicable: Boolean,
        reverseAssignable: Boolean
    ): RequirementModeToday {

        if (!cancelApplicable) {

            return when {
                soloApplicable ->
                    when (this) {
                        AUTO -> SOLO
                        SOLO -> AUTO
                        CANCELED -> AUTO
                        REVERSE -> AUTO
                    }
                reverseAssignable ->
                    when (this) {
                        AUTO -> REVERSE
                        REVERSE -> AUTO
                        CANCELED -> AUTO
                        SOLO -> AUTO
                    }
                else  -> AUTO
            }
        }

        return when {
            soloApplicable ->
                when (this) {
                    AUTO -> SOLO
                    SOLO -> CANCELED
                    CANCELED -> AUTO
                    REVERSE -> AUTO
                }
            reverseAssignable ->
                when (this) {
                    AUTO -> REVERSE
                    REVERSE -> CANCELED
                    CANCELED -> AUTO
                    SOLO -> AUTO
                }
            else ->
                when (this) {
                    AUTO -> CANCELED
                    CANCELED -> AUTO
                    SOLO -> CANCELED
                    REVERSE -> CANCELED
                }
        }
    }
}