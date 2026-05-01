package com.hfad.lacasapgmanagement.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PollConfigurationDao {
    @Query("SELECT * FROM poll_configuration WHERE id = 1")
    fun getConfiguration(): Flow<PollConfiguration?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfiguration(config: PollConfiguration)
}
