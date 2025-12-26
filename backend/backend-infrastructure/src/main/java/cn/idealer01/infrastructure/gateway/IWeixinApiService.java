package cn.idealer01.infrastructure.gateway;

import cn.idealer01.infrastructure.dao.po.WeixinUser;
import cn.idealer01.infrastructure.gateway.dto.WeixinQrCodeRequestDTO;
import cn.idealer01.infrastructure.gateway.dto.WeixinQrCodeResponseDTO;
import cn.idealer01.infrastructure.gateway.dto.WeixinTemplateMessageDTO;
import cn.idealer01.infrastructure.gateway.dto.WeixinTokenResponseDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface IWeixinApiService {
    /**
     * 获取access_token
     * @param grantType 获取access_token填写client_credential
     * @param appId 第三方用户唯一凭证
     * @param appSecret 第三方用户唯一凭证密钥，即appsecret
     * @return
     */
    @GET("cgi-bin/token")
    Call<WeixinTokenResponseDTO> getToken(@Query("grant_type") String grantType,
                                          @Query("appid") String appId,
                                          @Query("secret") String appSecret);

    /**
     * 获得Ticket二维码
     * @param accessToken
     * @param weixinQrCodeRequestDTO
     * @return
     */
    @POST("cgi-bin/qrcode/create")
    Call<WeixinQrCodeResponseDTO> createQrCode(@Query("access_token") String accessToken,
                                               @Body WeixinQrCodeRequestDTO weixinQrCodeRequestDTO);

    /**
     * 发送模板信息
     * @param accessToken
     * @param weixinTemplateMessageDTO
     * @return
     */
    @POST("cgi-bin/message/template/send")
    Call<Void> sendMessage(@Query("access_token") String accessToken,
                           @Body WeixinTemplateMessageDTO weixinTemplateMessageDTO);

    /**
     * 获取微信用户昵称和头像
     * @return
     */
    WeixinUser getUserInfo();
}
