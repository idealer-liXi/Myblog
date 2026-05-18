package cn.idealer01.api;

import cn.idealer01.api.dto.UserAvatarRequestDTO;
import cn.idealer01.api.response.Response;

import java.util.List;
import java.util.Map;

public interface IUserImageAdminController {
    Response<List<Map<String, Object>>> getUserImages();
    Response<Map<String, Object>> getUserImageByUserId(Long userId);
    Response<Map<String, Object>> updateUserImage(Long userId, UserAvatarRequestDTO request);
    Response<Void> clearUserImage(Long userId);
}
