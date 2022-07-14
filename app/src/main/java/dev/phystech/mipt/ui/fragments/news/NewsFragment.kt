package dev.phystech.mipt.ui.fragments.news

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.size
import androidx.viewpager.widget.ViewPager
import com.ogaclejapan.smarttablayout.SmartTabLayout
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems
import dev.phystech.mipt.R
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.models.News
import dev.phystech.mipt.repositories.UserRepository
import dev.phystech.mipt.ui.fragments.news.news_page.NewsPageFragment
import dev.phystech.mipt.ui.fragments.study.chair_filter.ChairEventFilterFragment
import dev.phystech.mipt.ui.fragments.study.chair_filter.ChairFilterFragment
import dev.phystech.mipt.utils.UserRole


class NewsFragment : BaseFragment() {

    lateinit var tabLayout: SmartTabLayout
    lateinit var viewPager: ViewPager
    lateinit var pages: FragmentPagerItems
    lateinit var viewPagerTab: SmartTabLayout
    lateinit var ivSandwitch: ImageView

    var adapter: FragmentPagerItemAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("NewsFragment", "onCreateView")
        return inflater.inflate(R.layout.fragment_news, container, false)
    }

    override fun bindView(view: View?) {
        if (view == null) return
        viewPager = view.findViewById(R.id.viewPager) as ViewPager
        viewPagerTab = view.findViewById(R.id.tabLayoutCategories)
        ivSandwitch = view.findViewById(R.id.ivSandwitch)

        ivSandwitch.setOnClickListener {
            val fragment = if (lastPage == 0) {
                ChairFilterFragment.newInstance()
            } else {
                ChairEventFilterFragment.newInstance()
            }

            navigationPresenter.pushFragment(fragment, true)
        }
    }

    override fun onStart() {
        Log.d("NewsFragment", "onStart")
        super.onStart()
        setViewPager()
    }

    fun setViewPager() {
        val miptBundle = Bundle()
        miptBundle.putString(NewsPageFragment.TITLE_KEY, "MIPT")

        val newsBundle = Bundle()
        newsBundle.putInt("type", News.Type.News.index)
        newsBundle.putString(NewsPageFragment.TITLE_KEY, getString(R.string.tabbar_news))

        val eventBundle = Bundle()
        eventBundle.putInt("type", News.Type.Events.index)
        eventBundle.putString(NewsPageFragment.TITLE_KEY, getString(R.string.tabbar_events))

        val chairBundle = Bundle()
        chairBundle.putInt("type", News.Type.Chair.index)
        chairBundle.putString(NewsPageFragment.TITLE_KEY, getString(R.string.tabbar_inner))


        val fragmentCreator = FragmentPagerItems.with(mainActivity)
//                .add("MIPT", NewsPageFragment::class.java, miptBundle)
            .add(getString(R.string.tabbar_news), NewsPageFragment::class.java, newsBundle)
            .add(getString(R.string.tabbar_events), NewsPageFragment::class.java, eventBundle)

        if (UserRepository.shared.userRole != UserRole.Guest) {
//            fragmentCreator.add(getString(R.string.tabbar_inner), NewsPageFragment::class.java, chairBundle)    // бывшая "Кафедры"
        }

        val adapter = FragmentPagerItemAdapter(
            childFragmentManager, fragmentCreator.create()
        )

        viewPager.adapter = adapter
        viewPagerTab.setViewPager(viewPager)
        viewPager.addOnPageChangeListener(pagerListener)

        val lp = LinearLayout.LayoutParams(viewPagerTab.getTabAt(0).layoutParams)
        lp.width = 0
        lp.weight = 1f


        for (i in 0 until adapter.count) {
            viewPagerTab.getTabAt(i).layoutParams = lp
            viewPagerTab.getTabAt(i).setBackgroundColor(Color.TRANSPARENT)
        }

    }

    private var lastPage = 0
    private val pagerListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
        override fun onPageSelected(position: Int) { lastPage = position }
        override fun onPageScrollStateChanged(state: Int) {}

    }
}
