package com.hfad.lacasapgmanagement.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Tenant::class, Payment::class, Complaint::class, Bed::class, Branch::class, Poll::class, PollVote::class, ComplaintCategory::class, FoodMenuItem::class, PollConfiguration::class, Announcement::class], version = 31, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tenantDao(): TenantDao
    abstract fun paymentDao(): PaymentDao
    abstract fun complaintDao(): ComplaintDao
    abstract fun bedDao(): BedDao
    abstract fun branchDao(): BranchDao
    abstract fun pollDao(): PollDao
    abstract fun complaintCategoryDao(): ComplaintCategoryDao
    abstract fun foodMenuItemDao(): FoodMenuItemDao
    abstract fun pollConfigurationDao(): PollConfigurationDao
    abstract fun announcementDao(): AnnouncementDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "pg_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
