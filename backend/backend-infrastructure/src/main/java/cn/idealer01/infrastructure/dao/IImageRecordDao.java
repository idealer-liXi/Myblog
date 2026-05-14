package cn.idealer01.infrastructure.dao;

import cn.idealer01.infrastructure.dao.po.ImageRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IImageRecordDao {

    List<ImageRecord> queryAll();

    ImageRecord queryById(Long id);

    void insert(ImageRecord record);

    void deleteById(Long id);
}
