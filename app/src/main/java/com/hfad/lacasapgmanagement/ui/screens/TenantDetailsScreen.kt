package com.hfad.lacasapgmanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hfad.lacasapgmanagement.data.Payment
import com.hfad.lacasapgmanagement.data.Tenant
import com.hfad.lacasapgmanagement.ui.viewmodel.TenantViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantDetailsScreen(
    tenantId: Int,
    viewModel: TenantViewModel,
    onNavigateBack: () -> Unit
) {
    val tenant by viewModel.getTenantById(tenantId).collectAsState(initial = null)
    val payments by viewModel.getPayments(tenantId).collectAsState(initial = emptyList())
    var showAddPaymentDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tenant?.name ?: "Tenant Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            tenant?.let {
                TenantInfoCard(it)
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Payment History",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn {
                    items(payments) { payment ->
                        PaymentItem(payment)
                    }
                }
            }
        }
    }

    if (showAddPaymentDialog) {
        AddPaymentDialog(
            tenantId = tenantId,
            onDismiss = { showAddPaymentDialog = false },
            onConfirm = { amount, month, type ->
                tenant?.let {
                    viewModel.addPayment(
                        Payment(
                            tenantId = tenantId,
                            amount = amount,
                            date = System.currentTimeMillis(),
                            month = month,
                            paymentType = type,
                            tenantPhone = it.phoneNumber,
                            status = "Verified"
                        )
                    )
                }
                showAddPaymentDialog = false
            }
        )
    }
}

@Composable
fun TenantInfoCard(tenant: Tenant) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Room: ${tenant.roomNumber}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Phone: ${tenant.phoneNumber}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Rent: ₹${tenant.rentAmount}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Deposit: ₹${tenant.depositAmount}", style = MaterialTheme.typography.bodyLarge)
            if (!tenant.guardianName.isNullOrBlank()) {
                Text(text = "Guardian: ${tenant.guardianName}", style = MaterialTheme.typography.bodyLarge)
            }
            if (!tenant.guardianPhoneNumber.isNullOrBlank()) {
                Text(text = "Guardian Phone: ${tenant.guardianPhoneNumber}", style = MaterialTheme.typography.bodyLarge)
            }
            if (!tenant.address.isNullOrBlank()) {
                Text(text = "Address: ${tenant.address}", style = MaterialTheme.typography.bodyLarge)
            }
            if (tenant.isAadhaarVerified) {
                Text(text = "Aadhaar: ${tenant.aadhaarNumber} (Verified)", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            } else if (!tenant.aadhaarNumber.isNullOrBlank()) {
                Text(text = "Aadhaar: ${tenant.aadhaarNumber} (Not Verified)", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun PaymentItem(payment: Payment) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = payment.month, style = MaterialTheme.typography.titleMedium)
                Text(text = dateFormat.format(Date(payment.date)), style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Text(text = "₹${payment.amount}", style = MaterialTheme.typography.titleMedium)
                Text(text = payment.paymentType, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun AddPaymentDialog(
    tenantId: Int,
    onDismiss: () -> Unit,
    onConfirm: (Double, String, String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var paymentType by remember { mutableStateOf("Rent") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Payment") },
        text = {
            Column {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = month,
                    onValueChange = { month = it },
                    label = { Text("Month (e.g. Oct 2024)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.padding(top = 8.dp)) {
                    FilterChip(
                        selected = paymentType == "Rent",
                        onClick = { paymentType = "Rent" },
                        label = { Text("Rent") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = paymentType == "Deposit",
                        onClick = { paymentType = "Deposit" },
                        label = { Text("Deposit") }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val amountDouble = amount.toDoubleOrNull() ?: 0.0
                onConfirm(amountDouble, month, paymentType)
            }) {
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
