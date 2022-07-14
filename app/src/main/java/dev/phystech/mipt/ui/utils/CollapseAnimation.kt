package dev.phystech.mipt.ui.utils

import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation

class CollapseAnimation(private val mView: View) : Animation(),
    Animation.AnimationListener {
    private var mInitialHeight = 0
    private var mAnimating = false
    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        // we need this verification because applyTransformation gets called one or two
        // times before onAnimationStart and the same happens after onAnimationEnd
        if (mAnimating) {
            mView.layoutParams.height = mInitialHeight - (mInitialHeight * interpolatedTime).toInt()
            mView.requestLayout()
        }
    }

    override fun willChangeBounds(): Boolean {
        return true
    }

    override fun onAnimationStart(animation: Animation) {
        mInitialHeight = mView.measuredHeight
        mAnimating = true
    }

    override fun onAnimationEnd(animation: Animation) {
        mAnimating = false
        mView.layoutParams.height = 1
        mView.requestLayout()
//        mView.visibility = View.GONE
    }

    override fun onAnimationRepeat(animation: Animation) {}

    init {
        setAnimationListener(this)
    }
}