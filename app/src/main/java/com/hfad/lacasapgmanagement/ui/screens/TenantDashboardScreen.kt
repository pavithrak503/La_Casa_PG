package com.hfad.lacasapgmanagement.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Announcement
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import com.hfad.lacasapgmanagement.R
import com.hfad.lacasapgmanagement.data.Payment
import com.hfad.lacasapgmanagement.data.Tenant
import com.hfad.lacasapgmanagement.ui.viewmodel.TenantViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.graphics.Color
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantDashboardScreen(
    phone: String,
    viewModel: TenantViewModel,
    onLogout: () -> Unit
) {
    var tenant by remember { mutableStateOf<Tenant?>(null) }
    var payments by remember { mutableStateOf<List<Payment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddPaymentDialog by remember { mutableStateOf(false) }
    var showAddComplaintDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(phone) {
        tenant = viewModel.fetchTenantByPhone(phone)
        payments = viewModel.fetchPaymentsByPhone(phone)
        isLoading = false
    }

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
                        Text(
                            text = when(selectedTab) {
                                0 -> "My Profile"
                                1 -> "Announcements"
                                2 -> "Food Poll"
                                3 -> "My Complaints"
                                4 -> "My Payments"
                                else -> "My Rules"
                            },
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(if (selectedTab == 0) Icons.Filled.Person else Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile", style = MaterialTheme.typography.labelSmall, maxLines = 1) },
                    alwaysShowLabel = false
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.AutoMirrored.Filled.Announcement, contentDescription = "Announcements") },
                    label = { Text("Announce", style = MaterialTheme.typography.labelSmall, maxLines = 1) },
                    alwaysShowLabel = false
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(if (selectedTab == 2) Icons.Filled.Poll else Icons.Default.Poll, contentDescription = "Poll") },
                    label = { Text("Poll", style = MaterialTheme.typography.labelSmall, maxLines = 1) },
                    alwaysShowLabel = false
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(if (selectedTab == 3) Icons.Filled.ReportProblem else Icons.Default.ReportProblem, contentDescription = "Complaints") },
                    label = { Text("Complaints", style = MaterialTheme.typography.labelSmall, maxLines = 1) },
                    alwaysShowLabel = false
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(if (selectedTab == 4) Icons.Filled.Receipt else Icons.Default.Receipt, contentDescription = "Payments") },
                    label = { Text("Payments", style = MaterialTheme.typography.labelSmall, maxLines = 1) },
                    alwaysShowLabel = false
                )
                NavigationBarItem(
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 },
                    icon = { Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = "Rules") },
                    label = { Text("Rules", style = MaterialTheme.typography.labelSmall, maxLines = 1) },
                    alwaysShowLabel = false
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 4) {
                FloatingActionButton(onClick = { showAddPaymentDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Notify Payment")
                }
            } else if (selectedTab == 3) {
                FloatingActionButton(onClick = { showAddComplaintDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Raise Complaint")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (tenant != null) {
                if (selectedTab == 0) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Welcome, ${tenant!!.name}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        TenantInfoItem(label = "Mobile Number", value = tenant!!.phoneNumber)
                        TenantInfoItem(label = "Room Number", value = tenant!!.roomNumber)
                        TenantInfoItem(label = "Monthly Rent", value = "₹${tenant!!.rentAmount}")
                        TenantInfoItem(label = "Security Deposit", value = "₹${tenant!!.depositAmount}")
                    }
                } else if (selectedTab == 1) {
                    AnnouncementScreen(viewModel = viewModel, isAdmin = false)
                } else if (selectedTab == 2) {
                    PollScreen(viewModel = viewModel, isAdmin = false, tenantId = tenant!!.id)
                } else if (selectedTab == 3) {
                    TenantComplaintsScreen(viewModel, phone)
                } else if (selectedTab == 4) {
                    TenantPaymentsScreen(context, tenant!!, payments)
                } else {
                    TenantRulesScreen()
                }
            } else {
                Text("Failed to load tenant details.")
            }
        }
    }

    val categories by viewModel.allComplaintCategories.collectAsState()

    if (showAddComplaintDialog && tenant != null) {
        AddComplaintDialog(
            categories = categories,
            onDismiss = { showAddComplaintDialog = false },
            onConfirm = { description, category ->
                viewModel.addComplaint(
                    com.hfad.lacasapgmanagement.data.Complaint(
                        tenantId = tenant!!.id,
                        tenantName = tenant!!.name,
                        tenantPhone = tenant!!.phoneNumber,
                        description = description,
                        category = category
                    )
                )
                showAddComplaintDialog = false
            }
        )
    }

    if (showAddPaymentDialog && tenant != null) {
        TenantAddPaymentDialog(
            tenant = tenant!!,
            onDismiss = { showAddPaymentDialog = false },
            onConfirm = { amount, dateLong, type, proofUri ->
                val monthFormatter = SimpleDateFormat("MMM yyyy", Locale.getDefault())
                val monthString = monthFormatter.format(Date(dateLong))
                viewModel.addPayment(
                    Payment(
                        tenantId = tenant!!.id,
                        amount = amount,
                        date = dateLong,
                        month = monthString,
                        paymentType = type,
                        tenantPhone = tenant!!.phoneNumber,
                        status = "Pending",
                        proofImageUrl = proofUri?.toString()
                    )
                )
                showAddPaymentDialog = false
                Toast.makeText(context, "Payment submitted for verification", Toast.LENGTH_SHORT).show()
                // Refresh payments after adding
                scope.launch {
                    payments = viewModel.fetchPaymentsByPhone(phone)
                }
            }
        )
    }
}

