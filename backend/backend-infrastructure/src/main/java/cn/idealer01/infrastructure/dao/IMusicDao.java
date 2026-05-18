package cn.idealer01.infrastructure.dao;

import cn.idealer01.infrastructure.dao.po.MusicRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IMusicDao {

    List<MusicRecord> queryAll();

    List<MusicRecord> queryEnabledList();

    MusicRecord queryById(Long id);

    void insert(MusicRecord music);

    void update(MusicRecord music);

    void deleteById(Long id);
}
