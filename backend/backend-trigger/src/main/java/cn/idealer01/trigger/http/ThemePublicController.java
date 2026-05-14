package cn.idealer01.trigger.http;

import cn.idealer01.api.dto.ThemeResponseDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.domain.article.service.IThemeService;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/public/themes")
public class ThemePublicController {

    @Resource
    private IThemeService themeService;

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
}
