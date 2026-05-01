package com.hfad.lacasapgmanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.hfad.lacasapgmanagement.ui.viewmodel.TenantViewModel
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage

@Composable
fun PaymentListScreen(viewModel: TenantViewModel) {
    val payments by viewModel.allPayments.collectAsState()
    val allTenants by viewModel.allTenants.collectAsState()

    val totalCollected = payments.filter { it.status == "Verified" }.sumOf { it.amount }
    val pendingAmount = payments.filter { it.status == "Pending" }.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OverviewCard(
                label = "Collected",
                amount = "₹$totalCollected",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.weight(1f)
            )
            OverviewCard(
                label = "Pending",
                amount = "₹$pendingAmount",
                containerColor = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.weight(1f)
            )
        }

        if (payments.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No payments found.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(payments) { payment ->
                    val tenant = allTenants.find { it.id == payment.tenantId }
                    AdminPaymentItem(
                        payment = payment,
                        tenantName = tenant?.name ?: "Unknown Tenant",
                        onStatusChange = { newStatus ->
                            viewModel.updatePaymentStatus(payment.id, newStatus)
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun OverviewCard(label: String, amount: String, containerColor: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(amount, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        }
    }
}

@Composable
fun AdminPaymentItem(
    payment: com.hfad.lacasapgmanagement.data.Payment, 
    tenantName: String,
    onStatusChange: (String) -> Unit = {}
) {
    val dateFormat = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
    val context = LocalContext.current
    var showProofDialog by remember { mutableStateOf(false) }

    if (showProofDialog && payment.proofImageUrl != null) {
        AlertDialog(
            onDismissRequest = { showProofDialog = false },
            title = { Text("Payment Proof", style = MaterialTheme.typography.titleMedium) },
            text = {
                Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                    AsyncImage(
                        model = payment.proofImageUrl,
                        contentDescription = "Payment Proof",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showProofDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = tenantName, style = MaterialTheme.typography.titleSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text(
                        text = "${payment.month} • ${payment.paymentType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${payment.amount.toInt()}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                    )
                    Surface(
                        color = when (payment.status) {
                            "Verified" -> MaterialTheme.colorScheme.primaryContainer
                            "Rejected" -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.secondaryContainer
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = payment.status.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            color = when (payment.status) {
                                "Verified" -> MaterialTheme.colorScheme.onPrimaryContainer
                                "Rejected" -> MaterialTheme.colorScheme.onErrorContainer
                                else -> MaterialTheme.colorScheme.onSecondaryContainer
                            }
                        )
                    }
                }
            }

            if (payment.status == "Pending") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (payment.proofImageUrl != null) {
                        TextButton(
                            onClick = { showProofDialog = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("View Proof", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    TextButton(
                        onClick = { onStatusChange("Rejected") },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Reject", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                    }
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    FilledTonalButton(
                        onClick = { onStatusChange("Verified") },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Verify", style = MaterialTheme.typography.labelMedium)
                    }
                }
            } else if (payment.proofImageUrl != null) {
                TextButton(
                    onClick = { showProofDialog = true },
                    contentPadding = PaddingValues(horizontal = 0.dp),
                    modifier = Modifier.height(24.dp).padding(top = 4.dp)
                ) {
                    Text("View Proof", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
