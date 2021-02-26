package com.dronina.cofolder.ui.imagedelails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.SharedElementCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.dronina.cofolder.R
import com.dronina.cofolder.ui.base.BaseViewModelFactory
import com.dronina.cofolder.ui.insidefolder.InsideFolderViewModel
import com.dronina.cofolder.utils.other.IMAGE_POSITION
import com.dronina.cofolder.utils.customviews.ImagesViewPager
import com.dronina.cofolder.utils.extensions.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class ImageDetailsFragment : BottomSheetDialogFragment() {
    private lateinit var viewModel: ImageDetailsViewModel
    private lateinit var insideFolderViewModel: InsideFolderViewModel
    private var rootView: View? = null
    private var viewPager: ViewPager? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.showToolbarAndNavigation(false)
        initViewModels()
        setupViews(inflater, container)
        setupSharedElements(savedInstanceState)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.enterFullScreen()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        insideFolderViewModel.scrollPosition.value = viewPager?.currentItem
        activity?.styleStatusBar()
        activity?.showToolbarAndNavigation(true)
    }

    fun imageSaved() {
        requireActivity().toast(requireContext().getString(R.string.image_saved))
    }

    fun imageDeleted() {
        insideFolderViewModel.navigateWithoutTransition.value = true
        findNavController().navigateUp()
        viewPager?.adapter?.notifyDataSetChanged()

    }

    fun openBottomSheet() {
        viewModel.position.value?.let { position ->
            val bundle = Bundle()
            bundle.putInt(IMAGE_POSITION, position)
            findNavController().navigate(R.id.action_imageDetails_to_bottomSheetImage, bundle)
        }
    }

    private fun initViewModels() {
        viewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(ImageDetailsViewModel::class.java)
        insideFolderViewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(InsideFolderViewModel::class.java)
        viewModel.onCreateView(arguments)
        viewModel.view = this
    }

    private fun setupViews(inflater: LayoutInflater, container: ViewGroup?) {
        rootView = inflater.inflate(R.layout.fragment_image_details, container, false) as View
        viewPager = rootView?.findViewById<ImagesViewPager>(R.id.view_pager)
        viewModel.images.value?.let { images ->
            viewPager?.adapter = ViewPagerAdapter(this, images)
        }
        insideFolderViewModel.scrollPosition.value?.let { position ->
            viewPager?.currentItem = position
        }
        viewPager?.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
            override fun onPageSelected(pos: Int) {
                insideFolderViewModel.scrollPosition.value = pos
                viewModel.position.value = pos
            }
        })
    }

    private fun setupSharedElements(savedInstanceState: Bundle?) {
        prepareSharedElementTransition()
        if (savedInstanceState == null) {
            postponeEnterTransition()
        }
    }

    private fun prepareSharedElementTransition() {
        val transition = TransitionInflater.from(context)
            .inflateTransition(R.transition.image_shared_element_transition)
        sharedElementEnterTransition = transition
        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: List<String>,
                elements: MutableMap<String, View>
            ) {
                viewPager?.let { pager ->
                    insideFolderViewModel.scrollPosition.value?.let { position ->
                        val currentFragment =
                            viewPager?.adapter?.instantiateItem(pager, position) as Fragment
                        val view = currentFragment.view ?: return
                        elements[names[0]] = view.findViewById(R.id.iv_image)
                    }
                }
            }
        })
    }
}
