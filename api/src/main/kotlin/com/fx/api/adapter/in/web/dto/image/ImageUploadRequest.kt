package com.fx.api.adapter.`in`.web.dto.image

import com.fx.api.domain.ImageType
import org.springframework.http.codec.multipart.FilePart

data class ImageUploadRequest(

    val image: FilePart,
    val type: ImageType

)