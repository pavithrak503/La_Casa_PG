package com.hfad.lacasapgmanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hfad.lacasapgmanagement.data.Payment
import com.hfad.lacasapgmanagement.ui.viewmodel.TenantViewModel
import java.util.*

@Composable
fun PaymentListScreen(viewModel: TenantViewModel) {
    val payments by viewModel.allPayments.collectAsState()
    val allTenants by viewModel.allTenants.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "All Payments",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (payments.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No payments found.")
            }
        } else {
            LazyColumn {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentDialog(
    tenants: List<com.hfad.lacasapgmanagement.data.Tenant>,
    onDismiss: () -> Unit,
    onConfirm: (Int, String, Double, String, String) -> Unit
) {
    var selectedTenant by remember { mutableStateOf<com.hfad.lacasapgmanagement.data.Tenant?>(null) }
    var amount by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var paymentType by remember { mutableStateOf("Rent") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Payment") },
        text = {
            Column {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedTenant?.name ?: "Select Tenant",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tenant") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        tenants.forEach { tenant ->
                            DropdownMenuItem(
                                text = { Text(tenant.name) },
                                onClick = {
                                    selectedTenant = tenant
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
                OutlinedTextField(
                    value = month,
                    onValueChange = { month = it },
                    label = { Text("Month (e.g. October 2023)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = paymentType,
                    onValueChange = { paymentType = it },
                    label = { Text("Payment Type (Rent/Deposit/Other)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    selectedTenant?.let {
                        onConfirm(it.id, it.phoneNumber, amt, month, paymentType)
                    }
                },
                enabled = selectedTenant != null && amount.isNotBlank() && month.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun AdminPaymentItem(
    payment: com.hfad.lacasapgmanagement.data.Payment, 
    tenantName: String,
    onStatusChange: (String) -> Unit = {}
) {
    val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = tenantName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "${payment.month} (${payment.paymentType})",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = dateFormat.format(java.util.Date(payment.date)),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                    Text(
                        text = "₹${payment.amount}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = payment.status,
                        style = MaterialTheme.typography.labelMedium,
                        color = when (payment.status) {
                            "Verified" -> MaterialTheme.colorScheme.primary
                            "Rejected" -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.secondary
                        }
                    )
                }
            }

            if (payment.status == "Pending") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { onStatusChange("Rejected") }) {
                        Text("Reject", color = MaterialTheme.colorScheme.error)
                    }
                    Button(onClick = { onStatusChange("Verified") }) {
                        Text("Verify")
                    }
                }
            }
        }
    }
}
