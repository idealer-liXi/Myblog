package cn.idealer01.trigger.http;

import cn.idealer01.api.IProjectAdminController;
import cn.idealer01.api.dto.ProjectAdminResponseDTO;
import cn.idealer01.api.dto.ProjectRequestDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.domain.project.service.IProjectService;
import cn.idealer01.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/projects")
public class ProjectAdminController implements IProjectAdminController {

    @Resource
    private IProjectService projectService;

    @Override
    @GetMapping("")
    public Response<List<ProjectAdminResponseDTO>> getProjects() {
        return Response.<List<ProjectAdminResponseDTO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(projectService.getProjects())
                .build();
    }

    @Override
    @GetMapping("/{projectId}")
    public Response<ProjectAdminResponseDTO> getProjectById(@PathVariable Long projectId) {
        ProjectAdminResponseDTO data = projectService.getProjectById(projectId);
        if (data == null) {
            return Response.<ProjectAdminResponseDTO>builder()
                    .code(ResponseCode.USER_NOT_EXIST.getCode())
                    .info("项目不存在")
                    .build();
        }

        return Response.<ProjectAdminResponseDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(data)
                .build();
    }

    @Override
    @PostMapping("")
    public Response<ProjectAdminResponseDTO> createProject(@RequestBody ProjectRequestDTO request) {
        return Response.<ProjectAdminResponseDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(projectService.createProject(request))
                .build();
    }

    @Override
    @PutMapping("/{projectId}")
    public Response<ProjectAdminResponseDTO> updateProject(@PathVariable Long projectId, @RequestBody ProjectRequestDTO request) {
        ProjectAdminResponseDTO data = projectService.updateProject(projectId, request);
        if (data == null) {
            return Response.<ProjectAdminResponseDTO>builder()
                    .code(ResponseCode.USER_NOT_EXIST.getCode())
                    .info("项目不存在")
                    .build();
        }

        return Response.<ProjectAdminResponseDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(data)
                .build();
    }

    @Override
    @DeleteMapping("/{projectId}")
    public Response<Void> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return Response.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .build();
    }
}
