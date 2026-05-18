package cn.idealer01.domain;

import cn.idealer01.api.dto.BindExistingAccountRequestDTO;
import cn.idealer01.api.dto.CurrentUserResponseDTO;
import cn.idealer01.api.dto.LoginResponseDTO;
import cn.idealer01.api.dto.UserAvatarRequestDTO;
import cn.idealer01.domain.auth.service.UserAvatarAdminService;
import cn.idealer01.domain.auth.service.login.WeixinLoginService;
import cn.idealer01.domain.auth.adapter.repository.ILoginRepository;
import cn.idealer01.domain.auth.adapter.port.ILoginPort;
import cn.idealer01.domain.auth.model.entity.WeixinUserEntity;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import cn.idealer01.types.utils.JwtUtil;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserAvatarAdminServiceTest {

    @Mock
    private ILoginPort loginPort;

    @Mock
    private ILoginRepository loginRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Test
    public void buildAvatarPayload_shouldPreferSourceSpecificEffectiveAvatar() {
        UserAvatarAdminService service = new UserAvatarAdminService();

        Map<String, Object> result = service.buildAvatarPayload(
                1L,
                "admin",
                "管理员",
                "https://cdn.example.com/upload.png",
                "UPLOAD",
                null
        );

        Assertions.assertEquals("UPLOAD", result.get("avatarSource"));
        Assertions.assertEquals("https://cdn.example.com/upload.png", result.get("effectiveAvatar"));
        Assertions.assertEquals(Boolean.TRUE, result.get("hasUploadedAvatar"));
        Assertions.assertEquals(Boolean.FALSE, result.get("hasWeixinAvatar"));
        Assertions.assertEquals(UserAvatarAdminService.DEFAULT_AVATAR, result.get("defaultAvatar"));
    }

    @Test
    public void prepareAvatarUpdate_shouldSwitchToWechatWhenWechatAvatarExists() {
        UserAvatarAdminService service = new UserAvatarAdminService();

        UserAvatarRequestDTO result = service.prepareAvatarUpdate(
                UserAvatarRequestDTO.builder().avatarSource("WECHAT").build(),
                "https://cdn.example.com/old-upload.png",
                "https://wx.example.com/avatar.png"
        );

        Assertions.assertEquals("WECHAT", result.getAvatarSource());
        Assertions.assertEquals("https://cdn.example.com/old-upload.png", result.getAvatar());
    }

    @Test
    public void prepareAvatarUpdate_shouldPreserveUploadedAvatarWhenSwitchingToDefault() {
        UserAvatarAdminService service = new UserAvatarAdminService();

        UserAvatarRequestDTO result = service.prepareAvatarUpdate(
                UserAvatarRequestDTO.builder().avatarSource("DEFAULT").build(),
                "https://cdn.example.com/existing-upload.png",
                "https://wx.example.com/avatar.png"
        );

        Assertions.assertEquals("DEFAULT", result.getAvatarSource());
        Assertions.assertEquals("https://cdn.example.com/existing-upload.png", result.getAvatar());
    }

    @Test
    public void prepareAvatarUpdate_shouldRejectUploadWhenNoUploadedAvatarExists() {
        UserAvatarAdminService service = new UserAvatarAdminService();

        AppException exception = Assertions.assertThrows(AppException.class, () -> service.prepareAvatarUpdate(
                UserAvatarRequestDTO.builder().avatarSource("UPLOAD").avatar("   ").build(),
                "",
                "https://wx.example.com/avatar.png"
        ));

        Assertions.assertEquals(ResponseCode.ILLEGAL_PARAMETER.getCode(), exception.getCode());
    }

    @Test
    public void applyCurrentUserAvatar_shouldExposeConsistentAvatarFields() {
        UserAvatarAdminService service = new UserAvatarAdminService();

        CurrentUserResponseDTO result = service.applyCurrentUserAvatar(
                CurrentUserResponseDTO.builder()
                        .id(3L)
                        .username("wx_user")
                        .displayName("微信用户")
                        .build(),
                "",
                "WECHAT",
                "https://wx.example.com/avatar.png"
        );

        Assertions.assertEquals("WECHAT", result.getAvatarSource());
        Assertions.assertEquals("https://wx.example.com/avatar.png", result.getEffectiveAvatar());
        Assertions.assertEquals(UserAvatarAdminService.DEFAULT_AVATAR, result.getDefaultAvatar());
        Assertions.assertEquals("https://wx.example.com/avatar.png", result.getWeixinImageUrl());
        Assertions.assertEquals("", result.getAvatar());
    }

    @Test
    public void bindExistingAccount_shouldReturnRequeriedAvatarFieldsAfterBinding() {
        WeixinLoginService service = new WeixinLoginService();
        ReflectionTestUtils.setField(service, "loginPort", loginPort);
        ReflectionTestUtils.setField(service, "loginReposity", loginRepository);
        ReflectionTestUtils.setField(service, "jwtUtil", jwtUtil);
        ReflectionTestUtils.setField(service, "authenticationManager", authenticationManager);

        BindExistingAccountRequestDTO request = new BindExistingAccountRequestDTO();
        request.setPendingToken("pending-token");
        request.setUsername("admin");
        request.setPassword("password");

        CurrentUserResponseDTO beforeBind = CurrentUserResponseDTO.builder()
                .id(1L)
                .username("admin")
                .displayName("管理员")
                .avatar("")
                .avatarSource("WECHAT")
                .effectiveAvatar(UserAvatarAdminService.DEFAULT_AVATAR)
                .defaultAvatar(UserAvatarAdminService.DEFAULT_AVATAR)
                .weixinBound(Boolean.FALSE)
                .weixinName("")
                .weixinImageUrl("")
                .build();
        CurrentUserResponseDTO afterBind = CurrentUserResponseDTO.builder()
                .id(1L)
                .username("admin")
                .displayName("管理员")
                .avatar("")
                .avatarSource("WECHAT")
                .effectiveAvatar("https://wx.example.com/avatar.png")
                .defaultAvatar(UserAvatarAdminService.DEFAULT_AVATAR)
                .weixinBound(Boolean.TRUE)
                .weixinName("微信昵称")
                .weixinImageUrl("https://wx.example.com/avatar.png")
                .build();

        when(loginRepository.checkLogin("pending-token")).thenReturn("openid-1");
        when(loginRepository.queryUserIdByAuthTypeAndAuthKey("wechat", "openid-1")).thenReturn(null);
        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(loginRepository.queryCurrentUser("admin")).thenReturn(beforeBind, afterBind);
        when(loginRepository.queryWeixinUserByOpenId("openid-1")).thenReturn(
                WeixinUserEntity.builder()
                        .weixinName("微信昵称")
                        .weixinImageUrl("https://wx.example.com/avatar.png")
                        .build()
        );
        when(jwtUtil.extractExpirationTime(any())).thenReturn(1776883200000L);

        LoginResponseDTO result = service.bindExistingAccount(request);

        Assertions.assertEquals("WECHAT", result.getUser().getAvatarSource());
        Assertions.assertEquals("https://wx.example.com/avatar.png", result.getUser().getEffectiveAvatar());
        Assertions.assertEquals("https://wx.example.com/avatar.png", result.getUser().getWeixinImageUrl());
        Assertions.assertEquals(Boolean.TRUE, result.getUser().getWeixinBound());
        verify(loginRepository).bindAuthToUser(1L, "wechat", "openid-1");
        verify(loginRepository, times(2)).queryCurrentUser("admin");
    }
}
