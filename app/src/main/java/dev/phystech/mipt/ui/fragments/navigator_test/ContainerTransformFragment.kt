package dev.phystech.mipt.ui.fragments.tabs.navigator_test

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.transition.TransitionManager
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialElevationScale
import dev.phystech.mipt.R
import dev.phystech.mipt.base.BaseFragment

@Deprecated("")
class ContainerTransformFragment: BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frament_container_transform, container, false)
    }


    override fun bindView(view: View?) {
        if (view == null) return

        val btn: RelativeLayout = view.findViewById(R.id.btn)
        val win: RelativeLayout = view.findViewById(R.id.window)


        btn.setOnClickListener {
            val transform = MaterialContainerTransform().apply {
                startView = btn

                exitTransition = MaterialElevationScale(false).apply {
                    duration = 200
                }


                reenterTransition = MaterialElevationScale(true).apply {
                    duration = 200
                }

                endView = win
                duration = 200

                addTarget(btn)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    scrimColor = resources.getColor(android.R.color.transparent, null)
                } else {
                    scrimColor = resources.getColor(android.R.color.transparent)
                }
            }

            TransitionManager.beginDelayedTransition(view as ConstraintLayout, transform)
            btn.visibility = View.GONE
            win.visibility = View.VISIBLE
        }

    }

}