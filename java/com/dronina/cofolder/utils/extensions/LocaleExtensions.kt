package com.dronina.cofolder.utils.extensions

import android.app.Application
import android.content.res.Configuration
import android.content.res.Resources
import android.view.ContextThemeWrapper
import com.dronina.cofolder.data.preferences.PreferenceManager
import com.dronina.cofolder.utils.other.*
import java.util.*

object LocaleUtils {
    private var locale: Locale? = null


    fun setLocale(locale: Locale?) {
        locale?.let { newLocale ->
            this.locale = newLocale
            Locale.setDefault(newLocale)
        }
    }

    fun updateConfig(wrapper: ContextThemeWrapper) {
        val configuration = Configuration()
        configuration.setLocale(locale)
        wrapper.applyOverrideConfiguration(configuration)
    }

    fun updateConfig(app: Application, configuration: Configuration?) {
        if (locale != null) {
            val config = Configuration(configuration)
            config.locale = locale
            val res: Resources = app.baseContext.resources
            res.updateConfiguration(config, res.displayMetrics)
        }
    }
}

fun getLocale(): String {
    return when (PreferenceManager.currentLanguage()) {
        AFRIKAANS_af -> "af"
        ALBANIAN_sq -> "sq"
        AMHARIC_am -> "am"
        ARABIC_ar -> "ar"
        ARMENIAN_hy -> "hy"
        AZERBAIJANI_az -> "az"
        BASQUE_eu -> "eu"
        BELARUSIAN_be -> "be"
        BENGALI_bn -> "bn"
        BOSNIAN_bs -> "bs"
        BULGARIAN_bg -> "bg"
        CATALAN_ca -> "ca"
        CEBUANO_ceb -> "ceb"
        CHICHEWA_ny -> "ny"
        CHINESE_zh -> "zh"
        CORSICAN_co -> "co"
        CROATIAN_hr -> "hr"
        CZECH_cs -> "cs"
        DANISH_da -> "da"
        DUTCH_nl -> "nl"
        ENGLISH_en -> "en"
        ESPERANTO_eo -> "eo"
        ESTONIAN_et -> "et"
        FILIPINO_fil -> "fil"
        FINNISH_fi -> "fi"
        FRENCH_fr -> "fr"
        GALICIAN_gl -> "gl"
        GEORGIAN__ka -> "ka"
        GERMAN_da -> "da"
        GREEK_el -> "el"
        HAWAIIAN_haw -> "haw"
        HINDI_hi -> "hi"
        HUNGARIAN_hu -> "hu"
        INDONESIAN_in -> "in"
        ITALIAN_it -> "it"
        JAPANESE_ja -> "ja"
        KOREAN_ko -> "ko"
        LATVIAN_lv -> "lv"
        LITHUANIAN_lt -> "lt"
        MONGOLIAN_mn -> "mn"
        NEPALI_ne -> "ne"
        NORWEGIAN_no -> "no"
        POLISH_pl -> "pl"
        PORTUGUESE_pt -> "pt"
        RUSSIAN_ru -> "ru"
        SERBIAN_sr -> "sr"
        SLOVAK_sk -> "sk"
        SLOVENIAN_sl -> "sl"
        SOMALI_so -> "so"
        SPANISH_es -> "es"
        SUNDANESE_su -> "su"
        SWEDISH_sv -> "sv"
        TAJIK_tg -> "tg"
        TATAR_tt -> "tt"
        THAI_th -> "th"
        TURKISH_tr -> "tr"
        TURKMEN_tk -> "tk"
        UKRAINIAN_uk -> "uk"
        UZBEK_uz -> "uz"
        VIETNAMESE_vi -> "vi"
        else -> ""
    }
}