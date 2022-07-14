package dev.phystech.mipt.base.interfaces;

public interface ApiResponseInterface {

    void onResponse(Object object);
    void onMessage(String msg);
    void onFailure();
}
