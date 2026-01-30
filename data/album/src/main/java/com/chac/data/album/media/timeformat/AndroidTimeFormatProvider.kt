package com.chac.data.album.media.timeformat

import android.content.Context
import android.text.format.DateFormat
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

/**
 * Android 로케일 설정에 맞춰 클러스터 타이틀 시간 포맷을 제공한다.
 */
internal class AndroidTimeFormatProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : TimeFormatProvider {
    override fun formatClusterTime(epochMillis: Long): String {
        val locale = context.resources.configuration.locales[0]
        val pattern = DateFormat.getBestDateTimePattern(locale, "yMMMdH")
        return SimpleDateFormat(pattern, locale).format(Date(epochMillis))
    }
}
