package com.curio.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Room entity for saved captures.
 *
 * Stores topic metadata + format + timestamp as columns, and the
 * format-specific data as a JSON blob. This single-table design
 * lets us add new formats without migrations.
 */
@Entity(tableName = "captures")
@TypeConverters(CaptureConverters::class)
data class CaptureEntity(
    @PrimaryKey val id: String,
    val topicId: String,
    val categoryId: String,
    val topicName: String,
    val topicSubtype: String,
    val topicTeaser: String,
    val format: String,          // CaptureFormat enum name
    val capturedAtMillis: Long,
    val title: String?,
    val formatDataJson: String    // JSON-serialized CaptureData
)

/**
 * Type converters for Room — handles enum-to-string and Gson serialization.
 */
class CaptureConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromCaptureFormat(format: CaptureFormat): String = format.name

    @TypeConverter
    fun toCaptureFormat(value: String): CaptureFormat = CaptureFormat.valueOf(value)

    @TypeConverter
    fun fromCaptureData(data: CaptureData): String = gson.toJson(data)

    @TypeConverter
    fun toCaptureData(json: String): CaptureData = deserializeCaptureData(json)

    companion object {
        /**
         * Shared deserializer for [CaptureData] from JSON.
         *
         * Uses field-based detection to determine the concrete subclass,
         * then delegates to Gson for the final deserialization.
         * OpenNotebook's nested [CaptureData.subData] is handled by
         * recursively calling this function on the sub-object.
         */
        fun deserializeCaptureData(json: String): CaptureData {
            val gson = Gson()
            @Suppress("UNCHECKED_CAST")
            val map = gson.fromJson(json, Map::class.java) as Map<String, Any?>
            return when (map["subFormat"]) {
                null -> {
                    when {
                        map.containsKey("durationSeconds") ->
                            gson.fromJson(json, CaptureData.SoundBite::class.java)
                        map.containsKey("rating") ->
                            gson.fromJson(json, CaptureData.ReelNotes::class.java)
                        map.containsKey("quotes") ->
                            gson.fromJson(json, CaptureData.Marginalia::class.java)
                        map.containsKey("caption") ->
                            gson.fromJson(json, CaptureData.GalleryWall::class.java)
                        map.containsKey("learnNext") ->
                            gson.fromJson(json, CaptureData.FieldNotes::class.java)
                        else -> throw IllegalArgumentException("Unknown CaptureData type: $json")
                    }
                }
                else -> {
                    // OpenNotebook: manually reconstruct to handle nested CaptureData
                    val subFormat = CaptureFormat.valueOf(map["subFormat"] as String)
                    @Suppress("UNCHECKED_CAST")
                    val subDataMap = map["subData"] as Map<String, Any?>
                    val subDataJson = gson.toJson(subDataMap)
                    val subData = deserializeCaptureData(subDataJson)
                    CaptureData.OpenNotebook(subFormat, subData)
                }
            }
        }
    }
}

/**
 * Convert [CurioEntry] to [CaptureEntity] for Room storage.
 */
fun CurioEntry.toEntity(): CaptureEntity = CaptureEntity(
    id = id,
    topicId = topic.id,
    categoryId = topic.categoryId.name,
    topicName = topic.name,
    topicSubtype = topic.subtype,
    topicTeaser = topic.teaser,
    format = format.name,
    capturedAtMillis = capturedAtMillis,
    title = title,
    formatDataJson = Gson().toJson(captureData)
)

/**
 * Reconstruct [CurioEntry] from a [CaptureEntity] stored in Room.
 * 
 * Fixed to properly handle movie entries and other formats by ensuring
 * the topic is fully reconstructed with all available data, including
 * fallback exploreAction and tags when the cached topic isn't found.
 */
fun CaptureEntity.toEntry(): CurioEntry {
    val categoryId = CategoryId.valueOf(this.categoryId)
    
    // Try to find the full topic data from cache
    val cachedTopic = TopicJsonLoader.cached(categoryId)?.find { 
        it.id == topicId || it.name == topicName 
    }
    
    // Use cached topic if available, otherwise create a complete fallback
    val topic = cachedTopic ?: CurioTopic(
        id = topicId,
        categoryId = categoryId,
        subtype = topicSubtype,
        name = topicName,
        teaser = topicTeaser,
        imageUrl = "",
        exploreAction = ExploreAction(
            verb = when (categoryId) {
                CategoryId.FILMS, CategoryId.DIRECTORS -> "Watch"
                CategoryId.ALBUMS, CategoryId.ARTISTS -> "Listen"
                CategoryId.BOOKS, CategoryId.AUTHORS -> "Read"
                CategoryId.PAINTERS, CategoryId.ARTWORKS -> "View"
                CategoryId.SCIENTISTS, CategoryId.DISCOVERIES -> "Explore"
                CategoryId.WILDCARD -> "Discover"
            },
            targetName = topicName,
            durationMinutes = 30,
            instruction = "Revisit this saved topic: $topicName"
        ),
        tags = emptyList(),
        tier = 1
    )
    
    val captureData = try {
        CaptureConverters.deserializeCaptureData(formatDataJson)
    } catch (e: Exception) {
        // Fallback for malformed data - create empty ReelNotes for movies
        when (CaptureFormat.valueOf(format)) {
            CaptureFormat.ReelNotes -> CaptureData.ReelNotes(0, "", 0)
            CaptureFormat.SoundBite -> CaptureData.SoundBite(0, "")
            CaptureFormat.Marginalia -> CaptureData.Marginalia("", emptyList())
            CaptureFormat.GalleryWall -> CaptureData.GalleryWall(0, "")
            CaptureFormat.FieldNotes -> CaptureData.FieldNotes("", "", "")
            CaptureFormat.OpenNotebook -> CaptureData.OpenNotebook(
                CaptureFormat.ReelNotes,
                CaptureData.ReelNotes(0, "", 0)
            )
        }
    }
    
    return CurioEntry(
        id = id,
        topic = topic,
        format = CaptureFormat.valueOf(format),
        captureData = captureData,
        title = title,
        capturedAtMillis = capturedAtMillis
    )
}
