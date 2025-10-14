package com.worksync.app

import android.app.Application
import kotlin.text.Typography.dagger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WorkSyncApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
