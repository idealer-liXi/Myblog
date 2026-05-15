package cn.idealer01.domain.article.adapter.repository;

import cn.idealer01.domain.article.model.entity.ProjectEntity;

import java.util.List;

public interface IProjectRepository {

    List<ProjectEntity> findAll();

    ProjectEntity findById(Long id);

    List<ProjectEntity> findPublicProjects();

    Long save(ProjectEntity project);

    void update(ProjectEntity project);

    void deleteById(Long id);
}
