package cn.idealer01.domain.auth.service;

import cn.idealer01.api.dto.CurrentUserResponseDTO;

import java.util.Map;

public interface IUserAdminService {

    Map<String, Object> getUsers(String keyword, String loginType, String status, int page, int pageSize);

    CurrentUserResponseDTO getUserById(Long userId);

    CurrentUserResponseDTO updateUserStatus(Long userId, String status);

    void deleteUser(Long userId);
}