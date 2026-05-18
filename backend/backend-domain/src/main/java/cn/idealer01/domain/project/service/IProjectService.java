package cn.idealer01.domain.project.service;

import cn.idealer01.api.dto.ProjectAdminResponseDTO;
import cn.idealer01.api.dto.ProjectPublicResponseDTO;
import cn.idealer01.api.dto.ProjectRequestDTO;

import java.util.List;

public interface IProjectService {
    List<ProjectAdminResponseDTO> getProjects();

    ProjectAdminResponseDTO getProjectById(Long projectId);

    ProjectAdminResponseDTO createProject(ProjectRequestDTO request);

    ProjectAdminResponseDTO updateProject(Long projectId, ProjectRequestDTO request);

    void deleteProject(Long projectId);

    List<ProjectPublicResponseDTO> getPublicProjects();
}
