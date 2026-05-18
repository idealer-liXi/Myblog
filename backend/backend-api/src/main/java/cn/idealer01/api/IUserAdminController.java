package cn.idealer01.api;

import cn.idealer01.api.dto.CurrentUserResponseDTO;
import cn.idealer01.api.dto.UserStatusUpdateRequestDTO;
import cn.idealer01.api.response.Response;

import java.util.Map;

public interface IUserAdminController {

    Response<Map<String, Object>> getUsers(String keyword, String loginType, String status, int page, int pageSize);

    Response<CurrentUserResponseDTO> getUserById(Long userId);

    Response<CurrentUserResponseDTO> updateUserStatus(Long userId, UserStatusUpdateRequestDTO request);

    Response<Void> deleteUser(Long userId);
}