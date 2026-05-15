package cn.idealer01.api;

import cn.idealer01.api.dto.ProjectAdminResponseDTO;
import cn.idealer01.api.dto.ProjectRequestDTO;
import cn.idealer01.api.response.Response;

import java.util.List;

public interface IProjectAdminController {
    Response<List<ProjectAdminResponseDTO>> getProjects();

    Response<ProjectAdminResponseDTO> getProjectById(Long projectId);

    Response<ProjectAdminResponseDTO> createProject(ProjectRequestDTO request);

    Response<ProjectAdminResponseDTO> updateProject(Long projectId, ProjectRequestDTO request);

    Response<Void> deleteProject(Long projectId);
}
