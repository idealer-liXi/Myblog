package cn.idealer01.infrastructure.adapter.repository;

import cn.idealer01.api.dto.CurrentUserResponseDTO;
import cn.idealer01.domain.auth.adapter.repository.ILoginRepository;
import cn.idealer01.domain.auth.model.aggregate.LoginUserAggregate;
import cn.idealer01.domain.auth.model.entity.UserEntity;
import cn.idealer01.domain.auth.model.entity.WeixinUserEntity;
import cn.idealer01.domain.auth.service.UserAvatarAdminService;
import cn.idealer01.infrastructure.dao.IRoleDao;
import cn.idealer01.infrastructure.dao.IUserDao;
import cn.idealer01.infrastructure.dao.IUserAuthDao;
import cn.idealer01.infrastructure.dao.IUserRoleDao;
import cn.idealer01.infrastructure.dao.IWeixinUserDao;
import cn.idealer01.infrastructure.dao.po.Role;
import cn.idealer01.infrastructure.dao.po.User;
import cn.idealer01.infrastructure.dao.po.UserAuth;
import cn.idealer01.infrastructure.dao.po.UserRole;
import cn.idealer01.infrastructure.dao.po.WeixinUser;
import cn.idealer01.types.enums.AuthType;
import cn.idealer01.infrastructure.redis.IRedisService;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class LoginReposity implements ILoginRepository {
    @Resource
    private IRedisService redisService;
    @Resource
    private IWeixinUserDao weixinUserDao;
    @Resource
    private IUserDao userDao;
    @Resource
    private IUserAuthDao userAuthDao;
    @Resource
    private IRoleDao roleDao;
    @Resource
    private IUserRoleDao userRoleDao;

    @Override
    public String checkLogin(String ticket) {
        return redisService.checkLogin(ticket);
    }

    @Override
    public void saveLoginState(String ticket, String openId) {
        redisService.saveLoginState(ticket, openId);
    }

    @Override
    public WeixinUserEntity queryWeixinUserByOpenId(String openid) {
        log.info("查询用户{}是否存在", openid);
        WeixinUser weixinUser = weixinUserDao.queryWeixinUserByOpenId(openid);
        if(null == weixinUser) return null;
        return WeixinUserEntity.builder()
                .weixinName(weixinUser.getWeixinName())
                .weixinImageUrl(weixinUser.getWeixinImageUrl())
                .build();
    }

    @Override
    public void addWeixinUser(String openid) {
        log.info("将用户{}信息保存至数据库",openid);
        //1.查询微信信息，微信名称，微信头像
        //WeixinUser weixinUser = weixinApiService.getUserInfo();
        WeixinUser weixinUser = new WeixinUser();
        weixinUser.setWeixinName("weixin-" + openid.substring(0, 4));
        weixinUser.setWeixinImageUrl("https://i1.hdslb.com/bfs/face/d3a9fa7b57d61470b32c29b9a113958dd078b3c7.jpg@150w_150h.jpg");
        weixinUser.setOpenId(openid);
        //2.保存微信用户信息
        weixinUserDao.addWeixinUser(weixinUser);
    }

    @Override
    public LoginUserAggregate getRegularUserByUserName(String username) {
        User user = userDao.queryUserByUserName(username);

        if(null == user){
            throw new AppException(ResponseCode.USER_NOT_EXIST);
        }

        UserAuth passwordAuth = userAuthDao.queryUserAuthByTypeAndKey(AuthType.PASSWORD.getCode(), username);
        if (null == passwordAuth || StringUtils.isBlank(passwordAuth.getCredential())) {
            throw new AppException(ResponseCode.LOGIN_ERROR);
        }

        Role role = roleDao.queryRoleByUserId(user.getId());
        String roleCode = null == role ? null : role.getRoleCode();
        if(StringUtils.isBlank(roleCode)){
            throw new AppException(ResponseCode.ROLE_NOT_EXIST);
        }


        return LoginUserAggregate.builder()
                .userEntity(
                        UserEntity.builder()
                                .username(user.getUsername())
                                .password(passwordAuth.getCredential())
                                .roleCode(roleCode)
                                .status(user.getStatus())
                                .build()

                )
                .build();
    }

    @Override
    public CurrentUserResponseDTO queryCurrentUser(String username) {
        User user = userDao.queryUserByUserName(username);
        if (null == user) {
            throw new AppException(ResponseCode.USER_NOT_EXIST);
        }

        return buildCurrentUserResponse(user);
    }

    @Override
    public CurrentUserResponseDTO queryCurrentUserByUserId(Long userId) {
        User user = userDao.queryUserById(userId);
        if (null == user) {
            throw new AppException(ResponseCode.USER_NOT_EXIST);
        }

        return buildCurrentUserResponse(user);
    }

    @Override
    public List<CurrentUserResponseDTO> queryAllCurrentUsers() {
        List<User> users = userDao.queryAllUsers();
        List<CurrentUserResponseDTO> result = new ArrayList<>();
        for (User user : users) {
            result.add(buildCurrentUserResponse(user));
        }
        return result;
    }

    @Override
    public Long queryUserIdByAuthTypeAndAuthKey(String authType, String authKey) {
        UserAuth userAuth = userAuthDao.queryUserAuthByTypeAndKey(authType, authKey);
        return null == userAuth ? null : userAuth.getUserId();
    }

    @Override
    public void bindAuthToUser(Long userId, String authType, String authKey) {
        userAuthDao.insertUserAuth(UserAuth.builder()
                .userId(userId)
                .authType(authType)
                .authKey(authKey)
                .credential(null)
                .verified(1)
                .build());
    }

    @Override
    public CurrentUserResponseDTO createUserForThirdParty(String authType, String authKey, String displayName, String avatar) {
        Role defaultRole = roleDao.queryRoleByCode("USER");
        if (null == defaultRole) {
            throw new AppException(ResponseCode.ROLE_NOT_EXIST);
        }

        String generatedUsername = buildWechatUsername(authKey);
        User user = User.builder()
                .username(generatedUsername)
                .displayName(StringUtils.isBlank(displayName) ? generatedUsername : displayName)
                .avatar("")
                .avatarSource(AuthType.WECHAT.getCode().equals(authType)
                        ? UserAvatarAdminService.AVATAR_SOURCE_WECHAT
                        : UserAvatarAdminService.AVATAR_SOURCE_DEFAULT)
                .status(1)
                .build();
        userDao.insertUser(user);

        userAuthDao.insertUserAuth(UserAuth.builder()
                .userId(user.getId())
                .authType(authType)
                .authKey(authKey)
                .credential(null)
                .verified(1)
                .build());

        userRoleDao.insertUserRole(UserRole.builder()
                .userId(user.getId())
                .roleId(defaultRole.getId())
                .build());

        return buildCurrentUserResponse(user);
    }

    @Override
    public void unbindAuthFromUser(Long userId, String authType) {
        userAuthDao.deleteUserAuthByTypeAndUserId(authType, userId);
    }

@Override
    public void updateUserAvatar(Long userId, String avatar, String avatarSource) {
        userDao.updateUserAvatar(userId, avatar, avatarSource);
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        userDao.updateUserStatus(userId, status);
    }

    private String buildWechatUsername(String authKey) {
        String baseUsername = "wx_" + authKey;
        if (baseUsername.length() <= 64) {
            return baseUsername;
        }

        return baseUsername.substring(0, 64);
    }

    private CurrentUserResponseDTO buildCurrentUserResponse(User user) {

        Role role = roleDao.queryRoleByUserId(user.getId());
        String roleCode = null == role ? null : role.getRoleCode();
        List<String> roles = StringUtils.isBlank(roleCode)
                ? Collections.emptyList()
                : Collections.singletonList(roleCode);

        List<UserAuth> authList = userAuthDao.queryUserAuthByUserId(user.getId());
        UserAuth wechatAuth = authList.stream()
                .filter(auth -> AuthType.WECHAT.getCode().equals(auth.getAuthType()))
                .findFirst()
                .orElse(null);
        UserAuth passwordAuth = authList.stream()
                .filter(auth -> AuthType.PASSWORD.getCode().equals(auth.getAuthType()))
                .findFirst()
                .orElse(null);
        UserAuth emailAuth = authList.stream()
                .filter(auth -> AuthType.EMAIL.getCode().equals(auth.getAuthType()))
                .findFirst()
                .orElse(null);

        boolean weixinBound = null != wechatAuth;
        String weixinName = "";
        String weixinImageUrl = "";
        if (weixinBound) {
            WeixinUserEntity weixinUser = queryWeixinUserByOpenId(wechatAuth.getAuthKey());
            if (null != weixinUser) {
                if (StringUtils.isNotBlank(weixinUser.getWeixinName())) {
                    weixinName = weixinUser.getWeixinName();
                }
                if (StringUtils.isNotBlank(weixinUser.getWeixinImageUrl())) {
                    weixinImageUrl = weixinUser.getWeixinImageUrl();
                }
            }
        }

        String loginType = AuthType.PASSWORD.getCode();
        if (null == passwordAuth && null != wechatAuth) {
            loginType = AuthType.WECHAT.getCode();
        } else if (null == passwordAuth && null == wechatAuth && null != emailAuth) {
            loginType = AuthType.EMAIL.getCode();
        }

CurrentUserResponseDTO response = CurrentUserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .loginType(loginType)
                .roles(roles)
                .status(mapUserStatus(user.getStatus()))
                .weixinBound(weixinBound)
                .weixinName(weixinName)
                .createdAt(user.getCreateTime())
                .updatedAt(user.getUpdateTime())
                .build();

        return UserAvatarAdminService.applyCurrentUserAvatarFields(
                response,
                user.getAvatar(),
                user.getAvatarSource(),
                weixinImageUrl
        );
    }

    private String mapUserStatus(Integer status) {
        if (null == status) return "active";
        switch (status) {
            case 0: return "disabled";
            case 2: return "deleted";
            default: return "active";
        }
    }


}
