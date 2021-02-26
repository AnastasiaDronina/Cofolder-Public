package com.dronina.cofolder.utils.extensions

import android.animation.Animator
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Animatable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.dronina.cofolder.CofolderApp
import com.dronina.cofolder.CofolderApp.Companion.context
import com.dronina.cofolder.R
import com.dronina.cofolder.data.model.entities.Image
import com.dronina.cofolder.data.model.entities.User
import com.dronina.cofolder.data.preferences.PreferenceManager
import com.dronina.cofolder.data.repository.UserRepository
import com.dronina.cofolder.data.room.CofolderDatabase
import com.dronina.cofolder.ui.intro.IntroActivity
import com.dronina.cofolder.ui.launch.LaunchActivity
import com.dronina.cofolder.ui.main.MainActivity
import com.dronina.cofolder.ui.register.RegisterActivity
import com.dronina.cofolder.utils.itemdecorations.GridSpaceItemDecoration
import com.dronina.cofolder.utils.itemdecorations.ImagesSpaceItemDecoration
import com.dronina.cofolder.utils.itemdecorations.VerticalSpaceItemDecoration
import com.dronina.cofolder.utils.other.*
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.drawee.controller.ControllerListener
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.image.ImageInfo
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun Activity.softKeyboardAboveContent() {
    window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
}

fun Activity.navigateRegisterPage() {
    startActivity(Intent(this, RegisterActivity::class.java))
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
}

fun Activity.navigateMainPage() {
    startActivity(Intent(this, MainActivity::class.java))
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
}

fun Activity.navigateIntroPage() {
    startActivity(Intent(this, IntroActivity::class.java))
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
}

fun Activity.navigateLoginPage() {
    context?.let { context ->
        CofolderDatabase.getDatabase(context)?.userDao()?.let { dao ->
            startActivityForResult(Intent(UserRepository(dao).signInIntent(this)), RC_SIGN_IN)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}

fun Activity.navigateLaunchPage() {
    startActivity(Intent(this, LaunchActivity::class.java))
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
}

fun Context.rateApp() {
    try {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + this.packageName)
            )
        )
    } catch (e: android.content.ActivityNotFoundException) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=" + this.packageName)
            )
        )
    }
}

fun View.animateViewAppearance(animate: Boolean) {
    if (animate) {
        YoYo.with(Techniques.BounceIn).duration(500).playOn(this)
    }
}

fun View.fadeInUp() {
    YoYo.with(Techniques.FadeInUp).duration(500).playOn(this)
}

