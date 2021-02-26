package com.dronina.cofolder.utils.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_UP
import androidx.viewpager.widget.ViewPager


class ImagesViewPager : ViewPager {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    private var isDisallowIntercept = true

    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        isDisallowIntercept = disallowIntercept
        super.requestDisallowInterceptTouchEvent(disallowIntercept)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return if (ev.pointerCount > 1 && isDisallowIntercept) {
            requestDisallowInterceptTouchEvent(false)
            val handled = super.dispatchTouchEvent(ev)
            requestDisallowInterceptTouchEvent(true)
            handled
        } else {
            super.dispatchTouchEvent(ev)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return if (ev.pointerCount > 1) {
            false
        } else try {
            val result = super.onInterceptTouchEvent(ev)
            when (ev.action) {
                ACTION_UP -> {
                    //swipe
                }
            }
            result
        } catch (ex: IllegalArgumentException) {
            false
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return try {
            super.onTouchEvent(ev)
        } catch (ex: IllegalArgumentException) {
            false
        }
    }
}