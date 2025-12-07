package cn.idealer01.infrastructure.adapter.port;

import cn.idealer01.domain.auth.adapter.port.ILoginPort;
import cn.idealer01.infrastructure.gateway.IWeixinApiService;
import cn.idealer01.infrastructure.gateway.dto.WeixinQrCodeRequestDTO;
import cn.idealer01.infrastructure.gateway.dto.WeixinQrCodeResponseDTO;
import cn.idealer01.infrastructure.gateway.dto.WeixinTemplateMessageDTO;
import cn.idealer01.infrastructure.gateway.dto.WeixinTokenResponseDTO;
import cn.idealer01.infrastructure.redis.IRedisService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Call;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class LoginPort implements ILoginPort {
    @Value("${weixin.config.appId}")
    private String appId;
    @Value("${weixin.config.appSecret}")
    private String appSecret;
    @Value("${weixin.config.templateId}")
    private String templateId;
    @Resource
    private IRedisService redisService;

    @Resource
    private IWeixinApiService weixinApiService;

    @Override
    public String createQrCodeTicket() throws IOException {
        //1.获取access_token
        //1.1 检查是否已经有access_token缓存
        String accessToken = redisService.queryAccessTokenByAppId(appId);
        //1.2如果不存在，则重新请求获取
        if (null == accessToken){
            Call<WeixinTokenResponseDTO> call = weixinApiService.getToken("client_credential", appId, appSecret);
            WeixinTokenResponseDTO weixinTokenRes = call.execute().body();
            assert weixinTokenRes != null;
            accessToken = weixinTokenRes.getAccess_token();
            redisService.saveAccessToken(appId, accessToken);
        }

        //2.已经获取access_token, 发送请求获取ticket
        WeixinQrCodeRequestDTO weixinQrCodeReq = WeixinQrCodeRequestDTO.builder()
                .expire_seconds(2592000)
                .action_name(WeixinQrCodeRequestDTO.ActionNameTypeVO.QR_SCENE.getCode())
                .action_info(WeixinQrCodeRequestDTO.ActionInfo.builder()
                        .scene(WeixinQrCodeRequestDTO.ActionInfo.Scene.builder()
                                .scene_id(100601)
                                .build())
                        .build())
                .build();

        Call<WeixinQrCodeResponseDTO> call = weixinApiService.createQrCode(accessToken, weixinQrCodeReq);
        WeixinQrCodeResponseDTO weixinQrCodeRes = call.execute().body();
        assert null != weixinQrCodeRes;
        return weixinQrCodeRes.getTicket();
    }

    @Override
    public void sendLoginTemplate(String openId) throws IOException {
        //1.查看AccessToken是否存在
        String accessToken = redisService.queryAccessTokenByAppId(appId);
        if(null == accessToken){
            Call<WeixinTokenResponseDTO> call = weixinApiService.getToken("client_credential", appId, appSecret);
            WeixinTokenResponseDTO weixinTokenRes = call.execute().body();
            assert weixinTokenRes != null;
            accessToken = weixinTokenRes.getAccess_token();
            redisService.saveAccessToken(appId, accessToken);
        }

        //2.发送模板消息
        Map<String, Map<String, String>> data = new HashMap<>();
        WeixinTemplateMessageDTO.put(data, WeixinTemplateMessageDTO.TemplateKey.USER, openId);
        WeixinTemplateMessageDTO weixinTemplateMessageDTO = new WeixinTemplateMessageDTO(openId, templateId);;
        weixinTemplateMessageDTO.setUrl("https://idealer01.cn");
        weixinTemplateMessageDTO.setData(data);

        Call<Void> call = weixinApiService.sendMessage(accessToken, weixinTemplateMessageDTO);
        call.execute();
    }


}
