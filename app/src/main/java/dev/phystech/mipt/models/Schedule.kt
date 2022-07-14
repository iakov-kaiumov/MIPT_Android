package dev.phystech.mipt.models

import dev.phystech.mipt.models.ScheduleItem


import java.io.Serializable

class Schedule(val version: String, var timetable: HashMap<String, ArrayList<ScheduleItem>>) : Serializable