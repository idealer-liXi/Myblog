package cn.idealer01.domain.article.service;

import cn.idealer01.api.dto.ProjectAdminResponseDTO;
import cn.idealer01.api.dto.ProjectPublicResponseDTO;
import cn.idealer01.api.dto.ProjectRequestDTO;
import cn.idealer01.domain.article.adapter.repository.IProjectRepository;
import cn.idealer01.domain.article.model.entity.ProjectEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Service
public class ProjectService implements IProjectService {

    @Resource
    private IProjectRepository projectRepository;

    @Override
    public List<ProjectAdminResponseDTO> getProjects() {
        return projectRepository.findAll().stream()
                .map(this::toAdminResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectAdminResponseDTO getProjectById(Long projectId) {
        ProjectEntity entity = projectRepository.findById(projectId);
        return entity == null ? null : toAdminResponse(entity);
    }

    @Override
    public List<ProjectPublicResponseDTO> getPublicProjects() {
        return projectRepository.findPublicProjects().stream()
                .map(this::toPublicResponse)
                .collect(Collectors.toList());
    }

    public ProjectAdminResponseDTO createProject(ProjectRequestDTO request) {
        Date now = new Date();
        ProjectEntity entity = ProjectEntity.builder()
                .name(request.getName())
                .description(defaultString(request.getDescription()))
                .techStack(defaultString(request.getTechStack()))
                .projectUrl(defaultString(request.getProjectUrl()))
                .githubUrl(defaultString(request.getGithubUrl()))
                .previewUrl(defaultString(request.getPreviewUrl()))
                .coverImage(defaultString(request.getCoverImage()))
                .status(defaultString(request.getStatus(), "进行中"))
                .sortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder())
                .startDate(defaultString(request.getStartDate()))
                .endDate(defaultString(request.getEndDate()))
                .isPublic(request.getIsPublic() == null ? Boolean.TRUE : request.getIsPublic())
                .enabled(request.getEnabled() == null ? Boolean.TRUE : request.getEnabled())
                .accessType(defaultString(request.getAccessType(), "public"))
                .allowedRoles(defaultString(request.getAllowedRoles()))
                .createTime(now)
                .updateTime(now)
                .build();

        Long id = projectRepository.save(entity);
        entity.setId(id);
        return toAdminResponse(entity);
    }

    public ProjectAdminResponseDTO updateProject(Long projectId, ProjectRequestDTO request) {
        ProjectEntity entity = projectRepository.findById(projectId);
        if (entity == null) {
            return null;
        }

        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getTechStack() != null) {
            entity.setTechStack(request.getTechStack());
        }
        if (request.getProjectUrl() != null) {
            entity.setProjectUrl(request.getProjectUrl());
        }
        if (request.getGithubUrl() != null) {
            entity.setGithubUrl(request.getGithubUrl());
        }
        if (request.getPreviewUrl() != null) {
            entity.setPreviewUrl(request.getPreviewUrl());
        }
        if (request.getCoverImage() != null) {
            entity.setCoverImage(request.getCoverImage());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        if (request.getSortOrder() != null) {
            entity.setSortOrder(request.getSortOrder());
        }
        if (request.getStartDate() != null) {
            entity.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            entity.setEndDate(request.getEndDate());
        }
        if (request.getIsPublic() != null) {
            entity.setIsPublic(request.getIsPublic());
        }
        if (request.getEnabled() != null) {
            entity.setEnabled(request.getEnabled());
        }
        if (request.getAccessType() != null) {
            entity.setAccessType(request.getAccessType());
        }
        if (request.getAllowedRoles() != null) {
            entity.setAllowedRoles(request.getAllowedRoles());
        }

        entity.setUpdateTime(new Date());
        projectRepository.update(entity);
        return toAdminResponse(entity);
    }

    public void deleteProject(Long projectId) {
        projectRepository.deleteById(projectId);
    }

    private ProjectAdminResponseDTO toAdminResponse(ProjectEntity entity) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        return ProjectAdminResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .techStack(entity.getTechStack())
                .projectUrl(entity.getProjectUrl())
                .githubUrl(entity.getGithubUrl())
                .previewUrl(entity.getPreviewUrl())
                .coverImage(entity.getCoverImage())
                .status(entity.getStatus())
                .sortOrder(entity.getSortOrder())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .isPublic(entity.getIsPublic())
                .enabled(entity.getEnabled())
                .accessType(entity.getAccessType())
                .allowedRoles(entity.getAllowedRoles())
                .createdAt(formatDate(entity.getCreateTime(), formatter))
                .updatedAt(formatDate(entity.getUpdateTime(), formatter))
                .build();
    }

    private ProjectPublicResponseDTO toPublicResponse(ProjectEntity entity) {
        return ProjectPublicResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .techStack(entity.getTechStack())
                .projectUrl(entity.getProjectUrl())
                .githubUrl(entity.getGithubUrl())
                .previewUrl(entity.getPreviewUrl())
                .coverImage(entity.getCoverImage())
                .status(entity.getStatus())
                .sortOrder(entity.getSortOrder())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .isPublic(entity.getIsPublic())
                .build();
    }

    private String formatDate(Date date, SimpleDateFormat formatter) {
        return date == null ? null : formatter.format(date);
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private String defaultString(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }
}
