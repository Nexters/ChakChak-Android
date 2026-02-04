package com.chac.data.album.media.reversGeocoder

import com.chac.domain.album.media.model.MediaLocation

/** 좌표를 사람이 읽을 수 있는 주소로 변환하는 리버스 지오코더. */
interface ReverseGeocoder {
    /**
     * 위/경도에 대한 주소 문자열을 조회한다.
     *
     * @param location 조회할 위치 정보
     * @return 주소 문자열. 조회 실패 시 null
     */
    suspend fun reverseGeocode(location: MediaLocation): String?
}
