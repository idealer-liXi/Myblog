package cn.idealer01.domain.message.adapter.repository;

import cn.idealer01.domain.message.model.entity.MessageSettingsEntity;

public interface IMessageSettingsRepository {
    MessageSettingsEntity getSettings();

    void saveOrUpdate(MessageSettingsEntity entity);
}
