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
import com.hfad.lacasapgmanagement.ui.viewmodel.TenantViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportsScreen(viewModel: TenantViewModel) {
    val tenants by viewModel.allTenants.collectAsState()
    val payments by viewModel.allPayments.collectAsState()
    val beds by viewModel.allBeds.collectAsState()

    val totalRevenue = payments.filter { it.status == "Verified" }.sumOf { it.amount }
    val currentMonth = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date())
    val monthlyRevenue = payments.filter { 
        it.status == "Verified" && 
        it.month.contains(currentMonth.split(" ")[0], ignoreCase = true) &&
        it.month.contains(currentMonth.split(" ")[1])
    }.sumOf { it.amount }

    val occupancyRate = if (beds.isNotEmpty()) {
        (beds.count { it.isOccupied }.toDouble() / beds.size * 100).toInt()
    } else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Business Reports",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                ReportCard(
                    title = "Monthly Revenue ($currentMonth)",
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
