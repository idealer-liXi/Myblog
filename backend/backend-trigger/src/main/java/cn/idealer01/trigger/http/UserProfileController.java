package cn.idealer01.trigger.http;

import cn.idealer01.api.dto.CurrentUserResponseDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.domain.auth.adapter.repository.ILoginRepository;
import cn.idealer01.types.common.Constants;
import cn.idealer01.domain.auth.service.IWeixinLoginService;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
@RestController
public class UserProfileController {

    @Resource
    private ILoginRepository loginRepository;
    @Resource
    private IWeixinLoginService weixinLoginService;

    @GetMapping("/api/user/me")
    public Response<CurrentUserResponseDTO> currentUser(Authentication authentication) {
        CurrentUserResponseDTO profile = loginRepository.queryCurrentUser(authentication.getName());
        return Response.<CurrentUserResponseDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(profile)
                .build();
    }

    @GetMapping("/api/user/weixin/bind/qrcode")
    public Response<String> weixinBindQrCode(Authentication authentication) {
        try {
            String ticket = weixinLoginService.createBindQrCodeTicket();
            return Response.<String>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(ticket)
                    .build();
        } catch (IOException e) {
            log.error("获取微信绑定二维码失败 user:{}", authentication.getName(), e);
            return Response.<String>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @GetMapping("/api/user/weixin/bind/check")
    public Response<CurrentUserResponseDTO> checkWeixinBind(@RequestParam("ticket") String ticket, Authentication authentication) {
        try {
            CurrentUserResponseDTO profile = weixinLoginService.checkBind(ticket, authentication.getName());
            if (null == profile) {
                return Response.<CurrentUserResponseDTO>builder()
                        .code(Constants.ResponseCode.NO_LOGIN.getCode())
                        .info(Constants.ResponseCode.NO_LOGIN.getInfo())
                        .build();
            }

            return Response.<CurrentUserResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(profile)
                    .build();
        } catch (AppException e) {
            return Response.<CurrentUserResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        }
    }

    @DeleteMapping("/api/user/weixin/bind")
    public Response<Void> deleteWeixinBind(Authentication authentication) {
        try {
            weixinLoginService.unbindWechat(authentication.getName());
            return Response.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .build();
        } catch (AppException e) {
            return Response.<Void>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        }
    }
}
