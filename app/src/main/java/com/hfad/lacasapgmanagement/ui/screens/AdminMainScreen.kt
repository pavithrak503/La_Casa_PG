package com.hfad.lacasapgmanagement.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Announcement
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.outlined.Announcement
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
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
    val categories by viewModel.allComplaintCategories.collectAsState()

    // Define navigation items dynamically
    data class NavItem(val id: Int, val label: String, val icon: ImageVector, val selectedIcon: ImageVector)
    
    val navItems = listOf(
        NavItem(0, "Tenants", Icons.Outlined.Group, Icons.Filled.Group),
        NavItem(1, "Beds", Icons.Outlined.Bed, Icons.Filled.Bed),
        NavItem(2, "Payments", Icons.Outlined.Payments, Icons.Filled.Payments),
        NavItem(3, "Complaints", Icons.Outlined.ReportProblem, Icons.Filled.ReportProblem),
        NavItem(4, "Dues", Icons.AutoMirrored.Outlined.EventNote, Icons.AutoMirrored.Filled.EventNote),
        NavItem(5, "Poll", Icons.Outlined.Poll, Icons.Filled.Poll),
        NavItem(6, "Reports", Icons.Outlined.BarChart, Icons.Filled.BarChart),
        NavItem(7, "Announce", Icons.AutoMirrored.Outlined.Announcement, Icons.AutoMirrored.Filled.Announcement)
    )

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
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(NavigationBarDefaults.windowInsets)
                        .padding(vertical = 4.dp)
                ) {
                    navItems.chunked(4).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            rowItems.forEach { item ->
                                Box(modifier = Modifier.width(90.dp)) {
                                    NavigationBar(
                                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                                        tonalElevation = 0.dp,
                                        modifier = Modifier.height(80.dp)
                                    ) {
                                        NavigationBarItem(
                                            selected = selectedTab == item.id,
                                            onClick = { selectedTab = item.id },
                                            icon = {
                                                Icon(
                                                    if (selectedTab == item.id) item.selectedIcon else item.icon,
                                                    contentDescription = item.label
                                                )
                                            },
                                            label = {
                                                Text(
                                                    item.label,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    maxLines = 1
                                                )
                                            },
                                            alwaysShowLabel = true
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (selectedTab in 0..3) {
                FloatingActionButton(
                    onClick = {
                        when (selectedTab) {
                            0 -> onAddTenantClick()
                            1 -> showAddBedDialog = true
                            2 -> showAddPaymentDialog = true
                            3 -> showAddComplaintDialog = true
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
                1 -> BedListScreen(
                    viewModel = viewModel,
                    isEmbedded = true
                )
                2 -> PaymentListScreen(
                    viewModel = viewModel
                )
                3 -> ComplaintListScreen(
                    viewModel = viewModel
                )
                4 -> UpcomingDuesScreen(
                    viewModel = viewModel
                )
                5 -> PollScreen(
                    viewModel = viewModel,
                    isAdmin = true
                )
                6 -> ReportsScreen(
                    viewModel = viewModel
                )
                7 -> AnnouncementScreen(
                    viewModel = viewModel,
                    isAdmin = true
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
            categories = categories,
            onDismiss = { showAddComplaintDialog = false },
            onConfirm = { name, phone, description, category ->
                viewModel.addComplaint(
                    com.hfad.lacasapgmanagement.data.Complaint(
                        tenantName = name,
                        tenantPhone = phone,
                        description = description,
                        category = category,
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
