package com.hfad.lacasapgmanagement.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BedDao {
    @Query("SELECT * FROM beds ORDER BY roomNumber ASC, bedNumber ASC")
    fun getAllBeds(): Flow<List<Bed>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBed(bed: Bed)

    @Update
    suspend fun updateBed(bed: Bed)

    @Query("DELETE FROM beds WHERE id = :id")
    suspend fun deleteBed(id: Int)
}
