package cn.idealer01.domain.profile.service;

import cn.idealer01.api.dto.ProfileRequestDTO;
import cn.idealer01.api.dto.ProfileResponseDTO;

public interface IProfileService {
    ProfileResponseDTO getPublicProfile();

    ProfileResponseDTO getAdminProfile();

    ProfileResponseDTO updateProfile(ProfileRequestDTO request);
}
