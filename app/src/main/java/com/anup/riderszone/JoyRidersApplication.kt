package com.anup.riderszone

import android.app.Application
import com.facebook.FacebookSdk
import com.facebook.LoggingBehavior
import com.facebook.appevents.AppEventsLogger
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class JoyRidersApplication: Application() {
    lateinit var mGoogleSigninClient: GoogleSignInClient
    lateinit var mGoogleSignInOptions: GoogleSignInOptions

   // val googleSignInOptions
    override fun onCreate() {
        super.onCreate()
        initialiseFbSDK()
        //initialiseGoogleClient()
       //Created develop branch to test the jenking merge between branch to branch. Must remove later
    }

//    private fun initialiseGoogleClient() {
//        mGoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken(getString(R.string.default_web_client_id))
//            .requestEmail()
//            .build()
//        mGoogleSigninClient = GoogleSignIn.getClient(this, mGoogleSignInOptions)
//    }

    private fun initialiseFbSDK() {
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this);
        FacebookSdk.addLoggingBehavior(LoggingBehavior.REQUESTS)
    }


}