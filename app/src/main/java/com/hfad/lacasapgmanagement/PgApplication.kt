package com.hfad.lacasapgmanagement

import android.app.Application
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json
import com.hfad.lacasapgmanagement.data.AppDatabase
import com.hfad.lacasapgmanagement.data.TenantRepository

class PgApplication : Application() {
    val supabase by lazy {
        createSupabaseClient(
            supabaseUrl = "https://zzfqqbswqfjwgoqkdked.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inp6ZnFxYnN3cWZqd2dvcWtka2VkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzcwNzY3NjcsImV4cCI6MjA5MjY1Mjc2N30.b-0qwV8bSLQPxvyUQhqBp8T4iwe6gOokbb6mc43ANCg"
        ) {
            install(Postgrest) {
                serializer = KotlinXSerializer(Json {
                    encodeDefaults = true
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
    }

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: TenantRepository by lazy { 
        TenantRepository(
            database.tenantDao(),
            database.paymentDao(),
            database.complaintDao(),
            database.bedDao(),
            database.branchDao(),
            database.pollDao(),
            supabase
        )
    }
}
