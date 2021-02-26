package com.dronina.cofolder.ui.imagedelails

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dronina.cofolder.data.model.entities.Image
import com.dronina.cofolder.data.repository.ImagesRepository
import com.dronina.cofolder.data.room.CofolderDatabase
import com.dronina.cofolder.utils.other.FOLDER_ID_BUNDLE
import com.dronina.cofolder.utils.other.IMAGE_OBJECTS
import com.dronina.cofolder.utils.other.IMAGE_POSITION
import com.dronina.cofolder.utils.extensions.formatAsDate
import com.dronina.cofolder.utils.extensions.ifLet
import com.facebook.common.executors.CallerThreadExecutor
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber
import com.facebook.imagepipeline.image.CloseableImage
import com.facebook.imagepipeline.request.ImageRequestBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImageDetailsViewModel(context: Context) : ViewModel() {
    private var repo: ImagesRepository? = null
    var view: ImageDetailsFragment? = null
    var bottomSheet: ImageDialogFragment? = null
    val images = MutableLiveData<ArrayList<Image>>()
    val position = MutableLiveData<Int>()
    private val folderId = MutableLiveData<String>()

    init {
        CofolderDatabase.getDatabase(context)?.folderDao()?.let { dao ->
            repo = ImagesRepository(dao)
        }
    }

    fun imageViewClicked() {
        view?.openBottomSheet()
    }

    fun onCreateView(arguments: Bundle?) {
        arguments?.let {
            folderId.value = arguments.getString(FOLDER_ID_BUNDLE)
            position.value = arguments.getInt(IMAGE_POSITION)
            images.value = arguments.getParcelableArrayList(IMAGE_OBJECTS)
        }
    }

    fun bottomSheetCreated(arguments: Bundle?) {
        position.value = arguments?.getInt(IMAGE_POSITION)
        images.value?.let { images ->
            position.value?.let { position -> images[position] }
        }?.let { image ->
            val creator = bottomSheet?.getImageCreator(image.creator).toString()
            bottomSheet?.setCreator(creator)
            bottomSheet?.setDate(image.dateOfCreation.formatAsDate())
            repo?.let { repo ->
                if (repo.isImageCreator(image)) {
                    bottomSheet?.showDelete()
                } else bottomSheet?.hideDelete()
            }
        }
    }

    fun saveToGallery(resolver: ContentResolver) {
        images.value?.let { images ->
            position.value?.let { position -> images[position] }
        }?.let { image ->
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    val imageRequest =
                        ImageRequestBuilder.newBuilderWithSource(Uri.parse(image.url))
                            .setAutoRotateEnabled(true).build()
                    val dataSource = Fresco.getImagePipeline().fetchDecodedImage(imageRequest, this)
                    dataSource.subscribe(object : BaseBitmapDataSubscriber() {
                        override fun onNewResultImpl(bitmap: Bitmap?) {
                            if (dataSource.isFinished && bitmap != null) {
                                MediaStore.Images.Media.insertImage(
                                    resolver,
                                    Bitmap.createBitmap(bitmap),
                                    repo?.createUniqueId(),
                                    ""
                                )
                                dataSource.close()
                            }
                        }

                        override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage>>) {
                            dataSource.close()
                        }
                    }, CallerThreadExecutor.getInstance())
                }
                view?.imageSaved()
            }
        }
    }

    fun delete() {
        ifLet(repo, images.value, position.value) { (repo, images, position) ->
            val image = (images as ArrayList<Image>)[position as Int]
            this.images.value?.remove(image)
            view?.imageDeleted()
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    (repo as ImagesRepository).deleteImage(folderId.value, image.id)
                }
            }
        }
    }
}