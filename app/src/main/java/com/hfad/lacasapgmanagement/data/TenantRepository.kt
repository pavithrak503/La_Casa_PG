package com.hfad.lacasapgmanagement.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
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
    @SerialName("bed_number")
    val bedNumber: String = "",
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
    val password: String = "1234",
    @SerialName("guardian_name")
    val guardianName: String? = null,
    @SerialName("guardian_phone_number")
    val guardianPhoneNumber: String? = null,
    @SerialName("address")
    val address: String? = null
)

@Serializable
data class TenantInsertDto(
    @SerialName("name")
    val name: String,
    @SerialName("phone_number")
    val phoneNumber: String,
    @SerialName("room_number")
    val roomNumber: String,
    @SerialName("bed_number")
    val bedNumber: String = "",
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
    val password: String = "1234",
    @SerialName("guardian_name")
    val guardianName: String? = null,
    @SerialName("guardian_phone_number")
    val guardianPhoneNumber: String? = null,
    @SerialName("address")
    val address: String? = null
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
    val status: String,
    @SerialName("proof_image_url")
    val proofImageUrl: String? = null
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
    @SerialName("category")
    val category: String = "General",
    @SerialName("status")
    val status: String,
    @SerialName("created_at")
    val createdAt: Long
)

@Serializable
data class BranchSupabaseDto(
    @SerialName("name")
    val name: String
)

