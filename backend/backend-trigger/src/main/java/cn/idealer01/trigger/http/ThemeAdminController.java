package cn.idealer01.trigger.http;

import cn.idealer01.api.IThemeAdminController;
import cn.idealer01.api.dto.ThemeRequestDTO;
import cn.idealer01.api.dto.ThemeResponseDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.domain.article.service.IThemeService;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
@RequestMapping("/api/admin/themes")
public class ThemeAdminController implements IThemeAdminController {

    @Resource
    private IThemeService themeService;

    @Override
    @GetMapping("")
    public Response<List<ThemeResponseDTO>> getThemes() {
        try {
            return Response.<List<ThemeResponseDTO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(themeService.getThemes())
                    .build();
        } catch (AppException e) {
            return Response.<List<ThemeResponseDTO>>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("获取主题列表失败", e);
            return Response.<List<ThemeResponseDTO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @Override
    @GetMapping("/{themeId}")
    public Response<ThemeResponseDTO> getThemeById(@PathVariable Long themeId) {
        try {
            ThemeResponseDTO data = themeService.getThemeById(themeId);
            if (data == null) {
                return Response.<ThemeResponseDTO>builder()
                        .code(ResponseCode.USER_NOT_EXIST.getCode())
                        .info("主题不存在")
                        .build();
            }

            return Response.<ThemeResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(data)
                    .build();
        } catch (AppException e) {
            return Response.<ThemeResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("获取主题详情失败 themeId:{}", themeId, e);
            return Response.<ThemeResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @Override
    @PostMapping("")
    public Response<ThemeResponseDTO> createTheme(@RequestBody ThemeRequestDTO request) {
        if (request == null || StringUtils.isBlank(request.getName())) {
            return Response.<ThemeResponseDTO>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("主题名称不能为空")
                    .build();
        }

        try {
            return Response.<ThemeResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(themeService.createTheme(request))
                    .build();
        } catch (AppException e) {
            return Response.<ThemeResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("创建主题失败", e);
            return Response.<ThemeResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @Override
    @PutMapping("/{themeId}")
    public Response<ThemeResponseDTO> updateTheme(@PathVariable Long themeId, @RequestBody ThemeRequestDTO request) {
        try {
            if (themeService.getThemeById(themeId) == null) {
                return Response.<ThemeResponseDTO>builder()
                        .code(ResponseCode.USER_NOT_EXIST.getCode())
                        .info("主题不存在")
                        .build();
            }

            return Response.<ThemeResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(themeService.updateTheme(themeId, request))
                    .build();
        } catch (AppException e) {
            return Response.<ThemeResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("更新主题失败 themeId:{}", themeId, e);
            return Response.<ThemeResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @Override
    @DeleteMapping("/{themeId}")
    public Response<Void> deleteTheme(@PathVariable Long themeId) {
        try {
            themeService.deleteTheme(themeId);
            return Response.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .build();
        } catch (AppException e) {
            return Response.<Void>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("删除主题失败 themeId:{}", themeId, e);
            return Response.<Void>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}
