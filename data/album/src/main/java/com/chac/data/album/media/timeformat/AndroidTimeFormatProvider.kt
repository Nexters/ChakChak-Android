package com.chac.data.album.media.timeformat

import android.content.Context
import android.text.format.DateFormat
import com.chac.core.resources.R.string
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
        val skeleton = context.getString(string.clustering_date_skeleton)
        val localizedPattern = context.getString(string.clustering_date_pattern)
        val pattern = localizedPattern.ifBlank {
            DateFormat.getBestDateTimePattern(locale, skeleton)
        }
        return SimpleDateFormat(pattern, locale).format(Date(epochMillis))
    }
}
