package com.example.core

fun toBengaliErrorMessage(error: Throwable?): String {
    val msg = error?.localizedMessage ?: ""
    return when {
        msg.contains("network", ignoreCase = true) ->
            "ইন্টারনেট সংযোগ পাওয়া যায়নি। স্থানীয়ভাবে সংরক্ষিত হয়েছে।"
        msg.isBlank() ->
            "একটি অপ্রত্যাশিত ত্রুটি ঘটেছে। অনুগ্রহ করে পুনরায় চেষ্টা করুন।"
        else ->
            "ত্রুটি: $msg"
    }
}
