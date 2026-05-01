package com.hfad.lacasapgmanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
                    DueItem(tenant = tenant, currentMonth = currentMonth)
                }
            }
        }
    }
}

@Composable
fun DueItem(tenant: com.hfad.lacasapgmanagement.data.Tenant, currentMonth: String) {
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
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${tenant.rentAmount.toInt()}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = tenant.phoneNumber,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                val context = androidx.compose.ui.platform.LocalContext.current
                IconButton(
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                            data = android.net.Uri.parse("tel:${tenant.phoneNumber}")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Phone, 
                        contentDescription = "Call", 
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = {
                        val message = "Hi ${tenant.name}, this is a reminder regarding your PG rent of ₹${tenant.rentAmount.toInt()} for $currentMonth. Please pay at your earliest convenience."
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                            data = android.net.Uri.parse("https://api.whatsapp.com/send?phone=91${tenant.phoneNumber}&text=${android.net.Uri.encode(message)}")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "WhatsApp", 
                        tint = androidx.compose.ui.graphics.Color(0xFF25D366),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
