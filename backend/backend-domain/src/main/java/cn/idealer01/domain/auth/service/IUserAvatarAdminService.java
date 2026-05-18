package cn.idealer01.domain.auth.service;

import cn.idealer01.api.dto.CurrentUserResponseDTO;
import cn.idealer01.api.dto.UserAvatarRequestDTO;

import java.util.List;
import java.util.Map;

public interface IUserAvatarAdminService {

    List<Map<String, Object>> getUserAvatars();

    Map<String, Object> getUserAvatarByUserId(Long userId);

    Map<String, Object> updateUserAvatar(Long userId, UserAvatarRequestDTO request);

    void clearUploadedAvatar(Long userId);

    Map<String, Object> buildAvatarPayload(Long userId, String username, String displayName,
                                           String uploadedAvatar, String avatarSource, String weixinImageUrl);

    UserAvatarRequestDTO prepareAvatarUpdate(UserAvatarRequestDTO request, String existingUploadedAvatar,
                                             String weixinImageUrl);

    CurrentUserResponseDTO applyCurrentUserAvatar(CurrentUserResponseDTO response, String uploadedAvatar,
                                                  String avatarSource, String weixinImageUrl);
}
