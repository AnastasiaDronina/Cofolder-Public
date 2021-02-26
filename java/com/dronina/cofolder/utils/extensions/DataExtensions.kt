package com.dronina.cofolder.utils.extensions

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import com.dronina.cofolder.CofolderApp
import com.dronina.cofolder.utils.other.*

inline fun <T : Any> ifLet(vararg elements: T?, closure: (List<T>) -> Unit) {
    if (elements.all { it != null }) {
        closure(elements.filterNotNull())
    }
}

fun connected(): Boolean {
    return CofolderApp.instance?.let { instance ->
        instance.isConnectedToNetwork()
    } ?: run { false }
}


fun Int.array(): String {
    return when (this) {
        NOTE -> NOTES_ARRAY
        LIST -> LISTS_ARRAY
        FOLDER -> FOLDERS_ARRAY
        USER -> FRIENDS_ARRAY
        else -> ""
    }
}

fun Int.collection(): String {
    return when (this) {
        USER -> USERS_COLLECTION
        NOTE -> NOTES_COLLECTION
        LIST -> LISTS_COLLECTION
        FOLDER -> FOLDERS_COLLECTION
        REQUEST -> REQUESTS_COLLECTION
        ISSUE -> ISSUES_COLLECTION
        else -> ""
    }
}

fun currentTime(): com.google.firebase.Timestamp {
    return com.google.firebase.Timestamp.now()
}

fun Uri.rotate(contentResolver: ContentResolver): Bitmap? {
    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, this)
    var rotate = 0
    val inputStream = contentResolver.openInputStream(this)
    val exif = inputStream?.let { stream ->
        ExifInterface(stream)
    }
    val orientation = exif?.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
        ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
        ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
    }
    val matrix = Matrix()
    matrix.postRotate(rotate.toFloat())
    return Bitmap.createBitmap(
        bitmap, 0, 0, bitmap.width,
        bitmap.height, matrix, true
    )
}