package com.hfad.lacasapgmanagement.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "beds")
data class Bed(
    @PrimaryKey(autoGenerate = true)
    @SerialName("id")
    val id: Int = 0,
    @SerialName("room_number")
    val roomNumber: String,
    @SerialName("bed_number")
    val bedNumber: String,
    @SerialName("is_occupied")
    val isOccupied: Boolean = false,
    @SerialName("tenant_name")
    val tenantName: String? = null,
    @SerialName("branch")
    val branch: String = "Main Branch"
)
