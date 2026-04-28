package com.hfad.lacasapgmanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hfad.lacasapgmanagement.data.Tenant
import com.hfad.lacasapgmanagement.ui.viewmodel.TenantViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTenantScreen(
    viewModel: TenantViewModel,
    tenantId: Int? = null,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var roomNumber by remember { mutableStateOf("") }
    var rentAmount by remember { mutableStateOf("") }
    var depositAmount by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var existingTenantId by remember { mutableStateOf<Int?>(tenantId) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedBranch by remember { mutableStateOf("Main Branch") }
    var expanded by remember { mutableStateOf(false) }
    val branches by viewModel.allBranches.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(tenantId) {
        if (tenantId != null && tenantId != 0) {
            isLoading = true
            viewModel.getTenantById(tenantId).collect { tenant ->
                if (tenant != null) {
                    name = tenant.name
                    phoneNumber = tenant.phoneNumber
                    roomNumber = tenant.roomNumber
                    rentAmount = tenant.rentAmount.toString()
                    depositAmount = tenant.depositAmount.toString()
                    selectedBranch = tenant.branch
                    password = tenant.password
                }
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existingTenantId != null) "Edit Tenant" else "Add New Tenant") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { newPhone ->
                    phoneNumber = newPhone.trim()
                    if (phoneNumber.length >= 10) {
                        scope.launch {
                            isLoading = true
                            val existingTenant = viewModel.fetchTenantByPhone(phoneNumber)
                            if (existingTenant != null) {
                                existingTenantId = existingTenant.id
                                name = existingTenant.name
                                roomNumber = existingTenant.roomNumber
                                rentAmount = existingTenant.rentAmount.toString()
                                depositAmount = existingTenant.depositAmount.toString()
                                selectedBranch = existingTenant.branch
                                password = existingTenant.password
                            } else {
                                existingTenantId = null
                            }
                            isLoading = false
                        }
                    }
                },
                label = { Text("Phone Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = roomNumber,
                onValueChange = { roomNumber = it },
                label = { Text("Room Number") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = rentAmount,
                onValueChange = { rentAmount = it },
                label = { Text("Rent Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = depositAmount,
                onValueChange = { depositAmount = it },
                label = { Text("Deposit Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Set Password") },
                modifier = Modifier.fillMaxWidth()
            )

            // Branch Selection
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedBranch,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Branch") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    if (branches.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Main Branch") },
                            onClick = {
                                selectedBranch = "Main Branch"
                                expanded = false
                            }
                        )
                    } else {
                        branches.forEach { branch ->
                            DropdownMenuItem(
                                text = { Text(branch.name) },
                                onClick = {
                                    selectedBranch = branch.name
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    val trimmedPhone = phoneNumber.trim()
                    val tenant = Tenant(
                        id = existingTenantId ?: 0,
                        name = name,
                        phoneNumber = trimmedPhone,
                        roomNumber = roomNumber,
                        rentAmount = rentAmount.toDoubleOrNull() ?: 0.0,
                        depositAmount = depositAmount.toDoubleOrNull() ?: 0.0,
                        joiningDate = System.currentTimeMillis(),
                        branch = selectedBranch,
                        password = if (password.isBlank()) "1234" else password
                    )
                    if (existingTenantId != null) {
                        viewModel.updateTenant(tenant)
                    } else {
                        viewModel.insertTenant(tenant)
                    }
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && roomNumber.isNotBlank() && phoneNumber.length >= 10
            ) {
                Text("Save Tenant")
            }
        }
    }
}
