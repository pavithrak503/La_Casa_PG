package com.hfad.lacasapgmanagement.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "complaints")
data class Complaint(
    @PrimaryKey(autoGenerate = true)
    @SerialName("id")
    val id: Int = 0,
    @SerialName("tenant_id")
    val tenantId: Int = 0,
    @SerialName("tenant_name")
    val tenantName: String = "",
    @SerialName("tenant_phone")
    val tenantPhone: String = "",
    @SerialName("description")
    val description: String = "",
    @SerialName("status")
    val status: String = "Pending", // Pending, Resolved
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis()
)
