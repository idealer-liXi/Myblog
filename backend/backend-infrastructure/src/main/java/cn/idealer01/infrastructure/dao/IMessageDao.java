package cn.idealer01.infrastructure.dao;

import cn.idealer01.infrastructure.dao.po.MessageRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IMessageDao {
    void insert(MessageRecord record);

    void update(MessageRecord record);

    MessageRecord queryById(Long id);

    List<MessageRecord> queryPublishedPage(int offset, int pageSize);

    Long countPublished();

    List<MessageRecord> queryByUserId(Long userId);

    List<MessageRecord> queryAdminPage(String status, int offset, int pageSize);

    Long countAdminPage(String status);
}
