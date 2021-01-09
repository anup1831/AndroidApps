package com.anup.riderszone

import android.app.Application
import com.facebook.FacebookSdk
import com.facebook.LoggingBehavior
import com.facebook.appevents.AppEventsLogger

class JoyRidersApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        initialiseFbSDK()
        initialiseGoogleClient()

    }

    private fun initialiseGoogleClient() {
        TODO("Not yet implemented")
    }

    private fun initialiseFbSDK() {
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this);
        FacebookSdk.addLoggingBehavior(LoggingBehavior.REQUESTS)
    }
}