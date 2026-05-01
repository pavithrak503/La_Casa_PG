package com.hfad.lacasapgmanagement.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ComplaintCategoryDao {
    @Query("SELECT * FROM complaint_categories")
    fun getAllCategories(): Flow<List<ComplaintCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: ComplaintCategory)

    @Delete
    suspend fun deleteCategory(category: ComplaintCategory)
}
