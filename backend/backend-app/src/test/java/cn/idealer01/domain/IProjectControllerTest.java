package cn.idealer01.domain;

import cn.idealer01.api.dto.ProjectAdminResponseDTO;
import cn.idealer01.api.dto.ProjectPublicResponseDTO;
import cn.idealer01.domain.project.service.IProjectService;
import cn.idealer01.trigger.http.ProjectAdminController;
import cn.idealer01.trigger.http.ProjectPublicController;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class IProjectControllerTest {

    private MockMvc adminMockMvc;
    private MockMvc publicMockMvc;
    private IProjectService projectService;

    @Before
    public void setUp() {
        ProjectAdminController adminController = new ProjectAdminController();
        ProjectPublicController publicController = new ProjectPublicController();
        projectService = mock(IProjectService.class);
        ReflectionTestUtils.setField(adminController, "projectService", projectService);
        ReflectionTestUtils.setField(publicController, "projectService", projectService);
        adminMockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
        publicMockMvc = MockMvcBuilders.standaloneSetup(publicController).build();
    }

    @Test
    public void getAdminProjects_shouldWrapServicePayloadInUnifiedResponse() throws Exception {
        when(projectService.getProjects()).thenReturn(Collections.singletonList(
                ProjectAdminResponseDTO.builder().id(1L).name("MyBlog").enabled(Boolean.TRUE).build()
        ));

        adminMockMvc.perform(get("/api/admin/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.data[0].name").value("MyBlog"));
    }

    @Test
    public void getPublicProjects_shouldReturnPlainPublicList() throws Exception {
        when(projectService.getPublicProjects()).thenReturn(Collections.singletonList(
                ProjectPublicResponseDTO.builder().id(1L).name("MyBlog").isPublic(Boolean.TRUE).build()
        ));

        publicMockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("MyBlog"))
                .andExpect(jsonPath("$[0].isPublic").value(true));
    }

    @Test
    public void getAdminProjectById_shouldWrapDetailPayload() throws Exception {
        when(projectService.getProjectById(9L)).thenReturn(
                ProjectAdminResponseDTO.builder().id(9L).name("Detail").build()
        );

        adminMockMvc.perform(get("/api/admin/projects/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.data.id").value(9))
                .andExpect(jsonPath("$.data.name").value("Detail"));
    }

    @Test
    public void getAdminProjectById_shouldReturnNonSuccessWhenMissing() throws Exception {
        when(projectService.getProjectById(99L)).thenReturn(null);

        adminMockMvc.perform(get("/api/admin/projects/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0005"))
                .andExpect(jsonPath("$.info").value("项目不存在"));
    }

    @Test
    public void createAdminProject_shouldWrapCreatedPayload() throws Exception {
        when(projectService.createProject(org.mockito.ArgumentMatchers.any())).thenReturn(
                ProjectAdminResponseDTO.builder().id(5L).name("Created").build()
        );

        adminMockMvc.perform(post("/api/admin/projects")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Created\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(5))
                .andExpect(jsonPath("$.data.name").value("Created"));
    }

    @Test
    public void updateAdminProject_shouldWrapUpdatedPayload() throws Exception {
        when(projectService.updateProject(org.mockito.ArgumentMatchers.eq(5L), org.mockito.ArgumentMatchers.any())).thenReturn(
                ProjectAdminResponseDTO.builder().id(5L).name("Updated").build()
        );

        adminMockMvc.perform(put("/api/admin/projects/5")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated"));
    }

    @Test
    public void updateAdminProject_shouldReturnNonSuccessWhenMissing() throws Exception {
        when(projectService.updateProject(org.mockito.ArgumentMatchers.eq(404L), org.mockito.ArgumentMatchers.any())).thenReturn(null);

        adminMockMvc.perform(put("/api/admin/projects/404")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0005"))
                .andExpect(jsonPath("$.info").value("项目不存在"));
    }

    @Test
    public void deleteAdminProject_shouldReturnUnifiedSuccess() throws Exception {
        adminMockMvc.perform(delete("/api/admin/projects/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"));
    }
}
