package cn.idealer01.infrastructure.dao;

import cn.idealer01.infrastructure.dao.po.UserRole;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IUserRoleDao {

    void insertUserRole(UserRole userRole);

    List<UserRole> queryUserRoleByUserId(Long userId);
}
