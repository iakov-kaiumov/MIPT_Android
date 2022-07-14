package dev.phystech.mipt.ui.utils

import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import kotlin.math.abs

class ExpandAnimation(private val mView: View) : Animation(),
    Animation.AnimationListener {
    private var mInitialHeight = 0
    private var mTargetHeight = 0

    private var mAnimating = false

    init {
        mAnimating = false
        setAnimationListener(this)
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
        // we need this verification because applyTransformation gets called one or two
        // times before onAnimationStart and the same happens after onAnimationEnd
        if (mAnimating) {
            mView.layoutParams.height =
                ((mTargetHeight - mInitialHeight) * interpolatedTime).toInt() + mInitialHeight
        }
        mView.requestLayout()
    }

    override fun willChangeBounds(): Boolean {
        return true
    }

    override fun onAnimationStart(animation: Animation?) {
//        mView.setVisibility(View.GONE)
        mInitialHeight = mView.getHeight()
        mView.measure(
            View.MeasureSpec.makeMeasureSpec(mView.getWidth(), View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        mTargetHeight = mView.getMeasuredHeight()
//        mView.setVisibility(View.VISIBLE)
        mAnimating = true
    }

    override fun onAnimationEnd(animation: Animation?) {
        mAnimating = false
        mView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT
        mView.requestLayout()
    }

    override fun onAnimationRepeat(animation: Animation?) {}
}