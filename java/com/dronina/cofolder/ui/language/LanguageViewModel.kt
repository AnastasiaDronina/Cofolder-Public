package com.dronina.cofolder.ui.language

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dronina.cofolder.CofolderApp
import com.dronina.cofolder.data.preferences.PreferenceManager
import com.dronina.cofolder.utils.other.*

class LanguageViewModel(private val context: Context) : ViewModel() {
    var view: LanguageFragment? = null
    val languages = MutableLiveData<ArrayList<String>>()

    init {
        val languages = ArrayList<String>()
        languages.add(AFRIKAANS_af)
        languages.add(ALBANIAN_sq)
        languages.add(AMHARIC_am)
        languages.add(ARABIC_ar)
        languages.add(ARMENIAN_hy)
        languages.add(AZERBAIJANI_az)
        languages.add(BASQUE_eu)
        languages.add(BELARUSIAN_be)
        languages.add(BENGALI_bn)
        languages.add(BOSNIAN_bs)
        languages.add(BULGARIAN_bg)
        languages.add(CATALAN_ca)
        languages.add(CEBUANO_ceb)
        languages.add(CHICHEWA_ny)
        languages.add(CHINESE_zh)
        languages.add(CORSICAN_co)
        languages.add(CROATIAN_hr)
        languages.add(CZECH_cs)
        languages.add(DANISH_da)
        languages.add(DUTCH_nl)
        languages.add(ENGLISH_en)
        languages.add(ESPERANTO_eo)
        languages.add(ESTONIAN_et)
        languages.add(FILIPINO_fil)
        languages.add(FINNISH_fi)
        languages.add(FRENCH_fr)
        languages.add(GALICIAN_gl)
        languages.add(GEORGIAN__ka)
        languages.add(GERMAN_da)
        languages.add(GREEK_el)
        languages.add(HAWAIIAN_haw)
        languages.add(HINDI_hi)
        languages.add(HUNGARIAN_hu)
        languages.add(INDONESIAN_in)
        languages.add(ITALIAN_it)
        languages.add(JAPANESE_ja)
        languages.add(KOREAN_ko)
        languages.add(LATVIAN_lv)
        languages.add(LITHUANIAN_lt)
        languages.add(MONGOLIAN_mn)
        languages.add(NEPALI_ne)
        languages.add(NORWEGIAN_no)
        languages.add(POLISH_pl)
        languages.add(PORTUGUESE_pt)
        languages.add(RUSSIAN_ru)
        languages.add(SERBIAN_sr)
        languages.add(SLOVAK_sk)
        languages.add(SLOVENIAN_sl)
        languages.add(SOMALI_so)
        languages.add(SPANISH_es)
        languages.add(SUNDANESE_su)
        languages.add(SWEDISH_sv)
        languages.add(TAJIK_tg)
        languages.add(TATAR_tt)
        languages.add(THAI_th)
        languages.add(TURKISH_tr)
        languages.add(TURKMEN_tk)
        languages.add(UKRAINIAN_uk)
        languages.add(UZBEK_uz)
        languages.add(VIETNAMESE_vi)
        this.languages.value = languages
    }

    fun changeLanguage(language: String?) {
        language?.let {
            PreferenceManager.setCurrentLanguage(language)
            CofolderApp.instance?.setNewLang()
            view?.recreateActivity()
        }

    }
}