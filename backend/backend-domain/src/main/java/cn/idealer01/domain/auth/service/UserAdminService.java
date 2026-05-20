package cn.idealer01.domain.auth.service;

import cn.idealer01.api.dto.CurrentUserResponseDTO;
import cn.idealer01.domain.auth.adapter.repository.ILoginRepository;
import cn.idealer01.types.enums.AuthType;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserAdminService implements IUserAdminService {

    @Resource
    private ILoginRepository loginRepository;

    @Override
    public Map<String, Object> getUsers(String keyword, String loginType, String status, int page, int pageSize) {
        List<CurrentUserResponseDTO> allUsers = loginRepository.queryAllCurrentUsers();

        List<CurrentUserResponseDTO> filtered = allUsers.stream()
                .filter(user -> !"deleted".equals(user.getStatus()))
                .filter(user -> matchesStatus(user, status))
                .filter(user -> matchesLoginType(user, loginType))
                .filter(user -> matchesKeyword(user, keyword))
                .collect(Collectors.toList());

        int normalizedPage = Math.max(page, 1);
        int normalizedPageSize = Math.max(pageSize, 1);
        int offset = (normalizedPage - 1) * normalizedPageSize;
        int end = Math.min(offset + normalizedPageSize, filtered.size());
        List<CurrentUserResponseDTO> paged = offset >= filtered.size()
                ? Collections.emptyList()
                : filtered.subList(offset, end);

        Map<String, Object> result = new HashMap<>();
        result.put("total", (long) filtered.size());
        result.put("page", normalizedPage);
        result.put("pageSize", normalizedPageSize);
        result.put("users", paged);
        return result;
    }

    @Override
    public CurrentUserResponseDTO getUserById(Long userId) {
        CurrentUserResponseDTO user = loginRepository.queryCurrentUserByUserId(userId);
        if (null == user) {
            throw new AppException(ResponseCode.USER_NOT_EXIST);
        }
        return user;
    }

    @Override
    public CurrentUserResponseDTO updateUserStatus(Long userId, String status) {
        loginRepository.queryCurrentUserByUserId(userId);
        loginRepository.updateUserStatus(userId, mapStatus(status));
        return loginRepository.queryCurrentUserByUserId(userId);
    }

    @Override
    public void deleteUser(Long userId) {
        CurrentUserResponseDTO user = loginRepository.queryCurrentUserByUserId(userId);
        if (Boolean.TRUE.equals(user.getWeixinBound())) {
            loginRepository.unbindAuthFromUser(userId, AuthType.WECHAT.getCode());
        }
        loginRepository.updateUserStatus(userId, 2);
    }

    private Integer mapStatus(String status) {
        if ("active".equalsIgnoreCase(status)) return 1;
        if ("disabled".equalsIgnoreCase(status)) return 0;
        throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "无效的状态值: " + status);
    }

    private boolean matchesStatus(CurrentUserResponseDTO user, String status) {
        if (StringUtils.isBlank(status)) return true;
        return status.equalsIgnoreCase(user.getStatus());
    }

    private boolean matchesLoginType(CurrentUserResponseDTO user, String loginType) {
        if (StringUtils.isBlank(loginType)) return true;
        return loginType.equalsIgnoreCase(user.getLoginType());
    }

    private boolean matchesKeyword(CurrentUserResponseDTO user, String keyword) {
        if (StringUtils.isBlank(keyword)) return true;
        String kw = keyword.toLowerCase();
        return StringUtils.defaultString(user.getUsername()).toLowerCase().contains(kw)
                || StringUtils.defaultString(user.getDisplayName()).toLowerCase().contains(kw)
                || StringUtils.defaultString(user.getWeixinName()).toLowerCase().contains(kw);
    }
}
