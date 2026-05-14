package cn.idealer01.infrastructure.dao;

import cn.idealer01.infrastructure.dao.po.Theme;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IThemeDao {

    List<Theme> queryAll();

    Theme queryById(Long id);

    Theme queryByName(@Param("name") String name);

    void insert(Theme theme);

    void update(Theme theme);

    void deleteById(Long id);
}
