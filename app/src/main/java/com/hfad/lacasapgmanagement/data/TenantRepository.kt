package com.hfad.lacasapgmanagement.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.flow.firstOrNull

@Serializable
data class TenantSupabaseDto(
    @SerialName("id")
    val id: Int? = null,
    @SerialName("name")
    val name: String,
    @SerialName("phone_number")
    val phoneNumber: String,
    @SerialName("room_number")
    val roomNumber: String,
    @SerialName("rent_amount")
    val rentAmount: Double,
    @SerialName("deposit_amount")
    val depositAmount: Double,
    @SerialName("joining_date")
    val joiningDate: Long,
    @SerialName("is_active")
    val isActive: Boolean,
    @SerialName("branch")
    val branch: String = "Main Branch",
    @SerialName("password")
    val password: String = "1234"
)

@Serializable
data class PaymentSupabaseDto(
    @SerialName("tenant_id")
    val tenantId: Int,
    @SerialName("amount")
    val amount: Double,
    @SerialName("date")
    val date: Long,
    @SerialName("month")
    val month: String,
    @SerialName("payment_type")
    val paymentType: String,
    @SerialName("tenant_phone")
    val tenantPhone: String,
    @SerialName("status")
    val status: String
)

@Serializable
data class ComplaintSupabaseDto(
    @SerialName("tenant_id")
    val tenantId: Int,
    @SerialName("tenant_name")
    val tenantName: String,
    @SerialName("tenant_phone")
    val tenantPhone: String,
    @SerialName("description")
    val description: String,
    @SerialName("status")
    val status: String,
    @SerialName("created_at")
    val createdAt: Long
)

