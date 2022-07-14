package dev.phystech.mipt.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.size
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.OnBoardingAdapter
import dev.phystech.mipt.adapters.SchedulersShortAdapter
import dev.phystech.mipt.repositories.Storage
import kotlin.math.max
import kotlin.math.min

class OnBoardingActivity: AppCompatActivity() {

    lateinit var viewPager: ViewPager2
    lateinit var tabLayout: TabLayout
    lateinit var tvSkip: TextView
    lateinit var tvNext: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_boarding)
        Storage.shared.setFirstRun(false)

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        tvSkip = findViewById(R.id.tvSkip)
        tvNext = findViewById(R.id.tvNext)

        viewPager.adapter = OnBoardingAdapter()
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->

        }.attach()

        tvSkip.setOnClickListener {
            closeScreen()
        }

        tvNext.setOnClickListener {
            if (viewPager.currentItem == 2) {
                closeScreen()
                return@setOnClickListener
            }

            viewPager.currentItem = min(viewPager.currentItem + 1, 2)
        }
    }

    private fun closeScreen() {
        val intent: Intent = Intent(this, SplashActivity::class.java)
        startActivity(intent)
        finish()
    }

}