fun View.setVisibility(visible: Boolean) {
    visibility = if (visible) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

fun RecyclerView.removeAllItemDecorations() {
    while (itemDecorationCount > 0) {
        removeItemDecorationAt(0)
    }
}

fun RecyclerView.setItemDecorations(decorations: Int) {
    removeAllItemDecorations()
    when (decorations) {
        VERTICAL -> addItemDecoration(VerticalSpaceItemDecoration(16))
        GRID -> addItemDecoration(GridSpaceItemDecoration(3, 16))
        IMAGES -> addItemDecoration(ImagesSpaceItemDecoration(3, 4))
    }
}

fun Fragment.navigatePublicProfile(user: User?) {
    user?.let {
        val userBundle = Bundle()
        userBundle.putParcelable(USER_BUNDLE, user)
        findNavController().navigate(R.id.action_addFriends_to_publicProfile, userBundle)
    }
}

fun SimpleDraweeView.setPicture(url: String?) {
    if (url == null || url.isEmpty()) {
        this.setImageDrawable(CofolderApp.context?.getDrawable(R.drawable.ic_person))
        return
    } else {
        this.setImageURI(Uri.parse(url))
    }
}

fun Activity.showKeyboard(editText: EditText) {
    editText.requestFocus()
    val inputMethodManager: InputMethodManager? =
        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    inputMethodManager?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
}

fun Context.showEditTextDialog(
    title: String,
    message: String,
    text: String,
    positiveOnClick: (String) -> Unit
) {
    val style: Int
    val color: Int
    when (whichTheme()) {
        DARK_THEME -> {
            style = R.style.AlertDialogCustomDark
            color = R.color.nightOnSurface
        }
        else -> {
            style = R.style.AlertDialogCustom
            color = R.color.colorOnSurface
        }
    }
    val builder = AlertDialog.Builder(this, style)
    val editText = EditText(this)
    editText.setTextColor(resources.getColor(color))
    editText.setText(text)
    builder.setTitle(title)
    builder.setMessage(message)

    builder.setPositiveButton(android.R.string.yes) { dialog, which ->
        positiveOnClick(editText.text.toString())
        dialog.dismiss()
    }
    builder.setNegativeButton(android.R.string.no) { dialog, which ->
        dialog.dismiss()
    }
    val dialog = builder.create()
    if (dialog.window != null) {
        dialog.setView(editText, 50, 0, 50, 0)
    }
    dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
    dialog.show()
}

fun Activity.showToolbarAndNavigation(show: Boolean) {
    findViewById<AppBarLayout>(R.id.app_bar_layout)?.setVisibility(show)
    findViewById<BottomNavigationView>(R.id.bottom_nav)?.setVisibility(show)
}

fun Activity.enterFullScreen() {
    window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    findViewById<CoordinatorLayout>(R.id.main_activity_layout).setPadding(0, 0, 0, 0)
}

fun Activity.exitFullScreen() {
    window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    findViewById<CoordinatorLayout>(R.id.main_activity_layout).setPadding(0, 0, 0, 0)
}

fun Activity.showCustomDialog(
    view: View? = null,
    title: String = "",
    positiveText: String = getString(android.R.string.yes),
    negativeText: String = getString(android.R.string.no),
    positiveOnClick: () -> Unit = {},
    negativeOnClick: () -> Unit = {},
) {
    val style = when (whichTheme()) {
        DARK_THEME -> R.style.AlertDialogCustomDark
        else -> R.style.AlertDialogCustom
    }
    val builder = AlertDialog.Builder(this, style)
    builder.setTitle(title)
    builder.setPositiveButton(positiveText) { dialog, which ->
        positiveOnClick()
        dialog.dismiss()
    }
    builder.setNegativeButton(negativeText) { dialog, which ->
        negativeOnClick()
        dialog.dismiss()
    }
    val dialog = builder.create()
    view?.let {
        dialog.window?.let {
            dialog.setView(view, 50, 35, 50, 0)
        }
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
    }
    dialog.show()
}


fun Context.showConfirmationDialog(
    message: String,
    positiveOnClick: () -> Unit = { },
    negativeOnClick: () -> Unit = { }
) {
    val style = when (whichTheme()) {
        DARK_THEME -> R.style.AlertDialogCustomDark
        else -> R.style.AlertDialogCustom
    }
    val builder = AlertDialog.Builder(this, style)
    builder.setTitle(getString(R.string.confirm))
    builder.setMessage(message)
    builder.setPositiveButton(android.R.string.yes) { dialog, which ->
        positiveOnClick()
        dialog.dismiss()
    }
    builder.setNegativeButton(android.R.string.no) { dialog, which ->
        negativeOnClick()
        dialog.dismiss()
    }
    val dialog = builder.create()
    dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
    dialog.show()
}

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun SimpleDraweeView.setImage(
    image: Image,
    onLoagingComplete: () -> Unit
) {
    try {
        val uri = Uri.parse(image.url)
        setImageURI(uri)
        transitionName = image.url

        val listener: ControllerListener<ImageInfo?> =
            object : BaseControllerListener<ImageInfo?>() {
                override fun onFinalImageSet(
                    id: String?,
                    imageInfo: ImageInfo?,
                    animatable: Animatable?
                ) {
                    super.onFinalImageSet(id, imageInfo, animatable)
                    onLoagingComplete()
                }
            }
        val controller = Fresco.newDraweeControllerBuilder()
        controller.setUri(uri)
        controller.oldController = this.controller
        controller.controllerListener = listener
        this.controller = controller.build()
    } catch (e: Exception) {
    }
}

fun Context.whichTheme(): Int? {
    return when (PreferenceManager.appTheme()) {
        DARK_THEME -> DARK_THEME
        LIGHT_THEME -> LIGHT_THEME
        DEFAULT_THEME -> {
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> DARK_THEME
                else -> LIGHT_THEME
            }
        }
        else -> LIGHT_THEME
    }
}

