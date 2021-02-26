package com.dronina.cofolder.utils.extensions

import android.view.View
import android.widget.Button
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.dronina.cofolder.R

class ContactExtensions(view: View?) {
    private val btnSendRequest: Button? = view?.findViewById(R.id.btn_send_request)
    private val btnAccept: Button? = view?.findViewById(R.id.btn_accept)
    private val btnRemoveFriend: Button? = view?.findViewById(R.id.btn_remove_friend)
    private val btnCancelRequest: Button? = view?.findViewById(R.id.btn_cancel_request)

    fun showAcceptButton() {
        btnCancelRequest?.visibility = View.GONE
        btnRemoveFriend?.visibility = View.GONE
        YoYo.with(Techniques.ZoomIn).duration(400).playOn(btnAccept)
        btnAccept?.visibility = View.VISIBLE
        btnSendRequest?.visibility = View.GONE
    }

    fun showCancelRequestButton() {
        YoYo.with(Techniques.ZoomIn).duration(400).playOn(btnCancelRequest)
        btnCancelRequest?.visibility = View.VISIBLE
        btnRemoveFriend?.visibility = View.GONE
        btnAccept?.visibility = View.GONE
        btnSendRequest?.visibility = View.GONE
    }

    fun showSendRequestButton() {
        btnCancelRequest?.visibility = View.GONE
        btnRemoveFriend?.visibility = View.GONE
        btnAccept?.visibility = View.GONE
        YoYo.with(Techniques.ZoomIn).duration(400).playOn(btnSendRequest)
        btnSendRequest?.visibility = View.VISIBLE
    }

    fun showRemoveFriendButton() {
        btnCancelRequest?.visibility = View.GONE
        YoYo.with(Techniques.ZoomIn).duration(400).playOn(btnRemoveFriend)
        btnRemoveFriend?.visibility = View.VISIBLE
        btnAccept?.visibility = View.GONE
        btnSendRequest?.visibility = View.GONE
    }
}