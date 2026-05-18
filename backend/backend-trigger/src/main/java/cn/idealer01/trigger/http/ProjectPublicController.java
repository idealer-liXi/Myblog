package cn.idealer01.trigger.http;

import cn.idealer01.api.IProjectPublicController;
import cn.idealer01.api.dto.ProjectPublicResponseDTO;
import cn.idealer01.domain.project.service.IProjectService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectPublicController implements IProjectPublicController {

    @Resource
    private IProjectService projectService;

    @Override
    @GetMapping("")
    public List<ProjectPublicResponseDTO> getProjects() {
        return projectService.getPublicProjects();
    }
}
