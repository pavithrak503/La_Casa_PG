package com.hfad.lacasapgmanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var bedNumber by remember { mutableStateOf("") }
    var rentAmount by remember { mutableStateOf("") }
    var depositAmount by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var guardianName by remember { mutableStateOf("") }
    var guardianPhoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var existingTenantId by remember { mutableStateOf(tenantId) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedBranch by remember { mutableStateOf("Main Branch") }
    var expanded by remember { mutableStateOf(false) }
    var roomExpanded by remember { mutableStateOf(false) }
    var bedExpanded by remember { mutableStateOf(false) }
    val branches by viewModel.allBranches.collectAsState()
    val allBeds by viewModel.allBeds.collectAsState()

    val availableRooms = remember(allBeds, selectedBranch) {
        allBeds.filter { it.branch == selectedBranch }
            .map { it.roomNumber }
            .distinct()
            .sorted()
    }

    val availableBeds = remember(allBeds, selectedBranch, roomNumber) {
        allBeds.filter { it.branch == selectedBranch && it.roomNumber == roomNumber && !it.isOccupied }
            .map { it.bedNumber }
            .distinct()
            .sorted()
    }

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
                    bedNumber = tenant.bedNumber
                    rentAmount = tenant.rentAmount.toString()
                    depositAmount = tenant.depositAmount.toString()
                    selectedBranch = tenant.branch
                    password = tenant.password
                    guardianName = tenant.guardianName ?: ""
                    guardianPhoneNumber = tenant.guardianPhoneNumber ?: ""
                    address = tenant.address ?: ""
                    isAadhaarVerified = tenant.isAadhaarVerified
                    aadhaarNumber = tenant.aadhaarNumber ?: ""
                }
                isLoading = false
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = innerPadding.calculateTopPadding() + 16.dp,
                end = 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
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
                            "Aadhaar Verification (Optional)", 
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
                                    tint = Color(0xFF4CAF50),
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
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            }

            item {
                Text(
                    "Personal Details",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                // 1. Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name", fontSize = 12.sp) },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                // 2. Phone
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { newPhone ->
                        // Limit to 10 digits and only numbers
                        val filtered = newPhone.filter { it.isDigit() }.take(10)
                        phoneNumber = filtered
                        if (filtered.length == 10) {
                            scope.launch {
                                isLoading = true
                                val existingTenant = viewModel.fetchTenantByPhone(filtered)
                                if (existingTenant != null) {
                                    existingTenantId = existingTenant.id
                                    name = existingTenant.name
                                    roomNumber = existingTenant.roomNumber
                                    bedNumber = existingTenant.bedNumber
                                    rentAmount = existingTenant.rentAmount.toString()
                                    depositAmount = existingTenant.depositAmount.toString()
                                    selectedBranch = existingTenant.branch
                                    password = existingTenant.password
                                    guardianName = existingTenant.guardianName ?: ""
                                    guardianPhoneNumber = existingTenant.guardianPhoneNumber ?: ""
                                    address = existingTenant.address ?: ""
                                    isAadhaarVerified = existingTenant.isAadhaarVerified
                                    aadhaarNumber = existingTenant.aadhaarNumber ?: ""
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
            }

            item { Spacer(modifier = Modifier.height(4.dp)) }
            
            item {
                Text(
                    "Guardian & Address Information",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                // 3. Guardian Name & 4. Guardian Phone
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = guardianName,
                        onValueChange = { guardianName = it },
                        label = { Text("Guardian Name", fontSize = 12.sp) },
                        textStyle = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = guardianPhoneNumber,
                        onValueChange = { guardianPhoneNumber = it },
                        label = { Text("Guardian Phone", fontSize = 12.sp) },
                        textStyle = MaterialTheme.typography.bodyMedium,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                // 5. Address
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address", fontSize = 12.sp) },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { Spacer(modifier = Modifier.height(4.dp)) }
            
            item {
                Text(
                    "PG & Room Allocation",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                // 6. Branch
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
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
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
                                        if (selectedBranch != branch.name) {
                                            selectedBranch = branch.name
                                            roomNumber = ""
                                            bedNumber = ""
                                        }
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                // 7. Room No
                ExposedDropdownMenuBox(
                    expanded = roomExpanded,
                    onExpandedChange = { roomExpanded = !roomExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = roomNumber,
                        onValueChange = { roomNumber = it },
                        label = { Text("Room No", fontSize = 12.sp) },
                        textStyle = MaterialTheme.typography.bodyMedium,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roomExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = roomExpanded,
                        onDismissRequest = { roomExpanded = false }
                    ) {
                        if (availableRooms.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No rooms available") },
                                onClick = { roomExpanded = false }
                            )
                        } else {
                            availableRooms.forEach { room ->
                                DropdownMenuItem(
                                    text = { Text(room) },
                                    onClick = {
                                        if (roomNumber != room) {
                                            roomNumber = room
                                            bedNumber = ""
                                        }
                                        roomExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                // 8. Bed No
                ExposedDropdownMenuBox(
                    expanded = bedExpanded,
                    onExpandedChange = { bedExpanded = !bedExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = bedNumber,
                        onValueChange = { bedNumber = it },
                        label = { Text("Bed No", fontSize = 12.sp) },
                        textStyle = MaterialTheme.typography.bodyMedium,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bedExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = bedExpanded,
                        onDismissRequest = { bedExpanded = false }
                    ) {
                        if (availableBeds.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text(if (roomNumber.isBlank()) "Select a room first" else "No vacant beds") },
                                onClick = { bedExpanded = false }
                            )
                        } else {
                            availableBeds.forEach { bed ->
                                DropdownMenuItem(
                                    text = { Text(bed) },
                                    onClick = {
                                        bedNumber = bed
                                        bedExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                // Rent & Deposit
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
            }

            item {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("App Password", fontSize = 12.sp) },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                Button(
                    onClick = {
                        val trimmedPhone = phoneNumber.trim()
                        val tenant = Tenant(
                            id = existingTenantId ?: 0,
                            name = name,
                            phoneNumber = trimmedPhone,
                            roomNumber = roomNumber,
                            bedNumber = bedNumber,
                            rentAmount = rentAmount.toDoubleOrNull() ?: 0.0,
                            depositAmount = depositAmount.toDoubleOrNull() ?: 0.0,
                            joiningDate = System.currentTimeMillis(),
                            branch = selectedBranch,
                            password = password.ifBlank { "1234" },
                            isAadhaarVerified = isAadhaarVerified,
                            aadhaarNumber = aadhaarNumber.ifBlank { null },
                            guardianName = guardianName.ifBlank { null },
                            guardianPhoneNumber = guardianPhoneNumber.ifBlank { null },
                            address = address.ifBlank { null }
                        )
                        if (existingTenantId != null) {
                            viewModel.updateTenant(tenant)
                        } else {
                            viewModel.insertTenant(tenant)
                        }
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    enabled = name.isNotBlank() && roomNumber.isNotBlank() && phoneNumber.length >= 10
                ) {
                    Text(
                        if (existingTenantId != null) "Update Details" else "Save Tenant",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
