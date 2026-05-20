package cn.idealer01.infrastructure.dao;

import cn.idealer01.infrastructure.dao.po.MessageRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IMessageDao {
    void insert(MessageRecord record);

    void update(MessageRecord record);

    MessageRecord queryById(Long id);

    List<MessageRecord> queryPublishedPage(@Param("offset") int offset, @Param("pageSize") int pageSize);

    Long countPublished();

    List<MessageRecord> queryByUserId(Long userId);

    List<MessageRecord> queryAdminPage(@Param("status") String status,
                                       @Param("offset") int offset,
                                       @Param("pageSize") int pageSize);

    Long countAdminPage(@Param("status") String status);
}
