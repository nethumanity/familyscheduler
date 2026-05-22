package com.example.familyscheduler.ui.state

import com.example.familyscheduler.domain.person.Person

sealed class MainSheet {
    object CHILD: MainSheet()
    object DAILY_OVERVIEW: MainSheet()
    data class TEMPLATE(val person: Person): MainSheet()
}