package cn.idealer01.trigger.http;

import cn.idealer01.api.IProfileAdminController;
import cn.idealer01.api.dto.ProfileRequestDTO;
import cn.idealer01.api.dto.ProfileResponseDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.domain.profile.service.IProfileService;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/admin/profile")
public class ProfileAdminController implements IProfileAdminController {

    @Resource
    private IProfileService profileService;

    @Override
    @GetMapping("")
    public Response<ProfileResponseDTO> getProfile() {
        return Response.<ProfileResponseDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(profileService.getAdminProfile())
                .build();
    }

    @Override
    @PutMapping("")
    public Response<ProfileResponseDTO> updateProfile(@RequestBody ProfileRequestDTO request) {
        try {
            return Response.<ProfileResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(profileService.updateProfile(request))
                    .build();
        } catch (AppException e) {
            return Response.<ProfileResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        }
    }
}
