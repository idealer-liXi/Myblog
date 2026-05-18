package cn.idealer01.domain;

import cn.idealer01.api.dto.ProjectAdminResponseDTO;
import cn.idealer01.api.dto.ProjectPublicResponseDTO;
import cn.idealer01.api.dto.ProjectRequestDTO;
import cn.idealer01.domain.project.adapter.repository.IProjectRepository;
import cn.idealer01.domain.project.model.entity.ProjectEntity;
import cn.idealer01.domain.project.service.ProjectService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private IProjectRepository projectRepository;

    @Test
    void createProject_shouldApplyDefaultFutureFields() {
        ProjectService projectService = new ProjectService();
        ReflectionTestUtils.setField(projectService, "projectRepository", projectRepository);

        ProjectRequestDTO request = new ProjectRequestDTO();
        request.setName("MyBlog");
        request.setIsPublic(Boolean.TRUE);

        when(projectRepository.save(any(ProjectEntity.class))).thenAnswer(invocation -> {
            ProjectEntity entity = invocation.getArgument(0);
            entity.setId(9L);
            return 9L;
        });

        ProjectAdminResponseDTO result = projectService.createProject(request);

        Assertions.assertEquals(Long.valueOf(9L), result.getId());
        Assertions.assertEquals("MyBlog", result.getName());
        Assertions.assertEquals(Boolean.TRUE, result.getIsPublic());
        Assertions.assertEquals(Boolean.TRUE, result.getEnabled());
        Assertions.assertEquals("public", result.getAccessType());
        Assertions.assertEquals("", result.getAllowedRoles());
        Assertions.assertEquals("进行中", result.getStatus());
    }

    @Test
    void getPublicProjects_shouldReturnRepositoryMappedPayload() {
        ProjectService projectService = new ProjectService();
        ReflectionTestUtils.setField(projectService, "projectRepository", projectRepository);

        when(projectRepository.findPublicProjects()).thenReturn(java.util.Arrays.asList(
                ProjectEntity.builder().id(2L).name("Second").isPublic(Boolean.TRUE).status("进行中").build(),
                ProjectEntity.builder().id(1L).name("First").isPublic(Boolean.TRUE).status("已完成").build()
        ));

        java.util.List<ProjectPublicResponseDTO> result = projectService.getPublicProjects();

        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(Long.valueOf(2L), result.get(0).getId());
        Assertions.assertEquals("Second", result.get(0).getName());
        Assertions.assertEquals(Boolean.TRUE, result.get(0).getIsPublic());
        verify(projectRepository).findPublicProjects();
    }

    @Test
    void updateProject_shouldOverwriteEditableFields() {
        ProjectService projectService = new ProjectService();
        ReflectionTestUtils.setField(projectService, "projectRepository", projectRepository);

        ProjectEntity existing = ProjectEntity.builder()
                .id(5L)
                .name("Old")
                .status("进行中")
                .isPublic(Boolean.TRUE)
                .enabled(Boolean.TRUE)
                .accessType("public")
                .allowedRoles("")
                .build();
        when(projectRepository.findById(5L)).thenReturn(existing);
        doNothing().when(projectRepository).update(any(ProjectEntity.class));

        ProjectRequestDTO request = new ProjectRequestDTO();
        request.setName("New");
        request.setStatus("已完成");
        request.setEnabled(Boolean.FALSE);
        request.setAccessType("restricted");
        request.setAllowedRoles("ADMIN,USER");

        ProjectAdminResponseDTO result = projectService.updateProject(5L, request);

        Assertions.assertEquals(Long.valueOf(5L), result.getId());
        Assertions.assertEquals("New", result.getName());
        Assertions.assertEquals("已完成", result.getStatus());
        Assertions.assertEquals(Boolean.FALSE, result.getEnabled());
        Assertions.assertEquals("restricted", result.getAccessType());
        Assertions.assertEquals("ADMIN,USER", result.getAllowedRoles());
        verify(projectRepository).update(any(ProjectEntity.class));
    }

    @Test
    void getProjects_shouldMapRepositoryEntitiesToAdminPayload() {
        ProjectService projectService = new ProjectService();
        ReflectionTestUtils.setField(projectService, "projectRepository", projectRepository);

        when(projectRepository.findAll()).thenReturn(java.util.Collections.singletonList(
                ProjectEntity.builder().id(3L).name("Admin View").enabled(Boolean.TRUE).build()
        ));

        java.util.List<ProjectAdminResponseDTO> result = projectService.getProjects();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(Long.valueOf(3L), result.get(0).getId());
        Assertions.assertEquals("Admin View", result.get(0).getName());
        verify(projectRepository).findAll();
    }

    @Test
    void deleteProject_shouldDelegateToRepository() {
        ProjectService projectService = new ProjectService();
        ReflectionTestUtils.setField(projectService, "projectRepository", projectRepository);

        doNothing().when(projectRepository).deleteById(7L);

        projectService.deleteProject(7L);

        verify(projectRepository).deleteById(7L);
    }

    @Test
    void getProjectById_shouldReturnMappedAdminPayload() {
        ProjectService projectService = new ProjectService();
        ReflectionTestUtils.setField(projectService, "projectRepository", projectRepository);

        when(projectRepository.findById(11L)).thenReturn(
                ProjectEntity.builder().id(11L).name("Detail").enabled(Boolean.TRUE).build()
        );

        ProjectAdminResponseDTO result = projectService.getProjectById(11L);

        Assertions.assertEquals(Long.valueOf(11L), result.getId());
        Assertions.assertEquals("Detail", result.getName());
        verify(projectRepository).findById(11L);
    }
}
