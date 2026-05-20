package cn.idealer01.trigger.http;

import cn.idealer01.api.IProfilePublicController;
import cn.idealer01.api.dto.ProfileResponseDTO;
import cn.idealer01.domain.profile.service.IProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/public/profile")
public class ProfilePublicController implements IProfilePublicController {

    @Resource
    private IProfileService profileService;

    @Override
    @GetMapping("")
    public ProfileResponseDTO getProfile() {
        return profileService.getPublicProfile();
    }
}
