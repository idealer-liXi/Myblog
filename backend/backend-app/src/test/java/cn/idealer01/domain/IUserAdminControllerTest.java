package cn.idealer01.domain;

import cn.idealer01.api.dto.CurrentUserResponseDTO;
import cn.idealer01.domain.auth.service.IUserAdminService;
import cn.idealer01.trigger.http.UserAdminController;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class IUserAdminControllerTest {

    private MockMvc mockMvc;
    private IUserAdminService userAdminService;

    @Before
    public void setUp() {
        UserAdminController controller = new UserAdminController();
        userAdminService = mock(IUserAdminService.class);
        ReflectionTestUtils.setField(controller, "userAdminService", userAdminService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void getUsers_shouldReturnUnifiedListPayload() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("total", 1L);
        payload.put("page", 1);
        payload.put("pageSize", 10);
        payload.put("users", Collections.singletonList(
                CurrentUserResponseDTO.builder().id(1L).displayName("管理员").status("active").build()
        ));
        when(userAdminService.getUsers(isNull(), isNull(), isNull(), eq(1), eq(10))).thenReturn(payload);

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.data.users[0].displayName").value("管理员"));
    }

    @Test
    public void getUsers_shouldPassQueryParams() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("total", 0L);
        payload.put("page", 2);
        payload.put("pageSize", 5);
        payload.put("users", Collections.emptyList());
        when(userAdminService.getUsers(eq("test"), eq("wechat"), eq("active"), eq(2), eq(5))).thenReturn(payload);

        mockMvc.perform(get("/api/admin/users")
                        .param("keyword", "test")
                        .param("loginType", "wechat")
                        .param("status", "active")
                        .param("page", "2")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    public void getUserById_shouldReturnDetailPayload() throws Exception {
        when(userAdminService.getUserById(1L)).thenReturn(
                CurrentUserResponseDTO.builder().id(1L).displayName("管理员").status("active").build()
        );

        mockMvc.perform(get("/api/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.displayName").value("管理员"));
    }

    @Test
    public void getUserById_shouldReturnErrorWhenNotFound() throws Exception {
        when(userAdminService.getUserById(99L)).thenThrow(
                new cn.idealer01.types.exception.AppException(cn.idealer01.types.enums.ResponseCode.USER_NOT_EXIST)
        );

        mockMvc.perform(get("/api/admin/users/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0005"));
    }

    @Test
    public void updateUserStatus_shouldWrapUpdatedUser() throws Exception {
        when(userAdminService.updateUserStatus(1L, "disabled")).thenReturn(
                CurrentUserResponseDTO.builder().id(1L).status("disabled").build()
        );

        mockMvc.perform(put("/api/admin/users/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"disabled\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.data.status").value("disabled"));
    }

    @Test
    public void deleteUser_shouldReturnUnifiedSuccess() throws Exception {
        mockMvc.perform(delete("/api/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"));

        verify(userAdminService).deleteUser(1L);
    }
}