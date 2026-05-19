package cn.idealer01.domain.message.service;

import cn.idealer01.api.dto.MessagePageResponseDTO;
import cn.idealer01.api.dto.MessageRequestDTO;
import cn.idealer01.api.dto.MessageResponseDTO;
import cn.idealer01.api.dto.MessageSettingsRequestDTO;
import cn.idealer01.api.dto.MessageSettingsResponseDTO;
import cn.idealer01.domain.message.adapter.repository.IMessageRepository;
import cn.idealer01.domain.message.adapter.repository.IMessageSettingsRepository;
import cn.idealer01.domain.message.model.entity.MessageEntity;
import cn.idealer01.domain.message.model.entity.MessageSettingsEntity;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Service
public class MessageService implements IMessageService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_CONTENT_LENGTH = 500;
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String STATUS_DELETED = "DELETED";
    private static final String SENTIMENT_UNKNOWN = "UNKNOWN";
    private static final String MODE_AUTO_PUBLISH = "AUTO_PUBLISH";
    private static final String MODE_MANUAL_REVIEW = "MANUAL_REVIEW";

    @Resource
    private IMessageRepository messageRepository;

    @Resource
    private IMessageSettingsRepository messageSettingsRepository;

    @Override
    public MessageResponseDTO createMessage(Long userId, String username, MessageRequestDTO request) {
        validateMessageRequest(request);
        String reviewMode = resolveReviewMode();
        Date now = new Date();
        MessageEntity entity = MessageEntity.builder()
                .userId(userId)
                .username(defaultString(username))
                .content(request.getContent().trim())
                .status(MODE_MANUAL_REVIEW.equals(reviewMode) ? STATUS_PENDING : STATUS_PUBLISHED)
                .sentiment(SENTIMENT_UNKNOWN)
                .createTime(now)
                .updateTime(now)
                .build();
        Long id = messageRepository.save(entity);
        entity.setId(id);
        return toResponse(entity);
    }

    @Override
    public MessagePageResponseDTO getPublicMessages(int page, int pageSize) {
        int normalizedPage = normalizePage(page);
        int normalizedPageSize = normalizePageSize(pageSize);
        int offset = (normalizedPage - 1) * normalizedPageSize;
        List<MessageResponseDTO> items = messageRepository.findPublishedPage(offset, normalizedPageSize).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return MessagePageResponseDTO.builder()
                .items(items)
                .page(normalizedPage)
                .pageSize(normalizedPageSize)
                .total(messageRepository.countPublished())
                .build();
    }

    @Override
    public List<MessageResponseDTO> getMyMessages(Long userId) {
        List<MessageEntity> list = messageRepository.findByUserId(userId);
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public void deleteOwnMessage(Long messageId, Long currentUserId) {
        MessageEntity entity = requireMessage(messageId);
        if (!currentUserId.equals(entity.getUserId())) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "无权删除他人留言");
        }
        entity.setStatus(STATUS_DELETED);
        entity.setUpdateTime(new Date());
        messageRepository.update(entity);
    }

    @Override
    public MessagePageResponseDTO getAdminMessages(String status, int page, int pageSize) {
        int normalizedPage = normalizePage(page);
        int normalizedPageSize = normalizePageSize(pageSize);
        int offset = (normalizedPage - 1) * normalizedPageSize;
        String normalizedStatus = normalizeStatusFilter(status);
        List<MessageResponseDTO> items = messageRepository.findAdminPage(normalizedStatus, offset, normalizedPageSize).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return MessagePageResponseDTO.builder()
                .items(items)
                .page(normalizedPage)
                .pageSize(normalizedPageSize)
                .total(messageRepository.countAdminPage(normalizedStatus))
                .build();
    }

    @Override
    public MessageResponseDTO publishMessage(Long messageId) {
        MessageEntity entity = requireMessage(messageId);
        entity.setStatus(STATUS_PUBLISHED);
        entity.setUpdateTime(new Date());
        messageRepository.update(entity);
        return toResponse(entity);
    }

    @Override
    public MessageResponseDTO rejectMessage(Long messageId) {
        MessageEntity entity = requireMessage(messageId);
        entity.setStatus(STATUS_REJECTED);
        entity.setUpdateTime(new Date());
        messageRepository.update(entity);
        return toResponse(entity);
    }

    @Override
    public void deleteAdminMessage(Long messageId) {
        MessageEntity entity = requireMessage(messageId);
        entity.setStatus(STATUS_DELETED);
        entity.setUpdateTime(new Date());
        messageRepository.update(entity);
    }

    @Override
    public MessageSettingsResponseDTO getSettings() {
        return MessageSettingsResponseDTO.builder()
                .reviewMode(resolveReviewMode())
                .build();
    }

    @Override
    public MessageSettingsResponseDTO updateSettings(MessageSettingsRequestDTO request) {
        String reviewMode = request == null ? null : request.getReviewMode();
        if (!MODE_AUTO_PUBLISH.equals(reviewMode) && !MODE_MANUAL_REVIEW.equals(reviewMode)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "留言板发布模式非法");
        }
        MessageSettingsEntity entity = MessageSettingsEntity.builder()
                .reviewMode(reviewMode)
                .updateTime(new Date())
                .build();
        messageSettingsRepository.saveOrUpdate(entity);
        return MessageSettingsResponseDTO.builder()
                .reviewMode(reviewMode)
                .build();
    }

    private void validateMessageRequest(MessageRequestDTO request) {
        if (request == null || request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "留言内容不能为空");
        }
        if (request.getContent().trim().length() > MAX_CONTENT_LENGTH) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "留言内容不能超过500字");
        }
    }

    private MessageEntity requireMessage(Long messageId) {
        MessageEntity entity = messageRepository.findById(messageId);
        if (entity == null) {
            throw new AppException(ResponseCode.USER_NOT_EXIST.getCode(), "留言不存在");
        }
        return entity;
    }

    private String resolveReviewMode() {
        MessageSettingsEntity settings = messageSettingsRepository.getSettings();
        if (settings == null || settings.getReviewMode() == null || settings.getReviewMode().trim().isEmpty()) {
            return MODE_AUTO_PUBLISH;
        }
        return settings.getReviewMode();
    }

    private String normalizeStatusFilter(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        return status.trim();
    }

    private int normalizePage(int page) {
        return page <= 0 ? DEFAULT_PAGE : page;
    }

    private int normalizePageSize(int pageSize) {
        return pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize;
    }

    private MessageResponseDTO toResponse(MessageEntity entity) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return MessageResponseDTO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .username(defaultString(entity.getUsername()))
                .content(entity.getContent())
                .status(entity.getStatus())
                .sentiment(defaultString(entity.getSentiment(), SENTIMENT_UNKNOWN))
                .createdAt(formatDate(entity.getCreateTime(), formatter))
                .updatedAt(formatDate(entity.getUpdateTime(), formatter))
                .build();
    }

    private String formatDate(Date date, SimpleDateFormat formatter) {
        return date == null ? null : formatter.format(date);
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}
