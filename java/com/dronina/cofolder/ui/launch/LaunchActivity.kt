package com.dronina.cofolder.ui.launch

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.dronina.cofolder.R
import com.dronina.cofolder.ui.base.BaseActivity
import com.dronina.cofolder.ui.base.BaseViewModelFactory
import com.dronina.cofolder.utils.extensions.navigateIntroPage
import com.dronina.cofolder.utils.extensions.navigateLoginPage
import com.dronina.cofolder.utils.extensions.navigateMainPage
import com.dronina.cofolder.utils.extensions.navigateRegisterPage

class LaunchActivity : BaseActivity() {
    private lateinit var viewModel: LaunchViewModel

    override fun initViewModels() {
        viewModel = ViewModelProviders
            .of(this, BaseViewModelFactory(this))
            .get(LaunchViewModel::class.java)
        viewModel.view = this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)
    }


    override fun onStart() {
        super.onStart()
        viewModel.onActivityStarted()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.onActivityResult(requestCode, resultCode)
    }

    fun navigateLogin() {
        navigateLoginPage()
    }

    fun navigateRegister() {
        navigateRegisterPage()
    }

    fun navigateMain() {
        navigateMainPage()
    }

    fun showIntro() {
        navigateIntroPage()
    }
}
