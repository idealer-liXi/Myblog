package cn.idealer01.infrastructure.dao;

import cn.idealer01.infrastructure.dao.po.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IUserDao {

    void insertUser(User user);

    User queryUserByUserName(String username);

    User queryUserById(Long userId);
}
