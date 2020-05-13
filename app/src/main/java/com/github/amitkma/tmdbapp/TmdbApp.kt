package com.github.amitkma.tmdbapp

import android.app.Application
import timber.log.Timber

class TmdbApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}