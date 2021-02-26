package com.dronina.cofolder.ui.insidefolder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dronina.cofolder.R
import com.dronina.cofolder.data.model.entities.Image
import com.dronina.cofolder.utils.extensions.setImage
import com.facebook.drawee.view.SimpleDraweeView

class ImagesRvAdapter(
    private val context: Context,
    private val listener: ViewHolderListener
) : ListAdapter<Image, ImagesRvAdapter.ImageViewHolder>(ImageDiffCallback()) {

    interface ViewHolderListener {
        fun onLoadCompleted(image: Image, position: Int)
        fun onShortClick(
            image: Image,
            position: Int,
            extras: FragmentNavigator.Extras
        )

        fun onLongClick(image: Image, position: Int): Boolean
    }

    override fun submitList(list: MutableList<Image>?) {
        val updated = list?.distinctBy { it.id }
        super.submitList(updated)
    }

    class ImageDiffCallback : DiffUtil.ItemCallback<Image>() {
        override fun areItemsTheSame(oldItem: Image, newItem: Image): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Image, newItem: Image): Boolean =
            oldItem.url == newItem.url
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.image_rv_item, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = getItem(position)
        holder.imageView.setImage(
            image = image,
            onLoagingComplete = {
                listener.onLoadCompleted(image, position)
                holder.imageView.setOnClickListener {
                    ViewCompat.setTransitionName(holder.imageView as View, image.url)
                    val extras =
                        FragmentNavigatorExtras(holder.imageView as View to image.url)
                    listener.onShortClick(image, position, extras)
                }
                holder.imageView.setOnLongClickListener {
                    listener.onLongClick(image, position)
                    true
                }
            }
        )
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: SimpleDraweeView = itemView.findViewById(R.id.iv_image)
    }
}