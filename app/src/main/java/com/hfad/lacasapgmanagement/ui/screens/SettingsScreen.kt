package com.hfad.lacasapgmanagement.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.widget.Toast
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.outlined.Restaurant
import coil.compose.AsyncImage
import com.hfad.lacasapgmanagement.data.ComplaintCategory
import androidx.compose.ui.draw.scale
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Timer
import com.hfad.lacasapgmanagement.data.FoodMenuItem
import com.hfad.lacasapgmanagement.ui.viewmodel.TenantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onConfigureBranchesClick: () -> Unit,
    onManageBedsClick: () -> Unit,
    viewModel: TenantViewModel
) {
    var showCategoryDialog by remember { mutableStateOf(false) }
    val categories by viewModel.allComplaintCategories.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                SettingsItem(
                    title = "Configure Branches",
                    subtitle = "Add or remove PG branches",
                    icon = Icons.Outlined.AdminPanelSettings,
                    onClick = onConfigureBranchesClick
                )
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
            item {
                var showAutoPollDialog by remember { mutableStateOf(false) }
                val menuItems by viewModel.allMenuItems.collectAsState()
                val config by viewModel.pollConfiguration.collectAsState()

                SettingsItem(
                    title = "Auto-Poll Settings",
                    subtitle = "Automate daily food attendance polls",
                    icon = Icons.Outlined.Timer,
                    onClick = { showAutoPollDialog = true }
                )

                if (showAutoPollDialog) {
                    AutoPollSettingsDialog(
                        config = config,
                        menuItems = menuItems,
                        onDismiss = { showAutoPollDialog = false },
                        onSave = { newConfig -> viewModel.savePollConfiguration(newConfig) }
                    )
                }
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
            item {
                SettingsItem(
                    title = "Manage Beds",
                    subtitle = "View and update bed occupancy",
                    icon = Icons.Default.Bed,
                    onClick = onManageBedsClick
                )
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
            item {
                SettingsItem(
                    title = "Complaint Categories",
                    subtitle = "Manage categories for tenant complaints",
                    icon = Icons.Outlined.Category,
                    onClick = { showCategoryDialog = true }
                )
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
            item {
                var showMenuDialog by remember { mutableStateOf(false) }
                val menuItems by viewModel.allMenuItems.collectAsState()
                
                SettingsItem(
                    title = "Manage Food Menu",
                    subtitle = "Configure breakfast, lunch, and dinner dishes",
                    icon = Icons.Outlined.Restaurant,
                    onClick = { showMenuDialog = true }
                )
                
                if (showMenuDialog) {
                    FoodMenuDialog(
                        menuItems = menuItems,
                        onDismiss = { showMenuDialog = false },
                        onAdd = { name, category, url -> viewModel.addMenuItem(name, category, url) },
                        onDelete = { viewModel.deleteMenuItem(it) }
                    )
                }
            }
        }
    }

    if (showCategoryDialog) {
        ComplaintCategoryDialog(
            categories = categories,
            onDismiss = { showCategoryDialog = false },
            onAdd = { viewModel.addComplaintCategory(it) },
            onDelete = { viewModel.deleteComplaintCategory(it) }
        )
    }
}

@Composable
fun ComplaintCategoryDialog(
    categories: List<ComplaintCategory>,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit,
    onDelete: (ComplaintCategory) -> Unit
) {
    var newCategory by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Complaint Categories") },
        text = {
            Column(modifier = Modifier.width(300.dp)) {
                OutlinedTextField(
                    value = newCategory,
                    onValueChange = { newCategory = it },
                    label = { Text("New Category") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                val trimmed = newCategory.trim()
                                if (trimmed.isNotBlank()) {
                                    if (categories.none { it.name.equals(trimmed, ignoreCase = true) }) {
                                        onAdd(trimmed)
                                        newCategory = ""
                                    } else {
                                        Toast.makeText(context, "Category already exists", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    items(categories) { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = category.name,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            IconButton(onClick = { onDelete(category) }) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        HorizontalDivider(thickness = 0.5.dp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = "dish_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)
        val outputStream = FileOutputStream(file)
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun FoodMenuDialog(
    menuItems: List<FoodMenuItem>,
    onDismiss: () -> Unit,
    onAdd: (String, String, String?) -> Unit,
    onDelete: (FoodMenuItem) -> Unit
) {
    var newItemName by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedCategory by remember { mutableStateOf("Breakfast") }
    val categories = listOf("Breakfast", "Lunch", "Dinner", "Snack")
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }
    
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Food Menu") },
        text = {
            Column(modifier = Modifier.width(300.dp)) {
                OutlinedTextField(
                    value = newItemName,
                    onValueChange = { newItemName = it },
                    label = { Text("Dish Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedCard(
                    onClick = { launcher.launch(arrayOf("image/*")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Text("Select Dish Image", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
                
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceAround) {
                    categories.forEach { category ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            RadioButton(
                                selected = (selectedCategory == category),
                                onClick = { selectedCategory = category }
                            )
                            Text(category, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Button(
                    onClick = {
                        val trimmed = newItemName.trim()
                        if (trimmed.isNotBlank()) {
                            val finalPath = selectedImageUri?.let { uri ->
                                saveImageToInternalStorage(context, uri)
                            }
                            onAdd(trimmed, selectedCategory, finalPath)
                            newItemName = ""
                            selectedImageUri = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Dish")
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 250.dp)
                ) {
                    categories.forEach { category ->
                        val itemsInCategory = menuItems.filter { it.category == category }
                        if (itemsInCategory.isNotEmpty()) {
                            item {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            items(itemsInCategory) { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        if (item.imageUrl != null) {
                                            AsyncImage(
                                                model = item.imageUrl,
                                                contentDescription = null,
                                                modifier = Modifier.size(40.dp).padding(end = 8.dp)
                                            )
                                        }
                                        Text(item.name)
                                    }
                                    IconButton(onClick = { onDelete(item) }) {
                                        Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
fun AutoPollSettingsDialog(
    config: com.hfad.lacasapgmanagement.data.PollConfiguration?,
    menuItems: List<FoodMenuItem>,
    onDismiss: () -> Unit,
    onSave: (com.hfad.lacasapgmanagement.data.PollConfiguration) -> Unit
) {
    var isEnabled by remember(config) { mutableStateOf(config?.isAutomationEnabled ?: false) }
    var defBreakfast by remember(config) { mutableStateOf(config?.defaultBreakfastId) }
    var defLunch by remember(config) { mutableStateOf(config?.defaultLunchId) }
    var defDinner by remember(config) { mutableStateOf(config?.defaultDinnerId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Auto-Poll Settings") },
        text = {
            Column(modifier = Modifier.width(300.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Daily Automation", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it },
                        modifier = Modifier.scale(0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Default Menu Dishes:", style = MaterialTheme.typography.labelLarge)

                DishSelectorCompact("Breakfast", menuItems.filter { it.category == "Breakfast" }, defBreakfast) { defBreakfast = it }
                DishSelectorCompact("Lunch", menuItems.filter { it.category == "Lunch" }, defLunch) { defLunch = it }
                DishSelectorCompact("Dinner", menuItems.filter { it.category == "Dinner" }, defDinner) { defDinner = it }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(
                    com.hfad.lacasapgmanagement.data.PollConfiguration(
                        isAutomationEnabled = isEnabled,
                        defaultBreakfastId = defBreakfast,
                        defaultLunchId = defLunch,
                        defaultDinnerId = defDinner
                    )
                )
                onDismiss()
            }) {
                Text("Save")
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
fun DishSelectorCompact(label: String, options: List<FoodMenuItem>, selectedId: Int?, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selectedDish = options.find { it.id == selectedId }

    Box(modifier = Modifier.padding(vertical = 4.dp)) {
        OutlinedTextField(
            value = selectedDish?.name ?: "Select $label",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Box(modifier = Modifier.matchParentSize().clickable { expanded = true })

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.width(250.dp)) {
            if (options.isEmpty()) {
                DropdownMenuItem(text = { Text("No items found") }, onClick = { expanded = false })
            } else {
                options.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item.name) },
                        onClick = {
                            onSelect(item.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
