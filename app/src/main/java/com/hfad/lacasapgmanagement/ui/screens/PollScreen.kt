package com.hfad.lacasapgmanagement.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.draw.scale
import com.hfad.lacasapgmanagement.data.FoodMenuItem
import com.hfad.lacasapgmanagement.data.Poll
import com.hfad.lacasapgmanagement.data.PollVote
import com.hfad.lacasapgmanagement.ui.viewmodel.TenantViewModel

@Composable
fun PollScreen(viewModel: TenantViewModel, isAdmin: Boolean, tenantId: Int? = null) {
    val activePoll by viewModel.activePoll.collectAsState()
    val menuItems by viewModel.allMenuItems.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.checkAndCreateAutoPoll()
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (activePoll == null) {
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("No active poll for today.", style = MaterialTheme.typography.bodySmall)
                    if (isAdmin) {
                        Spacer(modifier = Modifier.height(12.dp))
                        CreatePollSection(viewModel, menuItems)
                    }
                }
            }
        } else {
            val poll = activePoll!!
            if (isAdmin) {
                item {
                    AdminPollView(poll, viewModel, menuItems)
                }
            } else if (tenantId != null) {
                item {
                    TenantVoteView(poll, tenantId, viewModel, menuItems)
                }
            }
        }
    }
}

@Composable
fun CreatePollSection(viewModel: TenantViewModel, menuItems: List<FoodMenuItem>) {
    var breakfastId by remember { mutableStateOf<Int?>(null) }
    var lunchId by remember { mutableStateOf<Int?>(null) }
    var dinnerId by remember { mutableStateOf<Int?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        DishSelector("Breakfast", menuItems.filter { it.category == "Breakfast" }, breakfastId) { breakfastId = it }
        DishSelector("Lunch", menuItems.filter { it.category == "Lunch" }, lunchId) { lunchId = it }
        DishSelector("Dinner", menuItems.filter { it.category == "Dinner" }, dinnerId) { dinnerId = it }
        
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { 
                if (breakfastId != null || lunchId != null || dinnerId != null) {
                    viewModel.createPoll("Food Attendance for Today", breakfastId, lunchId, dinnerId)
                } else {
                    android.widget.Toast.makeText(context, "Please select at least one dish", android.widget.Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Daily Poll")
        }
    }
}

@Composable
fun DishSelector(label: String, options: List<FoodMenuItem>, selectedId: Int?, onSelect: (Int) -> Unit) {
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
                .width(250.dp)
                .clickable { expanded = true },
            enabled = false, // Set to false but use colors to make it look active
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        
        // Transparent overlay to capture clicks even though the TextField is "disabled"
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded, 
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(250.dp)
        ) {
            if (options.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No $label items found", style = MaterialTheme.typography.bodySmall) },
                    onClick = { expanded = false }
                )
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
fun AdminPollView(poll: Poll, viewModel: TenantViewModel, menuItems: List<FoodMenuItem>) {
    val votes by viewModel.getVotesForPoll(poll.id).collectAsState()
    
    val breakfastCount = votes.count { it.breakfast }
    val lunchCount = votes.count { it.lunch }
    val dinnerCount = votes.count { it.dinner }
    val vegCount = votes.count { it.isVeg }
    val nonVegCount = votes.count { !it.isVeg }

    val breakfastDish = menuItems.find { it.id == poll.breakfastDishId }
    val lunchDish = menuItems.find { it.id == poll.lunchDishId }
    val dinnerDish = menuItems.find { it.id == poll.dinnerDishId }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Results: ${poll.title}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { viewModel.deactivatePoll(poll) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "End Poll",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text("Date: ${poll.date}", style = MaterialTheme.typography.labelSmall)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            ResultRow("Breakfast (${breakfastDish?.name ?: "N/A"})", breakfastCount)
            ResultRow("Lunch (${lunchDish?.name ?: "N/A"})", lunchCount)
            ResultRow("Dinner (${dinnerDish?.name ?: "N/A"})", dinnerCount)
            Spacer(modifier = Modifier.height(8.dp))
            ResultRow("Veg", vegCount)
            ResultRow("Non-Veg", nonVegCount)
            
            Text(
                "Total Responses: ${votes.size}",
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ResultRow(label: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text(count.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TenantVoteView(poll: Poll, tenantId: Int, viewModel: TenantViewModel, menuItems: List<FoodMenuItem>) {
    var breakfast by remember { mutableStateOf(false) }
    var lunch by remember { mutableStateOf(false) }
    var dinner by remember { mutableStateOf(false) }
    var isVeg by remember { mutableStateOf(true) }
    var hasVoted by remember { mutableStateOf(false) }

    val breakfastDish = menuItems.find { it.id == poll.breakfastDishId }
    val lunchDish = menuItems.find { it.id == poll.lunchDishId }
    val dinnerDish = menuItems.find { it.id == poll.dinnerDishId }

    LaunchedEffect(poll.id) {
        val existingVote = viewModel.getVoteForTenant(poll.id, tenantId)
        if (existingVote != null) {
            breakfast = existingVote.breakfast
            lunch = existingVote.lunch
            dinner = existingVote.dinner
            isVeg = existingVote.isVeg
            hasVoted = true
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(poll.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("Please select the meals you will have today:", style = MaterialTheme.typography.bodySmall)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            MealItem("Breakfast", breakfastDish, breakfast) { breakfast = it }
            MealItem("Lunch", lunchDish, lunch) { lunch = it }
            MealItem("Dinner", dinnerDish, dinner) { dinner = it }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            
            Text("Dietary Preference:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = isVeg, onClick = { isVeg = true })
                Text("Veg", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(selected = !isVeg, onClick = { isVeg = false })
                Text("Non-Veg", style = MaterialTheme.typography.bodySmall)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    viewModel.submitVote(PollVote(pollId = poll.id, tenantId = tenantId, breakfast = breakfast, lunch = lunch, dinner = dinner, isVeg = isVeg))
                    hasVoted = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (hasVoted) "Update Vote" else "Submit Vote", style = MaterialTheme.typography.labelLarge)
            }
            
            if (hasVoted) {
                Text(
                    "Your response has been recorded.",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun MealItem(label: String, dish: FoodMenuItem?, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = checked, 
                onCheckedChange = onCheckedChange,
                modifier = Modifier.size(36.dp)
            )
            Text(label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            if (dish != null) {
                Text(" - ${dish.name}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (dish?.imageUrl != null) {
            AsyncImage(
                model = dish.imageUrl,
                contentDescription = dish.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(start = 36.dp, end = 8.dp, top = 2.dp),
                contentScale = ContentScale.Crop
            )
        }
    }
}
