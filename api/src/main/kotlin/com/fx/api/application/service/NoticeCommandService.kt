package com.fx.api.application.service

import com.fx.api.application.port.`in`.NoticeCommandUseCase
import com.fx.api.application.port.`in`.dto.NoticeCommand
import com.fx.api.application.port.out.NoticePersistencePort
import com.fx.api.exception.NoticeException
import com.fx.api.exception.errorcode.NoticeErrorCode
import com.fx.global.domain.Notice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Notice Command Service
 *
 * @author 이동섭
 * @since 2025-10-21
 */
@Service
class NoticeCommandService(
    private val noticePersistencePort: NoticePersistencePort
): NoticeCommandUseCase {

    @Transactional
    override fun updateNotice(noticeCommand: NoticeCommand): Boolean {
        val noticeExists = (noticePersistencePort.getNotice(noticeCommand.nttId)
            ?: throw NoticeException(NoticeErrorCode.NOTICE_NOT_FOUND))

        val updatedNotice = this.updateNotice(noticeCommand, noticeExists)
        noticePersistencePort.saveNotice(updatedNotice)
        return true
    }

    @Transactional
    override fun deleteNotice(nttId: Long): Boolean {
        if (!noticePersistencePort.existsById(nttId)) {
            throw NoticeException(NoticeErrorCode.NOTICE_NOT_FOUND)
        }
        noticePersistencePort.deleteById(nttId)
        return true
    }

    private fun updateNotice(noticeCommand: NoticeCommand, noticeExists: Notice): Notice =
        Notice(
            nttId = noticeCommand.nttId,
            title = noticeCommand.title,
            department = noticeCommand.department,
            contentUrl = noticeCommand.contentUrl,
            content = noticeExists.content,
            contentSummary = noticeCommand.contentSummary,
            contentImageUrl = noticeCommand.contentImageUrl,
            registrationDate = noticeCommand.registrationDate,
            isAttachment = noticeCommand.isAttachment,
            topic = noticeCommand.topic,
            createdAt = noticeExists.createdAt // updatedAt 은 필요 없지만 createdAt 은 무조건 써야함! - NoticeEntity.from 에 createdAt 이 있기 때문임
        )

}