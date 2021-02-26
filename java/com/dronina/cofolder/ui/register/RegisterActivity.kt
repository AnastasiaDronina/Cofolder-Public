package com.dronina.cofolder.ui.register

import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.dronina.cofolder.R
import com.dronina.cofolder.databinding.ActivityRegisterBinding
import com.dronina.cofolder.ui.base.BaseActivity
import com.dronina.cofolder.ui.base.BaseViewModelFactory
import com.dronina.cofolder.utils.extensions.navigateMainPage
import com.dronina.cofolder.utils.extensions.toast
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : BaseActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: RegisterViewModel

    override fun onCreate() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register)
    }

    override fun initViewModels() {
        viewModel = ViewModelProviders
            .of(this, BaseViewModelFactory(this))
            .get(RegisterViewModel::class.java)
        binding.viewModel = viewModel
        viewModel.view = this
    }

    override fun addListeners() {
        binding.btnCreateAccount.setOnClickListener {
            viewModel.saveData()
        }
    }

    fun success() {
        navigateMainPage()
    }

    fun userGeneratedError() {
        toast(getString(R.string.enter_name_and_surname))
    }

    override fun showProgress() {
        progress_bar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        progress_bar.visibility = View.GONE
    }
}
