package com.hfad.lacasapgmanagement.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    val filteredBeds = if (selectedBranchFilter == "All") beds else beds.filter { it.branch == selectedBranchFilter }
    
    var showAddBedDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddBedDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Bed")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
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

            // Branch Filter (More compact height)
            ScrollableTabRow(
                selectedTabIndex = if (selectedBranchFilter == "All") 0 else (branches.indexOfFirst { it.name == selectedBranchFilter } + 1).coerceAtLeast(0),
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {},
                modifier = Modifier.height(48.dp)
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

            // Group by Branch then Room
            val branchesInFilter = filteredBeds.groupBy { it.branch }

            if (filteredBeds.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No beds found for this selection.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    branchesInFilter.forEach { (branch, bedsInBranch) ->
                        item {
                            Text(
                                text = branch.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }

                        val roomsInBranch = bedsInBranch.groupBy { it.roomNumber }
                        roomsInBranch.forEach { (room, bedsInRoom) ->
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "R$room",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.width(40.dp),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray
                                    )
                                    
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        bedsInRoom.forEach { bed ->
                                            var showDetail by remember { mutableStateOf(false) }
                                            
                                            NanoBedItem(
                                                bed = bed,
                                                onClick = { showDetail = true }
                                            )

                                            if (showDetail) {
                                                BedDetailDialog(
                                                    bed = bed,
                                                    onDismiss = { showDetail = false },
                                                    onToggleOccupancy = {
                                                        viewModel.updateBed(bed.copy(isOccupied = !bed.isOccupied))
                                                    },
                                                    onDelete = {
                                                        viewModel.deleteBed(bed.id)
                                                        showDetail = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
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
fun NanoBedItem(bed: Bed, onClick: () -> Unit) {
    val statusColor = if (bed.isOccupied) Color(0xFFF44336) else Color(0xFF4CAF50)

    Surface(
        modifier = Modifier
            .size(30.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(4.dp),
        color = statusColor,
        tonalElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = bed.bedNumber,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = Color.White
            )
        }
    }
}

@Composable
fun BedDetailDialog(
    bed: Bed,
    onDismiss: () -> Unit,
    onToggleOccupancy: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = if (bed.isOccupied) Color(0xFFF44336) else Color(0xFF4CAF50)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Bed, contentDescription = null, tint = statusColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Room ${bed.roomNumber} - Bed ${bed.bedNumber}")
            }
        },
        text = {
            Column {
                Text("Branch: ${bed.branch}")
                Text("Status: ${if (bed.isOccupied) "Occupied" else "Vacant"}")
                if (bed.isOccupied && !bed.tenantName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Current Tenant:", fontWeight = FontWeight.Bold)
                    Text(bed.tenantName)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onToggleOccupancy,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = statusColor)
                ) {
                    Text(if (bed.isOccupied) "Mark as Vacant" else "Mark as Occupied")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        dismissButton = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Bed", tint = Color.Gray)
            }
        }
    )
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
