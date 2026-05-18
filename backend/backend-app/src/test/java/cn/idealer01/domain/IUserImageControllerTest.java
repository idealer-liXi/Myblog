package cn.idealer01.domain;

import cn.idealer01.api.dto.UserAvatarRequestDTO;
import cn.idealer01.domain.auth.service.IUserAvatarAdminService;
import cn.idealer01.trigger.http.UserImageAdminController;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class IUserImageControllerTest {

    private MockMvc mockMvc;
    private IUserAvatarAdminService userAvatarAdminService;

    @Before
    public void setUp() {
        UserImageAdminController controller = new UserImageAdminController();
        userAvatarAdminService = mock(IUserAvatarAdminService.class);
        ReflectionTestUtils.setField(controller, "userAvatarAdminService", userAvatarAdminService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void getUserImages_shouldReturnUnifiedAvatarList() throws Exception {
        Map<String, Object> user = new HashMap<>();
        user.put("userId", 1L);
        user.put("displayName", "管理员");
        user.put("avatarSource", "DEFAULT");
        when(userAvatarAdminService.getUserAvatars()).thenReturn(Collections.singletonList(user));

        mockMvc.perform(get("/api/admin/user-images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.data[0].displayName").value("管理员"));
    }

    @Test
    public void getUserImageByUserId_shouldWrapDetailPayload() throws Exception {
        Map<String, Object> user = new HashMap<>();
        user.put("userId", 9L);
        user.put("displayName", "张三");
        user.put("avatarSource", "WECHAT");
        when(userAvatarAdminService.getUserAvatarByUserId(9L)).thenReturn(user);

        mockMvc.perform(get("/api/admin/user-images/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.data.userId").value(9))
                .andExpect(jsonPath("$.data.displayName").value("张三"));
    }

    @Test
    public void getUserImageByUserId_shouldReturnNonSuccessWhenMissing() throws Exception {
        when(userAvatarAdminService.getUserAvatarByUserId(99L)).thenReturn(null);

        mockMvc.perform(get("/api/admin/user-images/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0005"))
                .andExpect(jsonPath("$.info").value("用户不存在"));
    }

    @Test
    public void updateUserImage_shouldWrapUpdatedPayload() throws Exception {
        Map<String, Object> user = new HashMap<>();
        user.put("userId", 1L);
        user.put("avatarSource", "WECHAT");
        when(userAvatarAdminService.updateUserAvatar(eq(1L), any(UserAvatarRequestDTO.class))).thenReturn(user);

        mockMvc.perform(put("/api/admin/user-images/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"avatarSource\":\"WECHAT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.avatarSource").value("WECHAT"));
    }

    @Test
    public void clearUserImage_shouldReturnUnifiedSuccess() throws Exception {
        mockMvc.perform(delete("/api/admin/user-images/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"));

        verify(userAvatarAdminService).clearUploadedAvatar(1L);
    }
}
