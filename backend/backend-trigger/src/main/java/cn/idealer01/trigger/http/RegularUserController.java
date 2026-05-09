package cn.idealer01.trigger.http;

import cn.idealer01.api.IRegularUserController;
import cn.idealer01.api.dto.LoginResponseDTO;
import cn.idealer01.api.dto.RegularUserRequestDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.domain.auth.service.IRegularUserLoginService;
import cn.idealer01.domain.auth.service.IRegularUserRegisterService;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/api/r1")
public class RegularUserController implements IRegularUserController {
    @Resource
    private IRegularUserLoginService loginService;
    @Resource
    private IRegularUserRegisterService registerService;
    @Override
    @PostMapping("/login/token")
    public Response<LoginResponseDTO> getToken(@RequestBody RegularUserRequestDTO regularUserLoginDTO) {
        try{
            String username = regularUserLoginDTO.getUsername();
            String password = regularUserLoginDTO.getPassword();

            log.info("网站用户:{}，请求获取token", username);
            LoginResponseDTO loginResponse = loginService.getToken(username,password);

            log.info("网站用户:{}, 获取token成功", username);
            return Response.<LoginResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(loginResponse)
                    .build();
        }catch (AppException e){
            log.info("网站用户:{}, 获取token失败", regularUserLoginDTO.getUsername());
            return Response.<LoginResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        }catch (BadCredentialsException e){
            return Response.<LoginResponseDTO>builder()
                    .code(ResponseCode.LOGIN_ERROR.getCode())
                    .info(ResponseCode.LOGIN_ERROR.getInfo())
                    .build();
        }catch (Exception e){
            log.error("网站用户登录，未知异常",e);
            return Response.<LoginResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @Override
    @PostMapping("/register")
    public Response<Void> register(@RequestBody RegularUserRequestDTO regularUserRequestDTO) {
        try{
            String username = regularUserRequestDTO.getUsername();
            String password = regularUserRequestDTO.getPassword();
            log.info("请求注册用户名：{}", username);

            registerService.register(username, password);

            log.info("用户名注册成功：{}", username);
            return Response.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .build();
        }catch (AppException e){
            log.info("用户名注册失败:{}", regularUserRequestDTO.getUsername());
            return Response.<Void>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        }catch (Exception e){
            log.error("用户注册出现未知异常:",e);
            return Response.<Void>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}
