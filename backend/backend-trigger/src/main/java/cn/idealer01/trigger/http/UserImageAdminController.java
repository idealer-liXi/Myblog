package cn.idealer01.trigger.http;

import cn.idealer01.api.IUserImageAdminController;
import cn.idealer01.api.dto.UserAvatarRequestDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.domain.auth.service.IUserAvatarAdminService;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/user-images")
public class UserImageAdminController implements IUserImageAdminController {

    @Resource
    private IUserAvatarAdminService userAvatarAdminService;

    @Override
    @GetMapping("")
    public Response<List<Map<String, Object>>> getUserImages() {
        try {
            return Response.<List<Map<String, Object>>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(userAvatarAdminService.getUserAvatars())
                    .build();
        } catch (AppException e) {
            return Response.<List<Map<String, Object>>>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("获取用户头像列表失败", e);
            return Response.<List<Map<String, Object>>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @Override
    @GetMapping("/{userId}")
    public Response<Map<String, Object>> getUserImageByUserId(@PathVariable Long userId) {
        try {
            Map<String, Object> data = userAvatarAdminService.getUserAvatarByUserId(userId);
            if (data == null) {
                return Response.<Map<String, Object>>builder()
                        .code(ResponseCode.USER_NOT_EXIST.getCode())
                        .info("用户不存在")
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
            log.error("获取用户头像详情失败 userId:{}", userId, e);
            return Response.<Map<String, Object>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @Override
    @PutMapping("/{userId}")
    public Response<Map<String, Object>> updateUserImage(@PathVariable Long userId, @RequestBody UserAvatarRequestDTO request) {
        try {
            return Response.<Map<String, Object>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(userAvatarAdminService.updateUserAvatar(userId, request))
                    .build();
        } catch (AppException e) {
            return Response.<Map<String, Object>>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("更新用户头像失败 userId:{}", userId, e);
            return Response.<Map<String, Object>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @Override
    @DeleteMapping("/{userId}")
    public Response<Void> clearUserImage(@PathVariable Long userId) {
        try {
            userAvatarAdminService.clearUploadedAvatar(userId);
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
            log.error("清空用户上传头像失败 userId:{}", userId, e);
            return Response.<Void>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}
