package com.dronina.cofolder.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dronina.cofolder.R
import com.dronina.cofolder.utils.extensions.LocaleUtils
import com.dronina.cofolder.utils.extensions.style
import com.dronina.cofolder.utils.extensions.styleStatusBar
import com.dronina.cofolder.utils.extensions.toast

open class BaseActivity : AppCompatActivity() {

    init {
        LocaleUtils.updateConfig(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        style()
        styleStatusBar()
        super.onCreate(savedInstanceState)
        onCreate()
        initViewModels()
        addListeners()
    }

    open fun onCreate() {}

    open fun initViewModels() {}

    open fun addListeners() {}

    open fun showProgress() {}

    open fun hideProgress() {}

    fun networkError() {
        toast(getString(R.string.network_error))
    }
}
