package dev.phystech.mipt.ui.fragments.news.news_page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.R
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.models.News
import dev.phystech.mipt.ui.fragments.news.NewsDetailsFragment

class NewsPageFragment : BaseFragment(), NewsPageContract.View {

    lateinit var recyclerNews: RecyclerView

    private lateinit var presenter: NewsPagePresenter

    private var type: News.Type = News.Type.News
    private var title: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = arguments?.getString(TITLE_KEY, null)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        (arguments?.get("type") as? Int)?.let {
            type = News.Type.getByOrdinal(it) ?: type
        }

        presenter = NewsPagePresenter(type)

        return inflater.inflate(R.layout.fragment_news_page, container, false)
    }

    override fun onStart() {
        super.onStart()
        presenter.attach(this)
    }

    override fun onStop() {
        presenter.detach()
        super.onStop()
    }


    override fun bindView(view: View?) {
        if (view == null) return;

        recyclerNews = view.findViewById(R.id.recyclerNews)
    }


    override fun <T : RecyclerView.ViewHolder> setAdapter(adapter: RecyclerView.Adapter<T>) {
        recyclerNews.adapter = adapter
    }

    override fun showNewsDetail(model: News) {
        val fragment = NewsDetailsFragment(model).apply {
            arguments = bundleOf(
                NewsDetailsFragment.TITLE_KEY to title
            )
        }

        (parentFragment as? BaseFragment)?.navigationPresenter?.add(fragment)
    }

    companion object {
        const val TITLE_KEY = "title"
    }
}
