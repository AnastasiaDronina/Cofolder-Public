package com.dronina.cofolder.data.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.dronina.cofolder.CofolderApp
import com.dronina.cofolder.R
import com.dronina.cofolder.data.firebase.FirebaseSource
import com.dronina.cofolder.data.model.entities.Image
import com.dronina.cofolder.data.repository.ImagesRepository
import com.dronina.cofolder.data.room.CofolderDatabase
import com.dronina.cofolder.ui.main.MainActivity
import com.dronina.cofolder.utils.extensions.rotate
import com.dronina.cofolder.utils.other.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream


class LoadImagesService : Service() {
    private var repo: ImagesRepository? = CofolderApp.context?.let { context ->
        CofolderDatabase.getDatabase(context)?.folderDao()?.let { dao ->
            ImagesRepository(dao)
        }
    }
    private var notificationManager: NotificationManagerCompat? = null
    private var builder: NotificationCompat.Builder? = null
    private var imagesAmount = 0
    private var uploadedImagesCount = 0
    private lateinit var progresses: ArrayList<Double>

    private val binder: IBinder = MyBinder()

    val imageLoaded = MutableLiveData<Image>()
    val needsUnbind = MutableLiveData<Boolean>()

    inner class MyBinder : Binder() {
        val service: LoadImagesService
            get() = this@LoadImagesService
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    companion object {
        fun startService(
            context: Context,
            folderId: String,
            images: ArrayList<Uri>,
            imagesSpace: Int
        ) {
            val startIntent = Intent(context, LoadImagesService::class.java)
            startIntent.putExtra(IMAGES_LIST, images)
            startIntent.putExtra(FOLDER_ID_VAL, folderId)
            startIntent.putExtra(IMAGES_SPACE, imagesSpace)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, LoadImagesService::class.java)
            context.stopService(stopIntent)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val folderId = intent.getStringExtra(FOLDER_ID_VAL)
        var images = intent.getParcelableArrayListExtra<Uri>(IMAGES_LIST)
        val currentImagesSize = intent.getIntExtra(IMAGES_SPACE, 0)

        val spaceLeft = MAX_IMAGE_SPACE - currentImagesSize
        var trimmedImages = mutableListOf<Uri>()
        images?.let { images ->
            if (images.size > spaceLeft) {
                trimmedImages = trimmedImages.subList(0, spaceLeft)
            }
            trimmedImages.addAll(images)
        }
        images?.clear()
        images?.addAll(trimmedImages)

        val bytes = ArrayList<ByteArray>()
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                images?.forEach { image ->
                    val rotatedImage = image.rotate(contentResolver)
                    val baos = ByteArrayOutputStream()
                    rotatedImage?.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                    val data = baos.toByteArray()
                    bytes.add(data)
                }
                setValues(bytes)
                uploadImages(folderId, bytes)
            }
            startForeground(LOAD_IMAGES_SERVICE_ID, builder?.build())
        }
        setValues(bytes)
        uploadImages(folderId, bytes)
        startForeground(LOAD_IMAGES_SERVICE_ID, builder?.build())
        return START_NOT_STICKY
    }

    private fun setValues(images: ArrayList<ByteArray>?) {
        images?.size?.let { imagesSize ->
            imagesAmount = imagesSize
        }
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        notificationManager = NotificationManagerCompat.from(this)
        builder = NotificationCompat.Builder(this, LOAD_IMAGES_CHANNEL)
            .setContentTitle(getString(R.string.uploading_images))
            .setContentText("$uploadedImagesCount / $imagesAmount " + getString(R.string.images_uploaded))
            .setContentIntent(pendingIntent)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setSound(null)
            .setProgress(100, 0, false)

        builder?.build()?.let { notification ->
            notificationManager?.notify(LOAD_IMAGES_SERVICE_ID, notification)
            startForeground(LOAD_IMAGES_SERVICE_ID, builder?.build())
            startForeground(1, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                LOAD_IMAGES_CHANNEL, LOAD_IMAGES_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun uploadImages(folderId: String?, images: ArrayList<ByteArray>?) {
        if (images != null && folderId != null) {
            initProgress()

            for ((i, img: ByteArray) in images.withIndex()) {
                setProgress()

                val imageId = repo?.createUniqueImageId()
                val ref = imageId?.let { FirebaseSource.imageStorageRef(imageId, folderId) }

                ref?.putBytes(img)
                    ?.addOnProgressListener {
                        updateProgress(i, it)
                    }
                    ?.continueWithTask { task ->
                        if (task.isSuccessful) {
                            ref.downloadUrl
                        } else {
                            task.exception?.let { throw it }
                        }
                    }
                    ?.addOnCompleteListener {
                        val image = repo?.createBlankImage(imageId, it.result.toString())
                        if (image != null) {
                            if (it.isSuccessful) {
                                updateContentText()
                                saveImageToDatabase(
                                    image,
                                    folderId,
                                    imageIsLast(i)
                                )
                            } else {
                                stopSelf()
                            }
                        }
                    }
            }
        }
    }

    private fun imageIsLast(i: Int): Boolean {
        var imageIsLast = false
        if (i == imagesAmount - 1) {
            imageIsLast = true
        }
        return imageIsLast
    }

    private fun updateContentText() {
        uploadedImagesCount++
        builder?.setContentText("$uploadedImagesCount / $imagesAmount " + getString(R.string.images_uploaded))
        builder?.build()?.let { notification ->
            notificationManager?.notify(LOAD_IMAGES_SERVICE_ID, notification)
        }
    }

    private fun initProgress() {
        progresses = ArrayList()
    }

    private fun setProgress() {
        progresses.add(0.0)
    }

    private fun stopProgress() {
        builder?.setContentText(getString(R.string.loading_complete))?.setProgress(0, 0, false)
        builder?.build()?.let { notification ->
            notificationManager?.notify(LOAD_IMAGES_SERVICE_ID, notification)
        }

    }

    private fun updateProgress(i: Int, taskSnapshot: UploadTask.TaskSnapshot) {
        val percentage = 100.0 * 1 / imagesAmount
        progresses[i] =
            (percentage * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
        builder?.setProgress(100, progresses.sum().toInt(), false)
        builder?.build()?.let { notification ->
            notificationManager?.notify(LOAD_IMAGES_SERVICE_ID, notification)
        }
    }

    private fun saveImageToDatabase(image: Image, folderId: String, imageIsLast: Boolean) {
        FirebaseSource.firestoreRef(FOLDER, folderId)
            ?.update(IMAGES_ARRAY, FieldValue.arrayUnion(image.id))
            .addOnCompleteListener {
                FirebaseSource.imageDatabaseRef(image.id, folderId)?.set(image)
                    ?.addOnCompleteListener {
                        imageLoaded.value = image
                        if (imageIsLast) {
                            stopProgress()
                            needsUnbind.value = true
                            stopSelf()
                        }
                    }
            }
    }
}