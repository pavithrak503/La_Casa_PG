package com.hfad.lacasapgmanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalContext
import com.hfad.lacasapgmanagement.ui.viewmodel.TenantViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: TenantViewModel) {
    val tenants by viewModel.allTenants.collectAsState()
    val payments by viewModel.allPayments.collectAsState()
    val beds by viewModel.allBeds.collectAsState()
    val context = LocalContext.current

    var selectedCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    val monthFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
    val selectedMonthText = monthFormat.format(selectedCalendar.time)

    val totalRevenue = payments.filter { it.status == "Verified" }.sumOf { it.amount }
    
    val monthlyRevenue = payments.filter { 
        it.status == "Verified" && 
        it.month.contains(selectedMonthText.split(" ")[0], ignoreCase = true) &&
        it.month.contains(selectedMonthText.split(" ")[1])
    }.sumOf { it.amount }

    val occupancyRate = if (beds.isNotEmpty()) {
        (beds.count { it.isOccupied }.toDouble() / beds.size * 100).toInt()
    } else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Business Reports",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Row {
                IconButton(onClick = {
                    val datePickerDialog = android.app.DatePickerDialog(
                        context,
                        { _, year, month, _ ->
                            val newCal = Calendar.getInstance()
                            newCal.set(Calendar.YEAR, year)
                            newCal.set(Calendar.MONTH, month)
                            selectedCalendar = newCal
                        },
                        selectedCalendar.get(Calendar.YEAR),
                        selectedCalendar.get(Calendar.MONTH),
                        selectedCalendar.get(Calendar.DAY_OF_MONTH)
                    )
                    datePickerDialog.show()
                }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Select Month")
                }
                
                IconButton(onClick = {
                    val reportText = """
                        PG Business Report - $selectedMonthText
                        --------------------------------
                        Monthly Revenue: ₹${monthlyRevenue.toInt()}
                        Total Revenue: ₹${totalRevenue.toInt()}
                        Occupancy Rate: $occupancyRate%
                        Total Tenants: ${tenants.size}
                        Available Beds: ${beds.count { !it.isOccupied }}
                    """.trimIndent()
                    
                    val sendIntent: android.content.Intent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        putExtra(android.content.Intent.EXTRA_TEXT, reportText)
                        type = "text/plain"
                    }
                    val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                    context.startActivity(shareIntent)
                }) {
                    Icon(Icons.Default.Share, contentDescription = "Export Report")
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                ReportCard(
                    title = "Monthly Revenue ($selectedMonthText)",
                    value = "₹${monthlyRevenue.toInt()}",
                    color = MaterialTheme.colorScheme.primary
                )
            }
            item {
                ReportCard(
                    title = "Total Lifetime Revenue",
                    value = "₹${totalRevenue.toInt()}",
                    color = Color(0xFF4CAF50)
                )
            }
            item {
                ReportCard(
                    title = "Occupancy Rate",
                    value = "$occupancyRate%",
                    color = Color(0xFF2196F3)
                )
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SmallReportCard(
                        modifier = Modifier.weight(1f),
                        title = "Total Tenants",
                        value = "${tenants.size}",
                        color = MaterialTheme.colorScheme.secondary
                    )
                    SmallReportCard(
                        modifier = Modifier.weight(1f),
                        title = "Available Beds",
                        value = "${beds.count { !it.isOccupied }}",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
fun ReportCard(title: String, value: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
fun SmallReportCard(modifier: Modifier = Modifier, title: String, value: String, color: Color) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
