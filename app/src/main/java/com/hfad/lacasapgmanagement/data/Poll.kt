package com.hfad.lacasapgmanagement.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "polls")
data class Poll(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val date: String, // yyyy-MM-dd
    val isActive: Boolean = true
)

@Entity(tableName = "poll_votes")
data class PollVote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pollId: Int,
    val tenantId: Int,
    val breakfast: Boolean = false,
    val lunch: Boolean = false,
    val dinner: Boolean = false,
    val isVeg: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
