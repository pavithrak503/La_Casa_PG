package com.hfad.lacasapgmanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hfad.lacasapgmanagement.ui.viewmodel.TenantViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UpcomingDuesScreen(viewModel: TenantViewModel) {
    val tenants by viewModel.allTenants.collectAsState()
    val payments by viewModel.allPayments.collectAsState()
    
    val currentMonth = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date())
    
    val unpaidTenants = tenants.filter { tenant ->
        // Check if there is a verified payment for the current month
        val hasPaid = payments.any { payment ->
            payment.tenantId == tenant.id && 
            payment.month.contains(currentMonth.split(" ")[0], ignoreCase = true) &&
            payment.month.contains(currentMonth.split(" ")[1]) &&
            payment.status == "Verified"
        }
        !hasPaid && tenant.isActive
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        if (unpaidTenants.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No pending dues for $currentMonth", 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(unpaidTenants) { tenant ->
                    DueItem(tenant = tenant)
                }
            }
        }
    }
}

@Composable
fun DueItem(tenant: com.hfad.lacasapgmanagement.data.Tenant) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tenant.name, 
                    style = MaterialTheme.typography.titleSmall, 
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Room ${tenant.roomNumber}", 
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "₹${tenant.rentAmount.toInt()}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { /* Call functionality */ },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Phone, 
                        contentDescription = "Call", 
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
