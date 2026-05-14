package cn.idealer01.domain.article.adapter.repository;

import java.util.List;

import cn.idealer01.domain.article.model.entity.ArticleEntity;

public interface IArticleRepository {

    ArticleEntity findById(Long id);

    List<ArticleEntity> findAll(String keyword, String status, int offset, int limit);

    long count(String keyword, String status);

    List<ArticleEntity> findByTheme(String theme);

    Long save(ArticleEntity article);

    void update(ArticleEntity article);

    void deleteById(Long id);

    void incrementViewCount(Long id);
}
