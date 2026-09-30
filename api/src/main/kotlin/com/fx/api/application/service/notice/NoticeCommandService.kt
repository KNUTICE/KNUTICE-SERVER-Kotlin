package com.fx.api.application.service.notice

import com.fx.api.application.port.`in`.notice.NoticeCommandUseCase
import com.fx.api.application.port.`in`.notice.dto.NoticeCommand
import com.fx.api.application.port.out.notice.NoticePersistencePort
import com.fx.api.application.service.topic.TopicResolver
import com.fx.common.domain.notice.Notice
import com.fx.common.domain.notice.NoticeContent
import com.fx.common.exception.NoticeException
import com.fx.common.exception.errorcode.NoticeErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 관리자 공지 등록 · 수정 · 삭제.
 * 관리자가 등록한 공지는 알림을 보내지 않고, 본문 없이 요약만 입력받는다.
 */
@Service
@Transactional(readOnly = true)
class NoticeCommandService(
    private val noticePersistencePort: NoticePersistencePort,
    private val topicResolver: TopicResolver,
) : NoticeCommandUseCase {

    @Transactional
    override fun saveNotice(noticeCommand: NoticeCommand): Boolean {
        if (noticePersistencePort.existsByNttId(noticeCommand.nttId)) {
            throw NoticeException(NoticeErrorCode.ALREADY_EXISTS)
        }
        val topic = topicResolver.byName(noticeCommand.topicName, noticeCommand.topicType)

        noticePersistencePort.create(
            Notice.registeredByAdmin(
                nttId = noticeCommand.nttId,
                topic = topic,
                title = noticeCommand.title,
                department = noticeCommand.department,
                contentUrl = noticeCommand.contentUrl,
                contentImageUrl = noticeCommand.contentImageUrl,
                registrationDate = noticeCommand.registrationDate,
                isAttachment = noticeCommand.isAttachment,
                hasSummary = !noticeCommand.contentSummary.isNullOrBlank(),
            ),
            noticeCommand.contentSummary,
        )
        return true
    }

    /** 요약은 요청 값으로 바꾸고, 크롤링한 본문은 그대로 둔다. */
    @Transactional
    override fun updateNotice(noticeCommand: NoticeCommand): Boolean {
        val notice = noticePersistencePort.getNotice(noticeCommand.nttId)
        val topic = topicResolver.byName(noticeCommand.topicName, noticeCommand.topicType)

        notice.update(
            topic = topic,
            title = noticeCommand.title,
            department = noticeCommand.department,
            contentUrl = noticeCommand.contentUrl,
            contentImageUrl = noticeCommand.contentImageUrl,
            registrationDate = noticeCommand.registrationDate,
            isAttachment = noticeCommand.isAttachment,
        )

        val noticeId = requireNotNull(notice.id)
        val content = noticePersistencePort.findNoticeContent(noticeId)
            ?: noticePersistencePort.saveNoticeContent(NoticeContent(noticeId, content = null, contentSummary = null))
        content.changeSummary(noticeCommand.contentSummary)
        notice.summaryChanged(hasSummary = content.contentSummary != null)
        return true
    }

    @Transactional
    override fun deleteNotice(nttId: Long): Boolean {
        noticePersistencePort.delete(noticePersistencePort.getNotice(nttId))
        return true
    }

}
