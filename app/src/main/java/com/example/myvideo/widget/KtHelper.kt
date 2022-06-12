@file:JvmName("KtHelper")

package com.example.myvideo.widget

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import com.example.myvideo.R
import com.lzf.easyfloat.EasyFloat
import com.lzf.easyfloat.anim.DefaultAnimator
import com.lzf.easyfloat.enums.ShowPattern
import com.lzf.easyfloat.enums.SidePattern
import com.lzf.easyfloat.interfaces.OnFloatCallbacks
import com.lzf.easyfloat.utils.DragUtils
import com.lzf.easyfloat.widget.BaseSwitchView
import java.lang.Integer.max

fun registerActivityFloat(
    context: Context,
    tag: String,
    builder: OnFloatCallbacks
) {
    val dm = context.resources.displayMetrics
    val screenWidth = dm.widthPixels
    val screenHeight = dm.heightPixels
    EasyFloat.with(context)
        .setTag(tag)
        .setShowPattern(ShowPattern.CURRENT_ACTIVITY)
        .setMatchParent(widthMatch = true, heightMatch = false)
//        .setSidePattern(SidePattern.RESULT_HORIZONTAL)
//        .setLocation(10, screenHeight/2)
        .setGravity(Gravity.CENTER_HORIZONTAL, 0, dm.heightPixels/2)
        .setAnimator(DefaultAnimator())
        .setLayout(R.layout.float_subtitle) {
            val content = it.findViewById<RelativeLayout>(R.id.rlContent)
            val params = content.layoutParams as FrameLayout.LayoutParams
            it.findViewById<ScaleImage>(R.id.ivScale).onScaledListener =
                object : ScaleImage.OnScaledListener {
                    override fun onScaled(x: Float, y: Float, event: MotionEvent) {
                        params.width = max(params.width + x.toInt(), 400)
                        params.height = max(params.height + y.toInt(), 300)
                        // 更新xml根布局的大小
//                            content.layoutParams = params
                        // 更新悬浮窗的大小，可以避免在其他应用横屏时，宽度受限
                        EasyFloat.updateFloat(tag, width = params.width, height = params.height)
                    }
                }

            it.findViewById<ImageView>(R.id.ivClose).setOnClickListener {
                EasyFloat.hide(tag)
            }
        }.registerCallbacks(builder).show()
}