package cn.idealer01.infrastructure.adapter.repository;

import cn.idealer01.domain.auth.adapter.repository.IRegisterRepository;
import cn.idealer01.infrastructure.dao.IRoleDao;
import cn.idealer01.infrastructure.dao.IUserDao;
import cn.idealer01.infrastructure.dao.IUserAuthDao;
import cn.idealer01.infrastructure.dao.IUserRoleDao;
import cn.idealer01.infrastructure.dao.po.Role;
import cn.idealer01.infrastructure.dao.po.User;
import cn.idealer01.infrastructure.dao.po.UserAuth;
import cn.idealer01.infrastructure.dao.po.UserRole;
import cn.idealer01.domain.auth.service.UserAvatarAdminService;
import cn.idealer01.types.enums.AuthType;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class RegisterRepository implements IRegisterRepository {
    @Resource
    private IUserDao userDao;
    @Resource
    private IUserAuthDao userAuthDao;
    @Resource
    private IUserRoleDao userRoleDao;
    @Resource
    private IRoleDao roleDao;

    @Override
    public void insertRegularUser(String username, String password) {
        //1.检查用户是否已经存在
        User userByUserName = userDao.queryUserByUserName(username);
        if(null != userByUserName){
            throw new AppException(ResponseCode.USER_EXIST);
        }

        Role defaultRole = roleDao.queryRoleByCode("USER");
        if (null == defaultRole) {
            throw new AppException(ResponseCode.ROLE_NOT_EXIST);
        }

        //2.不存在，插入数据
        User user = User.builder()
                .username(username)
                .displayName(username)
                .avatar("")
                .avatarSource(UserAvatarAdminService.AVATAR_SOURCE_DEFAULT)
                .status(1)
                .build();
        userDao.insertUser(user);

        userAuthDao.insertUserAuth(UserAuth.builder()
                .userId(user.getId())
                .authType(AuthType.PASSWORD.getCode())
                .authKey(username)
                .credential(password)
                .verified(1)
                .build());

        userRoleDao.insertUserRole(UserRole.builder()
                .userId(user.getId())
                .roleId(defaultRole.getId())
                .build());
    }
}
