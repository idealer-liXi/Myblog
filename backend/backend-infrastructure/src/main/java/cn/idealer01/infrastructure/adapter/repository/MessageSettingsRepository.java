package cn.idealer01.infrastructure.adapter.repository;

import cn.idealer01.domain.message.adapter.repository.IMessageSettingsRepository;
import cn.idealer01.domain.message.model.entity.MessageSettingsEntity;
import cn.idealer01.infrastructure.dao.IMessageSettingsDao;
import cn.idealer01.infrastructure.dao.po.MessageSettingsRecord;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class MessageSettingsRepository implements IMessageSettingsRepository {

    @Resource
    private IMessageSettingsDao messageSettingsDao;

    @Override
    public MessageSettingsEntity getSettings() {
        return toEntity(messageSettingsDao.queryCurrent());
    }

    @Override
    public void saveOrUpdate(MessageSettingsEntity entity) {
        MessageSettingsRecord current = messageSettingsDao.queryCurrent();
        if (current == null) {
            MessageSettingsRecord record = MessageSettingsRecord.builder()
                    .reviewMode(entity.getReviewMode())
                    .createTime(new Date())
                    .updateTime(new Date())
                    .build();
            messageSettingsDao.insert(record);
            return;
        }

        current.setReviewMode(entity.getReviewMode());
        current.setUpdateTime(new Date());
        messageSettingsDao.update(current);
    }

    private MessageSettingsEntity toEntity(MessageSettingsRecord record) {
        if (record == null) {
            return null;
        }
        return MessageSettingsEntity.builder()
                .id(record.getId())
                .reviewMode(record.getReviewMode())
                .createTime(record.getCreateTime())
                .updateTime(record.getUpdateTime())
                .build();
    }
}