fun Activity.style() {
    when (whichTheme()) {
        DARK_THEME -> {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            setTheme(R.style.DarkTheme)
        }
        LIGHT_THEME -> {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            setTheme(R.style.LightTheme)
        }
    }
}

fun Activity.getThemeForDialog(): Int {
    return when (whichTheme()) {
        DARK_THEME -> R.style.AppBottomSheetDialogThemeDark
        else -> R.style.AppBottomSheetDialogThemeLight
    }
}

fun View.handleBottomSheet(
    editTextHint: String = "",
    stateExpanded: () -> Unit = { },
    stateHidden: () -> Unit = { }
): Pair<View, BottomSheetBehavior<*>> {
    val bottomSheet = findViewById<View>(R.id.bottom_sheet)
    val btnClose: ImageButton? = bottomSheet.findViewById(R.id.btn_close)
    val etName: EditText? = bottomSheet.findViewById(R.id.et_name)
    etName?.hint = editTextHint
    val bottomSheetBehavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
    bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_EXPANDED -> stateExpanded()
                BottomSheetBehavior.STATE_HIDDEN -> stateHidden()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    })
    btnClose?.setOnClickListener {
        bottomSheetBehavior.hideOrExpand()
    }
    return Pair(bottomSheet, bottomSheetBehavior)
}

fun BottomSheetBehavior<*>.hideOrExpand() {
    state = if (state == BottomSheetBehavior.STATE_EXPANDED) {
        BottomSheetBehavior.STATE_HIDDEN
    } else {
        BottomSheetBehavior.STATE_EXPANDED
    }
}

fun Activity.styleStatusBar() {
    if (NAV_BAR_COLOR == 0) {
        NAV_BAR_COLOR = window.statusBarColor
    }
    if (this.whichTheme() == DARK_THEME) {
        window.statusBarColor = ContextCompat.getColor(this, R.color.nightColorPrimary)
    } else {
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }
}

fun Activity.getStatusBarHeight(): Int {
    val resourceId =
        resources.getIdentifier("status_bar_height", "dimen", "android")
    return if (resourceId > 0) {
        resources.getDimensionPixelSize(resourceId)
    } else 0
}

fun Activity.getNavigationBarHeight(): Int {
    val resourceId: Int =
        resources.getIdentifier("navigation_bar_height", "dimen", "android")
    return if (resourceId > 0) {
        resources.getDimensionPixelSize(resourceId)
    } else 0
}

fun View.animateCircularReveal(
    activity: Activity,
    arguments: Bundle?,
    doAfterReveal: () -> Unit = {}
) {
    this.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onLayoutChange(
            view: View,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
            oldLeft: Int,
            oldTop: Int,
            oldRight: Int,
            oldBottom: Int
        ) {
            if (arguments != null) {
                view.removeOnLayoutChangeListener(this)
                val cx = arguments.getInt(X_POSITION)
                val cy = arguments.getInt(Y_POSITION)

                val displayMetrics = DisplayMetrics()
                activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
                val height = displayMetrics.heightPixels
                val width = displayMetrics.widthPixels
                val finalRadius =
                    Math.max(width, height) / 2 + Math.max(
                        width - cx,
                        height - cy
                    ).toFloat()
                val anim: Animator =
                    ViewAnimationUtils.createCircularReveal(view, cx, cy, 0f, finalRadius)
                anim.duration = 500
                anim.start()
            }
        }
    })
    GlobalScope.launch {
        delay(500)
        doAfterReveal()
    }
}

