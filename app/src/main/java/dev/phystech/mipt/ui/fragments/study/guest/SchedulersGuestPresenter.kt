package dev.phystech.mipt.ui.fragments.study.guest

import dev.phystech.mipt.R
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import dev.phystech.mipt.adapters.SchedulersFilesAdapter
import dev.phystech.mipt.models.MeetItem
import dev.phystech.mipt.network.NetworkUtils
import dev.phystech.mipt.repositories.GuestFilesRepository
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SchedulersGuestPresenter: SchedulersGuestContract.Presenter(),
    SchedulersFilesAdapter.Delegate {

    private var files: ArrayList<MeetItem> = ArrayList()
    private val adapter = SchedulersFilesAdapter(files)

    init {
        adapter.delegate = this
    }

    override fun attach(view: SchedulersGuestContract.View?) {
        super.attach(view)
        view?.setAdapter(adapter)

        view?.showProgress()

        GuestFilesRepository.shared.load()
        GuestFilesRepository.shared.items.observeOn(AndroidSchedulers.mainThread()).subscribe ({
            val items = it
            val calendar = GregorianCalendar()
            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss") //.getDateTimeInstance()//

            files = ArrayList(items.filter { v -> v.endDate?.date != null && df.parse(v.endDate?.date!!) ?: Date() > calendar.time })

            adapter.items.clear()
            adapter.items.addAll(files)
            adapter.notifyDataSetChanged()
            view?.hideProgress()
        }, {
            view?.hideProgress()
//            view?.showMessage(R.string.schedulers_files_no_loaded)
        })
    }


    //  SCHEDULERS ADAPTER DELEGATE
    override fun onSelect(item: MeetItem) {
        item.file?.let {
            if (item.getMeetItemType() == MeetItem.MeetItemType.Model) {
                it.signatures.firstOrNull()?.let { model ->
                    val link = NetworkUtils.getImageUrl(model.id, model.dir, model.path)
                    view.showFile(link)
                }
            } else {
                item.url?.let { link ->
                    view.showFile(link)
                }
            }

//            if (view.checkIOPermission(true)) {
//                if (FileUtils.shared.downloadFile(it)) {
//                    FileUtils.shared.getFile(it)?.let { file ->
//                        view?.showFile(file)
//                    }
//                }
//            }

        }


    }


}