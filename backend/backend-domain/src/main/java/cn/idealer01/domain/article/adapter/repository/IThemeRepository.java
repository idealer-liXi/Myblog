package cn.idealer01.domain.article.adapter.repository;

import cn.idealer01.domain.article.model.entity.ThemeEntity;

import java.util.List;

public interface IThemeRepository {

    List<ThemeEntity> findAll();

    ThemeEntity findById(Long id);

    ThemeEntity findByName(String name);

    Long save(ThemeEntity theme);

    void update(ThemeEntity theme);

    void deleteById(Long id);

    int getArticleCount(String themeName);
}
