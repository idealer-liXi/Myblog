package cn.idealer01.domain;

import cn.idealer01.api.dto.CurrentUserResponseDTO;
import cn.idealer01.api.dto.LoginResponseDTO;
import cn.idealer01.api.dto.RegularUserRequestDTO;
import cn.idealer01.domain.auth.service.IRegularUserLoginService;
import cn.idealer01.trigger.http.RegularUserController;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ILoginControllerTest {

    private MockMvc mockMvc;
    private IRegularUserLoginService loginService;

    @Before
    public void setUp() {
        RegularUserController controller = new RegularUserController();
        loginService = mock(IRegularUserLoginService.class);
        ReflectionTestUtils.setField(controller, "loginService", loginService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
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

        String result = mockMvc.perform(post("/api/r1/login/token")
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
}
