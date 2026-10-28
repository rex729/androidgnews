package com.well.androidgnews

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.well.androidgnews.di.AppDependencies

class NewsApplication : Application() {

    val dependencies: AppDependencies by lazy {
        AppDependencies(this, BuildConfig.GNEWS_API_KEY)
    }

    override fun onCreate() {
        super.onCreate()
        // Crash reports from release builds only, so debugging sessions don't pollute the dashboard.
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG
    }
}
