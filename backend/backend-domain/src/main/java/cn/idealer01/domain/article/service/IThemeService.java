package cn.idealer01.domain.article.service;

import cn.idealer01.api.dto.ThemeRequestDTO;
import cn.idealer01.api.dto.ThemeResponseDTO;

import java.util.List;

public interface IThemeService {

    List<ThemeResponseDTO> getThemes();

    ThemeResponseDTO getThemeById(Long themeId);

    ThemeResponseDTO createTheme(ThemeRequestDTO request);

    ThemeResponseDTO updateTheme(Long themeId, ThemeRequestDTO request);

    void deleteTheme(Long themeId);
}
