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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

    fun createPoll(title: String) {
        viewModelScope.launch {
            val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            repository.createPoll(Poll(title = title, date = date))
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
