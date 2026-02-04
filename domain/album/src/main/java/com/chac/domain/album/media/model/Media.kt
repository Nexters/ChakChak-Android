package com.chac.domain.album.media.model

data class Media(
    val id: Long,
    val uriString: String,
    val dateTaken: Long,
    val mediaType: MediaType = MediaType.IMAGE,
)
