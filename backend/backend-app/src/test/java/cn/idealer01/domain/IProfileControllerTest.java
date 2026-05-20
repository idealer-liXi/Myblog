package cn.idealer01.domain;

import cn.idealer01.api.dto.ProfileResponseDTO;
import cn.idealer01.domain.profile.service.IProfileService;
import cn.idealer01.trigger.http.ProfileAdminController;
import cn.idealer01.trigger.http.ProfilePublicController;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class IProfileControllerTest {

    private MockMvc publicMockMvc;
    private MockMvc adminMockMvc;
    private IProfileService profileService;

    @Before
    public void setUp() {
        ProfilePublicController publicController = new ProfilePublicController();
        ProfileAdminController adminController = new ProfileAdminController();
        profileService = mock(IProfileService.class);
        ReflectionTestUtils.setField(publicController, "profileService", profileService);
        ReflectionTestUtils.setField(adminController, "profileService", profileService);
        publicMockMvc = MockMvcBuilders.standaloneSetup(publicController).build();
        adminMockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }

    @Test
    public void getPublicProfile_shouldReturnPlainProfilePayload() throws Exception {
        when(profileService.getPublicProfile()).thenReturn(ProfileResponseDTO.builder().name("Idealer").bio("Java开发工程师").schools(Collections.emptyList()).build());

        publicMockMvc.perform(get("/api/public/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Idealer"))
                .andExpect(jsonPath("$.bio").value("Java开发工程师"));
    }

    @Test
    public void getAdminProfile_shouldWrapProfilePayload() throws Exception {
        when(profileService.getAdminProfile()).thenReturn(ProfileResponseDTO.builder().name("Idealer").schools(Collections.emptyList()).build());

        adminMockMvc.perform(get("/api/admin/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.data.name").value("Idealer"));
    }

    @Test
    public void updateAdminProfile_shouldWrapUpdatedPayload() throws Exception {
        when(profileService.updateProfile(any())).thenReturn(ProfileResponseDTO.builder().name("Updated").schools(Collections.emptyList()).build());

        adminMockMvc.perform(put("/api/admin/profile")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated\",\"bio\":\"AI Engineer\",\"hobbies\":[\"编程\"],\"undergraduateHonors\":[\"奖项A\"],\"graduateHonors\":[\"奖项B\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated"));
    }
}
