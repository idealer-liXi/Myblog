package cn.idealer01.domain;

import cn.idealer01.domain.article.service.IImageService;
import cn.idealer01.trigger.http.ImageAdminController;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class IProjectImageControllerTest {

    private MockMvc mockMvc;
    private IImageService imageService;

    @Before
    public void setUp() {
        ImageAdminController controller = new ImageAdminController();
        imageService = mock(IImageService.class);
        ReflectionTestUtils.setField(controller, "imageService", imageService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void getProjectImages_shouldReturnProjectCoverList() throws Exception {
        Map<String, Object> image = new HashMap<>();
        image.put("projectId", 1L);
        image.put("projectName", "MyBlog");
        image.put("hasCover", true);
        when(imageService.getProjectImages()).thenReturn(Collections.singletonList(image));

        mockMvc.perform(get("/api/admin/project-images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.data[0].projectName").value("MyBlog"));
    }

    @Test
    public void getProjectImageByProjectId_shouldWrapDetailPayload() throws Exception {
        Map<String, Object> image = new HashMap<>();
        image.put("projectId", 9L);
        image.put("projectName", "Detail");
        image.put("hasCover", true);
        when(imageService.getProjectImageByProjectId(9L)).thenReturn(image);

        mockMvc.perform(get("/api/admin/project-images/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.data.projectId").value(9))
                .andExpect(jsonPath("$.data.projectName").value("Detail"));
    }

    @Test
    public void getProjectImageByProjectId_shouldReturnNonSuccessWhenMissing() throws Exception {
        when(imageService.getProjectImageByProjectId(99L)).thenReturn(null);

        mockMvc.perform(get("/api/admin/project-images/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0005"))
                .andExpect(jsonPath("$.info").value("项目不存在"));
    }

    @Test
    public void clearProjectImage_shouldReturnUnifiedSuccess() throws Exception {
        mockMvc.perform(delete("/api/admin/project-images/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"));

        verify(imageService).clearProjectImage(1L);
    }
}
