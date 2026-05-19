package cn.idealer01.infrastructure.dao;

import cn.idealer01.infrastructure.dao.po.MessageSettingsRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IMessageSettingsDao {
    MessageSettingsRecord queryCurrent();

    void insert(MessageSettingsRecord record);

    void update(MessageSettingsRecord record);
}
