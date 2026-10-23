package com.well.androidgnews

import android.app.Application
import com.well.androidgnews.data.remote.parsePins
import com.well.androidgnews.di.AppContainer

class NewsApplication : Application() {

    val container: AppContainer by lazy {
        AppContainer(BuildConfig.BACKEND_BASE_URL, parsePins(BuildConfig.BACKEND_CERT_PINS))
    }
}
