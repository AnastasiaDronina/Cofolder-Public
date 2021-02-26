package com.dronina.cofolder

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import com.dronina.cofolder.utils.extensions.LocaleUtils.setLocale
import com.dronina.cofolder.utils.extensions.LocaleUtils.updateConfig
import com.dronina.cofolder.utils.extensions.getLocale
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import java.util.*


class CofolderApp : Application() {
    companion object {
        var instance: CofolderApp? = null
            private set

        val context: Context?
            get() = instance
    }


    fun isConnectedToNetwork(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo

        return networkInfo != null && networkInfo.isAvailable && networkInfo.isConnected
    }

    override fun onCreate() {
        instance = this
        super.onCreate()

        val locale = getLocale()
        if (locale.isNotEmpty()) {
            setLocale(Locale(locale))
            updateConfig(this, baseContext.resources.configuration)
        }
        val config: ImagePipelineConfig =
            ImagePipelineConfig.newBuilder(this).setDownsampleEnabled(true)
                .build()
        Fresco.initialize(this, config)
    }

    fun setNewLang() {
        setLocale(Locale(getLocale()))
        updateConfig(this, baseContext.resources.configuration)
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateConfig(this, newConfig)
    }
}
