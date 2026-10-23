package com.well.androidgnews

import android.app.Application
import com.well.androidgnews.di.AppContainer

class NewsApplication : Application() {

    val container: AppContainer by lazy {
        AppContainer(BuildConfig.GNEWS_API_KEY)
    }
}
