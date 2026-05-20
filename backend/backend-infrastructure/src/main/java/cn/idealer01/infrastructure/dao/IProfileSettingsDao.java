package cn.idealer01.infrastructure.dao;

import cn.idealer01.infrastructure.dao.po.ProfileSettingsRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IProfileSettingsDao {
    ProfileSettingsRecord queryCurrent();

    void insert(ProfileSettingsRecord record);

    void update(ProfileSettingsRecord record);
}
