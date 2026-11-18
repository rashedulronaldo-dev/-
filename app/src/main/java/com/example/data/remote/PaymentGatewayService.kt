package com.example.data.remote

interface SmsProviderAdapter {
    val providerNameBn: String
    suspend fun sendBengaliSms(phones: List<String>, messageBn: String): Result<Int>
}

class DeferredSmsProviderAdapter(
    override val providerNameBn: String = "স্থানীয় SMS কিউ অ্যাডাপ্টার"
) : SmsProviderAdapter {
    override suspend fun sendBengaliSms(phones: List<String>, messageBn: String): Result<Int> {
        return Result.success(phones.size)
    }
}
