package dev.phystech.mipt.adapters

import androidx.fragment.app.FragmentManager
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems

class StudyViewPagerAdapter(private val fragmentManager: FragmentManager,
                            private val fragmentPagerItems: FragmentPagerItems
) :FragmentPagerItemAdapter(fragmentManager, fragmentPagerItems){

    init {

    }


}