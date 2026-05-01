package com.hfad.lacasapgmanagement.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "poll_configuration")
data class PollConfiguration(
    @PrimaryKey val id: Int = 1, // Only one config exists
    val isAutomationEnabled: Boolean = false,
    val defaultBreakfastId: Int? = null,
    val defaultLunchId: Int? = null,
    val defaultDinnerId: Int? = null
)
