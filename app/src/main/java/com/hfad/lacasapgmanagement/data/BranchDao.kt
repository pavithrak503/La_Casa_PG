package com.hfad.lacasapgmanagement.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BranchDao {
    @Query("SELECT * FROM branches ORDER BY name ASC")
    fun getAllBranches(): Flow<List<Branch>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBranch(branch: Branch)

    @Update
    suspend fun updateBranch(branch: Branch)

    @Delete
    suspend fun deleteBranch(branch: Branch)
}
