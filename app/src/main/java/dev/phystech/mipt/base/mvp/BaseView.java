package dev.phystech.mipt.base.mvp;

public interface BaseView {

    void showProgress();

    void hideProgress();

    void hideKeyboard();

    void showMessage(String message);

    void showMessage(int resId);

    void onError(String message);

    void onError(int resId);
}
