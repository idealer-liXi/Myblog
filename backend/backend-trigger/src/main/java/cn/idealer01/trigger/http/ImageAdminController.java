package cn.idealer01.trigger.http;

import cn.idealer01.api.IImageAdminController;
import cn.idealer01.api.dto.CurrentUserResponseDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.domain.image.service.IImageService;
import cn.idealer01.domain.auth.adapter.repository.ILoginRepository;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
public class ImageAdminController implements IImageAdminController {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Resource
    private IImageService imageService;

    @Resource
    private ILoginRepository loginRepository;

    @Override
    public Response<Map<String, String>> uploadImage(byte[] fileData, String fileName, String directory) {
        try {
            Map<String, String> data = imageService.uploadImage(fileData, fileName, getCurrentUserId(), directory);
            return Response.<Map<String, String>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(data)
                    .build();
        } catch (AppException e) {
            return Response.<Map<String, String>>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("上传图片失败 fileName:{}", fileName, e);
            return Response.<Map<String, String>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @PostMapping("/images/upload")
    public Response<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file,
                                                     @RequestParam(value = "directory", required = false) String directory) {
        if (file == null || file.isEmpty()) {
            return Response.<Map<String, String>>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("文件不能为空")
                    .build();
        }
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            return Response.<Map<String, String>>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("仅支持图片文件")
                    .build();
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return Response.<Map<String, String>>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("文件大小不能超过5MB")
                    .build();
        }

        try {
            return uploadImage(file.getBytes(), file.getOriginalFilename(), directory);
        } catch (IOException e) {
            log.error("读取上传图片失败", e);
            return Response.<Map<String, String>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("上传失败")
                    .build();
        }
    }

    @Override
    @GetMapping("/images")
    public Response<List<Map<String, Object>>> getImages() {
        try {
            return Response.<List<Map<String, Object>>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(imageService.getImages())
                    .build();
        } catch (AppException e) {
            return Response.<List<Map<String, Object>>>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("获取图片列表失败", e);
            return Response.<List<Map<String, Object>>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @Override
    @GetMapping("/project-images")
    public Response<List<Map<String, Object>>> getProjectImages() {
        try {
            return Response.<List<Map<String, Object>>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(imageService.getProjectImages())
                    .build();
        } catch (AppException e) {
            return Response.<List<Map<String, Object>>>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("获取项目图片列表失败", e);
            return Response.<List<Map<String, Object>>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @Override
    @GetMapping("/project-images/{projectId}")
    public Response<Map<String, Object>> getProjectImageByProjectId(@PathVariable Long projectId) {
        try {
            Map<String, Object> data = imageService.getProjectImageByProjectId(projectId);
            if (data == null) {
                return Response.<Map<String, Object>>builder()
                        .code(ResponseCode.USER_NOT_EXIST.getCode())
                        .info("项目不存在")
                        .build();
            }

            return Response.<Map<String, Object>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(data)
                    .build();
        } catch (AppException e) {
            return Response.<Map<String, Object>>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("获取项目图片详情失败 projectId:{}", projectId, e);
            return Response.<Map<String, Object>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @Override
    @DeleteMapping("/project-images/{projectId}")
    public Response<Void> clearProjectImage(@PathVariable Long projectId) {
        try {
            imageService.clearProjectImage(projectId);
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
            log.error("清空项目封面失败 projectId:{}", projectId, e);
            return Response.<Void>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @Override
    @DeleteMapping("/images/{imageId}")
    public Response<Void> deleteImage(@PathVariable Long imageId) {
        try {
            imageService.deleteImage(imageId);
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
            log.error("删除图片失败 imageId:{}", imageId, e);
            return Response.<Void>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        CurrentUserResponseDTO currentUser = loginRepository.queryCurrentUser(username);
        return currentUser.getId();
    }
}
