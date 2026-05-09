package cn.idealer01.infrastructure.dao;

import cn.idealer01.infrastructure.dao.po.UserAuth;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IUserAuthDao {

    void insertUserAuth(UserAuth userAuth);

    UserAuth queryUserAuthByTypeAndKey(@Param("authType") String authType, @Param("authKey") String authKey);

    List<UserAuth> queryUserAuthByUserId(Long userId);
}