fun RecyclerView.addSwipes(
    context: Context,
    enabled: Boolean = true,
    onMove: (holder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) -> Unit,
    onSwiped: (holder: RecyclerView.ViewHolder) -> Unit,
    onChildDraw: (c: Canvas, holder: RecyclerView.ViewHolder, dX: Float) -> Unit = { c, holder, dX ->
        context.onChildDraw(canvas = c, viewHolder = holder, dimentionX = dX)
    }
) {
    val itemTouchHelper = ItemTouchHelper(
        object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or
                    ItemTouchHelper.START or ItemTouchHelper.END, ItemTouchHelper.LEFT
        ) {
            override fun isItemViewSwipeEnabled(): Boolean {
                return enabled
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
            ): Boolean {
                onMove(viewHolder, target)
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                onSwiped(viewHolder)
            }

            override fun onChildDraw(
                c: Canvas,
                rv: RecyclerView,
                holder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                state: Int,
                isActive: Boolean
            ) {
                onChildDraw(c, holder, dX)
                super.onChildDraw(c, rv, holder, dX, dY, state, isActive)
            }
        })
    itemTouchHelper.attachToRecyclerView(this)
}

fun View.paint(color: Int) {
    when (color) {
        NO_COLOR -> setBackgroundResource(R.drawable.ripple_white)
        GRAY -> setBackgroundResource(R.drawable.ripple_gray)
        BROWN -> setBackgroundResource(R.drawable.ripple_brown)
        ORANGE -> setBackgroundResource(R.drawable.ripple_orange)
        YELLOW -> setBackgroundResource(R.drawable.ripple_yellow)
        GREEN -> setBackgroundResource(R.drawable.ripple_green)
        BLUE -> setBackgroundResource(R.drawable.ripple_blue)
        PURPLE -> setBackgroundResource(R.drawable.ripple_purple)
        PINK -> setBackgroundResource(R.drawable.ripple_pink)
        RED -> setBackgroundResource(R.drawable.ripple_red)
    }
}

fun ImageView.paintFolder(color: Int) {
    when (color) {
        NO_COLOR -> setImageResource(R.drawable.folder)
        GRAY -> setImageResource(R.drawable.gray_folder)
        BROWN -> setImageResource(R.drawable.brown_folder)
        ORANGE -> setImageResource(R.drawable.orange_folder)
        YELLOW -> setImageResource(R.drawable.yellow_folder)
        GREEN -> setImageResource(R.drawable.green_folder)
        BLUE -> setImageResource(R.drawable.blue_folder)
        PURPLE -> setImageResource(R.drawable.purple_folder)
        PINK -> setImageResource(R.drawable.pink_folder)
        RED -> setImageResource(R.drawable.red_folder)
    }
}

fun View.hideKeyboardFrom(context: Context) {
    val inputMethodManager: InputMethodManager =
        context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(this.windowToken, 0)
}

fun Fragment.showEditPageMenu(
    showAsCreator: Boolean,
    deleteOrLeaveOnClick: () -> Unit,
    shareOnClick: () -> Unit
) {
    val layout: View = layoutInflater.inflate(R.layout.edit_page_menu, null)
    layout.showAsCreator(requireActivity(), showAsCreator)
    val popup = PopupWindow(
        layout,
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT,
        true
    )
    popup.elevation = 20f
    popup.animationStyle = R.style.PopupAnimation
    popup.showAtLocation(layout, Gravity.TOP or Gravity.RIGHT, 20, 20)

    layout.findViewById<Button>(R.id.btn_leave_or_delete).setOnClickListener {
        deleteOrLeaveOnClick()
        popup.dismiss()

    }
    layout.findViewById<Button>(R.id.btn_share).setOnClickListener {
        shareOnClick()
        popup.dismiss()

    }
}

fun LayoutInflater.showListItemMenu(
    anchorView: View,
    editOnClick: () -> Unit,
    removeOnClick: () -> Unit
) {
    val layout = inflate(R.layout.list_item_menu, null)
    val popup = PopupWindow(
        layout,
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT,
        true
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        popup.overlapAnchor = true
    }
    popup.elevation = 20f
    popup.animationStyle = R.style.PopupAnimation
    popup.showAsDropDown(anchorView)

    layout.findViewById<Button>(R.id.btn_edit).setOnClickListener {
        editOnClick()
        popup.dismiss()
    }
    layout.findViewById<Button>(R.id.btn_remove).setOnClickListener {
        removeOnClick()
        popup.dismiss()
    }
}

