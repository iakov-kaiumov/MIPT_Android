package dev.phystech.mipt.base.mvp;


import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public abstract class BasePresenter<V extends BaseView> {

    private CompositeDisposable subscriptions = new CompositeDisposable();
    protected V view;

    public void subscribe(Disposable subscription) {
        subscriptions.add(subscription);
    }

    public void unsubscribe() {
        subscriptions.clear();
    }

    public void attach(V view) {
        this.view = view;
    }

    public void detach() {
        unsubscribe();
    }
}
