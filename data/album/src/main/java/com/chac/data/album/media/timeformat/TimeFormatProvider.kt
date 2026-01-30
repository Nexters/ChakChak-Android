package com.chac.data.album.media.timeformat

/** 클러스터 타이틀에 사용할 시간 포맷 제공자. */
interface TimeFormatProvider {
    /**
     * @param epochMillis 에포크 밀리초
     * @return 포맷된 시간 문자열
     */
    fun formatClusterTime(epochMillis: Long): String
}
