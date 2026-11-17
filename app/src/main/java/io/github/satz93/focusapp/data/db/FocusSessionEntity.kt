package io.github.satz93.focusapp.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

object SessionStatus {
    const val COMPLETED = "completed"
    const val ENDED_EARLY = "ended_early"
    /** Used the unlock challenge during the session (milestone 6). */
    const val BROKEN = "broken"
}

@Entity(tableName = "focus_sessions", indices = [Index("day")])
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Local date the session started on, ISO yyyy-MM-dd (sorts correctly as text). */
    val day: String,
    val startedAt: Long,
    val endedAt: Long,
    val plannedMin: Int,
    /** Focused time, excluding pauses. */
    val actualMs: Long,
    val status: String,
    val pickupsDuring: Int,
) {
    val counts: Boolean get() = status == SessionStatus.COMPLETED
}
