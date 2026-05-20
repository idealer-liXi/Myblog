package cn.idealer01.infrastructure.dao;

import cn.idealer01.infrastructure.dao.po.ProfileSchoolHonorsRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IProfileSchoolHonorsDao {
    List<ProfileSchoolHonorsRecord> queryAll();

    ProfileSchoolHonorsRecord queryBySchoolKey(String schoolKey);

    void insert(ProfileSchoolHonorsRecord record);

    void update(ProfileSchoolHonorsRecord record);
}
