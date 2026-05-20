package cn.idealer01.infrastructure.adapter.repository;

import cn.idealer01.api.dto.CurrentUserResponseDTO;
import cn.idealer01.infrastructure.dao.IRoleDao;
import cn.idealer01.infrastructure.dao.IUserAuthDao;
import cn.idealer01.infrastructure.dao.IUserDao;
import cn.idealer01.infrastructure.dao.IUserRoleDao;
import cn.idealer01.infrastructure.dao.IWeixinUserDao;
import cn.idealer01.infrastructure.dao.po.Role;
import cn.idealer01.infrastructure.dao.po.User;
import cn.idealer01.infrastructure.dao.po.UserAuth;
import cn.idealer01.infrastructure.redis.IRedisService;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LoginReposityTest {

    @Test
    public void createUserForThirdParty_shouldUseNextAvailableWechatUsernameWhenBaseUsernameExists() throws Exception {
        LoginReposity repository = new LoginReposity();
        IUserDao userDao = mock(IUserDao.class);
        IUserAuthDao userAuthDao = mock(IUserAuthDao.class);
        IUserRoleDao userRoleDao = mock(IUserRoleDao.class);
        IRoleDao roleDao = mock(IRoleDao.class);
        IWeixinUserDao weixinUserDao = mock(IWeixinUserDao.class);
        IRedisService redisService = mock(IRedisService.class);

        setField(repository, "userDao", userDao);
        setField(repository, "userAuthDao", userAuthDao);
        setField(repository, "userRoleDao", userRoleDao);
        setField(repository, "roleDao", roleDao);
        setField(repository, "weixinUserDao", weixinUserDao);
        setField(repository, "redisService", redisService);

        when(roleDao.queryRoleByCode("USER")).thenReturn(Role.builder().id(3L).roleCode("USER").build());
        when(userDao.queryUserByUserName("wx_openid-1")).thenReturn(User.builder().id(9L).username("wx_openid-1").status(2).build());
        when(userDao.queryUserByUserName("wx_openid-1_2")).thenReturn(null);
        when(roleDao.queryRoleByUserId(11L)).thenReturn(Role.builder().id(3L).roleCode("USER").build());
        when(userAuthDao.queryUserAuthByUserId(11L)).thenReturn(Collections.singletonList(
                UserAuth.builder().userId(11L).authType("wechat").authKey("openid-1").build()
        ));
        org.mockito.Mockito.doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(11L);
            return null;
        }).when(userDao).insertUser(any(User.class));

        CurrentUserResponseDTO result = repository.createUserForThirdParty("wechat", "openid-1", "微信昵称", "");

        Assert.assertEquals("wx_openid-1_2", result.getUsername());
        verify(userDao).insertUser(org.mockito.ArgumentMatchers.argThat(user -> "wx_openid-1_2".equals(user.getUsername())));
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = LoginReposity.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
