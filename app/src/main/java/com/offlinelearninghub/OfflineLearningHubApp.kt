package com.offlinelearninghub

import android.app.Application
import com.offlinelearninghub.data.local.AppDatabase

class OfflineLearningHubApp : Application() {

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
    }
}
