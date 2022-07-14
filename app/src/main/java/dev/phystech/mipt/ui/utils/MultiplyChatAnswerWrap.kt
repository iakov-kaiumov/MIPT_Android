package dev.phystech.mipt.ui.utils

import android.graphics.Color
import android.os.Build
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import dev.phystech.mipt.R
import dev.phystech.mipt.utils.visibility

class MultiplyChatAnswerWrap(val parens: View) {

    val page1: Page1 = Page1()
    val page2: Page2 = Page2()


    fun showPage1() {
        val transform = MaterialContainerTransform().apply {
            startView = page2.root
            endView = page1.root

            addTarget(endView!!)
            setPathMotion(MaterialArcMotion())
            scrimColor = Color.TRANSPARENT
        }

        TransitionManager.beginDelayedTransition(parens as ConstraintLayout, transform)

        page1.root.visibility = true.visibility()
        page2.root.visibility = false.visibility()
    }

    fun showPage2(withContent: Pair<String, String>) {
        val transform = MaterialContainerTransform().apply {
            startView = page1.root
            endView = page2.root

            addTarget(endView!!)
            setPathMotion(MaterialArcMotion())
            scrimColor = Color.TRANSPARENT
        }

        TransitionManager.beginDelayedTransition(parens as ConstraintLayout, transform)


        page1.root.visibility = false.visibility()
        page2.root.visibility = true.visibility()

        page2.setContent(withContent)
    }



    inner class Page1 {
        val root: ConstraintLayout = parens.findViewById(R.id.clPage1Container)
        val recycler: RecyclerView = root.findViewById(R.id.recycler)
    }

    inner class Page2 {
        val root: ConstraintLayout = parens.findViewById(R.id.clPage2Container)

        val rlBack: RelativeLayout = root.findViewById(R.id.rlBack)
        val tvQuestion: TextView = root.findViewById(R.id.tvQuestion)
        val tvAnswer: TextView = root.findViewById(R.id.tvAnswer)

        init {
            rlBack.setOnClickListener {
                showPage1()
            }
        }

        fun setContent(model: Pair<String, String>) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tvQuestion.text = Html.fromHtml(model.first, Html.FROM_HTML_MODE_LEGACY)
                tvAnswer.text = model.second//Html.fromHtml(model.second, Html.FROM_HTML_MODE_LEGACY)
            } else {
                tvQuestion.text = Html.fromHtml(model.first)
                tvAnswer.text = model.second//Html.fromHtml(model.second)
            }

            tvQuestion.movementMethod = LinkMovementMethod()
            tvAnswer.movementMethod = LinkMovementMethod()
        }
    }

}