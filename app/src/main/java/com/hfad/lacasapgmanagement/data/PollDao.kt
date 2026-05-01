package com.hfad.lacasapgmanagement.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PollDao {
    @Query("SELECT * FROM polls ORDER BY id DESC")
    fun getAllPolls(): Flow<List<Poll>>

    @Query("SELECT * FROM polls WHERE isActive = 1 ORDER BY id DESC LIMIT 1")
    fun getActivePoll(): Flow<Poll?>

    @Query("SELECT * FROM polls WHERE date = :date LIMIT 1")
    suspend fun getPollByDate(date: String): Poll?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoll(poll: Poll)

    @Update
    suspend fun updatePoll(poll: Poll)

    @Query("UPDATE polls SET isActive = 0")
    suspend fun deactivateAllPolls()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVote(vote: PollVote)

    @Query("SELECT * FROM poll_votes WHERE pollId = :pollId")
    fun getVotesForPoll(pollId: Int): Flow<List<PollVote>>

    @Query("SELECT * FROM poll_votes WHERE pollId = :pollId AND tenantId = :tenantId LIMIT 1")
    suspend fun getVoteForTenant(pollId: Int, tenantId: Int): PollVote?
}
