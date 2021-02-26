package com.dronina.cofolder.ui.intro

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.dronina.cofolder.R
import com.dronina.cofolder.data.preferences.PreferenceManager
import com.dronina.cofolder.utils.extensions.navigateLaunchPage
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment


class IntroActivity : AppIntro() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isSystemBackButtonLocked = true

        setColorDoneText(resources.getColor(R.color.colorOnPrimary))
        setNextArrowColor(resources.getColor(R.color.colorOnPrimary))
        setColorSkipButton(resources.getColor(R.color.colorOnPrimary))
        setIndicatorColor(
            resources.getColor(R.color.colorOnPrimary),
            resources.getColor(R.color.colorOnPrimary)
        )
        showStatusBar(true)
        setStatusBarColorRes(R.color.colorSecondary)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        setDoneText(getString(R.string.start))

        addSlide(
            AppIntroFragment.newInstance(
                title = getString(R.string.slide1_title),
                description = getString(R.string.slide1_text),
                imageDrawable = R.drawable.slide1,
                titleColor = resources.getColor(R.color.colorOnPrimary),
                descriptionColor = resources.getColor(R.color.colorOnPrimary),
                backgroundColor = resources.getColor(R.color.colorSecondary),
                titleTypefaceFontRes = R.font.roboto_slab_bold,
                descriptionTypefaceFontRes = R.font.roboto_light,
            )
        )
        addSlide(
            AppIntroFragment.newInstance(
                title = getString(R.string.slide2_title),
                description = getString(R.string.slide2_text),
                imageDrawable = R.drawable.slide2,
                titleColor = resources.getColor(R.color.colorOnPrimary),
                descriptionColor = resources.getColor(R.color.colorOnPrimary),
                backgroundColor = resources.getColor(R.color.colorSecondary),
                titleTypefaceFontRes = R.font.roboto_slab_bold,
                descriptionTypefaceFontRes = R.font.roboto_light,
            )
        )
        addSlide(
            AppIntroFragment.newInstance(
                title = getString(R.string.slide3_title),
                description = getString(R.string.slide3_text),
                imageDrawable = R.drawable.slide3,
                titleColor = resources.getColor(R.color.colorOnPrimary),
                descriptionColor = resources.getColor(R.color.colorOnPrimary),
                backgroundColor = resources.getColor(R.color.colorSecondary),
                titleTypefaceFontRes = R.font.roboto_slab_bold,
                descriptionTypefaceFontRes = R.font.roboto_light,
            )
        )
        addSlide(
            AppIntroFragment.newInstance(
                title = getString(R.string.slide4_title),
                description = getString(R.string.slide4_text),
                imageDrawable = R.drawable.slide4,
                titleColor = resources.getColor(R.color.colorOnPrimary),
                descriptionColor = resources.getColor(R.color.colorOnPrimary),
                backgroundColor = resources.getColor(R.color.colorSecondary),
                titleTypefaceFontRes = R.font.roboto_slab_bold,
                descriptionTypefaceFontRes = R.font.roboto_light,
            )
        )
        addSlide(
            AppIntroFragment.newInstance(
                title = getString(R.string.slide5_title),
                description = getString(R.string.slide5_text),
                imageDrawable = R.drawable.slide5,
                titleColor = resources.getColor(R.color.colorOnPrimary),
                descriptionColor = resources.getColor(R.color.colorOnPrimary),
                backgroundColor = resources.getColor(R.color.colorSecondary),
                titleTypefaceFontRes = R.font.roboto_slab_bold,
                descriptionTypefaceFontRes = R.font.roboto_light,
            )
        )
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        PreferenceManager.setFirstLaunch(false)
        navigateLaunchPage()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        PreferenceManager.setFirstLaunch(false)
        navigateLaunchPage()
    }
}
