package com.hfad.lacasapgmanagement.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "branches")
data class Branch(
    @PrimaryKey(autoGenerate = true)
    @SerialName("id")
    val id: Int = 0,
    @SerialName("name")
    val name: String
)
