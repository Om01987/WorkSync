package com.worksync.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WorkSyncApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
