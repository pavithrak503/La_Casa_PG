package com.hfad.lacasapgmanagement.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.platform.LocalContext
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
                                1 -> "Food Poll"
                                2 -> "My Complaints"
                                else -> "My Receipts"
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
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Poll, contentDescription = "Poll") },
                    label = { Text("Poll") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.ReportProblem, contentDescription = "Complaints") },
                    label = { Text("Complaints") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Receipt, contentDescription = "Receipts") },
                    label = { Text("Receipts") }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(onClick = { showAddPaymentDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Make Payment")
                }
            } else if (selectedTab == 1) {
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
                            .padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Welcome, ${tenant!!.name}",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                        
                        TenantInfoItem(label = "Mobile Number", value = tenant!!.phoneNumber)
                        TenantInfoItem(label = "Room Number", value = tenant!!.roomNumber)
                        TenantInfoItem(label = "Monthly Rent", value = "₹${tenant!!.rentAmount}")
                        TenantInfoItem(label = "Security Deposit", value = "₹${tenant!!.depositAmount}")
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "Payment History",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        if (payments.isEmpty()) {
                            Text("No payment history found.")
                        } else {
                            LazyColumn(modifier = Modifier.weight(1f)) {
                                items(payments) { payment ->
                                    TenantPaymentItem(payment)
                                }
                            }
                        }
                    }
                } else if (selectedTab == 1) {
                    PollScreen(viewModel = viewModel, isAdmin = false, tenantId = tenant!!.id)
                } else if (selectedTab == 2) {
                    TenantComplaintsScreen(viewModel, phone)
                } else {
                    TenantReceiptsScreen(context, tenant!!, payments)
                }
            } else {
                Text("Failed to load tenant details.")
            }
        }
    }

    if (showAddComplaintDialog && tenant != null) {
        AddComplaintDialog(
            onDismiss = { showAddComplaintDialog = false },
            onConfirm = { description ->
                viewModel.addComplaint(
                    com.hfad.lacasapgmanagement.data.Complaint(
                        tenantId = tenant!!.id,
                        tenantName = tenant!!.name,
                        tenantPhone = tenant!!.phoneNumber,
                        description = description
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
            onConfirm = { amount, month, type ->
                viewModel.addPayment(
                    Payment(
                        tenantId = tenant!!.id,
                        amount = amount,
                        date = System.currentTimeMillis(),
                        month = month,
                        paymentType = type,
                        tenantPhone = tenant!!.phoneNumber,
                        status = "Pending"
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantAddPaymentDialog(
    tenant: Tenant,
    onDismiss: () -> Unit,
    onConfirm: (Double, String, String) -> Unit
) {
    var amount by remember { mutableStateOf(tenant.rentAmount.toString()) }
    var month by remember { mutableStateOf("") }
    var paymentType by remember { mutableStateOf("Rent") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Make Payment") },
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
                Text("Pay")
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

@Composable
fun AddComplaintDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Raise a Complaint") },
        text = {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Describe your issue") },
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(description) },
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
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp), thickness = 0.5.dp)
    }
}

@Composable
fun TenantComplaintItem(complaint: com.hfad.lacasapgmanagement.data.Complaint) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = complaint.description, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "Status: ${complaint.status}",
                style = MaterialTheme.typography.bodySmall,
                color = if (complaint.status == "Resolved") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun TenantReceiptsScreen(context: Context, tenant: Tenant, payments: List<Payment>) {
    val verifiedPayments = payments.filter { it.status == "Verified" }

    if (verifiedPayments.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No verified receipts available.")
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(verifiedPayments) { payment ->
                ReceiptItem(context, tenant, payment)
            }
        }
    }
}

@Composable
fun ReceiptItem(context: Context, tenant: Tenant, payment: Payment) {
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Receipt: ${payment.month}", style = MaterialTheme.typography.titleMedium)
                Text(text = "Amount: ₹${payment.amount}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Date: ${dateFormat.format(Date(payment.date))}", style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = { generateAndShareReceipt(context, tenant, payment) }) {
                Text("Download")
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
