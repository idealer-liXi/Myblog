# MyBlog 后端搭建
## 1 项目初始化
  使用xfg提供的DDD脚手架搭建
## 2 微信登录
### 2.1 微信登录流程
1. 前端发送获取微信二维码请求
2. 后端接收到请求，根据自己的appId，向微信服务器发送请求获取 accessToken(redis 2小时有效期)，将<appId, accessToken>保存(2小时)
3. 后端将accessToken发送微信服务器获取ticket(2小时有效期)，发送给前端
4. 前端接收到ticket，根据ticket向微信服务器获取二维码图片
5. 微信服务器接收到用户扫描了登录二维码，将openId(30天有效期)发送给后端，后端保存<ticket, openId>(redis 2小时)
6. 前端不断轮询后端，依据ticket查询是否保存了openId，将openId保存30天
7. 前端接收到了openId，说明用户已经登录可以跳转到登录页面

### 2.2 难题:appId, accessToken, ticket, openId的认识？
