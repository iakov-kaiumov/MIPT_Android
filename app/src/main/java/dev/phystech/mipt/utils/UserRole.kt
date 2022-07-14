package dev.phystech.mipt.utils

enum class UserRole(val value: Int) {
    Guest(0),
    Student(1),
    Employee(2),
    Admin(3),
    Alumnus(4),
    Academ(5),
    Expelled(6);

    fun getName(): String {
        return when (this) {
            Admin -> "admin"
            Alumnus -> "alumnus"
            Academ -> "academ"
            Expelled -> "expelled"
            else -> ""
        }
    }

    companion object {
        fun getByValue(value: Int): UserRole? {
            return when (value) {
                0 -> UserRole.Guest
                1 -> UserRole.Student
                2 -> UserRole.Employee
                3 -> UserRole.Employee
                else -> null
            }
        }
    }
}




