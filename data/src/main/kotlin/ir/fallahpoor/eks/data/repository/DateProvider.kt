package ir.fallahpoor.eks.data.repository

import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class DateProvider @Inject constructor() {

    private val simpleDateFormat = SimpleDateFormat("MMM dd HH:mm", Locale.US)

    fun getCurrentDate(): String = simpleDateFormat.format(Date())

}