package com.dronina.cofolder.utils.customviews

import android.content.Context
import android.graphics.Rect
import android.telephony.TelephonyManager
import android.util.AttributeSet
import com.dronina.cofolder.utils.other.CheckCountryCode
import com.dronina.cofolder.utils.other.CountryCode
import com.google.android.material.textfield.TextInputEditText


class PhoneEditText : TextInputEditText {
    private var dialCode: String? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        setCountryCode(this)
    }

    private fun setCountryCode(phoneText: TextInputEditText) {
        if (!CheckCountryCode().countryCodeExisting(phoneText)) {
            val tm =
                this.context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val countryCodeValue = tm.networkCountryIso
            dialCode = "+" + CountryCode().getDialCode(countryCodeValue)
            phoneText.text?.let { text ->
                when {
                    text.isEmpty() -> phoneText.setText(
                        "+" + CountryCode().getDialCode(
                            countryCodeValue
                        )
                    )
                    text[0] == '+' -> phoneText.setText(
                        "+" + CountryCode().getDialCode(
                            countryCodeValue
                        ) + text.toString().substring(2)
                    )
                    else -> phoneText.setText("+" + CountryCode().getDialCode(countryCodeValue) + text)
                }
                phoneText.setSelection(text.length)
            }
        }
    }
}