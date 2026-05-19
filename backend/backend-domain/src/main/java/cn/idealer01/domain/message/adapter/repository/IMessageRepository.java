package cn.idealer01.domain.message.adapter.repository;

import cn.idealer01.domain.message.model.entity.MessageEntity;

import java.util.List;

public interface IMessageRepository {
    Long save(MessageEntity entity);

    void update(MessageEntity entity);

    MessageEntity findById(Long id);

    List<MessageEntity> findPublishedPage(int offset, int pageSize);

    Long countPublished();

    List<MessageEntity> findByUserId(Long userId);

    List<MessageEntity> findAdminPage(String status, int offset, int pageSize);

    Long countAdminPage(String status);
}
