package cn.idealer01.domain.auth.service;

import cn.idealer01.api.dto.LoginResponseDTO;

public interface IRegularUserLoginService {
    LoginResponseDTO getToken(String username, String password);
}
