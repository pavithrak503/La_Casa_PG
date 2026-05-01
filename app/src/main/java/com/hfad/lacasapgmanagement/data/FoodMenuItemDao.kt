package com.hfad.lacasapgmanagement.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodMenuItemDao {
    @Query("SELECT * FROM food_menu_items ORDER BY category ASC")
    fun getAllMenuItems(): Flow<List<FoodMenuItem>>

    @Query("SELECT * FROM food_menu_items WHERE category = :category")
    fun getMenuItemsByCategory(category: String): Flow<List<FoodMenuItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItem(item: FoodMenuItem)

    @Delete
    suspend fun deleteMenuItem(item: FoodMenuItem)
}
