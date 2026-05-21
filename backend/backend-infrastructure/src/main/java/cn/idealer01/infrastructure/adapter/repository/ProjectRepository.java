package cn.idealer01.infrastructure.adapter.repository;

import cn.idealer01.domain.project.adapter.repository.IProjectRepository;
import cn.idealer01.domain.project.model.entity.ProjectEntity;
import cn.idealer01.infrastructure.dao.IProjectDao;
import cn.idealer01.infrastructure.dao.po.Project;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectRepository implements IProjectRepository {

    @Resource
    private IProjectDao projectDao;

    @Override
    public List<ProjectEntity> findAll() {
        return projectDao.queryAll().stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectEntity findById(Long id) {
        return toEntity(projectDao.queryById(id));
    }

    @Override
    public List<ProjectEntity> findPublicProjects() {
        return projectDao.queryPublicList().stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Long save(ProjectEntity project) {
        Project po = toPo(project);
        projectDao.insert(po);
        return po.getId();
    }

    @Override
    public void update(ProjectEntity project) {
        projectDao.update(toPo(project));
    }

    @Override
    public void deleteById(Long id) {
        projectDao.deleteById(id);
    }

    private ProjectEntity toEntity(Project project) {
        if (project == null) {
            return null;
        }

        return ProjectEntity.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .techStack(project.getTechStack())
                .projectUrl(project.getProjectUrl())
                .githubUrl(project.getGithubUrl())
                .previewUrl(project.getPreviewUrl())
                .coverImage(project.getCoverImage())
                .showcaseImages(project.getShowcaseImages())
                .status(project.getStatus())
                .sortOrder(project.getSortOrder())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .isPublic(project.getIsPublic())
                .enabled(project.getEnabled())
                .accessType(project.getAccessType())
                .allowedRoles(project.getAllowedRoles())
                .createTime(project.getCreateTime())
                .updateTime(project.getUpdateTime())
                .build();
    }

    private Project toPo(ProjectEntity project) {
        if (project == null) {
            return null;
        }

        return Project.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .techStack(project.getTechStack())
                .projectUrl(project.getProjectUrl())
                .githubUrl(project.getGithubUrl())
                .previewUrl(project.getPreviewUrl())
                .coverImage(project.getCoverImage())
                .showcaseImages(project.getShowcaseImages())
                .status(project.getStatus())
                .sortOrder(project.getSortOrder())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .isPublic(project.getIsPublic())
                .enabled(project.getEnabled())
                .accessType(project.getAccessType())
                .allowedRoles(project.getAllowedRoles())
                .createTime(project.getCreateTime())
                .updateTime(project.getUpdateTime())
                .build();
    }
}
