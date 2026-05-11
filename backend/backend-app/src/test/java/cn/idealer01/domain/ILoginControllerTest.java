package cn.idealer01.domain;

import cn.idealer01.api.dto.CurrentUserResponseDTO;
import cn.idealer01.api.dto.LoginResponseDTO;
import cn.idealer01.api.dto.RegularUserRequestDTO;
import cn.idealer01.api.dto.ThirdPartyPendingLoginResponseDTO;
import cn.idealer01.domain.auth.adapter.repository.ILoginRepository;
import cn.idealer01.domain.auth.service.IRegularUserLoginService;
import cn.idealer01.domain.auth.service.IWeixinLoginService;
import cn.idealer01.trigger.http.RegularUserController;
import cn.idealer01.trigger.http.UserProfileController;
import cn.idealer01.trigger.http.WeixinLoginController;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ILoginControllerTest {

    private MockMvc loginMockMvc;
    private MockMvc profileMockMvc;
    private MockMvc weixinMockMvc;
    private IRegularUserLoginService loginService;
    private ILoginRepository loginRepository;
    private IWeixinLoginService weixinLoginService;

    @Before
    public void setUp() {
        RegularUserController controller = new RegularUserController();
        UserProfileController userProfileController = new UserProfileController();
        WeixinLoginController weixinLoginController = new WeixinLoginController();
        loginService = mock(IRegularUserLoginService.class);
        loginRepository = mock(ILoginRepository.class);
        weixinLoginService = mock(IWeixinLoginService.class);
        ReflectionTestUtils.setField(controller, "loginService", loginService);
        ReflectionTestUtils.setField(userProfileController, "loginRepository", loginRepository);
        ReflectionTestUtils.setField(userProfileController, "weixinLoginService", weixinLoginService);
        ReflectionTestUtils.setField(weixinLoginController, "loginService", weixinLoginService);
        loginMockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        profileMockMvc = MockMvcBuilders.standaloneSetup(userProfileController).build();
        weixinMockMvc = MockMvcBuilders.standaloneSetup(weixinLoginController).build();
    }

    @Test
    public void testRegularUserLoginReturnsUnifiedPayload() throws Exception {
        when(loginService.getToken("admin", "password"))
                .thenReturn(LoginResponseDTO.builder()
                        .token("raw-jwt-token")
                        .expiresAt(1776883200000L)
                        .user(CurrentUserResponseDTO.builder()
                                .username("admin")
                                .displayName("admin")
                                .loginType("password")
                                .build())
                        .build());

        String result = loginMockMvc.perform(post("/api/r1/login/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.expiresAt").isNumber())
                .andExpect(jsonPath("$.data.user.username").value("admin"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        System.out.println(result);
    }

    @Test
    public void testCurrentUserEndpointReturnsProfile() throws Exception {
        when(loginRepository.queryCurrentUser("admin"))
                .thenReturn(CurrentUserResponseDTO.builder()
                        .id(1L)
                        .username("admin")
                        .displayName("admin")
                        .loginType("password")
                        .roles(java.util.Collections.singletonList("ADMIN"))
                        .weixinBound(Boolean.FALSE)
                        .weixinName("")
                        .build());

        profileMockMvc.perform(get("/api/user/me")
                        .principal(new TestingAuthenticationToken("admin", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.roles[0]").value("ADMIN"));
    }

    @Test
    public void testWechatCheckLoginReturnsStructuredPayload() throws Exception {
        when(weixinLoginService.checkLogin("mock-ticket"))
                .thenReturn(ThirdPartyPendingLoginResponseDTO.builder()
                        .status("PENDING_DECISION")
                        .pendingToken("mock-ticket")
                        .authType("wechat")
                        .authKey("openid_xxx")
                        .displayName("微信昵称")
                        .avatar("https://wx.qlogo.cn/mock.png")
                        .weixinName("微信昵称")
                        .build());

        weixinMockMvc.perform(get("/api/v1/login/check_login").param("ticket", "mock-ticket"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.data").isMap())
                .andExpect(jsonPath("$.data.status").value("PENDING_DECISION"));
    }

    @Test
    public void testBindExistingAccountFromPendingWechatLogin() throws Exception {
        when(weixinLoginService.bindExistingAccount(org.mockito.ArgumentMatchers.any()))
                .thenReturn(LoginResponseDTO.builder()
                        .token("wechat-bind-token")
                        .expiresAt(1776883200000L)
                        .user(CurrentUserResponseDTO.builder()
                                .id(1L)
                                .username("admin")
                                .displayName("admin")
                                .loginType("wechat")
                                .roles(java.util.Collections.singletonList("ADMIN"))
                                .weixinBound(Boolean.TRUE)
                                .weixinName("微信昵称")
                                .build())
                        .build());

        weixinMockMvc.perform(post("/api/v1/login/weixin/bind-existing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pendingToken\":\"mock\",\"username\":\"admin\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    public void testCreateAccountFromPendingWechatLogin() throws Exception {
        when(weixinLoginService.createAccount(org.mockito.ArgumentMatchers.any()))
                .thenReturn(LoginResponseDTO.builder()
                        .token("wechat-create-token")
                        .expiresAt(1776883200000L)
                        .user(CurrentUserResponseDTO.builder()
                                .id(2L)
                                .username("wx_openid_xxx")
                                .displayName("微信昵称")
                                .loginType("wechat")
                                .roles(java.util.Collections.singletonList("USER"))
                                .weixinBound(Boolean.TRUE)
                                .weixinName("微信昵称")
                                .build())
                        .build());

        weixinMockMvc.perform(post("/api/v1/login/weixin/create-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pendingToken\":\"mock\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.username").exists());
    }

    @Test
    public void testFetchWechatBindQrCodeWhenLoggedIn() throws Exception {
        when(weixinLoginService.createBindQrCodeTicket()).thenReturn("bind-ticket");

        profileMockMvc.perform(get("/api/user/weixin/bind/qrcode")
                        .principal(new TestingAuthenticationToken("admin", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("bind-ticket"));
    }

    @Test
    public void testDeleteWechatBindWhenLoggedIn() throws Exception {
        profileMockMvc.perform(delete("/api/user/weixin/bind")
                        .principal(new TestingAuthenticationToken("admin", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"));
    }
}
