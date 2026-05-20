package cn.idealer01.trigger.http;

import cn.idealer01.api.dto.CurrentUserResponseDTO;
import cn.idealer01.api.dto.UserAvatarRequestDTO;
import cn.idealer01.api.dto.UserProfileUpdateRequestDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.domain.auth.adapter.repository.ILoginRepository;
import cn.idealer01.domain.auth.service.IUserAvatarAdminService;
import cn.idealer01.domain.image.service.IImageService;
import cn.idealer01.types.common.Constants;
import cn.idealer01.domain.auth.service.IWeixinLoginService;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
public class UserProfileController {

    private static final long MAX_AVATAR_FILE_SIZE = 5 * 1024 * 1024;

    @Resource
    private ILoginRepository loginRepository;
    @Resource
    private IWeixinLoginService weixinLoginService;
    @Resource
    private IUserAvatarAdminService userAvatarAdminService;
    @Resource
    private IImageService imageService;

    @GetMapping("/api/user/me")
    public Response<CurrentUserResponseDTO> currentUser(Authentication authentication) {
        CurrentUserResponseDTO profile = loginRepository.queryCurrentUser(authentication.getName());
        return Response.<CurrentUserResponseDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(profile)
                .build();
    }

    @GetMapping("/api/user/weixin/bind/qrcode")
    public Response<String> weixinBindQrCode(Authentication authentication) {
        try {
            String ticket = weixinLoginService.createBindQrCodeTicket();
            return Response.<String>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(ticket)
                    .build();
        } catch (IOException e) {
            log.error("获取微信绑定二维码失败 user:{}", authentication.getName(), e);
            return Response.<String>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @GetMapping("/api/user/weixin/bind/check")
    public Response<CurrentUserResponseDTO> checkWeixinBind(@RequestParam("ticket") String ticket, Authentication authentication) {
        try {
            CurrentUserResponseDTO profile = weixinLoginService.checkBind(ticket, authentication.getName());
            if (null == profile) {
                return Response.<CurrentUserResponseDTO>builder()
                        .code(Constants.ResponseCode.NO_LOGIN.getCode())
                        .info(Constants.ResponseCode.NO_LOGIN.getInfo())
                        .build();
            }

            return Response.<CurrentUserResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(profile)
                    .build();
        } catch (AppException e) {
            return Response.<CurrentUserResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        }
    }

    @PutMapping("/api/user/me/profile")
    public Response<CurrentUserResponseDTO> updateCurrentUserProfile(@RequestBody UserProfileUpdateRequestDTO request,
                                                                     Authentication authentication) {
        try {
            String displayName = StringUtils.trimToEmpty(null == request ? null : request.getDisplayName());
            if (StringUtils.isBlank(displayName)) {
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "显示名称不能为空");
            }

            CurrentUserResponseDTO currentUser = loginRepository.queryCurrentUser(authentication.getName());
            loginRepository.updateUserDisplayName(currentUser.getId(), displayName);
            userAvatarAdminService.updateUserAvatar(currentUser.getId(), UserAvatarRequestDTO.builder()
                    .avatarSource(null == request ? null : request.getAvatarSource())
                    .avatar(null == request ? null : request.getAvatar())
                    .build());

            return Response.<CurrentUserResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(loginRepository.queryCurrentUser(authentication.getName()))
                    .build();
        } catch (AppException e) {
            return Response.<CurrentUserResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("更新当前用户资料失败 user:{}", authentication.getName(), e);
            return Response.<CurrentUserResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @PostMapping("/api/user/me/avatar/upload")
    public Response<Map<String, String>> uploadCurrentUserAvatar(@RequestParam("file") MultipartFile file,
                                                                 Authentication authentication) {
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
        if (file.getSize() > MAX_AVATAR_FILE_SIZE) {
            return Response.<Map<String, String>>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("文件大小不能超过5MB")
                    .build();
        }

        try {
            CurrentUserResponseDTO currentUser = loginRepository.queryCurrentUser(authentication.getName());
            Map<String, String> data = imageService.uploadImage(
                    file.getBytes(),
                    file.getOriginalFilename(),
                    currentUser.getId(),
                    "user-avatars/" + currentUser.getId()
            );

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
        } catch (IOException e) {
            log.error("上传当前用户头像失败 user:{}", authentication.getName(), e);
            return Response.<Map<String, String>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("上传失败")
                    .build();
        }
    }

    @DeleteMapping("/api/user/weixin/bind")
    public Response<Void> deleteWeixinBind(Authentication authentication) {
        try {
            weixinLoginService.unbindWechat(authentication.getName());
            return Response.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .build();
        } catch (AppException e) {
            return Response.<Void>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        }
    }
}
