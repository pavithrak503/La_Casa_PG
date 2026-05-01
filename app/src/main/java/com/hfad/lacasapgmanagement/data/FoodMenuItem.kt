package com.hfad.lacasapgmanagement.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "food_menu_items")
data class FoodMenuItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val category: String, // Breakfast, Lunch, Dinner, Snack
    val imageUrl: String? = null
)