@Serializable
data class BedInsertDto(
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

@Serializable
data class AnnouncementSupabaseDto(
    @SerialName("id")
    val id: Int? = null,
    @SerialName("title")
    val title: String,
    @SerialName("content")
    val content: String,
    @SerialName("timestamp")
    val timestamp: Long,
    @SerialName("is_urgent")
    val isUrgent: Boolean
)

class TenantRepository(
    private val tenantDao: TenantDao,
    private val paymentDao: PaymentDao,
    private val complaintDao: ComplaintDao,
    private val complaintCategoryDao: ComplaintCategoryDao,
    private val foodMenuItemDao: FoodMenuItemDao,
    private val bedDao: BedDao,
    private val branchDao: BranchDao,
    private val pollDao: PollDao,
    private val pollConfigurationDao: PollConfigurationDao,
    private val announcementDao: AnnouncementDao,
    private val supabase: SupabaseClient
) {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }

    // Replace with your actual Supabase Edge Function URL or KYC Provider URL
    private val KYC_BASE_URL = "https://zzfqqbswqfjwgoqkdked.supabase.co/functions/v1/aadhaar-kyc"

    suspend fun generateAadhaarOtp(aadhaarNumber: String): String? {
        return try {
            val response = httpClient.post(KYC_BASE_URL + "/send-otp") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer ${supabase.supabaseKey}")
                setBody(AadhaarOtpRequest(aadhaarNumber))
            }.body<AadhaarOtpResponse>()
            
            if (response.status == "success") response.txnId else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun verifyAadhaarOtp(otp: String, txnId: String): AadhaarVerifyResponse? {
        return try {
            httpClient.post(KYC_BASE_URL + "/verify-otp") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer ${supabase.supabaseKey}")
                setBody(AadhaarVerifyRequest(txnId, otp))
            }.body<AadhaarVerifyResponse>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    val allActiveTenants: Flow<List<Tenant>> = tenantDao.getAllActiveTenants()
    val allPayments: Flow<List<Payment>> = paymentDao.getAllPayments()
    val allComplaints: Flow<List<Complaint>> = complaintDao.getAllComplaints()
    val allBeds: Flow<List<Bed>> = bedDao.getAllBeds()
    val allBranches: Flow<List<Branch>> = branchDao.getAllBranches()
    val allComplaintCategories: Flow<List<ComplaintCategory>> = complaintCategoryDao.getAllCategories()
    val allMenuItems: Flow<List<FoodMenuItem>> = foodMenuItemDao.getAllMenuItems()
    val allPolls: Flow<List<Poll>> = pollDao.getAllPolls()
    val activePoll: Flow<Poll?> = pollDao.getActivePoll()
    val pollConfiguration: Flow<PollConfiguration?> = pollConfigurationDao.getConfiguration()
    val allAnnouncements: Flow<List<Announcement>> = announcementDao.getAllAnnouncements()

    fun getMenuItemsByCategory(category: String): Flow<List<FoodMenuItem>> = foodMenuItemDao.getMenuItemsByCategory(category)

    suspend fun insertAnnouncement(announcement: Announcement) {
        announcementDao.insertAnnouncement(announcement)
        try {
            val dto = AnnouncementSupabaseDto(
                title = announcement.title,
                content = announcement.content,
                timestamp = announcement.timestamp,
                isUrgent = announcement.isUrgent
            )
            supabase.from("announcements").insert(dto)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteAnnouncement(announcement: Announcement) {
        announcementDao.deleteAnnouncement(announcement)
        try {
            supabase.from("announcements").delete {
                filter {
                    eq("id", announcement.id)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun fetchAnnouncementsFromSupabase() {
        try {
            val dtos = supabase.from("announcements").select().decodeList<AnnouncementSupabaseDto>()
            dtos.forEach { dto ->
                val announcement = Announcement(
                    id = dto.id ?: 0,
                    title = dto.title,
                    content = dto.content,
                    timestamp = dto.timestamp,
                    isUrgent = dto.isUrgent
                )
                announcementDao.insertAnnouncement(announcement)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun insertMenuItem(item: FoodMenuItem) {
        foodMenuItemDao.insertMenuItem(item)
    }

    suspend fun deleteMenuItem(item: FoodMenuItem) {
        foodMenuItemDao.deleteMenuItem(item)
    }

    fun getVotesForPoll(pollId: Int): Flow<List<PollVote>> = pollDao.getVotesForPoll(pollId)

    suspend fun getVoteForTenant(pollId: Int, tenantId: Int): PollVote? = pollDao.getVoteForTenant(pollId, tenantId)

    suspend fun createPoll(poll: Poll) {
        pollDao.deactivateAllPolls()
        pollDao.insertPoll(poll)
        // In a real app, you would also sync to Supabase and send push notifications
    }

    suspend fun deactivatePoll(poll: Poll) {
        pollDao.updatePoll(poll.copy(isActive = false))
    }

    suspend fun submitVote(vote: PollVote) {
        pollDao.insertVote(vote)
        // Sync to Supabase
    }

    suspend fun savePollConfiguration(config: PollConfiguration) {
        pollConfigurationDao.insertConfiguration(config)
    }

    suspend fun checkAndCreateAutoPoll() {
        val config = pollConfiguration.firstOrNull() ?: return
        if (!config.isAutomationEnabled) return

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val today = sdf.format(java.util.Date())
        
        // Check if a poll already exists for today
        val pollForToday = pollDao.getPollByDate(today)

        if (pollForToday == null) {
            createPoll(Poll(
                title = "Food Attendance for $today",
                date = today,
                breakfastDishId = config.defaultBreakfastId,
                lunchDishId = config.defaultLunchId,
                dinnerDishId = config.defaultDinnerId
            ))
        }
    }

    fun getTenantById(tenantId: Int): Flow<Tenant?> = tenantDao.getTenantById(tenantId)

    suspend fun getLocalTenantByPhone(phone: String): Tenant? = tenantDao.getTenantByPhone(phone)

    suspend fun insertTenant(tenant: Tenant): Int {
        val trimmedPhone = tenant.phoneNumber.trim()
        val tenantWithTrimmedPhone = tenant.copy(phoneNumber = trimmedPhone)
        
        // 1. Save locally to Room
        val localId = tenantDao.insertTenant(tenantWithTrimmedPhone).toInt()
        
        try {
            // 2. Prepare DTO for Supabase
            val supabaseDto = TenantInsertDto(
                name = tenant.name,
                phoneNumber = trimmedPhone,
                roomNumber = tenant.roomNumber,
                bedNumber = tenant.bedNumber,
                rentAmount = tenant.rentAmount,
                depositAmount = tenant.depositAmount,
                joiningDate = tenant.joiningDate,
                isActive = tenant.isActive,
                branch = tenant.branch,
                password = tenant.password,
                guardianName = tenant.guardianName,
                guardianPhoneNumber = tenant.guardianPhoneNumber,
                address = tenant.address
            )
            
            println("Supabase: Sending DTO: $supabaseDto")
            
            supabase.from("tenants").upsert<TenantInsertDto>(supabaseDto) {
                onConflict = "phone_number"
            }
            println("Supabase: Insert successful for ${tenant.name}")
        } catch (e: Exception) {
            println("Supabase Error: ${e.message}")
            e.printStackTrace()
        }
        return localId
    }

    suspend fun updateTenant(tenant: Tenant) {
        tenantDao.updateTenant(tenant)
        try {
            val supabaseDto = TenantSupabaseDto(
                name = tenant.name,
                phoneNumber = tenant.phoneNumber,
                roomNumber = tenant.roomNumber,
                bedNumber = tenant.bedNumber,
                rentAmount = tenant.rentAmount,
                depositAmount = tenant.depositAmount,
                joiningDate = tenant.joiningDate,
                isActive = tenant.isActive,
                branch = tenant.branch,
                password = tenant.password,
                guardianName = tenant.guardianName,
                guardianPhoneNumber = tenant.guardianPhoneNumber,
                address = tenant.address
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
                    status = payment.status,
                    proofImageUrl = payment.proofImageUrl
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
                    bedNumber = dto.bedNumber,
                    rentAmount = dto.rentAmount,
                    depositAmount = dto.depositAmount,
                    joiningDate = dto.joiningDate,
                    isActive = dto.isActive,
                    branch = dto.branch,
                    password = dto.password,
                    guardianName = dto.guardianName,
                    guardianPhoneNumber = dto.guardianPhoneNumber,
                    address = dto.address
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
                    bedNumber = it.bedNumber,
                    rentAmount = it.rentAmount,
                    depositAmount = it.depositAmount,
                    joiningDate = it.joiningDate,
                    isActive = it.isActive,
                    branch = it.branch,
                    password = it.password,
                    guardianName = it.guardianName,
                    guardianPhoneNumber = it.guardianPhoneNumber,
                    address = it.address
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
                    category = complaint.category,
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
            supabase.from("complaints").update(
                buildJsonObject {
                    put("status", complaint.status)
                    put("category", complaint.category)
                    put("description", complaint.description)
                }
            ) {
                filter {
                    eq("id", complaint.id)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun insertComplaintCategory(category: ComplaintCategory) {
        complaintCategoryDao.insertCategory(category)
        try {
            val dto = buildJsonObject {
                put("name", category.name)
            }
            supabase.from("complaint_categories").upsert(dto) {
                onConflict = "name"
            }
        } catch (e: Exception) {
            println("Supabase Category Error: ${e.message}")
        }
    }

    suspend fun deleteComplaintCategory(category: ComplaintCategory) {
        complaintCategoryDao.deleteCategory(category)
    }

    suspend fun insertBed(bed: Bed) {
        bedDao.insertBed(bed)
        try {
            val dto = BedInsertDto(
                roomNumber = bed.roomNumber,
                bedNumber = bed.bedNumber,
                isOccupied = bed.isOccupied,
                tenantName = bed.tenantName,
                branch = bed.branch
            )
            supabase.from("beds").insert<BedInsertDto>(dto)
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
            val dto = BranchSupabaseDto(name = branch.name)
            supabase.from("branches").upsert(dto) {
                onConflict = "name"
            }
        } catch (e: Exception) {
            println("Supabase Branch Error: ${e.message}")
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
