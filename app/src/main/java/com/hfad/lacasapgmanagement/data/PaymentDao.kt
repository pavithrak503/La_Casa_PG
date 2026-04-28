package com.hfad.lacasapgmanagement.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE tenantId = :tenantId ORDER BY date DESC")
    fun getPaymentsForTenant(tenantId: Int): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment)
    
    @Query("SELECT SUM(amount) FROM payments WHERE tenantId = :tenantId")
    fun getTotalPaidByTenant(tenantId: Int): Flow<Double?>

    @Query("SELECT * FROM payments ORDER BY date DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("UPDATE payments SET status = :status WHERE id = :id")
    suspend fun updatePaymentStatus(id: Int, status: String)
}
