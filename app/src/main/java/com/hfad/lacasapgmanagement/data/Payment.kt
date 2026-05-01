package com.hfad.lacasapgmanagement.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = Tenant::class,
            parentColumns = ["id"],
            childColumns = ["tenantId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Payment(
    @PrimaryKey(autoGenerate = true)
    @SerialName("id")
    val id: Int = 0,
    @SerialName("tenant_id")
    val tenantId: Int = 0,
    @SerialName("amount")
    val amount: Double = 0.0,
    @SerialName("date")
    val date: Long = 0,
    @SerialName("month")
    val month: String = "",
    @SerialName("payment_type")
    val paymentType: String = "",
    @SerialName("tenant_phone")
    val tenantPhone: String = "", // Added to facilitate fetching for tenant app
    @SerialName("status")
    val status: String = "Pending", // "Pending", "Verified", "Rejected"
    @SerialName("receipt_url")
    val receiptUrl: String? = null,
    @SerialName("proof_image_url")
    val proofImageUrl: String? = null
)
