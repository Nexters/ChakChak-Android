package com.chac.data.album.media.reversGeocoder

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.annotation.RequiresApi
import com.chac.domain.album.media.model.MediaLocation
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * Android [Geocoder] 기반의 리버스 지오코더 구현체.
 *
 * API 33 이상에서는 비동기 리스너 API를 사용하고, 그 이하에서는 동기 API를 사용한다.
 *
 * @param context 애플리케이션 컨텍스트
 */
internal class AndroidReverseGeocoder @Inject constructor(
    @ApplicationContext private val context: Context,
) : ReverseGeocoder {
    /**
     * 위/경도에 대한 주소 문자열을 조회한다.
     *
     * @param location 조회할 위치 정보
     * @return 주소 문자열. 조회 실패 시 null
     */
    override suspend fun reverseGeocode(location: MediaLocation): String? {
        val latitude = location.latitude ?: return null
        val longitude = location.longitude ?: return null
        return withContext(Dispatchers.IO) {
            if (!Geocoder.isPresent()) return@withContext null
            val address = runCatching {
                val geocoder = Geocoder(context, Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocodeAsync(geocoder, latitude, longitude)
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
                }
            }.getOrNull()
            address?.let { address ->
                address.subLocality
                    ?: address.locality
                    ?: address.adminArea
                    ?: address.countryName
                    ?: address.getAddressLine(0)
            }
        }
    }

    /**
     * API 33 이상에서 비동기 Geocoder 호출을 suspend 형태로 래핑한다.
     *
     * @param geocoder 사용할 Geocoder 인스턴스
     * @param latitude 위도
     * @param longitude 경도
     * @return 첫 번째 주소. 조회 실패 시 null
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun geocodeAsync(
        geocoder: Geocoder,
        latitude: Double,
        longitude: Double,
    ) = suspendCancellableCoroutine { continuation ->
        geocoder.getFromLocation(
            latitude,
            longitude,
            1,
            object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    if (continuation.isActive) {
                        continuation.resume(addresses.firstOrNull())
                    }
                }

                override fun onError(errorMessage: String?) {
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
            },
        )
    }
}
