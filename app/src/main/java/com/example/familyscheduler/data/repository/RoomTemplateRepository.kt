package com.example.familyscheduler.data.repository

import com.example.familyscheduler.data.local.dao.TemplateDao
import com.example.familyscheduler.data.mapper.TemplateMapper
import com.example.familyscheduler.domain.schedule.DailyTemplate
import com.example.familyscheduler.domain.schedule.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomTemplateRepository(
    private val dao: TemplateDao
) : TemplateRepository {

    override fun getAllFlow(): Flow<List<DailyTemplate>> {
        return dao.getAll().map { list ->
            list.map { TemplateMapper.toDomain(it) }
        }
    }

    override fun getTemplateById(id: String): Flow<DailyTemplate?> {
        return dao.getById(id).map { entity ->
            entity?.let { TemplateMapper.toDomain(it) }
        }
    }

    override suspend fun save(template: DailyTemplate) {
        dao.insert(TemplateMapper.toEntity(template))
    }

    override suspend fun delete(id: String) {
        dao.delete(id)
    }
}