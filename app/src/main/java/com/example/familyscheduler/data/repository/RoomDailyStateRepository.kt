package com.example.familyscheduler.data.repository

import com.example.familyscheduler.data.local.dao.DailyStateDao
import com.example.familyscheduler.data.mapper.DailyStateMapper
import com.example.familyscheduler.domain.schedule.DailyState
import com.example.familyscheduler.domain.schedule.repository.DailyStateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class RoomDailyStateRepository(
    private val dao: DailyStateDao
) : DailyStateRepository {

    override fun getByDate(date: LocalDate): Flow<List<DailyState>> {
        return dao.getByDate(date.toString()).map { list ->
            list.map { DailyStateMapper.toDomain(it) }
        }
    }

    override suspend fun save(state: DailyState) {
        dao.insert(DailyStateMapper.toEntity(state))
    }
}