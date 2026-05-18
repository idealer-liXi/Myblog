package cn.idealer01.infrastructure.dao;

import cn.idealer01.infrastructure.dao.po.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IUserDao {

    void insertUser(User user);

    User queryUserByUserName(String username);

    User queryUserById(Long userId);

    List<User> queryAllUsers();

    String queryWechatOpenIdByUserId(Long userId);

void updateUserAvatar(@Param("userId") Long userId, @Param("avatar") String avatar, @Param("avatarSource") String avatarSource);

    void updateUserStatus(@Param("userId") Long userId, @Param("status") Integer status);
}