fun Fragment.showMenuPopup(
    showAsGrid: Boolean,
    showAsGridOnClick: () -> Unit,
    sortOnClick: () -> Unit
) {
    val layout = layoutInflater.inflate(R.layout.popup_toolbar_menu, null)
    layout.showAsGrid(showAsGrid)
    val popup = PopupWindow(
        layout,
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT,
        true
    )
    popup.elevation = 20f
    popup.animationStyle = R.style.PopupAnimation
    popup.showAtLocation(layout, Gravity.TOP or Gravity.RIGHT, 20, 20)

    layout.findViewById<Button>(R.id.btn_view_grid).setOnClickListener {
        showAsGridOnClick()
        popup.dismiss()
    }
    layout.findViewById<Button>(R.id.btn_sort).setOnClickListener {
        sortOnClick()
        popup.dismiss()
    }
}

fun View.showAsGrid(showAsGrid: Boolean) {
    val button = findViewById<Button>(R.id.btn_view_grid)
    if (showAsGrid) {
        button.text = CofolderApp.context?.resources?.getString(R.string.view_list)
        button.setCompoundDrawablesWithIntrinsicBounds(
            CofolderApp.context?.resources?.getDrawable(R.drawable.ic_view_list),
            null,
            null,
            null
        )
    } else {
        button.text = CofolderApp.context?.resources?.getString(R.string.view_grid)
        button.setCompoundDrawablesWithIntrinsicBounds(
            CofolderApp.context?.resources?.getDrawable(R.drawable.ic_view_grid),
            null,
            null,
            null
        )
    }
}


fun View.showAsCreator(activity: Activity, showAsCreator: Boolean) {
    val button = findViewById<Button>(R.id.btn_leave_or_delete)
    val delete = CofolderApp.context?.resources?.getDrawable(R.drawable.ic_delete)
    val leave = CofolderApp.context?.resources?.getDrawable(R.drawable.ic_leave)
    if (activity.whichTheme() == LIGHT_THEME) {
        delete?.setTint(resources.getColor(R.color.colorOnSurface))
        leave?.setTint(resources.getColor(R.color.colorOnSurface))
    } else {
        delete?.setTint(resources.getColor(R.color.nightOnSurface))
        leave?.setTint(resources.getColor(R.color.nightOnSurface))
    }
    if (showAsCreator) {
        button.text = CofolderApp.context?.resources?.getString(R.string.delete)
        button.setCompoundDrawablesWithIntrinsicBounds(
            delete,
            null,
            null,
            null
        )
    } else {
        button.text = CofolderApp.context?.resources?.getString(R.string.leave)
        button.setCompoundDrawablesWithIntrinsicBounds(
            leave,
            null,
            null,
            null
        )
    }
}

fun Button.setShow(show: Boolean) {
    val expandMore = CofolderApp.context?.resources?.getDrawable(R.drawable.ic_expand)
    val expandLess = CofolderApp.context?.resources?.getDrawable(R.drawable.ic_expand_less)
    val color = when (context.applicationContext.whichTheme()) {
        DARK_THEME -> R.color.nightOnSurface
        else -> R.color.colorOnSurface
    }
    expandMore?.setTint(resources.getColor(color))
    expandLess?.setTint(resources.getColor(color))
    if (show) {
        setCompoundDrawablesWithIntrinsicBounds(null, null, expandMore, null)
    } else {
        setCompoundDrawablesWithIntrinsicBounds(null, null, expandLess, null)
    }
}

fun Fragment.doNothingOnBackPressed() {
    val callback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        }
    requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
}

