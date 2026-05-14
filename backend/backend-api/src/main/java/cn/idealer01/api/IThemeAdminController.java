package cn.idealer01.api;

import cn.idealer01.api.dto.ThemeRequestDTO;
import cn.idealer01.api.dto.ThemeResponseDTO;
import cn.idealer01.api.response.Response;

import java.util.List;

public interface IThemeAdminController {
    Response<List<ThemeResponseDTO>> getThemes();
    Response<ThemeResponseDTO> getThemeById(Long themeId);
    Response<ThemeResponseDTO> createTheme(ThemeRequestDTO request);
    Response<ThemeResponseDTO> updateTheme(Long themeId, ThemeRequestDTO request);
    Response<Void> deleteTheme(Long themeId);
}
