package com.well.androidgnews

import android.app.Application
import com.well.androidgnews.di.AppComponent
import com.well.androidgnews.di.DaggerAppComponent

class NewsApplication : Application() {

    val appComponent: AppComponent by lazy {
        DaggerAppComponent.factory().create(BuildConfig.GNEWS_API_KEY)
    }
}