class TenantRepository(
    private val tenantDao: TenantDao,
    private val paymentDao: PaymentDao,
    private val complaintDao: ComplaintDao,
    private val bedDao: BedDao,
    private val branchDao: BranchDao,
    private val pollDao: PollDao,
    private val supabase: SupabaseClient
) {
    val allActiveTenants: Flow<List<Tenant>> = tenantDao.getAllActiveTenants()
    val allPayments: Flow<List<Payment>> = paymentDao.getAllPayments()
    val allComplaints: Flow<List<Complaint>> = complaintDao.getAllComplaints()
    val allBeds: Flow<List<Bed>> = bedDao.getAllBeds()
    val allBranches: Flow<List<Branch>> = branchDao.getAllBranches()
    val allPolls: Flow<List<Poll>> = pollDao.getAllPolls()
    val activePoll: Flow<Poll?> = pollDao.getActivePoll()

    fun getVotesForPoll(pollId: Int): Flow<List<PollVote>> = pollDao.getVotesForPoll(pollId)

    suspend fun getVoteForTenant(pollId: Int, tenantId: Int): PollVote? = pollDao.getVoteForTenant(pollId, tenantId)

    suspend fun createPoll(poll: Poll) {
        pollDao.deactivateAllPolls()
        pollDao.insertPoll(poll)
        // In a real app, you would also sync to Supabase and send push notifications
    }

    suspend fun submitVote(vote: PollVote) {
        pollDao.insertVote(vote)
        // Sync to Supabase
    }

    fun getTenantById(tenantId: Int): Flow<Tenant?> = tenantDao.getTenantById(tenantId)

    suspend fun getLocalTenantByPhone(phone: String): Tenant? = tenantDao.getTenantByPhone(phone)

    suspend fun insertTenant(tenant: Tenant) {
        val trimmedPhone = tenant.phoneNumber.trim()
        val tenantWithTrimmedPhone = tenant.copy(phoneNumber = trimmedPhone)
        
        // 1. Save locally to Room
        tenantDao.insertTenant(tenantWithTrimmedPhone)
        
        try {
            // 2. Prepare DTO for Supabase
            val supabaseDto = TenantSupabaseDto(
                name = tenant.name,
                phoneNumber = trimmedPhone,
                roomNumber = tenant.roomNumber,
                rentAmount = tenant.rentAmount,
                depositAmount = tenant.depositAmount,
                joiningDate = tenant.joiningDate,
                isActive = tenant.isActive,
                branch = tenant.branch,
                password = tenant.password
            )
            
            println("Supabase: Sending DTO: $supabaseDto")
            
            supabase.from("tenants").upsert<TenantSupabaseDto>(supabaseDto) {
                onConflict = "phone_number"
            }
            println("Supabase: Insert successful for ${tenant.name}")
        } catch (e: Exception) {
            println("Supabase Error: ${e.message}")
            e.printStackTrace()
        }
    }

    suspend fun updateTenant(tenant: Tenant) {
        tenantDao.updateTenant(tenant)
        try {
            val supabaseDto = TenantSupabaseDto(
                name = tenant.name,
                phoneNumber = tenant.phoneNumber,
                roomNumber = tenant.roomNumber,
                rentAmount = tenant.rentAmount,
                depositAmount = tenant.depositAmount,
                joiningDate = tenant.joiningDate,
                isActive = tenant.isActive,
                branch = tenant.branch,
                password = tenant.password
            )
            println("Supabase: Updating DTO: $supabaseDto")
            supabase.from("tenants").update<TenantSupabaseDto>(supabaseDto) {
                filter {
                    eq("phone_number", tenant.phoneNumber)
                }
            }
            println("Supabase: Update successful for ${tenant.name}")
        } catch (e: Exception) {
            println("Supabase Update Error: ${e.message}")
            e.printStackTrace()
        }
    }

    suspend fun deleteTenant(tenant: Tenant) {
        tenantDao.deleteTenant(tenant)
        try {
            supabase.from("tenants").delete {
                filter {
                    eq("phone_number", tenant.phoneNumber)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun insertPayment(payment: Payment) {
        // 1. Save locally
        paymentDao.insertPayment(payment)
        
        try {
            // 2. Save to Supabase
            // Fetch the correct remote ID for the tenant using their phone number
            val remoteTenant = fetchTenantByPhone(payment.tenantPhone)
            
            if (remoteTenant != null) {
                val supabasePayment = PaymentSupabaseDto(
                    tenantId = remoteTenant.id,
                    amount = payment.amount,
                    date = payment.date,
                    month = payment.month,
                    paymentType = payment.paymentType,
                    tenantPhone = payment.tenantPhone,
                    status = payment.status
                )
                
                supabase.from("payments").insert(supabasePayment)
                println("Supabase: Payment insert successful for phone ${payment.tenantPhone}")
            } else {
                println("Supabase Error: Could not find tenant with phone ${payment.tenantPhone} on remote.")
            }
        } catch (e: Exception) {
            println("Supabase Payment Error: ${e.message}")
            e.printStackTrace()
        }
    }

    suspend fun updatePaymentStatus(paymentId: Int, status: String) {
        paymentDao.updatePaymentStatus(paymentId, status)
        try {
            supabase.from("payments").update(
                buildJsonObject {
                    put("status", status)
                }
            ) {
                filter {
                    eq("id", paymentId)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getPaymentsForTenant(tenantId: Int): Flow<List<Payment>> = paymentDao.getPaymentsForTenant(tenantId)

    fun getTotalPaidByTenant(tenantId: Int): Flow<Double?> = paymentDao.getTotalPaidByTenant(tenantId)

    suspend fun fetchAllTenantsFromSupabase() {
        try {
            val dtos = supabase.from("tenants").select().decodeList<TenantSupabaseDto>()
            dtos.forEach { dto ->
                val tenant = Tenant(
                    id = dto.id ?: 0,
                    name = dto.name,
                    phoneNumber = dto.phoneNumber,
                    roomNumber = dto.roomNumber,
                    rentAmount = dto.rentAmount,
                    depositAmount = dto.depositAmount,
                    joiningDate = dto.joiningDate,
                    isActive = dto.isActive,
                    branch = dto.branch,
                    password = dto.password
                )
                tenantDao.insertTenant(tenant)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun fetchTenantByPhone(phone: String): Tenant? {
        val trimmedPhone = phone.trim()
        println("Supabase: Fetching tenant with phone: '$trimmedPhone'")
        return try {
            val dto = supabase.from("tenants")
                .select {
                    filter {
                        eq("phone_number", trimmedPhone)
                    }
                }
                .decodeSingleOrNull<TenantSupabaseDto>()

            if (dto == null) {
                println("Supabase: No tenant found for phone '$trimmedPhone'")
            } else {
                println("Supabase: Found tenant: ${dto.name}")
            }

            dto?.let {
                Tenant(
                    id = it.id ?: 0,
                    name = it.name,
                    phoneNumber = it.phoneNumber,
                    roomNumber = it.roomNumber,
                    rentAmount = it.rentAmount,
                    depositAmount = it.depositAmount,
                    joiningDate = it.joiningDate,
                    isActive = it.isActive,
                    branch = it.branch,
                    password = it.password
                )
            }
        } catch (e: Exception) {
            println("Supabase Fetch Error: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun fetchPaymentsByPhone(phone: String): List<Payment> {
        val trimmedPhone = phone.trim()
        println("Supabase: Fetching payments for phone: '$trimmedPhone'")
        return try {
            val payments = supabase.from("payments")
                .select {
                    filter {
                        eq("tenant_phone", trimmedPhone)
                    }
                }
                .decodeList<Payment>()
            println("Supabase: Found ${payments.size} payments for phone '$trimmedPhone'")
            payments
        } catch (e: Exception) {
            println("Supabase Payments Fetch Error: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun insertComplaint(complaint: Complaint) {
        complaintDao.insertComplaint(complaint)
        try {
            val remoteTenant = fetchTenantByPhone(complaint.tenantPhone)
            if (remoteTenant != null) {
                val supabaseComplaint = ComplaintSupabaseDto(
                    tenantId = remoteTenant.id,
                    tenantName = complaint.tenantName,
                    tenantPhone = complaint.tenantPhone,
                    description = complaint.description,
                    status = complaint.status,
                    createdAt = complaint.createdAt
                )
                supabase.from("complaints").insert(supabaseComplaint)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getComplaintsByPhone(phone: String): Flow<List<Complaint>> = complaintDao.getComplaintsByPhone(phone)

    suspend fun fetchComplaintsByPhone(phone: String): List<Complaint> {
        val trimmedPhone = phone.trim()
        return try {
            supabase.from("complaints")
                .select {
                    filter {
                        eq("tenant_phone", trimmedPhone)
                    }
                }
                .decodeList<Complaint>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateComplaint(complaint: Complaint) {
        complaintDao.updateComplaint(complaint)
        try {
            supabase.from("complaints").update<Complaint>(complaint) {
                filter {
                    eq("id", complaint.id)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun insertBed(bed: Bed) {
        bedDao.insertBed(bed)
        try {
            supabase.from("beds").insert<Bed>(bed)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateBed(bed: Bed) {
        bedDao.updateBed(bed)
        try {
            supabase.from("beds").update<Bed>(bed) {
                filter {
                    eq("id", bed.id)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteBed(id: Int) {
        bedDao.deleteBed(id)
        try {
            supabase.from("beds").delete {
                filter {
                    eq("id", id)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun insertBranch(branch: Branch) {
        branchDao.insertBranch(branch)
        try {
            supabase.from("branches").insert<Branch>(branch)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteBranch(branch: Branch) {
        branchDao.deleteBranch(branch)
        try {
            supabase.from("branches").delete {
                filter {
                    eq("id", branch.id)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun fetchAllBranchesFromSupabase() {
        try {
            val branches = supabase.from("branches").select().decodeList<Branch>()
            branches.forEach { branch ->
                branchDao.insertBranch(branch)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
