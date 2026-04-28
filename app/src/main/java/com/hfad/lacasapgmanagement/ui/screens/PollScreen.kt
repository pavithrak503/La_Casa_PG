package com.hfad.lacasapgmanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hfad.lacasapgmanagement.data.Poll
import com.hfad.lacasapgmanagement.data.PollVote
import com.hfad.lacasapgmanagement.ui.viewmodel.TenantViewModel

@Composable
fun PollScreen(viewModel: TenantViewModel, isAdmin: Boolean, tenantId: Int? = null) {
    val activePoll by viewModel.activePoll.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Food Attendance Poll",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (activePoll == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No active poll for today.")
                    if (isAdmin) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.createPoll("Food Attendance for Today") }) {
                            Text("Create Daily Poll")
                        }
                    }
                }
            }
        } else {
            val poll = activePoll!!
            if (isAdmin) {
                AdminPollView(poll, viewModel)
            } else if (tenantId != null) {
                TenantVoteView(poll, tenantId, viewModel)
            }
        }
    }
}

@Composable
fun AdminPollView(poll: Poll, viewModel: TenantViewModel) {
    val votes by viewModel.getVotesForPoll(poll.id).collectAsState()
    
    val breakfastCount = votes.count { it.breakfast }
    val lunchCount = votes.count { it.lunch }
    val dinnerCount = votes.count { it.dinner }
    val vegCount = votes.count { it.isVeg }
    val nonVegCount = votes.count { !it.isVeg }

    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Live Results: ${poll.title}", style = MaterialTheme.typography.titleMedium)
            Text("Date: ${poll.date}", style = MaterialTheme.typography.bodySmall)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            ResultRow("Breakfast", breakfastCount)
            ResultRow("Lunch", lunchCount)
            ResultRow("Dinner", dinnerCount)
            Spacer(modifier = Modifier.height(8.dp))
            ResultRow("Veg", vegCount)
            ResultRow("Non-Veg", nonVegCount)
            
            Text(
                "Total Responses: ${votes.size}",
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun ResultRow(label: String, count: Int) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text(count.toString(), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TenantVoteView(poll: Poll, tenantId: Int, viewModel: TenantViewModel) {
    var breakfast by remember { mutableStateOf(false) }
    var lunch by remember { mutableStateOf(false) }
    var dinner by remember { mutableStateOf(false) }
    var isVeg by remember { mutableStateOf(true) }
    var hasVoted by remember { mutableStateOf(false) }

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

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(poll.title, style = MaterialTheme.typography.titleMedium)
            Text("Please select the meals you will have today:", style = MaterialTheme.typography.bodyMedium)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            MealCheckbox("Breakfast", breakfast) { breakfast = it }
            MealCheckbox("Lunch", lunch) { lunch = it }
            MealCheckbox("Dinner", dinner) { dinner = it }
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            
            Text("Dietary Preference:", style = MaterialTheme.typography.bodyMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = isVeg, onClick = { isVeg = true })
                Text("Veg")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(selected = !isVeg, onClick = { isVeg = false })
                Text("Non-Veg")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    viewModel.submitVote(PollVote(pollId = poll.id, tenantId = tenantId, breakfast = breakfast, lunch = lunch, dinner = dinner, isVeg = isVeg))
                    hasVoted = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (hasVoted) "Update Vote" else "Submit Vote")
            }
            
            if (hasVoted) {
                Text(
                    "Your response has been recorded.",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun MealCheckbox(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label)
    }
}
