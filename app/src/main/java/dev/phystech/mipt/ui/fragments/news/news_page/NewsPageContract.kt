package dev.phystech.mipt.ui.fragments.news.news_page

import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.base.mvp.BasePresenter
import dev.phystech.mipt.base.mvp.BaseView
import dev.phystech.mipt.models.LegendModel
import dev.phystech.mipt.models.News

interface NewsPageContract {
    interface View: BaseView {
        fun <T>setAdapter(adapter: RecyclerView.Adapter<T>) where T: RecyclerView.ViewHolder
        fun showNewsDetail(model: News)
    }

    abstract class Presenter: BasePresenter<View>() {

    }
}