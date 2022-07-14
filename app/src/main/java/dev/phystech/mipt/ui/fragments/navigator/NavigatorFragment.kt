package dev.phystech.mipt.ui.fragments.navigator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.ogaclejapan.smarttablayout.SmartTabLayout
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems
import dev.phystech.mipt.LocationServiceWrap
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.CatalogAdapter
import dev.phystech.mipt.adapters.NavigationAdapter
import dev.phystech.mipt.adapters.SearchAdapter
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.models.ReferenceModel
import dev.phystech.mipt.ui.fragments.navigator.legends_page.LegendsPageFragment
import dev.phystech.mipt.ui.fragments.navigator.place_page.PlacesPageFragment
import dev.phystech.mipt.ui.fragments.navigator.qr_scanner.QRScannerFragment
import dev.phystech.mipt.ui.fragments.navigator.references.ReferencesPageFragment
import dev.phystech.mipt.utils.visibility


class NavigatorFragment : BaseFragment(), NavigatorContract.View {

    private val presenter: NavigatorPresenter = NavigatorPresenter()

    lateinit var tab: FrameLayout
    lateinit var viewPagerTab: SmartTabLayout
    lateinit var tvHeaderTitle: TextView
    lateinit var viewPager: ViewPager
    lateinit var pages: FragmentPagerItems
    lateinit var etSearch: EditText
//    lateinit var llSearchResult: NestedScrollView
    lateinit var ivCamera: ImageView

//    lateinit var recyclerPlaces: RecyclerView
//    lateinit var recyclerLivSearchIconegends: RecyclerView
//    lateinit var recyclerReferences: RecyclerView
    lateinit var recycler: RecyclerView

    lateinit var tvPlacesTitle: TextView
    lateinit var tvHistoriesTitle: TextView
    lateinit var tvContactsTitle: TextView

