package cn.idealer01.api;

import cn.idealer01.api.dto.ProfileRequestDTO;
import cn.idealer01.api.dto.ProfileResponseDTO;
import cn.idealer01.api.response.Response;

public interface IProfileAdminController {
    Response<ProfileResponseDTO> getProfile();

    Response<ProfileResponseDTO> updateProfile(ProfileRequestDTO request);
}
