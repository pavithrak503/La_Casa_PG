package com.hfad.lacasapgmanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hfad.lacasapgmanagement.data.Tenant
import com.hfad.lacasapgmanagement.ui.viewmodel.TenantViewModel
import kotlinx.coroutines.delay
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
    var existingTenantId by remember { mutableStateOf(tenantId) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedBranch by remember { mutableStateOf("Main Branch") }
    var expanded by remember { mutableStateOf(false) }
    val branches by viewModel.allBranches.collectAsState()

    // Aadhaar Verification States
    var isAadhaarVerified by remember { mutableStateOf(false) }
    var isVerifyingAadhaar by remember { mutableStateOf(false) }
    var aadhaarNumber by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    var otpValue by remember { mutableStateOf("") }
    var isVerifyingOtp by remember { mutableStateOf(false) }
    var txnId by remember { mutableStateOf<String?>(null) }
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
                    isAadhaarVerified = tenant.isAadhaarVerified
                    aadhaarNumber = tenant.aadhaarNumber ?: ""
                }
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.White,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = { 
                    Text(
                        if (existingTenantId != null) "Edit Tenant" else "Add New Tenant",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Aadhaar Authentication Section (Simulated)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isAadhaarVerified) 
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Aadhaar E-Authentication", 
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = aadhaarNumber,
                            onValueChange = { if (it.length <= 12) aadhaarNumber = it.filter { c -> c.isDigit() } },
                            label = { Text("12-digit Aadhaar", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            textStyle = MaterialTheme.typography.bodyMedium,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            enabled = !isAadhaarVerified && !otpSent
                        )
                        
                        if (!isAadhaarVerified && !otpSent) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        isVerifyingAadhaar = true
                                        val result = viewModel.generateAadhaarOtp(aadhaarNumber)
                                        if (result != null) {
                                            txnId = result
                                            otpSent = true
                                        }
                                        isVerifyingAadhaar = false
                                    }
                                },
                                enabled = aadhaarNumber.length == 12 && !isVerifyingAadhaar,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                if (isVerifyingAadhaar) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("Get OTP", fontSize = 12.sp)
                                }
                            }
                        } else if (isAadhaarVerified) {
                            Icon(
                                Icons.Default.CheckCircle, 
                                contentDescription = "Verified", 
                                tint = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    if (otpSent && !isAadhaarVerified) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = otpValue,
                                onValueChange = { if (it.length <= 6) otpValue = it.filter { c -> c.isDigit() } },
                                label = { Text("Enter 6-digit OTP", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                textStyle = MaterialTheme.typography.bodyMedium,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                            
                            Button(
                                onClick = {
                                    scope.launch {
                                        isVerifyingOtp = true
                                        txnId?.let { id ->
                                            val response = viewModel.verifyAadhaarOtp(otpValue, id)
                                            if (response?.status == "success") {
                                                isAadhaarVerified = true
                                                otpSent = false
                                                if (response.fullName != null) {
                                                    name = response.fullName
                                                }
                                            }
                                        }
                                        isVerifyingOtp = false
                                    }
                                },
                                enabled = otpValue.length == 6 && !isVerifyingOtp,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                if (isVerifyingOtp) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("Verify OTP", fontSize = 12.sp)
                                }
                            }
                        }
                        Text(
                            "OTP sent to mobile linked with Aadhaar ending in ${aadhaarNumber.takeLast(4)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

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
                label = { Text("Phone Number", fontSize = 12.sp) },
                textStyle = MaterialTheme.typography.bodyMedium,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name", fontSize = 12.sp) },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1.5f)
                )
                OutlinedTextField(
                    value = roomNumber,
                    onValueChange = { roomNumber = it },
                    label = { Text("Room No", fontSize = 12.sp) },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = rentAmount,
                    onValueChange = { rentAmount = it },
                    label = { Text("Rent (₹)", fontSize = 12.sp) },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = depositAmount,
                    onValueChange = { depositAmount = it },
                    label = { Text("Deposit (₹)", fontSize = 12.sp) },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("App Password", fontSize = 12.sp) },
                textStyle = MaterialTheme.typography.bodyMedium,
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
                    label = { Text("Branch", fontSize = 12.sp) },
                    textStyle = MaterialTheme.typography.bodyMedium,
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
                        password = if (password.isBlank()) "1234" else password,
                        isAadhaarVerified = isAadhaarVerified,
                        aadhaarNumber = if (isAadhaarVerified) aadhaarNumber else null
                    )
                    if (existingTenantId != null) {
                        viewModel.updateTenant(tenant)
                    } else {
                        viewModel.insertTenant(tenant)
                    }
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                enabled = name.isNotBlank() && roomNumber.isNotBlank() && phoneNumber.length >= 10 && (isAadhaarVerified || existingTenantId != null)
            ) {
                Text(if (existingTenantId != null) "Update Details" else "Verify & Add Tenant", fontWeight = FontWeight.Bold)
            }
        }
    }
}
