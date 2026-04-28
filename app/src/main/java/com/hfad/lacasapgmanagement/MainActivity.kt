package com.hfad.lacasapgmanagement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hfad.lacasapgmanagement.ui.screens.AddTenantScreen
import com.hfad.lacasapgmanagement.ui.screens.AdminMainScreen
import com.hfad.lacasapgmanagement.ui.screens.BranchConfigScreen
import com.hfad.lacasapgmanagement.ui.screens.LoginScreen
import com.hfad.lacasapgmanagement.ui.screens.RoleSelectionScreen
import com.hfad.lacasapgmanagement.ui.screens.TenantDashboardScreen
import com.hfad.lacasapgmanagement.ui.screens.TenantDetailsScreen
import com.hfad.lacasapgmanagement.ui.screens.TenantListScreen
import com.hfad.lacasapgmanagement.ui.theme.LacasaPgManagementTheme
import com.hfad.lacasapgmanagement.ui.viewmodel.TenantViewModel
import com.hfad.lacasapgmanagement.ui.viewmodel.TenantViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LacasaPgManagementTheme {
                val viewModel: TenantViewModel = viewModel(
                    factory = TenantViewModelFactory((application as PgApplication).repository)
                )
                PgApp(viewModel)
            }
        }
    }
}

@Composable
fun PgApp(viewModel: TenantViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "roleSelection") {
        composable("roleSelection") {
            RoleSelectionScreen(
                onAdminClick = { navController.navigate("tenantList") },
                onTenantClick = { navController.navigate("login") }
            )
        }
        composable("login") {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { tenant ->
                    // Navigate to dashboard and pass tenant phone to fetch data
                    navController.navigate("tenantDashboard/${tenant.phoneNumber}")
                }
            )
        }
        composable("tenantDashboard/{phoneNumber}") { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            TenantDashboardScreen(
                phone = phone,
                viewModel = viewModel,
                onLogout = {
                    navController.navigate("roleSelection") {
                        popUpTo("roleSelection") { inclusive = true }
                    }
                }
            )
        }
        composable("tenantList") {
            AdminMainScreen(
                viewModel = viewModel,
                onAddTenantClick = { navController.navigate("addTenant") },
                onEditTenantClick = { tenantId ->
                    navController.navigate("editTenant/$tenantId")
                },
                onTenantClick = { tenantId ->
                    navController.navigate("tenantDetails/$tenantId")
                },
                onLogoutClick = {
                    navController.navigate("roleSelection") {
                        popUpTo("roleSelection") { inclusive = true }
                    }
                },
                onSettingsClick = { navController.navigate("settings") }
            )
        }
        composable("settings") {
            com.hfad.lacasapgmanagement.ui.screens.SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onConfigureBranchesClick = { navController.navigate("branchConfig") },
                onManageBedsClick = {
                    // We can either navigate to a separate bed management screen 
                    // or pass a state back to AdminMainScreen. 
                    // For now, let's assume we can navigate to bedList if we expose it as a route,
                    // but BedList is currently a tab in AdminMainScreen.
                    // If the user wants it inside settings, maybe it should be a separate route.
                    navController.navigate("bedList")
                }
            )
        }
        composable("bedList") {
            com.hfad.lacasapgmanagement.ui.screens.BedListScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("addTenant") {
            AddTenantScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("editTenant/{tenantId}") { backStackEntry ->
            val tenantId = backStackEntry.arguments?.getString("tenantId")?.toIntOrNull() ?: 0
            AddTenantScreen(
                viewModel = viewModel,
                tenantId = tenantId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("branchConfig") {
            BranchConfigScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("tenantDetails/{tenantId}") { backStackEntry ->
            val tenantId = backStackEntry.arguments?.getString("tenantId")?.toIntOrNull() ?: 0
            TenantDetailsScreen(
                tenantId = tenantId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
