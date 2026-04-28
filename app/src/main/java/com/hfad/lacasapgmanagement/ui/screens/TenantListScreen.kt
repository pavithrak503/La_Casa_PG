package com.hfad.lacasapgmanagement.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hfad.lacasapgmanagement.R
import com.hfad.lacasapgmanagement.data.Tenant
import com.hfad.lacasapgmanagement.data.Payment
import com.hfad.lacasapgmanagement.ui.viewmodel.TenantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantListScreen(
    viewModel: TenantViewModel,
    onAddTenantClick: () -> Unit,
    onEditTenantClick: (Int) -> Unit,
    onTenantClick: (Int) -> Unit,
    onLogoutClick: () -> Unit,
    isEmbedded: Boolean = false
) {
    val tenants by viewModel.allTenants.collectAsState()
    var selectedTenantForPayment by remember { mutableStateOf<Tenant?>(null) }

    if (isEmbedded) {
        TenantListContent(
            tenants = tenants,
            onTenantClick = onTenantClick,
            onEditTenant = onEditTenantClick,
            onDeleteTenant = { viewModel.deleteTenant(it) },
            onToggleActive = { tenant -> 
                viewModel.updateTenant(tenant.copy(isActive = !tenant.isActive))
            },
            onRecordPayment = { selectedTenantForPayment = it }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.White,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.la_casa_pg),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Tenants", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                    },
                    actions = {
                        IconButton(onClick = onLogoutClick) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = onAddTenantClick) {
                    Icon(Icons.Default.Add, contentDescription = "Add Tenant")
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                TenantListContent(
                    tenants = tenants,
                    onTenantClick = onTenantClick,
                    onEditTenant = onEditTenantClick,
                    onDeleteTenant = { viewModel.deleteTenant(it) },
                    onToggleActive = { tenant ->
                        viewModel.updateTenant(tenant.copy(isActive = !tenant.isActive))
                    },
                    onRecordPayment = { selectedTenantForPayment = it }
                )
            }
        }
    }

    if (selectedTenantForPayment != null) {
        AddPaymentDialog(
            tenant = selectedTenantForPayment!!,
            onDismiss = { selectedTenantForPayment = null },
            onConfirm = { amount, month, type ->
                viewModel.addPayment(
                    Payment(
                        tenantId = selectedTenantForPayment!!.id,
                        tenantPhone = selectedTenantForPayment!!.phoneNumber,
                        amount = amount,
                        month = month,
                        paymentType = type,
                        date = System.currentTimeMillis(),
                        status = "Verified"
                    )
                )
                selectedTenantForPayment = null
            }
        )
    }
}

@Composable
fun TenantListContent(
    tenants: List<Tenant>,
    onTenantClick: (Int) -> Unit,
    onEditTenant: (Int) -> Unit,
    onDeleteTenant: (Tenant) -> Unit,
    onToggleActive: (Tenant) -> Unit,
    onRecordPayment: (Tenant) -> Unit
) {
    if (tenants.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(8.dp))
                Text("No tenants found", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tenants, key = { it.id }) { tenant ->
                CompactTenantCard(
                    tenant = tenant,
                    onClick = { onTenantClick(tenant.id) },
                    onEdit = { onEditTenant(tenant.id) },
                    onDelete = { onDeleteTenant(tenant) },
                    onToggleActive = { onToggleActive(tenant) },
                    onRecordPayment = { onRecordPayment(tenant) }
                )
            }
        }
    }
}

@Composable
fun CompactTenantCard(
    tenant: Tenant,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit,
    onRecordPayment: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tenant.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Box {
                    IconButton(onClick = { expanded = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Record Payment") },
                            onClick = { onRecordPayment(); expanded = false },
                            leadingIcon = { Icon(Icons.Default.Payment, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = { onEdit(); expanded = false },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = { onDelete(); expanded = false },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                        )
                        DropdownMenuItem(
                            text = { Text(if (tenant.isActive) "Mark Inactive" else "Mark Active") },
                            onClick = { onToggleActive(); expanded = false },
                            leadingIcon = { Icon(if (tenant.isActive) Icons.Default.Block else Icons.Default.CheckCircle, contentDescription = null) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = tenant.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Room ${tenant.roomNumber}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹${tenant.rentAmount.toInt()}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
                
                if (!tenant.isActive) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "INACTIVE",
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddPaymentDialog(
    tenant: Tenant,
    onDismiss: () -> Unit,
    onConfirm: (Double, String, String) -> Unit
) {
    var amount by remember { mutableStateOf(tenant.rentAmount.toString()) }
    var month by remember { mutableStateOf("") }
    var paymentType by remember { mutableStateOf("Rent") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Payment for ${tenant.name}") },
        text = {
            Column {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
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
            Button(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull() ?: 0.0
                    onConfirm(amountDouble, month, paymentType)
                },
                enabled = amount.isNotEmpty() && month.isNotEmpty()
            ) {
                Text("Record")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
