package cn.idealer01.domain.auth.service;

import cn.idealer01.api.dto.CurrentUserResponseDTO;
import cn.idealer01.api.dto.UserAvatarRequestDTO;
import cn.idealer01.domain.auth.adapter.repository.ILoginRepository;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserAvatarAdminService implements IUserAvatarAdminService {

    public static final String AVATAR_SOURCE_DEFAULT = "DEFAULT";
    public static final String AVATAR_SOURCE_UPLOAD = "UPLOAD";
    public static final String AVATAR_SOURCE_WECHAT = "WECHAT";
    public static final String DEFAULT_AVATAR = "";

    @Resource
    private ILoginRepository loginRepository;

    @Override
    public List<Map<String, Object>> getUserAvatars() {
        return loginRepository.queryAllCurrentUsers().stream()
                .map(this::toAvatarPayload)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getUserAvatarByUserId(Long userId) {
        try {
            return toAvatarPayload(loginRepository.queryCurrentUserByUserId(userId));
        } catch (AppException e) {
            if (ResponseCode.USER_NOT_EXIST.getCode().equals(e.getCode())) {
                return null;
            }
            throw e;
        }
    }

    @Override
    public Map<String, Object> updateUserAvatar(Long userId, UserAvatarRequestDTO request) {
        CurrentUserResponseDTO user = loginRepository.queryCurrentUserByUserId(userId);
        UserAvatarRequestDTO prepared = prepareAvatarUpdate(request, user.getAvatar(), user.getWeixinImageUrl());
        loginRepository.updateUserAvatar(userId, prepared.getAvatar(), prepared.getAvatarSource());
        return toAvatarPayload(loginRepository.queryCurrentUserByUserId(userId));
    }

    @Override
    public void clearUploadedAvatar(Long userId) {
        loginRepository.queryCurrentUserByUserId(userId);
        loginRepository.updateUserAvatar(userId, "", AVATAR_SOURCE_DEFAULT);
    }

    @Override
    public Map<String, Object> buildAvatarPayload(Long userId, String username, String displayName,
                                                  String uploadedAvatar, String avatarSource, String weixinImageUrl) {
        String normalizedSource = normalizeAvatarSource(avatarSource);
        boolean hasUploadedAvatar = StringUtils.isNotBlank(uploadedAvatar);
        boolean hasWeixinAvatar = StringUtils.isNotBlank(weixinImageUrl);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("username", username);
        result.put("displayName", displayName);
        result.put("loginType", StringUtils.isNotBlank(weixinImageUrl) && StringUtils.isBlank(username) ? "weixin" : "password");
        result.put("avatar", StringUtils.defaultString(uploadedAvatar));
        result.put("avatarSource", normalizedSource);
        result.put("effectiveAvatar", resolveEffectiveAvatar(normalizedSource, uploadedAvatar, weixinImageUrl));
        result.put("defaultAvatar", DEFAULT_AVATAR);
        result.put("weixinImageUrl", StringUtils.defaultString(weixinImageUrl));
        result.put("hasUploadedAvatar", hasUploadedAvatar);
        result.put("hasWeixinAvatar", hasWeixinAvatar);
        return result;
    }

    @Override
    public UserAvatarRequestDTO prepareAvatarUpdate(UserAvatarRequestDTO request, String existingUploadedAvatar,
                                                    String weixinImageUrl) {
        String normalizedSource = normalizeAvatarSource(null == request ? null : request.getAvatarSource());
        if (AVATAR_SOURCE_WECHAT.equals(normalizedSource) && StringUtils.isBlank(weixinImageUrl)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "用户未绑定微信头像");
        }

        String normalizedAvatar = existingUploadedAvatar;
        if (AVATAR_SOURCE_UPLOAD.equals(normalizedSource)) {
            normalizedAvatar = StringUtils.trimToNull(
                    StringUtils.defaultIfBlank(null == request ? null : request.getAvatar(), existingUploadedAvatar)
            );
            if (StringUtils.isBlank(normalizedAvatar)) {
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "用户未上传头像");
            }
        }

        return UserAvatarRequestDTO.builder()
                .avatarSource(normalizedSource)
                .avatar(normalizedAvatar)
                .build();
    }

    @Override
    public CurrentUserResponseDTO applyCurrentUserAvatar(CurrentUserResponseDTO response, String uploadedAvatar,
                                                         String avatarSource, String weixinImageUrl) {
        return applyCurrentUserAvatarFields(response, uploadedAvatar, avatarSource, weixinImageUrl);
    }

    public static CurrentUserResponseDTO applyCurrentUserAvatarFields(CurrentUserResponseDTO response, String uploadedAvatar,
                                                                     String avatarSource, String weixinImageUrl) {
        String normalizedSource = normalizeAvatarSourceStatic(avatarSource);
        response.setAvatar(StringUtils.defaultString(uploadedAvatar));
        response.setAvatarSource(normalizedSource);
        response.setEffectiveAvatar(resolveEffectiveAvatarStatic(normalizedSource, uploadedAvatar, weixinImageUrl));
        response.setDefaultAvatar(DEFAULT_AVATAR);
        response.setWeixinImageUrl(StringUtils.defaultString(weixinImageUrl));
        return response;
    }

    private Map<String, Object> toAvatarPayload(CurrentUserResponseDTO user) {
        Map<String, Object> payload = buildAvatarPayload(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getAvatar(),
                user.getAvatarSource(),
                user.getWeixinImageUrl()
        );
        payload.put("loginType", user.getLoginType());
        payload.put("updatedAt", null);
        return payload;
    }

    private String resolveEffectiveAvatar(String avatarSource, String uploadedAvatar, String weixinImageUrl) {
        return resolveEffectiveAvatarStatic(avatarSource, uploadedAvatar, weixinImageUrl);
    }

    private String normalizeAvatarSource(String avatarSource) {
        return normalizeAvatarSourceStatic(avatarSource);
    }

    private static String resolveEffectiveAvatarStatic(String avatarSource, String uploadedAvatar, String weixinImageUrl) {
        if (AVATAR_SOURCE_UPLOAD.equals(avatarSource) && StringUtils.isNotBlank(uploadedAvatar)) {
            return uploadedAvatar;
        }
        if (AVATAR_SOURCE_WECHAT.equals(avatarSource) && StringUtils.isNotBlank(weixinImageUrl)) {
            return weixinImageUrl;
        }
        return DEFAULT_AVATAR;
    }

    private static String normalizeAvatarSourceStatic(String avatarSource) {
        if (AVATAR_SOURCE_UPLOAD.equalsIgnoreCase(StringUtils.defaultString(avatarSource))) {
            return AVATAR_SOURCE_UPLOAD;
        }
        if (AVATAR_SOURCE_WECHAT.equalsIgnoreCase(StringUtils.defaultString(avatarSource))) {
            return AVATAR_SOURCE_WECHAT;
        }
        return AVATAR_SOURCE_DEFAULT;
    }

}
