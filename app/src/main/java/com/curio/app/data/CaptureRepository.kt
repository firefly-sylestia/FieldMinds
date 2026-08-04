package com.curio.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import java.util.UUID

/**
 * Repository wrapping Room DAO for capture CRUD operations.
 *
 * Provides coroutine-friendly APIs and handles entity ↔ domain model conversion.
 */
class CaptureRepository(private val dao: CaptureDao) {

    /** Observe all captures as [CurioEntry] flow for reactive UI updates. */
    fun observeAll(): Flow<List<CurioEntry>> =
        // Entity → domain conversion includes Gson decoding and topic lookup;
        // keep that work off the Compose/main collector so opening Cabinet
        // stays responsive even with a large restored FieldMind archive.
        dao.getAllFlow()
            .map { entities -> entities.map { it.toEntry() } }
            .flowOn(Dispatchers.Default)

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

    /** Delete several captures in one Room transaction-friendly batch. */
    suspend fun deleteByIds(ids: Collection<String>) {
        if (ids.isNotEmpty()) dao.deleteByIds(ids.toList())
    }

    /** Count total captures. */
    suspend fun count(): Int = dao.count()

    /** Wipe every capture (restore-from-backup). Returns deleted count. */
    suspend fun clearAll(): Int = dao.clearAll()

    companion object {
        fun createId(): String = UUID.randomUUID().toString()
    }
}
