package com.hfad.lacasapgmanagement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hfad.lacasapgmanagement.data.Tenant
import com.hfad.lacasapgmanagement.ui.screens.LoginScreen
import com.hfad.lacasapgmanagement.ui.screens.TenantDetailsScreen
import com.hfad.lacasapgmanagement.ui.theme.LacasaPgManagementTheme
import com.hfad.lacasapgmanagement.ui.viewmodel.TenantViewModel
import com.hfad.lacasapgmanagement.ui.viewmodel.TenantViewModelFactory

class TenantLoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LacasaPgManagementTheme {
                val viewModel: TenantViewModel = viewModel(
                    factory = TenantViewModelFactory((application as PgApplication).repository)
                )
                var loggedInTenant by remember { mutableStateOf<Tenant?>(null) }

                if (loggedInTenant == null) {
                    LoginScreen(
                        viewModel = viewModel,
                        onLoginSuccess = { tenant ->
                            loggedInTenant = tenant
                        }
                    )
                } else {
                    TenantDetailsScreen(
                        tenantId = loggedInTenant!!.id,
                        viewModel = viewModel,
                        onNavigateBack = { loggedInTenant = null }
                    )
                }
            }
        }
    }
}
