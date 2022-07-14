package dev.phystech.mipt.base;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


import java.util.List;

import dev.phystech.mipt.base.mvp.BaseView;
import dev.phystech.mipt.base.utils.CommonUtils;


public abstract class BaseDialogFragment extends DialogFragment implements BaseView {

    private BaseActivity activity;
    private ProgressDialog progressDialog;

    public BaseActivity getMainActivity() {
        return activity;
    }

    protected abstract void bindView(View view);

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindView(view);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof BaseActivity) {
            this.activity = (BaseActivity) context;
        }
    }

    @Override
    public void showProgress() {
        if (activity != null) {
            hideProgress();
            progressDialog = CommonUtils.Companion.showLoadingDialog(getMainActivity());
        }
    }

    @Override
    public void hideProgress() {
        if (activity != null && progressDialog != null && progressDialog.isShowing()) {
            progressDialog.cancel();
        }
    }

    @Override
    public void showMessage(String message) {
        if (activity != null) {
            activity.showMessage(message);
        }
    }

    @Override
    public void showMessage(int resId) {
        showMessage(getString(resId));
    }

    @Override
    public void onError(String message) {
        if (activity != null) {
            activity.onError(message);
        }
    }

    @Override
    public void onError(int resId) {
        onError(getString(resId));
    }

    @Override
    public void onDetach() {
        activity = null;
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        if (activity != null && getMainActivity().isFinishing() && progressDialog != null) {
            progressDialog.dismiss();
        }
        super.onDestroy();
    }

    public void hideKeyboard() {
        if (activity != null)
            getMainActivity().hideKeyboard();
    }

    public static void dismissAllDialogs(FragmentManager manager) {
        List<Fragment> fragments = manager.getFragments();

        if (fragments == null)
            return;

        for (Fragment fragment : fragments) {
            if (fragment == null)
                continue;

            if (fragment instanceof DialogFragment) {
                DialogFragment dialogFragment = (DialogFragment) fragment;
                dialogFragment.dismissAllowingStateLoss();
            }

            FragmentManager childFragmentManager = fragment
                    .getChildFragmentManager();
            if (childFragmentManager != null)
                dismissAllDialogs(childFragmentManager);
        }
    }
}
