package cn.idealer01.infrastructure.adapter.repository;

import cn.idealer01.domain.article.adapter.repository.IThemeRepository;
import cn.idealer01.domain.article.model.entity.ThemeEntity;
import cn.idealer01.infrastructure.dao.IThemeDao;
import cn.idealer01.infrastructure.dao.po.Theme;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ThemeRepository implements IThemeRepository {

    @Resource
    private IThemeDao themeDao;

    @Override
    public List<ThemeEntity> findAll() {
        return themeDao.queryAll().stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public ThemeEntity findById(Long id) {
        return toEntity(themeDao.queryById(id));
    }

    @Override
    public ThemeEntity findByName(String name) {
        return toEntity(themeDao.queryByName(name));
    }

    @Override
    public Long save(ThemeEntity theme) {
        Theme po = toPo(theme);
        themeDao.insert(po);
        return po.getId();
    }

    @Override
    public void update(ThemeEntity theme) {
        themeDao.update(toPo(theme));
    }

    @Override
    public void deleteById(Long id) {
        themeDao.deleteById(id);
    }

    @Override
    public int getArticleCount(String themeName) {
        return 0;
    }

    private ThemeEntity toEntity(Theme theme) {
        if (null == theme) {
            return null;
        }

        return ThemeEntity.builder()
                .id(theme.getId())
                .name(theme.getName())
                .icon(theme.getIcon())
                .color(theme.getColor())
                .sortOrder(theme.getSortOrder())
                .articleCount(theme.getArticleCount())
                .createTime(theme.getCreateTime())
                .updateTime(theme.getUpdateTime())
                .build();
    }

    private Theme toPo(ThemeEntity theme) {
        if (null == theme) {
            return null;
        }

        return Theme.builder()
                .id(theme.getId())
                .name(theme.getName())
                .icon(theme.getIcon())
                .color(theme.getColor())
                .sortOrder(theme.getSortOrder())
                .articleCount(theme.getArticleCount())
                .createTime(theme.getCreateTime())
                .updateTime(theme.getUpdateTime())
                .build();
    }
}
