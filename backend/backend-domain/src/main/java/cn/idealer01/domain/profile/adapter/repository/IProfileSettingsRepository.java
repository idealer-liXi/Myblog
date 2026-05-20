package cn.idealer01.domain.profile.adapter.repository;

import cn.idealer01.domain.profile.model.entity.ProfileSettingsEntity;

public interface IProfileSettingsRepository {
    ProfileSettingsEntity getCurrent();

    void saveOrUpdate(ProfileSettingsEntity entity);
}
