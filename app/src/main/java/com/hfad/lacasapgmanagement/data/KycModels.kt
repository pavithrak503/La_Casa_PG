package com.hfad.lacasapgmanagement.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AadhaarOtpRequest(
    @SerialName("aadhaar_number")
    val aadhaarNumber: String
)

@Serializable
data class AadhaarOtpResponse(
    @SerialName("txn_id")
    val txnId: String? = null,
    @SerialName("message")
    val message: String? = null,
    @SerialName("status")
    val status: String = "error",
    @SerialName("code")
    val code: String? = null
)

@Serializable
data class AadhaarVerifyRequest(
    @SerialName("txn_id")
    val txnId: String,
    @SerialName("otp")
    val otp: String
)

@Serializable
data class AadhaarVerifyResponse(
    @SerialName("status")
    val status: String,
    @SerialName("message")
    val message: String? = null,
    @SerialName("full_name")
    val fullName: String? = null,
    @SerialName("code")
    val code: String? = null
)