fun Context.setSearchView(
    menu: Menu,
    viewId: Int,
    textChangeListener: SearchView.OnQueryTextListener
) {
    val searchView = SearchView((this as MainActivity).supportActionBar?.themedContext ?: context)
    menu.findItem(viewId).actionView = searchView

    val searchPlate = searchView.findViewById<View>(
        searchView.context.resources.getIdentifier(
            "android:id/search_plate",
            null,
            null
        )
    )
    val searchEditText = searchView.findViewById<View>(
        searchView.context.resources.getIdentifier(
            "android:id/search_src_text",
            null,
            null
        )
    ) as EditText

    searchView.maxWidth = Integer.MAX_VALUE
    searchPlate.setBackgroundColor(Color.TRANSPARENT)
    searchEditText.hint = context?.resources?.getString(R.string.search)
    val textColor = when (whichTheme()) {
        DARK_THEME -> resources.getColor(R.color.nightOnSurface)
        else -> resources.getColor(R.color.colorOnSurface)
    }
    searchEditText.setTextColor(textColor)
    searchEditText.setHintTextColor(textColor)
    searchView.setOnQueryTextListener(textChangeListener)
}

fun SearchView.style() {
    val searchPlate = findViewById<View>(
        context.resources.getIdentifier("android:id/search_plate", null, null)
    )
    val searchEditText = findViewById<View>(
        context.resources.getIdentifier("android:id/search_src_text", null, null)
    ) as EditText

    this.maxWidth = Integer.MAX_VALUE
    searchPlate.setBackgroundColor(Color.TRANSPARENT)
    searchEditText.hint = context?.resources?.getString(R.string.search)
}

fun LinearLayout.drawBoarder(color: Int) {
    val ivNoColor = findViewById<ImageView>(R.id.iv_no_color)
    val ivGray = findViewById<ImageView>(R.id.iv_gray)
    val ivBrown = findViewById<ImageView>(R.id.iv_brown)
    val ivOrange = findViewById<ImageView>(R.id.iv_orange)
    val ivYellow = findViewById<ImageView>(R.id.iv_yellow)
    val ivGreen = findViewById<ImageView>(R.id.iv_green)
    val ivBlue = findViewById<ImageView>(R.id.iv_blue)
    val ivPurple = findViewById<ImageView>(R.id.iv_purple)
    val ivPink = findViewById<ImageView>(R.id.iv_pink)
    val ivRed = findViewById<ImageView>(R.id.iv_red)

    val boarder = context.resources.getDrawable(R.drawable.color_checked_bg)
    if (context.whichTheme() == DARK_THEME) {
        boarder.setTint(resources.getColor(R.color.nightOnSurface))
    } else {
        boarder.setTint(resources.getColor((R.color.colorOnSurface)))
    }

    when (color) {
        NO_COLOR -> ivNoColor.setImageDrawable(boarder)
        GRAY -> ivGray.setImageDrawable(boarder)
        BROWN -> ivBrown.setImageDrawable(boarder)
        ORANGE -> ivOrange.setImageDrawable(boarder)
        YELLOW -> ivYellow.setImageDrawable(boarder)
        GREEN -> ivGreen.setImageDrawable(boarder)
        BLUE -> ivBlue.setImageDrawable(boarder)
        PURPLE -> ivPurple.setImageDrawable(boarder)
        PINK -> ivPink.setImageDrawable(boarder)
        RED -> ivRed.setImageDrawable(boarder)
    }
}

