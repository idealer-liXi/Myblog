package cn.idealer01.trigger.http;

import cn.idealer01.api.IWeixinLoginController;
import cn.idealer01.api.dto.WeixinUserInformationResponseDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.domain.auth.model.entity.WeixinUserEntity;
import cn.idealer01.domain.auth.service.IWeixinLoginService;
import cn.idealer01.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/v1/login/")
@CrossOrigin("*")
public class WeixinLoginController implements IWeixinLoginController {
    @Resource
    private IWeixinLoginService loginService;
    
    @GetMapping("weixin_qrcode_ticket")
    @Override
    public Response<String> weixinQrCodeTicket() {
        try {
            String qrCodeTicket = loginService.createQrCodeTicket();
            log.info("微信二维码生成，ticket:{}", qrCodeTicket);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(qrCodeTicket)
                    .build();
        } catch (IOException e) {
            log.error("微信二维码生成失败", e);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
    
    @GetMapping("check_login")
    @Override
    public Response<String> checkLogin(String ticket) {
        try{
            String openId = loginService.checkLogin(ticket);
            log.info("扫码检测登录结果 ticket:{} openId:{}", ticket, openId);

            if(StringUtils.isNotBlank(openId)){
                return Response.<String>builder()
                        .code(Constants.ResponseCode.SUCCESS.getCode())
                        .info(Constants.ResponseCode.SUCCESS.getInfo())
                        .data(openId)
                        .build();
            }else{
                return Response.<String>builder()
                        .code(Constants.ResponseCode.NO_LOGIN.getCode())
                        .info(Constants.ResponseCode.NO_LOGIN.getInfo())
                        .build();
            }


        }catch (Exception e){
            log.error("扫码检测登录结果失败 ticket:{}", ticket, e);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @GetMapping("weixin_user_information")
    public Response<WeixinUserInformationResponseDTO> getWeixinUserInformation(String openid){
        try{
            WeixinUserEntity weixinUser = loginService.getWeixinUser(openid);
            log.info("成功获取微信用户昵称：{},头像：{}", weixinUser.getWeixinName(), weixinUser.getWeixinImageUrl());

            if(ObjectUtils.isNotEmpty(weixinUser)){
                return Response.<WeixinUserInformationResponseDTO>builder()
                        .code(Constants.ResponseCode.SUCCESS.getCode())
                        .info(Constants.ResponseCode.SUCCESS.getInfo())
                        .data(WeixinUserInformationResponseDTO.builder()
                                .weixinName(weixinUser.getWeixinName())
                                .weixinImageUrl(weixinUser.getWeixinImageUrl())
                                .build())
                        .build();
            }else{
                return Response.<WeixinUserInformationResponseDTO>builder()
                        .code(Constants.ResponseCode.NO_LOGIN.getCode())
                        .info(Constants.ResponseCode.NO_LOGIN.getInfo())
                        .build();
            }


        }catch (Exception e){
            log.error("获取微信用户信息失败 openId:{}", openid, e);
            return Response.<WeixinUserInformationResponseDTO>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }

    }

}
