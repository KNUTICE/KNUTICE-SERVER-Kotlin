package com.fx.api.application.service.notice

import com.fx.api.application.port.`in`.notice.NoticeQueryUseCase
import com.fx.api.application.port.`in`.notice.dto.NoticeSearchCommand
import com.fx.api.application.port.out.notice.NoticePersistencePort
import com.fx.api.application.service.topic.TopicResolver
import com.fx.api.domain.NoticeQuery
import com.fx.common.domain.notice.Notice
import com.fx.common.exception.NoticeException
import com.fx.common.exception.errorcode.NoticeErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class NoticeQueryService(
    private val noticePersistencePort: NoticePersistencePort,
    private val topicResolver: TopicResolver,
) : NoticeQueryUseCase {

    override fun getNotices(noticeSearchCommand: NoticeSearchCommand): List<Notice> {
        val topic = topicResolver.byNameOrCode(noticeSearchCommand.topicName, noticeSearchCommand.topicId)
        val notices = noticePersistencePort.findNotices(
            NoticeQuery(
                nttId = noticeSearchCommand.nttId,
                topicCode = topic?.code,
                keyword = noticeSearchCommand.keyword?.takeIf {
                    it.isNotBlank()
                },
                size = noticeSearchCommand.size,
            )
        )
        if (notices.isEmpty()) {
            throw NoticeException(NoticeErrorCode.NOTICE_NOT_FOUND)
        }
        return notices
    }

    override fun getNotice(nttId: Long): Notice =
        noticePersistencePort.getNotice(nttId)

    override fun getNoticeSummary(nttId: Long): String {
        val notice = noticePersistencePort.getNotice(nttId)
        return noticePersistencePort.findNoticeContent(requireNotNull(notice.id))
            ?.contentSummary
            ?.takeIf {
                it.isNotBlank()
            }
            ?: throw NoticeException(NoticeErrorCode.SUMMARY_CONTENT_NOT_FOUND)
    }

}
