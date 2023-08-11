package com.example.disastertrack.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class ManagedDate {
    private fun containsTwoCommas(date: String): Int {
        val parts = date.split(",")
        return parts.size - 1
    }

    private fun getLastYear(date: String): String {
        val parts = date.split(", ", " – ")
        return parts.last()
    }

    fun convertDateRange(dateRange: String): String {
        val dateParts = dateRange.split(" – ")
        lateinit var startDate: Date
        lateinit var endDate : Date

        if (containsTwoCommas(dateRange) == 1) {
            Log.d("MainDate", "1 comma")
            var getLastYear = getLastYear(dateRange)
            startDate = parseStartDateWithSameYear(dateParts[0], getLastYear.toInt())
            endDate = parseEndDate(dateParts[1])
        } else {
            startDate = parseDate(dateParts[0])
            endDate = parseEndDate(dateParts[1])
        }

        val convertedStartDate = SimpleDateFormat("yyyy-MM-dd").format(startDate)
        val convertedEndDate = SimpleDateFormat("yyyy-MM-dd").format(endDate)

        return "$convertedStartDate/$convertedEndDate"
    }

    private fun parseDate(dateString: String): Date {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val dateFormat = if (dateString.contains(",")) "MMM d, yyyy" else "MMM d"

        return if ( dateString.contains(",")) {
            SimpleDateFormat(dateFormat, Locale.ENGLISH).parse(dateString)
        } else {
            val dateWithoutYear = SimpleDateFormat(dateFormat, Locale.ENGLISH).parse(dateString)
            val calendar = Calendar.getInstance()
            calendar.time = dateWithoutYear
            calendar.set(Calendar.YEAR, currentYear)

            calendar.time
        }
    }

    private fun parseStartDateWithSameYear(dateString: String, year : Int): Date {
        val dateFormat = if (dateString.contains(",")) "MMM d, yyyy" else "MMM d"

        return if ( dateString.contains(",")) {
            SimpleDateFormat(dateFormat, Locale.ENGLISH).parse(dateString)
        } else {
            val dateWithoutYear = SimpleDateFormat(dateFormat, Locale.ENGLISH).parse(dateString)
            val calendar = Calendar.getInstance()
            calendar.time = dateWithoutYear
            calendar.set(Calendar.YEAR, year)

            calendar.time
        }
    }

    private fun parseEndDate(endDateString: String): Date {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val dateFormat = if (endDateString.contains(",")) "MMM d, yyyy" else "MMM d"



        return if ( endDateString.contains(",")) {
            SimpleDateFormat(dateFormat, Locale.ENGLISH).parse(endDateString)
        } else {
            val dateWithoutYear = SimpleDateFormat(dateFormat, Locale.ENGLISH).parse(endDateString)
            val calendar = Calendar.getInstance()
            calendar.time = dateWithoutYear
            calendar.set(Calendar.YEAR, currentYear)

            calendar.time
        }

    }
}