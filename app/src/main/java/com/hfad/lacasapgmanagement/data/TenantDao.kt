package com.hfad.lacasapgmanagement.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TenantDao {
    @Query("SELECT * FROM tenants WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveTenants(): Flow<List<Tenant>>

    @Query("SELECT * FROM tenants WHERE id = :id")
    fun getTenantById(id: Int): Flow<Tenant?>

    @Query("SELECT * FROM tenants WHERE phoneNumber = :phone LIMIT 1")
    suspend fun getTenantByPhone(phone: String): Tenant?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTenant(tenant: Tenant)

    @Update
    suspend fun updateTenant(tenant: Tenant)

    @Delete
    suspend fun deleteTenant(tenant: Tenant)
}
