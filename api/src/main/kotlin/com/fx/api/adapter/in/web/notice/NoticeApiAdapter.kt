package com.fx.api.adapter.`in`.web.notice

import com.fx.api.adapter.`in`.web.notice.dto.NoticeRequest
import com.fx.api.application.port.`in`.notice.NoticeCommandUseCase
import com.fx.common.annotation.hexagonal.WebInputAdapter
import com.fx.common.domain.TopicType
import io.github.seob7.Api
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@WebInputAdapter
@RequestMapping("/api/v1/notices")
class NoticeApiAdapter(
    private val noticeCommandUseCase: NoticeCommandUseCase
) : NoticeApiSwagger {

    @PostMapping
    override fun saveNotice(
        @RequestBody @Valid noticeRequest: NoticeRequest,
        @RequestParam type: TopicType
    ): ResponseEntity<Api<Boolean>> =
        Api.OK(
            noticeCommandUseCase.saveNotice(
                noticeRequest.toCommand(type)
            ), "저장되었습니다.")


    @PutMapping()
    override fun updateNotice(
        @RequestBody @Valid noticeRequest: NoticeRequest,
        @RequestParam type: TopicType,
    ): ResponseEntity<Api<Boolean>> =
        Api.OK(
            noticeCommandUseCase.updateNotice(
                noticeRequest.toCommand(type)
            ), "수정되었습니다.")

    @DeleteMapping("/{nttId}")
    override fun deleteNotice(@PathVariable nttId: Long): ResponseEntity<Api<Boolean>> =
        Api.OK(noticeCommandUseCase.deleteNotice(nttId), "삭제되었습니다.")

}