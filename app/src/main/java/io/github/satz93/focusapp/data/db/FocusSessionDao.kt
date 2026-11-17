package io.github.satz93.focusapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {
    @Insert
    suspend fun insert(session: FocusSessionEntity): Long

    @Query("SELECT * FROM focus_sessions WHERE id = :id")
    suspend fun get(id: Long): FocusSessionEntity?

    @Query("SELECT * FROM focus_sessions WHERE day = :day ORDER BY startedAt")
    fun observeDay(day: String): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE day BETWEEN :fromDay AND :toDay ORDER BY startedAt")
    fun observeRange(fromDay: String, toDay: String): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE status = 'completed' ORDER BY endedAt DESC LIMIT 1")
    suspend fun latestCompleted(): FocusSessionEntity?

    @Query("SELECT COUNT(*) FROM focus_sessions WHERE day = :day AND status = 'completed'")
    suspend fun completedCount(day: String): Int
}
