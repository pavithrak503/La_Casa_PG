package com.hfad.lacasapgmanagement.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hfad.lacasapgmanagement.data.TenantRepository
import com.hfad.lacasapgmanagement.data.Payment
import com.hfad.lacasapgmanagement.data.Tenant
import com.hfad.lacasapgmanagement.data.Complaint
import com.hfad.lacasapgmanagement.data.Bed
import com.hfad.lacasapgmanagement.data.Branch
import com.hfad.lacasapgmanagement.data.Poll
import com.hfad.lacasapgmanagement.data.PollVote
import com.hfad.lacasapgmanagement.data.FoodMenuItem
import com.hfad.lacasapgmanagement.data.ComplaintCategory
import com.hfad.lacasapgmanagement.data.PollConfiguration
import com.hfad.lacasapgmanagement.data.Announcement
import com.hfad.lacasapgmanagement.data.AadhaarVerifyResponse
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.launch

class TenantViewModel(private val repository: TenantRepository) : ViewModel() {

    init {
        refreshTenants()
    }

    val allTenants: StateFlow<List<Tenant>> = repository.allActiveTenants
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allPayments: StateFlow<List<Payment>> = repository.allPayments
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allComplaints: StateFlow<List<Complaint>> = repository.allComplaints
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allBeds: StateFlow<List<Bed>> = repository.allBeds
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allBranches: StateFlow<List<Branch>> = repository.allBranches
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activePoll: StateFlow<Poll?> = repository.activePoll
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val allComplaintCategories: StateFlow<List<ComplaintCategory>> = repository.allComplaintCategories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allMenuItems: StateFlow<List<FoodMenuItem>> = repository.allMenuItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val pollConfiguration: StateFlow<PollConfiguration?> = repository.pollConfiguration
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val allAnnouncements: StateFlow<List<Announcement>> = repository.allAnnouncements
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addAnnouncement(title: String, content: String, isUrgent: Boolean) {
        viewModelScope.launch {
            repository.insertAnnouncement(Announcement(title = title, content = content, isUrgent = isUrgent))
        }
    }

    fun deleteAnnouncement(announcement: Announcement) {
        viewModelScope.launch {
            repository.deleteAnnouncement(announcement)
        }
    }

    fun refreshAnnouncements() {
        viewModelScope.launch {
            repository.fetchAnnouncementsFromSupabase()
        }
    }

    fun savePollConfiguration(config: PollConfiguration) {
        viewModelScope.launch {
            repository.savePollConfiguration(config)
        }
    }

    fun checkAndCreateAutoPoll() {
        viewModelScope.launch {
            repository.checkAndCreateAutoPoll()
        }
    }

    fun addComplaintCategory(name: String) {
        viewModelScope.launch {
            repository.insertComplaintCategory(ComplaintCategory(name = name))
        }
    }

    fun deleteComplaintCategory(category: ComplaintCategory) {
        viewModelScope.launch {
            repository.deleteComplaintCategory(category)
        }
    }

    fun addMenuItem(name: String, category: String, imageUrl: String? = null) {
        viewModelScope.launch {
            repository.insertMenuItem(FoodMenuItem(name = name, category = category, imageUrl = imageUrl))
        }
    }

    fun deleteMenuItem(item: FoodMenuItem) {
        viewModelScope.launch {
            repository.deleteMenuItem(item)
        }
    }

