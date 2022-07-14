package dev.phystech.mipt.adapters

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.scheduler_filtred.*
import dev.phystech.mipt.base.utils.empty
import dev.phystech.mipt.models.*
import dev.phystech.mipt.repositories.SchedulePlaceRepository
import dev.phystech.mipt.repositories.TeachersRepository
import dev.phystech.mipt.repositories.TimeSlotsRepository
import dev.phystech.mipt.ui.fragments.study.scheduler_edit.SchedulerEditFragment
import dev.phystech.mipt.ui.utils.CollapseAnimation
import dev.phystech.mipt.ui.utils.ExpandAnimation
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class SchedulerFiltredListAdapter: RecyclerView.Adapter<SchedulerFiltredListAdapter.ViewHolder>() {
    interface Delegate {
        fun onUpdateFilter(filter: FilterAdapterModelHelper)
        fun createScheduler(model: SchedulerIndex)
        fun createNewScheduler()

    }

    private val items: ArrayList<SchedulerFiltredListAdapterModel> = arrayListOf()
    lateinit var context: Context
    var delegate: Delegate? = null

    init {
        this.items.add(FilterAdapterModelHelper())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            Type.Item.typeIndex -> ItemViewHolder(inflater.inflate(R.layout.item_scheduler, parent, false))
            Type.Filter.typeIndex -> FilterViewHolder(inflater.inflate(R.layout.item_scheduler_filter, parent, false))
            Type.Empty.typeIndex -> EmptyViewHolder(inflater.inflate(R.layout.item_scheduler_empty, parent, false))
            else -> throw UnknownError()
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return items[position].getTypeIndex()
    }


    //  OTHERS
    fun setItems(newItems: List<SchedulerFiltredListAdapterModel>) {
        val filterModel = items.firstOrNull()
        val size = items.size
        items.clear()

        if (filterModel is FilterAdapterModelHelper) {
            this.items.add(filterModel)
            notifyItemRangeRemoved(1, size)
            this.items.addAll(newItems)
            notifyItemRangeChanged(1, this.items.size - 1)
//            notifyDataSetChanged()
        } else {
            this.items.add(FilterAdapterModelHelper())
            this.items.addAll(newItems)
            notifyItemRangeChanged(0, this.items.size)
//            notifyDataSetChanged()
        }

        if (newItems.size == 0) {
            this.items.add(SchedulerAdapterEmptyHelper())
            notifyItemChanged(this.items.size - 1)
        }

    }

    fun setItems(items: ArrayList<SchedulerIndex>) {
        val newModels = items.mapNotNull { v -> SchedulerAdapterModelHelper(v) }
        setItems(newModels)
    }

    fun setFilter(model: FilterAdapterModelHelper) {
        if (items.size > 0 && items[0] is FilterAdapterModelHelper) {
            items[0] = model
        } else {
            items.add(0, model)
        }

        notifyItemInserted(0)
    }



    //  VH
    inner abstract class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        abstract fun bind(item: SchedulerFiltredListAdapterModel)
    }


    inner class FilterViewHolder(view: View): ViewHolder(view) {
        private var modelHelper: FilterAdapterModelHelper? = null
        private var bindMode = false

        val tvDayOfWeek: AutoCompleteTextView = view.findViewById(R.id.tvDayOfWeek)
        val tvTimeBegin: AutoCompleteTextView = view.findViewById(R.id.tvTimeBegin)
        val tvAuditory: AutoCompleteTextView = view.findViewById(R.id.tvAuditory)
        val tvLector: AutoCompleteTextView = view.findViewById(R.id.tvLector)
        val tvSchedulerType: AutoCompleteTextView = view.findViewById(R.id.tvSchedulerType)

        val addField: LinearLayout = view.findViewById(R.id.addField)
        val tvToggle: TextView = view.findViewById(R.id.tvToggle)

        var timeslots: List<TimeSlotModel> = emptyList()

        val teachers: MutableList<Teacher> = arrayListOf()

        var auditoryes: List<SchedulePlace> = emptyList()

        val weeks = context.resources.getStringArray(R.array.weeks).toMutableList().apply { add(0, "") }
        val schTypes = arrayListOf(
            SchedulerType("", null),
            SchedulerType("lec", context.resources.getString(R.string.schedule_item_lec)),
            SchedulerType("sem", context.resources.getString(R.string.schedule_item_sem)),
            SchedulerType("lab", context.resources.getString(R.string.schedule_item_lab))
        )
        val schTypeAdapter = SchedulerEditFragment.SchedulerTypeAdapter(context, schTypes)

        init {

            TimeSlotsRepository.shared.slots
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        timeslots = it

                        modelHelper?.model?.let { model ->
                            model.timeBegin?.toIntOrNull()?.let {
                                tvTimeBegin.setText(timeslots.getOrNull(it - 1)?.start)
                            }
                        }

                        val adapter = ArrayAdapter(context, R.layout.list_item, it.mapNotNull { v -> v.start }.toMutableList().apply { add(0, "") })
                        tvTimeBegin.setAdapter(adapter)
                    }, {})

            TeachersRepository.shared.teachers.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    teachers.clear()
                    teachers.addAll(it)
                    tvLector.setAdapter(ArrayAdapter(context, R.layout.list_item, it.mapNotNull { v -> v.name }.sorted().toMutableList().apply { add(0, "") }))
                }, {})

            SchedulePlaceRepository.shared.places
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    auditoryes = it
                    tvAuditory.setAdapter(ArrayAdapter(context, R.layout.list_item, it.mapNotNull { v -> v.name }.toMutableList().apply { add(0, "") }))
                }, {})

            tvDayOfWeek.setAdapter(ArrayAdapter(context, R.layout.list_item, weeks))
            tvSchedulerType.setAdapter(schTypeAdapter)

            //  LISTENERS
            tvAuditory.addTextChangedListener ( SimpleTextWatcher(bindMode) {
                if (bindMode) return@SimpleTextWatcher false
                modelHelper?.model?.auditory = SchedulePlaceRepository.shared.places.value.firstOrNull { v -> v.name == it }?.id

                return@SimpleTextWatcher true
            })
            tvDayOfWeek.addTextChangedListener ( SimpleTextWatcher(bindMode) {
                if (bindMode) return@SimpleTextWatcher false
                modelHelper?.model?.dayOfWeek = if (weeks.contains(it) && it.isNotEmpty()) weeks.indexOf(it).toString() else null

                return@SimpleTextWatcher true
            })
            tvDayOfWeek.inputType = InputType.TYPE_NULL
            tvDayOfWeek.dropDownHorizontalOffset = context.resources.getDimension(R.dimen.fab_margin).toInt()
            tvTimeBegin.addTextChangedListener ( SimpleTextWatcher(bindMode) {
                if (bindMode) return@SimpleTextWatcher false
                modelHelper?.model?.timeBegin = (timeslots.indexOfFirst { v -> v.start == it } + 1).toString()

                return@SimpleTextWatcher true
            })
            tvLector.addTextChangedListener (SimpleTextWatcher(bindMode) {
                if (bindMode) return@SimpleTextWatcher false
                modelHelper?.model?.lector = TeachersRepository.shared.teachers.value.firstOrNull { v -> v.name == it }?.id

                return@SimpleTextWatcher true
            })
            tvSchedulerType.addTextChangedListener (SimpleTextWatcher(bindMode) {
                if (bindMode) return@SimpleTextWatcher false
                modelHelper?.model?.schedulerType = schTypes.firstOrNull{ v -> v.title == it }?.type

                return@SimpleTextWatcher true
            })

            tvToggle.setOnClickListener {
                modelHelper?.isShown = modelHelper?.isShown?.not() ?: false
                updateMoreVisible()
            }
        }

        override fun bind(item: SchedulerFiltredListAdapterModel) {

            modelHelper = item as FilterAdapterModelHelper
            val model = modelHelper?.model ?: return

            bindMode = true

            model.auditory?.let {
                SchedulePlaceRepository.shared.getById(it) { tvAuditory.setText(it?.name) }
            }
            tvAuditory.setAdapter(ArrayAdapter(context, R.layout.list_item, auditoryes.mapNotNull { v -> v.name }.toMutableList().apply { add(0, "") }))

            tvDayOfWeek.setText(weeks.getOrNull(model.dayOfWeek?.toIntOrNull() ?: -1))
            tvDayOfWeek.setAdapter(ArrayAdapter(context, R.layout.list_item, weeks))

            model.timeBegin?.toIntOrNull()?.let {
                tvTimeBegin.setText(timeslots.getOrNull(it - 1)?.start)
            }
            tvTimeBegin.setAdapter(ArrayAdapter(context, R.layout.list_item, timeslots.mapNotNull { v -> v.start }.toMutableList().apply { add(0, "") }))

            TeachersRepository.shared.getByID(model.lector ?: "") {
                tvLector.setText(it?.name)
            }
            tvLector.setAdapter(ArrayAdapter(context, R.layout.list_item, teachers.mapNotNull { v -> v.name }.sorted().toMutableList().apply { add(0, "") }))

            schTypes.firstOrNull { it.type == model.schedulerType }?.let {
                tvSchedulerType.setText(it.title)
            }
            tvSchedulerType.setAdapter(SchedulerEditFragment.SchedulerTypeAdapter(context, schTypes))

            bindMode = false
            updateMoreVisible()
        }

        private fun updateMoreVisible() {
//            addField.visibility = (modelHelper?.isShown == true).visibility()

            if (modelHelper?.isShown == true) {
                tvToggle.setText(R.string.show_less)
                val anim = ExpandAnimation(addField).apply { duration = 200 }
                addField.startAnimation(anim)
            } else {
                tvToggle.setText(R.string.show_more)
                val anim = CollapseAnimation(addField).apply { duration = 200 }
                addField.startAnimation(anim)
            }


        }

    }

    inner class ItemViewHolder(view: View): ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvAuditory: TextView = view.findViewById(R.id.tvAuditory)
        val ScheduleType: TextView = view.findViewById(R.id.ScheduleType)
        val tvLector: TextView = view.findViewById(R.id.tvLector)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val tvSchedulerTime: TextView = view.findViewById(R.id.tvSchedulerTime)


        private var modelHelper: SchedulerAdapterModelHelper? = null

        init {
            view.setOnClickListener {
                modelHelper?.model?.let { model ->
                    delegate?.createScheduler(model)
                }
            }
        }

        override fun bind(item: SchedulerFiltredListAdapterModel) {
            modelHelper = item as SchedulerAdapterModelHelper
            val model = modelHelper?.model ?: return

            tvTitle.text = model.name
            tvAuditory.text = model.auditorium.mapNotNull { v -> v.name }.joinToString(separator = "\n")
            tvLector.text = model.teachers.mapNotNull { v -> v.name }.joinToString("\n")
//            tvDescription.text = model.
            tvSchedulerTime.text = model.buildTime(context.resources)
            ScheduleType.text = typeName(ScheduleType.context, model.type)

        }

    }

    inner class EmptyViewHolder(view: View): ViewHolder(view) {
        val rlCreateScheduler: RelativeLayout = view.findViewById(R.id.rlCreateScheduler)
        init {
            rlCreateScheduler.setOnClickListener {
                delegate?.createNewScheduler()
            }
        }

        override fun bind(item: SchedulerFiltredListAdapterModel) {

        }
    }


    inner class SimpleTextWatcher(val bindMode: Boolean, val onChange: ((String) -> Boolean)): TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable?) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (count != 0 || before != 0) {
                if (onChange.invoke(s.toString()) == true) {
                    (items.firstOrNull() as? FilterAdapterModelHelper)?.let {
                        delegate?.onUpdateFilter(it)
                    }
                }
            }
        }
    }


    private fun typeName(context: Context, type: String?): String {
        return when (type) {
            "LAB" -> context.resources.getString(R.string.schedule_item_lab)
            "SEM" -> context.resources.getString(R.string.schedule_item_sem)
            "LEC" -> context.resources.getString(R.string.schedule_item_lec)
            else -> String.empty()
        }
    }
}
