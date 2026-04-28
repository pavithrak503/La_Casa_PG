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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hfad.lacasapgmanagement.R
import com.hfad.lacasapgmanagement.data.Bed
import com.hfad.lacasapgmanagement.ui.viewmodel.TenantViewModel

@Composable
fun BedListScreen(
    viewModel: TenantViewModel,
    isEmbedded: Boolean = false,
    onNavigateBack: (() -> Unit)? = null
) {
    val beds by viewModel.allBeds.collectAsState()
    val branches by viewModel.allBranches.collectAsState()
    var selectedBranchFilter by remember { mutableStateOf("All") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (!isEmbedded && onNavigateBack != null) {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
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
                        Text("Bed Management", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = "Bed Information",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Branch Filter
        ScrollableTabRow(
            selectedTabIndex = if (selectedBranchFilter == "All") 0 else (branches.indexOfFirst { it.name == selectedBranchFilter } + 1).coerceAtLeast(0),
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            divider = {}
        ) {
            Tab(
                selected = selectedBranchFilter == "All",
                onClick = { selectedBranchFilter = "All" },
                text = { Text("All") }
            )
            branches.forEach { branch ->
                Tab(
                    selected = selectedBranchFilter == branch.name,
                    onClick = { selectedBranchFilter = branch.name },
                    text = { Text(branch.name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val filteredBeds = if (selectedBranchFilter == "All") beds else beds.filter { it.branch == selectedBranchFilter }
        val groupedBeds = filteredBeds.groupBy { it.branch }

        // Legend / Summary
        val occupiedCount = filteredBeds.count { it.isOccupied }
        val totalCount = filteredBeds.size
        Row(
            modifier = Modifier.padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusIndicator(color = Color(0xFF4CAF50), label = "Vacant (${totalCount - occupiedCount})")
            Spacer(modifier = Modifier.width(16.dp))
            StatusIndicator(color = Color(0xFFF44336), label = "Occupied ($occupiedCount)")
        }

        if (filteredBeds.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No beds found for this selection.")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                groupedBeds.forEach { (branch, bedsInBranch) ->
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                        Text(
                            text = branch,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    items(bedsInBranch) { bed ->
                        GraphicalBedItem(
                            bed = bed,
                            onDelete = { viewModel.deleteBed(bed.id) },
                            onToggleOccupancy = {
                                viewModel.updateBed(bed.copy(isOccupied = !bed.isOccupied))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusIndicator(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun GraphicalBedItem(bed: Bed, onDelete: () -> Unit, onToggleOccupancy: () -> Unit) {
    val statusColor = if (bed.isOccupied) Color(0xFFF44336) else Color(0xFF4CAF50)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleOccupancy() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = statusColor.copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
    ) {
        Box {
            // Delete button in top right
            IconButton(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.TopEnd).size(32.dp)
            ) {
                Icon(
                    Icons.Default.Delete, 
                    contentDescription = "Delete", 
                    tint = Color.Gray.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (bed.isOccupied) Icons.Default.Person else Icons.Default.Bed,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Room ${bed.roomNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Bed ${bed.bedNumber}",
                    style = MaterialTheme.typography.bodySmall
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Surface(
                    color = statusColor,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (bed.isOccupied) "OCCUPIED" else "VACANT",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBedDialog(
    branches: List<com.hfad.lacasapgmanagement.data.Branch>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var roomNumber by remember { mutableStateOf("") }
    var bedNumber by remember { mutableStateOf("") }
    var selectedBranch by remember { mutableStateOf("Main Branch") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Bed") },
        text = {
            Column {
                OutlinedTextField(
                    value = roomNumber,
                    onValueChange = { roomNumber = it },
                    label = { Text("Room Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = bedNumber,
                    onValueChange = { bedNumber = it },
                    label = { Text("Bed Number (e.g. A, B, 1)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
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
                                onClick = { selectedBranch = "Main Branch"; expanded = false }
                            )
                        } else {
                            branches.forEach { branch ->
                                DropdownMenuItem(
                                    text = { Text(branch.name) },
                                    onClick = { selectedBranch = branch.name; expanded = false }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(roomNumber, bedNumber, selectedBranch) },
                enabled = roomNumber.isNotBlank() && bedNumber.isNotBlank()
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
