package com.dronina.cofolder.ui.terms

import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dronina.cofolder.R
import com.dronina.cofolder.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_privacy_policy.*

class TermsFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
        return inflater.inflate(R.layout.fragment_terms, container, false)
    }

    override fun onViewCreated() {
        tv_sample_text.text = Html.fromHtml(getString(R.string.terms_text))
        tv_sample_text.movementMethod = LinkMovementMethod.getInstance()
    }
}
