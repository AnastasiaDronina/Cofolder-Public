package com.dronina.cofolder.ui.privacypolicy

import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dronina.cofolder.R
import com.dronina.cofolder.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_privacy_policy.*

class PrivacyPolicyFragment : BaseFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
        return inflater.inflate(R.layout.fragment_privacy_policy, container, false)
    }

    override fun onViewCreated() {
        tv_sample_text.text = Html.fromHtml(getString(R.string.privacy_policy_text))
        tv_sample_text.movementMethod = LinkMovementMethod.getInstance()
    }
}
