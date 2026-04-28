package com.hfad.lacasapgmanagement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hfad.lacasapgmanagement.ui.screens.AddTenantScreen
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
    NavHost(navController = navController, startDestination = "tenantList") {
        composable("tenantList") {
            TenantListScreen(
                viewModel = viewModel,
                onAddTenantClick = { navController.navigate("addTenant") },
                onTenantClick = { tenantId -> navController.navigate("tenantDetails/$tenantId") }
            )
        }
        composable("addTenant") {
            AddTenantScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "tenantDetails/{tenantId}",
            arguments = listOf(navArgument("tenantId") { type = NavType.IntType })
        ) { backStackEntry ->
            val tenantId = backStackEntry.arguments?.getInt("tenantId") ?: return@composable
            TenantDetailsScreen(
                tenantId = tenantId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
