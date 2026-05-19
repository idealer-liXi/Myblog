package cn.idealer01.infrastructure.adapter.repository;

import cn.idealer01.domain.message.adapter.repository.IMessageRepository;
import cn.idealer01.domain.message.model.entity.MessageEntity;
import cn.idealer01.infrastructure.dao.IMessageDao;
import cn.idealer01.infrastructure.dao.po.MessageRecord;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageRepository implements IMessageRepository {

    @Resource
    private IMessageDao messageDao;

    @Override
    public Long save(MessageEntity entity) {
        MessageRecord record = toRecord(entity);
        messageDao.insert(record);
        return record.getId();
    }

    @Override
    public void update(MessageEntity entity) {
        messageDao.update(toRecord(entity));
    }

    @Override
    public MessageEntity findById(Long id) {
        return toEntity(messageDao.queryById(id));
    }

    @Override
    public List<MessageEntity> findPublishedPage(int offset, int pageSize) {
        return messageDao.queryPublishedPage(offset, pageSize).stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Long countPublished() {
        return messageDao.countPublished();
    }

    @Override
    public List<MessageEntity> findByUserId(Long userId) {
        return messageDao.queryByUserId(userId).stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageEntity> findAdminPage(String status, int offset, int pageSize) {
        return messageDao.queryAdminPage(status, offset, pageSize).stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Long countAdminPage(String status) {
        return messageDao.countAdminPage(status);
    }

    private MessageEntity toEntity(MessageRecord record) {
        if (record == null) {
            return null;
        }
        return MessageEntity.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .username(record.getUsername())
                .content(record.getContent())
                .status(record.getStatus())
                .sentiment(record.getSentiment())
                .createTime(record.getCreateTime())
                .updateTime(record.getUpdateTime())
                .build();
    }

    private MessageRecord toRecord(MessageEntity entity) {
        if (entity == null) {
            return null;
        }
        return MessageRecord.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .username(entity.getUsername())
                .content(entity.getContent())
                .status(entity.getStatus())
                .sentiment(entity.getSentiment())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
