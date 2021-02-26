package com.dronina.cofolder.ui.imagedelails

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.dronina.cofolder.data.model.entities.Image


class ViewPagerAdapter(
    fragment: Fragment,
    private val images: java.util.ArrayList<Image>
) :
    FragmentStatePagerAdapter(
        fragment.childFragmentManager,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {
    override fun getCount(): Int {
        return images.size
    }

    override fun getItem(position: Int): Fragment {
        return ImageFragment.newInstance(images[position])
    }
}