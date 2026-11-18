package com.example.core

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object BengaliFormat {
    private val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    private val dhakaTz: TimeZone = TimeZone.getTimeZone("Asia/Dhaka")

    fun toBnDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(bnDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun toBnNumber(value: Number): String = toBnDigits(value.toString())

    fun formatTaka(amount: Double): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat("#,##0.##", symbols)
        return "${toBnDigits(df.format(amount))} ৳"
    }

    fun todayDateKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = dhakaTz
        return sdf.format(Date())
    }

    fun currentMonthKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
        sdf.timeZone = dhakaTz
        return sdf.format(Date())
    }

    fun currentMonthLabelBn(): String {
        val months = listOf(
            "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
            "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
        )
        val cal = java.util.Calendar.getInstance(dhakaTz)
        val m = cal.get(java.util.Calendar.MONTH)
        val y = cal.get(java.util.Calendar.YEAR)
        return "${months[m]} ${toBnDigits(y.toString())}"
    }

    fun formatDateBn(dateKey: String): String {
        val parts = dateKey.split("-")
        if (parts.size != 3) return toBnDigits(dateKey)
        val months = listOf(
            "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
            "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
        )
        val monthIdx = (parts[1].toIntOrNull() ?: 1).coerceIn(1, 12) - 1
        return "${toBnDigits(parts[2].trimStart('0').ifEmpty { "0" })} ${months[monthIdx]} ${toBnDigits(parts[0])}"
    }

    fun calculateGrade(obtained: Int, total: Int): String {
        if (total <= 0) return "F"
        val pct = (obtained.toDouble() / total.toDouble()) * 100.0
        return when {
            pct >= 80.0 -> "A+"
            pct >= 70.0 -> "A"
            pct >= 60.0 -> "A-"
            pct >= 50.0 -> "B"
            pct >= 40.0 -> "C"
            pct >= 33.0 -> "D"
            else -> "F"
        }
    }

    fun formatPositionBn(position: Int): String {
        return when (position) {
            1 -> "১ম"
            2 -> "২য়"
            3 -> "৩য়"
            4 -> "৪র্থ"
            5 -> "৫ম"
            6 -> "৬ষ্ঠ"
            7 -> "৭ম"
            8 -> "৮ম"
            9 -> "৯ম"
            10 -> "১০ম"
            else -> "${toBnDigits(position.toString())}তম"
        }
    }

    val nooraniArabicLetters = listOf(
        "ا" to "আলিফ", "ب" to "বা", "ت" to "তা", "ث" to "ছা",
        "ج" to "জীম", "ح" to "হা", "خ" to "খা", "د" to "দাল",
        "ذ" to "যাল", "ر" to "রা", "ز" to "যা", "س" to "সীন",
        "ش" to "শীন", "ص" to "ছোয়াদ", "ض" to "দোয়াদ", "ط" to "ত্বা",
        "ظ" to "যোয়া", "ع" to "আইন", "غ" to "গাইন", "ف" to "ফা",
        "ق" to "ক্বফ", "ك" to "কাফ", "ل" to "লাম", "م" to "মীম",
        "ن" to "নূন", "و" to "ওয়াও", "ه" to "হা", "ي" to "ইয়া"
    )
}
