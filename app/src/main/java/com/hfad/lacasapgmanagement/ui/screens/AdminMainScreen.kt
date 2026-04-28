package com.hfad.lacasapgmanagement.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.hfad.lacasapgmanagement.R
import com.hfad.lacasapgmanagement.ui.viewmodel.TenantViewModel

@Composable
fun AdminMainScreen(
    viewModel: TenantViewModel,
    onAddTenantClick: () -> Unit,
    onEditTenantClick: (Int) -> Unit,
    onTenantClick: (Int) -> Unit,
    onLogoutClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddPaymentDialog by remember { mutableStateOf(false) }
    var showAddComplaintDialog by remember { mutableStateOf(false) }
    var showAddBedDialog by remember { mutableStateOf(false) }
    
    val tenants by viewModel.allTenants.collectAsState()
    val branches by viewModel.allBranches.collectAsState()

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
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
                        Text("La Casa Admin", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = onLogoutClick) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(if (selectedTab == 0) Icons.Filled.Group else Icons.Outlined.Group, contentDescription = "Tenants") },
                    label = { Text("Tenants") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(if (selectedTab == 1) Icons.Filled.Payments else Icons.Outlined.Payments, contentDescription = "Payments") },
                    label = { Text("Payments") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(if (selectedTab == 2) Icons.Filled.ReportProblem else Icons.Outlined.ReportProblem, contentDescription = "Complaints") },
                    label = { Text("Complaints") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(if (selectedTab == 3) Icons.Filled.EventNote else Icons.Outlined.EventNote, contentDescription = "Dues") },
                    label = { Text("Dues") }
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(if (selectedTab == 4) Icons.Filled.Poll else Icons.Outlined.Poll, contentDescription = "Poll") },
                    label = { Text("Poll") }
                )
                NavigationBarItem(
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 },
                    icon = { Icon(if (selectedTab == 5) Icons.Filled.BarChart else Icons.Outlined.BarChart, contentDescription = "Reports") },
                    label = { Text("Reports") }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0 || selectedTab == 1 || selectedTab == 2) {
                FloatingActionButton(
                    onClick = {
                        when (selectedTab) {
                            0 -> onAddTenantClick()
                            1 -> showAddPaymentDialog = true
                            2 -> showAddComplaintDialog = true
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add"
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> TenantListScreen(
                    viewModel = viewModel,
                    onAddTenantClick = onAddTenantClick,
                    onEditTenantClick = onEditTenantClick,
                    onTenantClick = onTenantClick,
                    onLogoutClick = onLogoutClick,
                    isEmbedded = true
                )
                1 -> PaymentListScreen(
                    viewModel = viewModel
                )
                2 -> ComplaintListScreen(
                    viewModel = viewModel
                )
                3 -> UpcomingDuesScreen(
                    viewModel = viewModel
                )
                4 -> PollScreen(
                    viewModel = viewModel,
                    isAdmin = true
                )
                5 -> ReportsScreen(
                    viewModel = viewModel
                )
            }
        }
    }

    if (showAddPaymentDialog) {
        GlobalAddPaymentDialog(
            tenants = tenants,
            onDismiss = { showAddPaymentDialog = false },
            onConfirm = { tenant, amount, month, type ->
                viewModel.addPayment(
                    com.hfad.lacasapgmanagement.data.Payment(
                        tenantId = tenant.id,
                        amount = amount,
                        date = System.currentTimeMillis(),
                        month = month,
                        paymentType = type,
                        tenantPhone = tenant.phoneNumber,
                        status = "Verified"
                    )
                )
                showAddPaymentDialog = false
            }
        )
    }

    if (showAddComplaintDialog) {
        AddComplaintDialog(
            onDismiss = { showAddComplaintDialog = false },
            onConfirm = { name, phone, description ->
                viewModel.addComplaint(
                    com.hfad.lacasapgmanagement.data.Complaint(
                        tenantName = name,
                        tenantPhone = phone,
                        description = description,
                        status = "Pending",
                        createdAt = System.currentTimeMillis()
                    )
                )
                showAddComplaintDialog = false
            }
        )
    }

    if (showAddBedDialog) {
        AddBedDialog(
            branches = branches,
            onDismiss = { showAddBedDialog = false },
            onConfirm = { room, bed, branch ->
                viewModel.addBed(com.hfad.lacasapgmanagement.data.Bed(roomNumber = room, bedNumber = bed, branch = branch))
                showAddBedDialog = false
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalAddPaymentDialog(
    tenants: List<com.hfad.lacasapgmanagement.data.Tenant>,
    onDismiss: () -> Unit,
    onConfirm: (com.hfad.lacasapgmanagement.data.Tenant, Double, String, String) -> Unit
) {
    var selectedTenant by remember { mutableStateOf<com.hfad.lacasapgmanagement.data.Tenant?>(null) }
    var amount by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var paymentType by remember { mutableStateOf("Rent") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Payment") },
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
                        modifier = Modifier.menuAnchor().fillMaxWidth()
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
                
                Spacer(modifier = Modifier.height(8.dp))
                
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
            Button(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull() ?: 0.0
                    selectedTenant?.let { onConfirm(it, amountDouble, month, paymentType) }
                },
                enabled = selectedTenant != null && amount.isNotEmpty() && month.isNotEmpty()
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
