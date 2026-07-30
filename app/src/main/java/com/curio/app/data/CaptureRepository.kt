package com.curio.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Repository wrapping Room DAO for capture CRUD operations.
 *
 * Provides coroutine-friendly APIs and handles entity ↔ domain model conversion.
 */
class CaptureRepository(private val dao: CaptureDao) {

    /** Observe all captures as [CurioEntry] flow for reactive UI updates. */
    fun observeAll(): Flow<List<CurioEntry>> =
        dao.getAllFlow().map { entities -> entities.map { it.toEntry() } }

    /** Get all captures (one-shot). */
    suspend fun getAll(): List<CurioEntry> =
        dao.getAll().map { it.toEntry() }

    /** Get captures filtered by category. */
    suspend fun getByCategory(categoryId: CategoryId): List<CurioEntry> =
        dao.getByCategory(categoryId.name).map { it.toEntry() }

    /** Save a new capture. Returns the generated entry ID. */
    suspend fun save(entry: CurioEntry) {
        dao.insert(entry.toEntity())
    }

    /** Get a single capture by ID. */
    suspend fun getById(id: String): CurioEntry? =
        dao.getById(id)?.toEntry()

    /** Delete a capture by ID. */
    suspend fun deleteById(id: String) {
        dao.deleteById(id)
    }

    /** Count total captures. */
    suspend fun count(): Int = dao.count()

    companion object {
        fun createId(): String = UUID.randomUUID().toString()
    }
}
