package com.dronina.cofolder.ui.main

import android.os.Bundle
import android.view.WindowManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.dronina.cofolder.R
import com.dronina.cofolder.ui.base.BaseActivity
import com.dronina.cofolder.ui.base.BaseViewModelFactory
import com.dronina.cofolder.utils.extensions.setupWithNavController
import com.dronina.cofolder.utils.extensions.softKeyboardAboveContent
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {
    private lateinit var viewModel: MainViewModel
    private var currentNavController: LiveData<NavController>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        this.softKeyboardAboveContent()
        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        }
        viewModel = ViewModelProviders
            .of(this, BaseViewModelFactory(this))
            .get(MainViewModel::class.java)
        viewModel.onCreate()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        setupBottomNavigationBar()
    }

    private fun setupBottomNavigationBar() {
        val navGraphIds = listOf(
            R.navigation.notes,
            R.navigation.lists,
            R.navigation.images,
            R.navigation.profile
        )

        val controller = bottom_nav.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.nav_host_container,
            intent = intent
        )
        controller.observe(this, { navController ->
            setupActionBarWithNavController(navController)
        })
        currentNavController = controller
    }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavController?.value?.navigateUp() ?: false
    }
}