    fun getVotesForPoll(pollId: Int): StateFlow<List<PollVote>> {
        return repository.getVotesForPoll(pollId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    suspend fun getVoteForTenant(pollId: Int, tenantId: Int): PollVote? {
        return repository.getVoteForTenant(pollId, tenantId)
    }

    fun createPoll(title: String, breakfastId: Int? = null, lunchId: Int? = null, dinnerId: Int? = null) {
        viewModelScope.launch {
            val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            repository.createPoll(Poll(
                title = title,
                date = date,
                breakfastDishId = breakfastId,
                lunchDishId = lunchId,
                dinnerDishId = dinnerId
            ))
        }
    }

    fun deactivatePoll(poll: Poll) {
        viewModelScope.launch {
            repository.deactivatePoll(poll)
        }
    }

    fun submitVote(vote: PollVote) {
        viewModelScope.launch {
            repository.submitVote(vote)
        }
    }

    fun refreshTenants() {
        viewModelScope.launch {
            repository.fetchAllTenantsFromSupabase()
            repository.fetchAllBranchesFromSupabase()
            repository.fetchAnnouncementsFromSupabase()
        }
    }


    fun getTenantById(tenantId: Int): Flow<Tenant?> {
        return repository.getTenantById(tenantId)
    }

    fun getPayments(tenantId: Int): StateFlow<List<Payment>> {
        return repository.getPaymentsForTenant(tenantId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun getTotalPaid(tenantId: Int): StateFlow<Double> {
        return repository.getTotalPaidByTenant(tenantId)
            .map { it ?: 0.0 }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0.0
            )
    }

    fun insertTenant(tenant: Tenant) {
        viewModelScope.launch {
            repository.insertTenant(tenant)
        }
    }

    fun updateTenant(tenant: Tenant) {
        viewModelScope.launch {
            repository.updateTenant(tenant)
        }
    }

    fun deleteTenant(tenant: Tenant) {
        viewModelScope.launch {
            repository.deleteTenant(tenant)
        }
    }

    fun addPayment(payment: Payment) {
        viewModelScope.launch {
            repository.insertPayment(payment)
        }
    }

    fun updatePaymentStatus(paymentId: Int, status: String) {
        viewModelScope.launch {
            repository.updatePaymentStatus(paymentId, status)
        }
    }

    fun addComplaint(complaint: Complaint) {
        viewModelScope.launch {
            repository.insertComplaint(complaint)
        }
    }

    fun updateComplaint(complaint: Complaint) {
        viewModelScope.launch {
            repository.updateComplaint(complaint)
        }
    }

    suspend fun fetchComplaintsByPhone(phone: String): List<Complaint> {
        return repository.fetchComplaintsByPhone(phone)
    }

    suspend fun fetchTenantByPhone(phone: String): Tenant? {
        val local = repository.getLocalTenantByPhone(phone)
        if (local != null) return local
        return repository.fetchTenantByPhone(phone)
    }

    suspend fun fetchPaymentsByPhone(phone: String): List<Payment> {
        return repository.fetchPaymentsByPhone(phone)
    }

    fun addBed(bed: Bed) {
        viewModelScope.launch {
            repository.insertBed(bed)
        }
    }

    fun updateBed(bed: Bed) {
        viewModelScope.launch {
            repository.updateBed(bed)
        }
    }

    fun deleteBed(id: Int) {
        viewModelScope.launch {
            repository.deleteBed(id)
        }
    }

    fun addBranch(branch: Branch) {
        viewModelScope.launch {
            repository.insertBranch(branch)
        }
    }

    fun deleteBranch(branch: Branch) {
        viewModelScope.launch {
            repository.deleteBranch(branch)
        }
    }

    suspend fun generateAadhaarOtp(aadhaarNumber: String): String? {
        return repository.generateAadhaarOtp(aadhaarNumber)
    }

    suspend fun verifyAadhaarOtp(otp: String, txnId: String): AadhaarVerifyResponse? {
        return repository.verifyAadhaarOtp(otp, txnId)
    }

    fun seedDummyData() {
        viewModelScope.launch {
            try {
                // 1. Seed Branches only if empty
                val currentBranches = repository.allBranches.firstOrNull() ?: emptyList()
                if (currentBranches.isEmpty()) {
                    val branches = listOf("Main Branch", "HSR Layout", "Koramangala")
                    branches.forEach { repository.insertBranch(Branch(name = it)) }
                }

                // 2. Seed Complaint Categories only if empty
                val currentCats = repository.allComplaintCategories.firstOrNull() ?: emptyList()
                if (currentCats.isEmpty()) {
                    val categories = listOf("Plumbing", "Electrical", "Cleaning", "Internet", "Food")
                    categories.forEach { repository.insertComplaintCategory(ComplaintCategory(name = it)) }
                }

                // 3. Seed Food Menu Items only if empty
                val currentMenu = repository.allMenuItems.firstOrNull() ?: emptyList()
                if (currentMenu.isEmpty()) {
                    val menuItems = listOf(
                        "Idli Sambar" to "Breakfast",
                        "Poha" to "Breakfast",
                        "Veg Thali" to "Lunch",
                        "Chicken Biryani" to "Lunch",
                        "Dal Rice" to "Dinner",
                        "Paratha" to "Dinner"
                    )
                    menuItems.forEach { (name, cat) ->
                        repository.insertMenuItem(FoodMenuItem(name = name, category = cat))
                    }
                }

                // 4. Seed Tenants only if empty
                val currentTenants = repository.allActiveTenants.firstOrNull() ?: emptyList()
                if (currentTenants.isEmpty()) {
                    val tenant1 = Tenant(name = "John Doe", phoneNumber = "9876543210", roomNumber = "101", rentAmount = 8000.0, depositAmount = 16000.0, joiningDate = System.currentTimeMillis(), isActive = true, branch = "Main Branch")
                    val tenant2 = Tenant(name = "Jane Smith", phoneNumber = "9123456789", roomNumber = "202", rentAmount = 7500.0, depositAmount = 15000.0, joiningDate = System.currentTimeMillis(), isActive = true, branch = "HSR Layout")

                    val id1 = repository.insertTenant(tenant1)
                    repository.insertTenant(tenant2)

                    // 5. Seed Beds (only if tenants were just added)
                    val beds = listOf(
                        Bed(roomNumber = "101", bedNumber = "A", isOccupied = true, tenantName = "John Doe", branch = "Main Branch"),
                        Bed(roomNumber = "101", bedNumber = "B", isOccupied = false, branch = "Main Branch"),
                        Bed(roomNumber = "202", bedNumber = "A", isOccupied = true, tenantName = "Jane Smith", branch = "HSR Layout")
                    )
                    beds.forEach { repository.insertBed(it) }

                    // 6. Seed Complaints
                    repository.insertComplaint(Complaint(
                        tenantId = id1,
                        tenantName = "John Doe",
                        tenantPhone = "9876543210",
                        description = "Tap is leaking in the bathroom.",
                        category = "Plumbing",
                        status = "Pending",
                        createdAt = System.currentTimeMillis()
                    ))

                    // 7. Seed Payments
                    repository.insertPayment(Payment(
                        tenantId = id1,
                        amount = 8000.0,
                        date = System.currentTimeMillis(),
                        month = "October 2024",
                        paymentType = "Rent",
                        tenantPhone = "9876543210",
                        status = "Verified"
                    ))
                }

                // 8. Seed Poll Configuration only if empty
                val currentConfig = repository.pollConfiguration.firstOrNull()
                if (currentConfig == null) {
                    repository.savePollConfiguration(PollConfiguration(
                        isAutomationEnabled = true,
                        defaultBreakfastId = 1,
                        defaultLunchId = 3,
                        defaultDinnerId = 5
                    ))
                }

                // 9. Trigger Auto Poll
                repository.checkAndCreateAutoPoll()
            } catch (e: Exception) {
                println("Seeding Error: ${e.message}")
            }
        }
    }
}

class TenantViewModelFactory(private val repository: TenantRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TenantViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TenantViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
