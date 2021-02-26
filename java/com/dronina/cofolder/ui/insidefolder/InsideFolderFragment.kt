package com.dronina.cofolder.ui.insidefolder

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.SharedElementCallback
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.transition.TransitionInflater
import com.dronina.cofolder.R
import com.dronina.cofolder.data.model.entities.FolderFile
import com.dronina.cofolder.data.model.entities.Image
import com.dronina.cofolder.data.services.LoadImagesService
import com.dronina.cofolder.databinding.FragmentInsideFolderBinding
import com.dronina.cofolder.ui.base.BaseFragment
import com.dronina.cofolder.ui.base.BaseViewModelFactory
import com.dronina.cofolder.ui.images.ImagesViewModel
import com.dronina.cofolder.ui.main.MainViewModel
import com.dronina.cofolder.utils.extensions.*
import com.dronina.cofolder.utils.other.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class InsideFolderFragment : BaseFragment(), ImagesRvAdapter.ViewHolderListener,
    View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private lateinit var binding: FragmentInsideFolderBinding
    private lateinit var viewModel: InsideFolderViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var imagesViewModel: ImagesViewModel
    private var recyclerViewAdapter: ImagesRvAdapter? = null
    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    private var colorPalette: LinearLayout? = null
    private var etFolderName: EditText? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_inside_folder, container, false)
        mainViewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(MainViewModel::class.java)
        setHasOptionsMenu(true)
        initViewModels()
        setupSharedElements()
        return binding.root
    }

    override fun initViewModels() {
        viewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(InsideFolderViewModel::class.java)
        imagesViewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(ImagesViewModel::class.java)
        mainViewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(MainViewModel::class.java)
        viewModel.view = this
        viewModel.lifecycleOwner = this
    }

    override fun onViewCreated() {
        requireActivity().styleStatusBar()
        setPageTitle()
        showProgress()
        setBottomSheet()
        activity?.exitFullScreen()
        recyclerViewAdapter = null
        viewModel.onViewCreated(arguments)
    }

    override fun addListeners() {
        colorPalette?.setColorListener(this)
        binding.swipeToRefresh.setOnRefreshListener(this)
        etFolderName?.doOnTextChanged { text, start, before, count ->
            (requireActivity() as AppCompatActivity).supportActionBar?.title = text.toString()
            viewModel.nameUpdated(text.toString())
            imagesViewModel.nameUpdated(
                viewModel.currentFolder.value,
                text.toString(),
                viewModel.time()
            )
        }
        binding.btnAddImages.setOnClickListener {
            openFileChooser()
        }
        viewModel.userIsEditor.value?.let { userIdEditor ->
            if (userIdEditor) {
                requireActivity().findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
                    .setOnClickListener { bottomSheetBehavior?.hideOrExpand() }
            }

        }
    }

    override fun addObservers() {
        viewModel.images.observe(viewLifecycleOwner, {
            refresh()
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onViewCreated()
        addListeners()
        addObservers()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.edit_folder_menu, menu)
        mainViewModel.imagesSpace.value?.let { space ->
            if (space >= MAX_IMAGE_SPACE) {
                menu.removeItem(R.id.action_add_image)
            }
        }
        viewModel.userIsEditor.value?.let { userIsEditor ->
            if (userIsEditor) return
            menu.removeItem(R.id.settings)
            menu.removeItem(R.id.action_add_image)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_image -> openFileChooser()
            R.id.settings -> bottomSheetBehavior?.hideOrExpand()
            R.id.sub_menu -> {
                if (viewModel.canClick) {
                    viewModel.canClick = false
                    showEditPageMenu(
                        viewModel.showAsCreator(),
                        { viewModel.deleteOrLeave() },
                        { viewModel.shareOnClick() }
                    )
                    GlobalScope.launch {
                        delay(500)
                        viewModel.canClick = true
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().styleStatusBar()
    }

    fun navigateSharePage(bundle: Bundle) {
        findNavController().navigate(R.id.action_insideFolder_to_share, bundle)
    }

    fun removeFolderFromList() {
        viewModel.currentFolder.value?.let { currentFolder ->
            val folders = mainViewModel.currentFolders.value
            (folders as ArrayList<FolderFile>).remove(currentFolder)
            mainViewModel.currentFolders.value = folders
        }
        navigateUp()
    }

    fun bindService() {
        requireActivity().bindService(
            Intent(requireContext(), LoadImagesService::class.java),
            viewModel.serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    fun unbindService() {
        requireActivity().unbindService(viewModel.serviceConnection)
    }

    fun updateMainModel() {
        mainViewModel.onCreate()
    }

    fun setColor(color: Int) {
        imagesViewModel.colorUpdated(viewModel.currentFolder.value, color, viewModel.time())
    }

    override fun onRefresh() {
        viewModel.syncContents()
        binding.swipeToRefresh.isRefreshing = false
    }

    override fun refresh() {
        viewModel.images.value?.let { images ->
            hideProgress()
            binding.btnAddImages.setVisibility(images.size == 0)
            recyclerViewAdapter?.let {
                recyclerViewAdapter?.submitList(images)
            } ?: run {
                setRecyclerView()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mainViewModel.imagesSpace.value?.let { space ->
            viewModel.onActivityResult(
                requireActivity().contentResolver,
                requestCode,
                resultCode,
                data,
                space
            )
        } ?: run {
            viewModel.onActivityResult(
                requireActivity().contentResolver,
                requestCode,
                resultCode,
                data,
                0
            )
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun serverError() {
        requireActivity().toast(getString(R.string.server_error))
    }

    fun setName(name: String) {
        try {
            viewModel.userIsEditor.value?.let { userIsEditor ->
                if (userIsEditor) {
                    etFolderName?.setText(name)
                }
            }
            (activity as AppCompatActivity).supportActionBar?.title = name
        } catch (e: Exception) {
        }
    }

    fun removeBoarder(color: Int) {
        colorPalette?.removeBoarder(color)
    }

    fun drawBoarder(color: Int) {
        colorPalette?.drawBoarder(color)
    }

    fun showConfirmationDialog(message: String) {
        requireContext().showConfirmationDialog(
            message = message,
            positiveOnClick = { viewModel.confirmed() })
    }

    override fun showProgress() {
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        binding.progressBar.visibility = View.GONE
    }

    override fun onLoadCompleted(image: Image, position: Int) {
        startPostponedEnterTransition()
    }

    override fun onShortClick(
        image: Image,
        position: Int,
        extras: FragmentNavigator.Extras
    ) {
        viewModel.scrollPosition.value = position
        binding.bottomSheet.visibility = View.GONE
        val bundle = Bundle()
        bundle.putString(FOLDER_ID_BUNDLE, viewModel.currentFolder.value?.id)
        bundle.putParcelableArrayList(IMAGE_OBJECTS, viewModel.images.value as ArrayList<Image>)
        bundle.putInt(IMAGE_POSITION, position)

        findNavController().navigate(R.id.action_insideFolder_to_imageDetails, bundle, null, extras)
    }

    override fun onLongClick(image: Image, position: Int): Boolean {
        requireContext().showConfirmationDialog(
            message = getString(R.string.delete_image),
            positiveOnClick = { viewModel.deleteImage(image, position) }
        )
        return true
    }

    override fun onClick(view: View?) {
        view?.let {
            val color = view.colorTag()
            viewModel.colorUpdated(color)
            imagesViewModel.colorUpdated(
                viewModel.currentFolder.value,
                color,
                viewModel.time()
            )
        }
    }

    private fun setupSharedElements() {
        viewModel.navigateWithoutTransition.value?.let { withoutTransition ->
            if (withoutTransition) {
                viewModel.navigateWithoutTransition.value = false
            } else {
                prepareTransitions()
                postponeEnterTransition()
            }
        }
    }

    private fun openFileChooser() {
        viewModel.canAddImages.value?.let { canAddImages ->
            if (!canAddImages) return
            viewModel.canAddImages.value = false
            val intent = Intent()
            intent.type = IMAGE_DIR
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, getString(R.string.select_images)),
                RC_PICK_IMAGES
            )
        }
    }

    private fun prepareTransitions() {
        exitTransition =
            TransitionInflater.from(context).inflateTransition(R.transition.grid_exit_transition)
        setExitSharedElementCallback(
            object : SharedElementCallback() {
                override fun onMapSharedElements(
                    names: List<String?>,
                    elements: MutableMap<String?, View?>
                ) {
                    viewModel.scrollPosition.value?.let { scrollPosition ->
                        val selectedViewHolder =
                            binding.rvImages.findViewHolderForAdapterPosition(scrollPosition)
                                ?: return
                        elements[names[0]] = selectedViewHolder.itemView.findViewById(R.id.iv_image)
                    }
                }
            })
    }

    private fun setRecyclerView() {
        binding.rvImages.setHasFixedSize(true)
        binding.rvImages.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvImages.setItemDecorations(IMAGES)
        recyclerViewAdapter = ImagesRvAdapter(requireContext(), this)
        binding.rvImages.adapter = recyclerViewAdapter
        viewModel.images.value?.let { images ->
            recyclerViewAdapter?.submitList(images)
        }
        viewModel.scrollPosition.value?.let { scrollPosition ->
            if (scrollPosition > 0) {
                binding.rvImages.scrollToPosition(scrollPosition)
            }
        }
    }

    private fun setPageTitle() {
        arguments?.let {
            (requireActivity() as AppCompatActivity).supportActionBar?.title =
                arguments?.getParcelable<FolderFile>(DATA_BUNDLE)?.name
        } ?: run {
            (requireActivity() as AppCompatActivity).supportActionBar?.title =
                getString(R.string.create_folder)
        }
    }

    private fun setBottomSheet() {
        val bottomSheet =
            requireView().handleBottomSheet(editTextHint = requireContext().getString(R.string.folder_name))
        bottomSheetBehavior = bottomSheet.second
        colorPalette = bottomSheet.first.findViewById(R.id.linear_layout)
        etFolderName = bottomSheet.first.findViewById(R.id.et_name)
        binding.bottomSheet.visibility = View.VISIBLE
    }
}
