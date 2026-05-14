package cn.idealer01.domain.article.service;

import cn.idealer01.api.dto.ThemeRequestDTO;
import cn.idealer01.api.dto.ThemeResponseDTO;
import cn.idealer01.domain.article.adapter.repository.IThemeRepository;
import cn.idealer01.domain.article.model.entity.ThemeEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Service
public class ThemeService implements IThemeService {

    @Resource
    private IThemeRepository themeRepository;

    private ThemeResponseDTO toThemeResponseDTO(ThemeEntity theme) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        return ThemeResponseDTO.builder()
                .id(theme.getId())
                .name(theme.getName())
                .icon(theme.getIcon())
                .color(theme.getColor())
                .sortOrder(theme.getSortOrder())
                .articleCount(theme.getArticleCount() == null ? 0 : theme.getArticleCount())
                .createdAt(formatDate(theme.getCreateTime(), formatter))
                .updatedAt(formatDate(theme.getUpdateTime(), formatter))
                .build();
    }

    private String formatDate(Date date, SimpleDateFormat formatter) {
        return date == null ? null : formatter.format(date);
    }

    @Override
    public List<ThemeResponseDTO> getThemes() {
        return themeRepository.findAll().stream()
                .map(this::toThemeResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ThemeResponseDTO getThemeById(Long themeId) {
        ThemeEntity theme = themeRepository.findById(themeId);
        if (theme == null) {
            return null;
        }
        return toThemeResponseDTO(theme);
    }

    @Override
    public ThemeResponseDTO createTheme(ThemeRequestDTO request) {
        Date now = new Date();
        ThemeEntity theme = ThemeEntity.builder()
                .name(request.getName())
                .icon(request.getIcon() == null ? "" : request.getIcon())
                .color(request.getColor() == null ? "#007bff" : request.getColor())
                .sortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder())
                .articleCount(0)
                .createTime(now)
                .updateTime(now)
                .build();

        Long themeId = themeRepository.save(theme);
        theme.setId(themeId);
        return toThemeResponseDTO(theme);
    }

    @Override
    public ThemeResponseDTO updateTheme(Long themeId, ThemeRequestDTO request) {
        ThemeEntity theme = themeRepository.findById(themeId);
        if (theme == null) {
            return null;
        }

        if (request.getName() != null) {
            theme.setName(request.getName());
        }
        if (request.getIcon() != null) {
            theme.setIcon(request.getIcon());
        }
        if (request.getColor() != null) {
            theme.setColor(request.getColor());
        }
        if (request.getSortOrder() != null) {
            theme.setSortOrder(request.getSortOrder());
        }

        theme.setUpdateTime(new Date());
        themeRepository.update(theme);
        return toThemeResponseDTO(theme);
    }

    @Override
    public void deleteTheme(Long themeId) {
        themeRepository.deleteById(themeId);
    }
}
