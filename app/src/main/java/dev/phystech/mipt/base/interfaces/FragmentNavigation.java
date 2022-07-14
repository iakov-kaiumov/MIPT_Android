package dev.phystech.mipt.base.interfaces;

import dev.phystech.mipt.base.BaseFragment;

public interface FragmentNavigation {

    interface View {
        void attachNavigationPresenter(Presenter presenter);
    }

    interface Presenter {
        void pushFragment(BaseFragment fragment, boolean is_add);
        void add(BaseFragment fragment);
        void popFragment();
        void showStudy();
        void showNews();
        void showSupport();
        void setStatusBarTransparency(boolean enabled);
        void setBottomNavigationVisibility(boolean isVisible);
    }
}