    lateinit var ivSearchIcon: ImageView

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_navigator, container, false)
    }

    override fun onStart() {
        super.onStart()
        setViewPager()
        presenter.attach(this)
    }

    override fun onStop() {
        presenter.detach()
        super.onStop()
    }


    override fun bindView(view: View?) {
        LocationServiceWrap.create(requireActivity())

        if (view == null) return
        viewPager = view.findViewById(R.id.viewPager) as ViewPager
        viewPagerTab = view.findViewById(R.id.tabLayoutCategories)
        tab = view.findViewById(R.id.tab)
        tvHeaderTitle = view.findViewById(R.id.tvHeaderTitle)
        etSearch = view.findViewById(R.id.etSearch)
//        llSearchResult = view.findViewById(R.id.searchResult)
        ivCamera = view.findViewById(R.id.ivCamera)

//        recyclerPlaces = view.findViewById(R.id.recyclerPlaces)
//        recyclerLegends = view.findViewById(R.id.recyclerLegends)
//        recyclerReferences = view.findViewById(R.id.recyclerReferences)

        recycler = view.findViewById(R.id.recycler)

//        tvPlacesTitle = view.findViewById(R.id.tvPlacesTitle)
//        tvHistoriesTitle = view.findViewById(R.id.tvHistoriesTitle)
//        tvContactsTitle = view.findViewById(R.id.tvContactsTitle)

        ivSearchIcon = view.findViewById(R.id.ivSearchIcon)

        etSearch.addTextChangedListener(textWatcher)

        ivSearchIcon.setOnClickListener { presenter.ivSearchIconClicked() }

        ivCamera.setOnClickListener {
            presenter.qrCodeClicked()
        }

    }


    fun setViewPager() {
        val adapter = FragmentPagerItemAdapter(
                childFragmentManager, FragmentPagerItems.with(mainActivity)
                .add(getString(R.string.tab_bar_places), PlacesPageFragment::class.java)
                .add(getString(R.string.tab_bar_legends), LegendsPageFragment::class.java)
                .add(getString(R.string.tab_bar_ref_book), ReferencesPageFragment::class.java)
                .create()
        )

        viewPager.adapter = adapter
        viewPagerTab.setViewPager(viewPager)

        val lp = viewPagerTab.getTabAt(0).layoutParams as LinearLayout.LayoutParams
        lp.width = 0
        lp.weight = 1f

        viewPagerTab.getTabAt(0).layoutParams = lp
        viewPagerTab.getTabAt(1).layoutParams = lp
        viewPagerTab.getTabAt(2).layoutParams = lp
    }



    override fun showSearch() {
        tab.visibility = false.visibility()
        viewPager.visibility = false.visibility()
        tvHeaderTitle.visibility = false.visibility()
        recycler.visibility = true.visibility()
        ivSearchIcon.setImageResource(R.drawable.ic_x)
        ivSearchIcon.visibility = false.visibility()    //true.visibility()

    }

    override fun showPager() {
        tab.visibility = true.visibility()
        viewPager.visibility = true.visibility()
        tvHeaderTitle.visibility = true.visibility()
        recycler.visibility = false.visibility()
        ivSearchIcon.setImageResource(R.drawable.ic_microphone)
        ivSearchIcon.visibility = false.visibility()
    }

    override fun showQRScanner() {
        navigationPresenter.pushFragment(QRScannerFragment(), true)
    }

    override fun setSearchPlaceAdapter(adapter: SearchAdapter) {
//        recyclerPlaces.adapter = adapter
    }

    override fun setSearchLegendAdapter(adapter: SearchAdapter) {
//        recyclerLegends.adapter = adapter
    }

    override fun setSearchReferenceAdapter(adapter: CatalogAdapter) {
//        recyclerReferences.adapter = adapter
    }

    override fun setSearch(withValue: String) {
        etSearch.setText(withValue)
    }

    override fun setHistoriesVisibility(isVisible: Boolean) {
        tvHistoriesTitle.visibility = isVisible.visibility()
    }

    override fun setPlacesVisibility(isVisible: Boolean) {
        tvPlacesTitle.visibility = isVisible.visibility()
    }

    override fun setContactsVisibility(isVisible: Boolean) {
        tvContactsTitle.visibility = isVisible.visibility()
    }

    override fun setAdapter(adapter: NavigationAdapter) {
        recycler.adapter = adapter
    }

    override fun runOnUI(method: () -> Unit) {
        activity?.runOnUiThread(method)
    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            presenter.updateSearch(s.toString())
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    override fun showContactPopup(forModel: ReferenceModel) {
        if (!forModel.containEmail && !forModel.containPhone) return

        val view = layoutInflater.inflate(R.layout.alert_contact, null)
        view.findViewById<TextView>(R.id.tvTitle).text = forModel.getAlertTitle()
        val call: Button = view.findViewById(R.id.btnCall)
        val copy: Button = view.findViewById(R.id.btnCopy)
        val email: Button = view.findViewById(R.id.btnEmail)
        val cancel: Button = view.findViewById(R.id.btnCancel)

        call.visibility = forModel.containPhone.visibility()
        copy.visibility = forModel.containPhone.visibility()
        email.visibility = forModel.containEmail.visibility()

        context?.let { context ->
            val alert = AlertDialog.Builder(context)
                .setView(view)
                .create()

            call.setOnClickListener {
                val callIntent = Intent(Intent.ACTION_VIEW, Uri.parse("tel:${forModel.personalPhoneNumber}"))
                startActivity(callIntent)
                alert.cancel()
            }

            copy.setOnClickListener {
                val clipboardManager =
                    ContextCompat.getSystemService(context, ClipboardManager::class.java)
                clipboardManager?.setPrimaryClip(ClipData.newPlainText(forModel.personalPhoneNumber, forModel.personalPhoneNumber))
                alert.cancel()
            }

            email.setOnClickListener {
                val callIntent = Intent(Intent.ACTION_VIEW, Uri.parse("email:${forModel.email}"))
                startActivity(callIntent)
                alert.cancel()
            }

            cancel.setOnClickListener {
                alert.cancel()
            }


            alert.show()
        }

    }

    override fun showFragment(fragment: BaseFragment) {
        navigationPresenter.pushFragment(fragment, true)
    }
}