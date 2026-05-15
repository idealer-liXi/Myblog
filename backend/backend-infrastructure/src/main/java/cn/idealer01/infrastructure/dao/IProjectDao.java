package cn.idealer01.infrastructure.dao;

import cn.idealer01.infrastructure.dao.po.Project;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IProjectDao {

    List<Project> queryAll();

    List<Project> queryPublicList();

    Project queryById(Long id);

    void insert(Project project);

    void update(Project project);

    void deleteById(Long id);
}