@Composable
fun TenantRulesScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        val rules = listOf(
            "1. Gate closing time is 11:00 PM.",
            "2. No loud music after 10:00 PM.",
            "3. Visitors are not allowed in rooms after 8:00 PM.",
            "4. Please keep your room and common areas clean.",
            "5. Rent must be paid by the 5th of every month.",
            "6. Electricity charges are extra as per sub-meter.",
            "7. 30 days notice period is mandatory before vacating.",
            "8. Smoking and alcohol are strictly prohibited inside the premises."
        )

        rules.forEach { rule ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = rule,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantAddPaymentDialog(
    tenant: Tenant,
    onDismiss: () -> Unit,
    onConfirm: (Double, Long, String, Uri?) -> Unit
) {
    var amount by remember { mutableStateOf(tenant.rentAmount.toString()) }
    var paymentType by remember { mutableStateOf("Rent") }
    var proofUri by remember { mutableStateOf<Uri?>(null) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        proofUri = uri
    }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    var showDatePicker by remember { mutableStateOf(false) }
    
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val selectedDateText = datePickerState.selectedDateMillis?.let {
        dateFormatter.format(Date(it))
    } ?: "Select Date"

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notify Payment") },
        text = {
            Column {
                Text(
                    text = "Please transfer the amount via UPI or Cash, then select the payment date below.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount Paid") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedCard(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Payment Date", style = MaterialTheme.typography.labelSmall)
                            Text(selectedDateText, style = MaterialTheme.typography.bodyLarge)
                        }
                        Icon(Icons.Default.Add, contentDescription = null) // Using Add as a placeholder for calendar icon if not imported
                    }
                }
                
                Text(
                    text = "Payment Type",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
                Row {
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

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedCard(
                    onClick = { launcher.launch(arrayOf("image/*")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Proof of Payment", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = if (proofUri != null) "Image Selected" else "Upload Screenshot",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (proofUri != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_camera),
                            contentDescription = null
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull() ?: 0.0
                    val dateLong = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    onConfirm(amountDouble, dateLong, paymentType, proofUri)
                },
                enabled = amount.isNotEmpty() && proofUri != null
            ) {
                Text("Submit for Verification")
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
fun TenantComplaintsScreen(viewModel: TenantViewModel, phone: String) {
    var complaints by remember { mutableStateOf<List<com.hfad.lacasapgmanagement.data.Complaint>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(phone) {
        complaints = viewModel.fetchComplaintsByPhone(phone)
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (complaints.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("You have no active complaints.")
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(complaints) { complaint ->
                TenantComplaintItem(complaint = complaint)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddComplaintDialog(
    categories: List<com.hfad.lacasapgmanagement.data.ComplaintCategory>,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(if (categories.isNotEmpty()) categories[0].name else "General") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Raise a Complaint") },
        text = {
            Column {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        if (categories.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("General") },
                                onClick = {
                                    selectedCategory = "General"
                                    expanded = false
                                }
                            )
                        } else {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        selectedCategory = category.name
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Describe your issue") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(description, selectedCategory) },
                enabled = description.isNotBlank()
            ) {
                Text("Submit")
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
fun TenantInfoItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp), thickness = 0.5.dp)
    }
}

@Composable
fun TenantComplaintItem(complaint: com.hfad.lacasapgmanagement.data.Complaint) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = complaint.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Surface(
                    color = if (complaint.status == "Resolved") MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = complaint.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (complaint.status == "Resolved") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = complaint.description, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun TenantPaymentsScreen(context: Context, tenant: Tenant, payments: List<Payment>) {
    if (payments.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No payment history found.", style = MaterialTheme.typography.bodySmall)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(payments) { payment ->
                TenantPaymentItemWithReceipt(context, tenant, payment)
            }
        }
    }
}

@Composable
fun TenantPaymentItemWithReceipt(context: Context, tenant: Tenant, payment: Payment) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = payment.month, style = MaterialTheme.typography.titleSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text(text = "Amount: ₹${payment.amount}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "Date: ${dateFormat.format(Date(payment.date))}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        color = when(payment.status) {
                            "Verified" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            "Rejected" -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                            else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = payment.status,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = when(payment.status) {
                                "Verified" -> MaterialTheme.colorScheme.primary
                                "Rejected" -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.secondary
                            }
                        )
                    }
                    Text(text = payment.paymentType, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            if (payment.status == "Verified") {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { generateAndShareReceipt(context, tenant, payment) },
                    modifier = Modifier.align(Alignment.End).height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Receipt", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

fun generateAndShareReceipt(context: Context, tenant: Tenant, payment: Payment) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(300, 400, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint()

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    paint.textSize = 16f
    paint.isFakeBoldText = true
    canvas.drawText("La Casa PG - Payment Receipt", 20f, 40f, paint)

    paint.isFakeBoldText = false
    paint.textSize = 12f
    canvas.drawText("Tenant: ${tenant.name}", 20f, 80f, paint)
    canvas.drawText("Phone: ${tenant.phoneNumber}", 20f, 100f, paint)
    canvas.drawText("Room: ${tenant.roomNumber}", 20f, 120f, paint)
    
    canvas.drawLine(20f, 140f, 280f, 140f, paint)

    canvas.drawText("Month: ${payment.month}", 20f, 170f, paint)
    canvas.drawText("Amount Paid: ₹${payment.amount}", 20f, 190f, paint)
    canvas.drawText("Payment Type: ${payment.paymentType}", 20f, 210f, paint)
    canvas.drawText("Date: ${dateFormat.format(Date(payment.date))}", 20f, 230f, paint)
    canvas.drawText("Status: VERIFIED", 20f, 250f, paint)

    canvas.drawLine(20f, 270f, 280f, 270f, paint)
    canvas.drawText("Thank you for your payment!", 20f, 300f, paint)

    pdfDocument.finishPage(page)

    val fileName = "Receipt_${payment.month.replace(" ", "_")}_${payment.id}.pdf"
    val file = File(context.cacheDir, fileName)

    try {
        pdfDocument.writeTo(FileOutputStream(file))
        sharePdf(context, file)
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        pdfDocument.close()
    }
}

fun sharePdf(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share Receipt"))
}

@Composable
fun TenantPaymentItem(payment: Payment) {
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
                Text(
                    text = payment.status,
                    style = MaterialTheme.typography.labelSmall,
                    color = when(payment.status) {
                        "Verified" -> MaterialTheme.colorScheme.primary
                        "Rejected" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.secondary
                    }
                )
            }
        }
    }
}
