package dev.phystech.mipt.base;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import dev.phystech.mipt.base.mvp.BaseView;
import dev.phystech.mipt.base.utils.CommonUtils;


public abstract class BaseActivity extends AppCompatActivity implements BaseView {

    private ProgressDialog progressDialog;

    private BroadcastReceiver broadcastReceiver;


    //  BASE VIEW
    @Override
    public void showProgress() {
        hideProgress();
        progressDialog = CommonUtils.Companion.showLoadingDialog(this);
    }

    @Override
    public void hideProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.cancel();
        }
    }

    @Override
    public void showMessage(String message) {
        if (message != null) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(message)
                    .setPositiveButton("Ok", (dialog, which) -> {}).show();
        } else {
//            Toasty.error(this, getString(R.string.some_error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showMessage(int resId) {
        showMessage(getString(resId));
    }

    @Override
    public void onError(String message) {
        if (message != null) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(message)
                    .setPositiveButton("Ok", (dialog, which) -> {}).show();
        } else {
//            Toasty.error(this, getString(R.string.some_error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onError(int resId) {
        onError(getString(resId));
    }

    public void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onDestroy() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        super.onDestroy();
    }
}