fun Context.onChildDraw(
    isCreator: Boolean? = true,
    canvas: Canvas,
    viewHolder: RecyclerView.ViewHolder,
    dimentionX: Float
) {
    isCreator?.let {
        val icon = if (isCreator) {
            resources.getDrawable(R.drawable.ic_delete)
        } else {
            resources.getDrawable(R.drawable.ic_leave)
        }
        icon.setTint(resources.getColor(R.color.colorSurface))
        val background = GradientDrawable()
        background.setColor(resources.getColor(R.color.colorError))
        background.cornerRadius = 20f
        val cornerOffset = 20
        val margin = (viewHolder.itemView.height - icon.intrinsicHeight) / 2
        val top = viewHolder.itemView.top + (viewHolder.itemView.height - icon.intrinsicHeight) / 2
        val bottom = top + icon.intrinsicHeight
        if (dimentionX < 0) {
            val iconLeft = viewHolder.itemView.right - margin - icon.intrinsicWidth
            val iconRight = viewHolder.itemView.right - margin
            icon.setBounds(iconLeft, top, iconRight, bottom)
            background.setBounds(
                viewHolder.itemView.right + dimentionX.toInt() - cornerOffset,
                viewHolder.itemView.top, viewHolder.itemView.right, viewHolder.itemView.bottom
            )
        } else {
            background.setBounds(0, 0, 0, 0)
        }
        background.draw(canvas)
        icon.draw(canvas)
    }
}

fun View.colorTag(): Int {
    return when (id) {
        R.id.iv_no_color -> NO_COLOR
        R.id.iv_gray -> GRAY
        R.id.iv_brown -> BROWN
        R.id.iv_orange -> ORANGE
        R.id.iv_yellow -> YELLOW
        R.id.iv_green -> GREEN
        R.id.iv_blue -> BLUE
        R.id.iv_purple -> PURPLE
        R.id.iv_pink -> PINK
        R.id.iv_red -> RED
        else -> NO_COLOR
    }
}

fun LinearLayout.removeBoarder(color: Int) {
    val ivNoColor = findViewById<ImageView>(R.id.iv_no_color)
    val ivGray = findViewById<ImageView>(R.id.iv_gray)
    val ivBrown = findViewById<ImageView>(R.id.iv_brown)
    val ivOrange = findViewById<ImageView>(R.id.iv_orange)
    val ivYellow = findViewById<ImageView>(R.id.iv_yellow)
    val ivGreen = findViewById<ImageView>(R.id.iv_green)
    val ivBlue = findViewById<ImageView>(R.id.iv_blue)
    val ivPurple = findViewById<ImageView>(R.id.iv_purple)
    val ivPink = findViewById<ImageView>(R.id.iv_pink)
    val ivRed = findViewById<ImageView>(R.id.iv_red)

    when (color) {
        NO_COLOR -> ivNoColor.setImageDrawable(null)
        GRAY -> ivGray.setImageDrawable(null)
        BROWN -> ivBrown.setImageDrawable(null)
        ORANGE -> ivOrange.setImageDrawable(null)
        YELLOW -> ivYellow.setImageDrawable(null)
        GREEN -> ivGreen.setImageDrawable(null)
        BLUE -> ivBlue.setImageDrawable(null)
        PURPLE -> ivPurple.setImageDrawable(null)
        PINK -> ivPink.setImageDrawable(null)
        RED -> ivRed.setImageDrawable(null)
    }
}

fun LinearLayout.setColorListener(listener: View.OnClickListener) {
    val ivNoColor = findViewById<ImageView>(R.id.iv_no_color)
    val ivGray = findViewById<ImageView>(R.id.iv_gray)
    val ivBrown = findViewById<ImageView>(R.id.iv_brown)
    val ivOrange = findViewById<ImageView>(R.id.iv_orange)
    val ivYellow = findViewById<ImageView>(R.id.iv_yellow)
    val ivGreen = findViewById<ImageView>(R.id.iv_green)
    val ivBlue = findViewById<ImageView>(R.id.iv_blue)
    val ivPurple = findViewById<ImageView>(R.id.iv_purple)
    val ivPink = findViewById<ImageView>(R.id.iv_pink)
    val ivRed = findViewById<ImageView>(R.id.iv_red)

    ivNoColor.setOnClickListener(listener)
    ivGray.setOnClickListener(listener)
    ivBrown.setOnClickListener(listener)
    ivOrange.setOnClickListener(listener)
    ivYellow.setOnClickListener(listener)
    ivGreen.setOnClickListener(listener)
    ivBlue.setOnClickListener(listener)
    ivPurple.setOnClickListener(listener)
    ivPink.setOnClickListener(listener)
    ivRed.setOnClickListener(listener)
}