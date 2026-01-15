package com.chac.domain.photo.media

data class Media(
    val id: Long,
    val uriString: String,
    val dateTaken: Long,
    val mediaType: MediaType = MediaType.IMAGE,
)
