package com.example.ntele

import android.app.Application
import com.mappls.sdk.maps.Mappls
import com.mappls.sdk.services.account.MapplsAccountManager

class NTeleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Set your Mappls REST API and Map SDK keys
        MapplsAccountManager.getInstance().restAPIKey = "20b2c7a2d61fa8e3c0f04cc4c3f5cbb3"
        MapplsAccountManager.getInstance().mapSDKKey = "20b2c7a2d61fa8e3c0f04cc4c3f5cbb3"
        MapplsAccountManager.getInstance().atlasClientId = "96dHZVzsAutlN8qj9sy53VREgcqL_Jay_di_SlZqMU9CrjLwBKi3QXEhvpVYjLVApcz9pgEP3F0oKG6RZPeJpg=="
        MapplsAccountManager.getInstance().atlasClientSecret = "lrFxI-iSEg_-U93zuGixRT6C7rUG8EkIkVqGlKPN-JclhattubOfSgbl1MrbSLNxzpMfghG_-3sEPJp7r_9iMcCeuGUjYucg"
        Mappls.getInstance(applicationContext)
    }
}
