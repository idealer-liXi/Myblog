package cn.idealer01.api;

import cn.idealer01.api.dto.ProjectPublicResponseDTO;
import cn.idealer01.api.dto.ProjectShowcasePublicResponseDTO;

import java.util.List;

public interface IProjectPublicController {
    List<ProjectPublicResponseDTO> getProjects();

    List<ProjectShowcasePublicResponseDTO> getProjectShowcase();
}
