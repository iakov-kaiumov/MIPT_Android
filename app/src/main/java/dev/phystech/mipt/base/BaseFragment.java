package dev.phystech.mipt.base;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import dev.phystech.mipt.base.interfaces.BottomSheetController;
import dev.phystech.mipt.base.interfaces.FragmentNavigation;
import dev.phystech.mipt.base.mvp.BaseView;
import dev.phystech.mipt.utils.BackgroundDelegate;

public abstract class BaseFragment extends Fragment implements BaseView, FragmentNavigation.View {

    private BaseActivity mainActivity;
    private ProgressDialog progressDialog;
    private BackgroundDelegate backgroundDelegate;
    public FragmentNavigation.Presenter navigationPresenter;
    public BottomSheetController bottomSheetController;


    protected abstract void bindView(View view);


    protected BaseActivity getMainActivity() {
        return mainActivity;
    }

    protected FragmentNavigation.Presenter getNavigationPresenter() {
        return navigationPresenter;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(false);
    }

    @Override
    public void onResume() {
        if (backgroundDelegate != null) {
            backgroundDelegate.setStatusBarColor(getBackgroundColor());
        }
        super.onResume();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindView(view);
    }

    @Override
    public void attachNavigationPresenter(FragmentNavigation.Presenter presenter) {
        navigationPresenter = presenter;
    }

    public void attachBottomSheetContoller(BottomSheetController controller) {
        bottomSheetController = controller;
    }


    @Override
    public void showProgress() {
        if (mainActivity != null) {
            mainActivity.showProgress();
        }

//        Context context = this.getContext();
//        if (context == null) return;
//
//        progressDialogShows += 1;
//
//        if (progressDialogShows == 1) {
////            hideProgress();
//            ProgressDialog pd = CommonUtils.showLoadingDialog(context);
//            if (pd == null) return;
//            progressDialog = pd;
//        }

    }

    @Override
    public void hideProgress() {
        if (mainActivity != null) mainActivity.hideProgress();
//        Context context = this.getContext();
//        if (context == null) return;
//
//        progressDialogShows -= 1;
//
//        if (progressDialogShows <= 0) {
//            if (progressDialog != null && progressDialog.isShowing()) {
//                progressDialog.cancel();
//            }
//        }

    }

    @Override
    public void showMessage(String message) {
        if (mainActivity != null) {
            mainActivity.showMessage(message);
        }
    }

    @Override
    public void showMessage(int resId) {
        if (getContext() != null) {
            showMessage(getString(resId));
        }
    }

    @Override
    public void onError(String message) {
        if (mainActivity != null) {
            mainActivity.onError(message);
        }
    }

    @Override
    public void onError(int resId) {
        if(getMainActivity() != null){
            onError(getString(resId));
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BaseActivity) {
            this.mainActivity = (BaseActivity) context;
        }
    }

    @Override
    public void onDetach() {
        mainActivity = null;
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        if (getMainActivity().isFinishing() && progressDialog != null) {
            progressDialog.dismiss();
        }
        super.onDestroy();
    }

    public void setBackgroundDelegate(BackgroundDelegate backgroundDelegate) {
        this.backgroundDelegate = backgroundDelegate;
    }

    public int getBackgroundColor() {
        return Color.parseColor("#FFFFFF");
    }

    public void hideKeyboard() {
        getMainActivity().hideKeyboard();
    }


}
