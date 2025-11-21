package com.shakti.ai.data

import android.content.Context
import androidx.room.*
import java.util.*

/**
 * Evidence Item - Represents a single piece of evidence (video, audio, photo)
 */
@Entity(tableName = "evidence")
data class EvidenceItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val incidentId: String,
    val type: String, // "video_front", "video_back", "audio", "photo"
    val filePath: String,
    val timestamp: Long,
    val duration: Long = 0, // in milliseconds
    val fileSize: Long = 0, // in bytes
    val thumbnailPath: String? = null
)

/**
 * Incident Record - Main incident with metadata
 */
@Entity(tableName = "incidents")
data class IncidentRecord(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val startTime: Long,
    val endTime: Long = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String? = null,
    val triggerType: String, // "voice_command", "manual_sos", "ai_detection"
    val confidence: Float = 0.0f,
    val notes: String? = null,
    val isShared: Boolean = false,
    val sharedTimestamp: Long = 0
)

/**
 * DAO for Evidence
 */
@Dao
interface EvidenceDao {
    @Query("SELECT * FROM evidence WHERE incidentId = :incidentId ORDER BY timestamp DESC")
    suspend fun getEvidenceForIncident(incidentId: String): List<EvidenceItem>

    @Query("SELECT * FROM evidence ORDER BY timestamp DESC")
    suspend fun getAllEvidence(): List<EvidenceItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvidence(evidence: EvidenceItem)

    @Delete
    suspend fun deleteEvidence(evidence: EvidenceItem)

    @Query("DELETE FROM evidence WHERE incidentId = :incidentId")
    suspend fun deleteEvidenceForIncident(incidentId: String)
}

/**
 * DAO for Incidents
 */
@Dao
interface IncidentDao {
    @Query("SELECT * FROM incidents ORDER BY startTime DESC")
    suspend fun getAllIncidents(): List<IncidentRecord>

    @Query("SELECT * FROM incidents WHERE id = :id")
    suspend fun getIncidentById(id: String): IncidentRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: IncidentRecord)

    @Update
    suspend fun updateIncident(incident: IncidentRecord)

    @Delete
    suspend fun deleteIncident(incident: IncidentRecord)

    @Query("SELECT COUNT(*) FROM incidents")
    suspend fun getIncidentCount(): Int
}

/**
 * Room Database
 */
@Database(
    entities = [EvidenceItem::class, IncidentRecord::class],
    version = 1,
    exportSchema = false
)
abstract class EvidenceDatabase : RoomDatabase() {
    abstract fun evidenceDao(): EvidenceDao
    abstract fun incidentDao(): IncidentDao

    companion object {
        @Volatile
        private var INSTANCE: EvidenceDatabase? = null

        fun getDatabase(context: Context): EvidenceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EvidenceDatabase::class.java,
                    "shakti_evidence_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
