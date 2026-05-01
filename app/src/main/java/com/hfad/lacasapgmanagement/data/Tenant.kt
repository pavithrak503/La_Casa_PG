package com.hfad.lacasapgmanagement.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
@Entity(
    tableName = "tenants",
    indices = [androidx.room.Index(value = ["phoneNumber"], unique = true)]
)
data class Tenant(
    @PrimaryKey(autoGenerate = true)
    @SerialName("id")
    val id: Int = 0,
    val name: String = "",
    @SerialName("phone_number")
    val phoneNumber: String = "",
    @SerialName("room_number")
    val roomNumber: String = "",
    @SerialName("rent_amount")
    val rentAmount: Double = 0.0,
    @SerialName("deposit_amount")
    val depositAmount: Double = 0.0,
    @SerialName("joining_date")
    val joiningDate: Long = 0,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("branch")
    val branch: String = "Main Branch",
    @SerialName("password")
    val password: String = "1234",
    @SerialName("is_aadhaar_verified")
    val isAadhaarVerified: Boolean = false,
    @SerialName("aadhaar_number")
    val aadhaarNumber: String? = null
)
