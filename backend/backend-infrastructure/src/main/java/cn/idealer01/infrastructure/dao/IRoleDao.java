package cn.idealer01.infrastructure.dao;

import cn.idealer01.infrastructure.dao.po.Role;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IRoleDao {

    Role queryRoleById(Long roleId);

    Role queryRoleByCode(String roleCode);

    Role queryRoleByUserId(Long userId);
}
