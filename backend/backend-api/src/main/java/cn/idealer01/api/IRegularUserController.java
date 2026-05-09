package cn.idealer01.api;

import cn.idealer01.api.dto.LoginResponseDTO;
import cn.idealer01.api.dto.RegularUserRequestDTO;
import cn.idealer01.api.response.Response;

public interface IRegularUserController {

    Response<LoginResponseDTO> getToken(RegularUserRequestDTO regularUserLoginDTO);

    Response<Void> register(RegularUserRequestDTO regularUserRequestDTO);

}
