package cn.idealer01.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IUserLevelDao {


    String queryLevelNameById(Integer levelId);
}
