package com.dronina.cofolder.ui.imagedelails

import android.graphics.drawable.Animatable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.dronina.cofolder.R
import com.dronina.cofolder.data.model.entities.Image
import com.dronina.cofolder.ui.base.BaseViewModelFactory
import com.dronina.cofolder.utils.other.IMAGES_BUNDLE
import com.dronina.cofolder.utils.other.RetainingDataSourceSupplier
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.imagepipeline.image.ImageInfo
import me.relex.photodraweeview.PhotoDraweeView

class ImageFragment : Fragment() {
    private var imageDetailsViewModel: ImageDetailsViewModel? = null
    private var imageView: PhotoDraweeView? = null
    private var image: Image? = null

    companion object {
        fun newInstance(image: Image): ImageFragment {
            val args = Bundle()
            args.putParcelable(IMAGES_BUNDLE, image)
            val fragment = ImageFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        bundle: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_image, container, false)
        view.setup()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModels()
        addListeners()
    }

    private fun initViewModels() {
        imageDetailsViewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(ImageDetailsViewModel::class.java)
    }

    private fun addListeners() {
        imageView?.setOnLongClickListener {
            imageDetailsViewModel?.imageViewClicked()
            true
        }
    }

    private fun View.setup() {
        image = arguments?.getParcelable(IMAGES_BUNDLE)
        imageView = findViewById(R.id.iv_image)
        imageView?.transitionName = image?.url
        try {
            val controller = Fresco.newDraweeControllerBuilder()
            controller.setUri(Uri.parse(image?.url))
            controller.oldController = imageView?.controller
            controller.controllerListener = object : BaseControllerListener<ImageInfo?>() {
                override fun onFinalImageSet(
                    id: String,
                    info: ImageInfo?,
                    animatable: Animatable?
                ) {
                    super.onFinalImageSet(id, info, animatable)
                    if (info == null || imageView == null) return
                    parentFragment?.startPostponedEnterTransition()
                    imageView?.update(info.width, info.height)
                    controller.dataSourceSupplier =
                        RetainingDataSourceSupplier()
                }
            }
            imageView?.controller = controller.build()
        } catch (e: Exception) {
        }
    }
}
