package com.dronina.cofolder.utils.extensions

import android.content.ClipData
import android.content.Context
import android.os.Build
import android.text.ClipboardManager
import android.widget.TextView
import com.dronina.cofolder.data.model.other.ListItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

fun String.createNameFromText(asGrid: Boolean): String {
    val words = this.split(' ')
    val result = if (words.size > 4) {
        words[0] + " " + words[1] + " " + words[2] + " " + words[3]
    } else this
    val lines = result.split("\n")
    return lines[0].shortAsName(asGrid)

}

fun String.formatAsPhone(): String {
    var phoneNo = this

    if (phoneNo.startsWith("8")) {
        phoneNo = "+7" + phoneNo.substring(1)
    }
    if (!phoneNo.startsWith("+")) {
        phoneNo = "+" + phoneNo.substring(0)
    }
    phoneNo = Regex("[^0-9+]").replace(phoneNo, "")

    return phoneNo
}

fun com.google.firebase.Timestamp.formatAsDate(): String {
    return if (this.toDate().year == Date().year) {
        SimpleDateFormat(
            "dd MMM HH:mm",
            Locale.getDefault()
        ).format(
            this.toDate()
        )
    } else {
        SimpleDateFormat(
            "dd MMM yy HH:mm",
            Locale.getDefault()
        ).format(
            this.toDate()
        )
    }
}

fun String.short(): String {
    val cleanString: String = this.replace("\r", " ").replace("\n", " ")
    return if (cleanString.length > 60) {
        cleanString.substring(0, 60) + "..."
    } else {
        cleanString.trimMargin()
    }
}

fun String.shortAsName(asGrid: Boolean): String {
    var maxLength = 40
    if (asGrid) maxLength = 20

    return if (this.length > maxLength) {
        this.substring(0, maxLength) + "..."
    } else {
        this.trimMargin()
    }
}

fun List<ListItem>.formatAsText(): String {
    return joinToString(
        prefix = "",
        separator = ", ",
        postfix = "",
        limit = 4,
        truncated = "..."
    )
}


fun TextView.copyText() {
    val sdk = Build.VERSION.SDK_INT
    if (sdk < Build.VERSION_CODES.HONEYCOMB) {
        val clipboard =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        clipboard?.text = this.text.toString().trim()
    } else {
        val clipboard =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager?
        val clip = ClipData.newPlainText("", this.text.toString().trim())
        clipboard?.setPrimaryClip(clip)
    }
}

fun List<ListItem>.formatListItems(): String {
    val gson = Gson()
    val type: Type = object :
        TypeToken<ArrayList<ListItem?>?>() {}.type
    return gson.toJson(this, type)
}