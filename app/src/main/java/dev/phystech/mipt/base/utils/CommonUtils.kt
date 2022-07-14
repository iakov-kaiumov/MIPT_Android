package dev.phystech.mipt.base.utils

import dev.phystech.mipt.base.dialogs.ProgressDialog;
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.TypedValue
import dev.phystech.mipt.R
import dev.phystech.mipt.ui.activities.main.MainActivity

typealias AndroidProgressDialog = android.app.ProgressDialog

class CommonUtils {

    companion object {
        fun showLoadingDialog(context: Context?): AndroidProgressDialog? {
            if (context == null) return null

            val progressDialog = AndroidProgressDialog(context)
            progressDialog.show()
            if (progressDialog.window != null) {
                progressDialog.window!!
                    .setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
            progressDialog.setContentView(R.layout.progress_dialog)
            progressDialog.isIndeterminate = true
            progressDialog.setCancelable(false)
            progressDialog.setCanceledOnTouchOutside(false)
            return progressDialog
        }

        fun showProgressDialog(context: Context) {
            val dialog = ProgressDialog()
            dialog.show((context as MainActivity).supportFragmentManager, "dialog")
        }

        fun dpFromPx(context: Context, px: Float): Float {
            return px / context.resources.displayMetrics.density
        }

        fun pxFromDp(context: Context, dp: Float): Float {
            return dp * context.resources.displayMetrics.density
        }

        fun dpToPx(dp: Int): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp.toFloat(),
                Resources.getSystem().displayMetrics
            ).toInt()
        }
    }
}

fun String.Companion.empty(): String = ""

fun String.nullIfEmpty(): String? {
    if (this.isEmpty()) return null
    else return this
}

