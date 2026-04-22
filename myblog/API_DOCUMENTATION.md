# Myblog 前端 API 文档

## 基础信息

- **基础URL**:
  - 用户认证相关: `http://localhost:8080/api`
  - 用户管理相关: `http://localhost:8080/api/v1`
  - 博客内容/分类/图片/项目相关: `http://localhost:3000/api`
  - 第三方 GitHub API: `https://api.github.com`
- **请求格式**: JSON
- **响应格式**: JSON
- **认证方式**: JWT Token / Cookie

---

## 一、用户认证相关 API

### 1. 用户注册

- **URL**: `POST /r1/register`
- **描述**: 用户注册账号
- **请求参数**:

| 参数名   | 类型   | 必填 | 描述   |
| -------- | ------ | ---- | ------ |
| username | string | 是   | 用户名 |
| password | string | 是   | 密码   |

- **请求示例**:
```json
{
  "username": "zhangsan",
  "password": "123456"
}
```

- **响应示例 (成功)**:
```json
{
  "code": "0000",
  "info": "注册成功"
}
```

- **响应示例 (失败)**:
```json
{
  "code": "0001",
  "info": "用户名已存在"
}
```

---

### 2. 用户登录

- **URL**: `POST /r1/login/token`
- **描述**: 用户登录获取 JWT Token
- **请求参数**:

| 参数名   | 类型   | 必填 | 描述   |
| -------- | ------ | ---- | ------ |
| username | string | 是   | 用户名 |
| password | string | 是   | 密码   |

- **请求示例**:
```json
{
  "username": "zhangsan",
  "password": "123456"
}
```

- **响应示例 (成功)**:
```json
{
  "code": "0000",
  "data": "eyJhbGciOiJIUzI1NiIs...",
  "info": "登录成功"
}
```

- **响应示例 (失败)**:
```json
{
  "code": "0001",
  "info": "用户名或密码错误"
}
```

---

### 3. 获取微信登录二维码

- **URL**: `GET /v1/login/weixin_qrcode_ticket`
- **描述**: 获取微信登录二维码的 ticket
- **请求参数**: 无

- **响应示例 (成功)**:
```json
{
  "code": "0000",
  "data": "gQH47joAAAAAAAAAASxodHRwOi8vd2VpeGluLnFxLmNvbS9xL2taV2xn..."
}
```

- **响应示例 (失败)**:
```json
{
  "code": "0001",
  "info": "获取二维码失败"
}
```

**前端处理**:
```javascript
qrCodeUrl.value = `https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=${ticket}`
```

---

### 4. 检查微信登录状态

- **URL**: `GET /v1/login/check_login`
- **描述**: 轮询检查用户是否完成微信扫码登录
- **请求参数**:

| 参数名 | 类型   | 必填 | 描述          |
| ------ | ------ | ---- | ------------- |
| ticket | string | 是   | 二维码 ticket |

- **请求示例**:
```
GET /v1/login/check_login?ticket=gQH47joAAAAAAAAAASxodHRwOi8vd2VpeGluLnFxLmNvbS9xL2taV2xn...
```

- **响应示例 (成功)**:
```json
{
  "code": "0000",
  "data": "openid_1234567890"
}
```

**前端处理**: 每3秒轮询一次，登录成功后保存 openid 到 Cookie (有效期30天)

---

### 5. 获取微信用户信息

- **URL**: `GET /v1/login/weixin_user_information`
- **描述**: 根据 openid 获取微信用户昵称和头像
- **请求参数**:

| 参数名 | 类型   | 必填 | 描述       |
| ------ | ------ | ---- | ---------- |
| openid | string | 是   | 微信 OpenID |

- **请求示例**:
```
GET /v1/login/weixin_user_information?openid=openid_1234567890
```

- **响应示例 (成功)**:
```json
{
  "code": "0000",
  "data": {
    "weixinName": "张三",
    "weixinImageUrl": "https://thirdwx.qlogo.cn/mmopen/vi_32/..."
  }
}
```

**前端处理**: 将用户信息保存到 localStorage 和 Vuex 中

---

## 二、用户管理相关 API

> **注意**: 以下用户管理 API 需要管理员权限，请求头须携带 JWT Token：`Authorization: Bearer <token>`

### 1. 获取用户列表

- **URL**: `GET /v1/users`
- **描述**: 获取所有用户列表（管理后台使用）
- **基础URL**: `http://localhost:8080/api`
- **请求头**:
```
Authorization: Bearer <jwtToken>
```
- **查询参数**:

| 参数名   | 类型   | 必填 | 描述                       |
| -------- | ------ | ---- | -------------------------- |
| keyword  | string | 否   | 按用户名搜索关键词         |
| loginType| string | 否   | 登录方式筛选 (password/weixin) |
| status   | string | 否   | 状态筛选 (active/disabled)  |
| page     | number | 否   | 页码，默认 1               |
| pageSize | number | 否   | 每页条数，默认 10          |

- **请求示例**:
```
GET /v1/users?keyword=张&loginType=weixin&page=1&pageSize=10
```

- **响应示例 (成功)**:
```json
{
  "code": "0000",
  "data": {
    "total": 25,
    "page": 1,
    "pageSize": 10,
    "users": [
      {
        "id": 1,
        "username": "zhangsan",
        "loginType": "password",
        "status": "active",
        "createdAt": "2024-01-15T10:30:00Z",
        "lastLoginAt": "2024-06-20T08:00:00Z"
      },
      {
        "id": 2,
        "weixinName": "张三",
        "weixinImageUrl": "https://thirdwx.qlogo.cn/mmopen/vi_32/...",
        "loginType": "weixin",
        "openid": "openid_1234567890",
        "status": "active",
        "createdAt": "2024-03-10T14:20:00Z",
        "lastLoginAt": "2024-06-22T15:30:00Z"
      }
    ]
  }
}
```

- **响应示例 (失败)**:
```json
{
  "code": "0001",
  "info": "未授权，请先登录"
}
```

---

### 2. 获取用户详情

- **URL**: `GET /v1/users/{userId}`
- **描述**: 根据 ID 获取单个用户详情
- **基础URL**: `http://localhost:8080/api`
- **请求头**:
```
Authorization: Bearer <jwtToken>
```
- **请求参数**:

| 参数名 | 类型   | 必填 | 描述   |
| ------ | ------ | ---- | ------ |
| userId | number | 是   | 用户ID |

- **请求示例**:
```
GET /v1/users/1
```

- **响应示例 (成功)**:
```json
{
  "code": "0000",
  "data": {
    "id": 1,
    "username": "zhangsan",
    "loginType": "password",
    "status": "active",
    "createdAt": "2024-01-15T10:30:00Z",
    "lastLoginAt": "2024-06-20T08:00:00Z"
  }
}
```

- **响应示例 (失败 - 用户不存在)**:
```json
{
  "code": "0001",
  "info": "用户不存在"
}
```

---

### 3. 更新用户状态（启用/禁用）

- **URL**: `PUT /v1/users/{userId}/status`
- **描述**: 启用或禁用用户账号
- **基础URL**: `http://localhost:8080/api`
- **请求头**:
```
Authorization: Bearer <jwtToken>
Content-Type: application/json
```
- **请求参数**:

| 参数名   | 类型   | 必填 | 描述                          |
| -------- | ------ | ---- | ----------------------------- |
| userId   | number | 是   | 用户ID (路径参数)             |
| status   | string | 是   | 用户状态: `active` 或 `disabled` |

- **请求示例**:
```
PUT /v1/users/2/status
```
```json
{
  "status": "disabled"
}
```

- **响应示例 (成功)**:
```json
{
  "code": "0000",
  "info": "用户状态已更新",
  "data": {
    "id": 2,
    "status": "disabled"
  }
}
```

- **响应示例 (失败)**:
```json
{
  "code": "0001",
  "info": "未授权，无管理员权限"
}
```

---

### 4. 删除用户

- **URL**: `DELETE /v1/users/{userId}`
- **描述**: 删除指定用户（软删除）
- **基础URL**: `http://localhost:8080/api`
- **请求头**:
```
Authorization: Bearer <jwtToken>
```
- **请求参数**:

| 参数名 | 类型   | 必填 | 描述   |
| ------ | ------ | ---- | ------ |
| userId | number | 是   | 用户ID |

- **请求示例**:
```
DELETE /v1/users/2
```

- **响应示例 (成功)**:
```json
{
  "code": "0000",
  "info": "用户已删除"
}
```

- **响应示例 (失败)**:
```json
{
  "code": "0001",
  "info": "用户不存在"
}
```

---

## 三、分类管理相关 API

> **注意**: 以下分类管理 API 需要管理员权限，请求头须携带 JWT Token：`Authorization: Bearer <token>`

### 1. 获取分类列表

- **URL**: `GET /categories`
- **描述**: 获取所有分类列表
- **基础URL**: `http://localhost:3000/api`
- **请求头**:
```
Authorization: Bearer <jwtToken>
```
- **请求参数**: 无

- **响应示例 (成功)**:
```json
{
  "categories": [
    {
      "id": 1,
      "name": "Java",
      "key": "java",
      "icon": "bi bi-cup-hot-fill",
      "color": "#e76f00",
      "articleCount": 12
    },
    {
      "id": 2,
      "name": "Python",
      "key": "python",
      "icon": "bi bi-stack",
      "color": "#3776ab",
      "articleCount": 8
    }
  ]
}
```

---

### 2. 根据 ID 获取分类详情

- **URL**: `GET /categories/{categoryId}`
- **描述**: 获取单个分类详情
- **基础URL**: `http://localhost:3000/api`
- **请求头**:
```
Authorization: Bearer <jwtToken>
```
- **请求参数**:

| 参数名     | 类型   | 必填 | 描述   |
| ---------- | ------ | ---- | ------ |
| categoryId | number | 是   | 分类ID |

- **请求示例**:
```
GET /categories/1
```

- **响应示例 (成功)**:
```json
{
  "category": {
    "id": 1,
    "name": "Java",
    "key": "java",
    "icon": "bi bi-cup-hot-fill",
    "color": "#e76f00",
    "articleCount": 12
  }
}
```

---

### 3. 创建分类

- **URL**: `POST /categories`
- **描述**: 创建新分类
- **基础URL**: `http://localhost:3000/api`
- **请求头**:
```
Authorization: Bearer <jwtToken>
Content-Type: application/json
```
- **请求参数**:

| 参数名 | 类型   | 必填 | 描述                       |
| ------ | ------ | ---- | -------------------------- |
| name   | string | 是   | 分类名称                   |
| key    | string | 是   | 分类标识键（唯一，创建后不可修改） |
| icon   | string | 否   | 图标类名（如 bi bi-cup-hot-fill） |
| color  | string | 否   | 分类颜色（如 #e76f00）      |

- **请求示例**:
```json
{
  "name": "Java",
  "key": "java",
  "icon": "bi bi-cup-hot-fill",
  "color": "#e76f00"
}
```

- **响应示例 (成功)**:
```json
{
  "category": {
    "id": 1,
    "name": "Java",
    "key": "java",
    "icon": "bi bi-cup-hot-fill",
    "color": "#e76f00",
    "articleCount": 0
  }
}
```

- **响应示例 (失败 - 标识键已存在)**:
```json
{
  "code": "0001",
  "info": "分类标识键已存在"
}
```

---

### 4. 更新分类

- **URL**: `PUT /categories/{categoryId}`
- **描述**: 更新分类信息（标识键不可修改）
- **基础URL**: `http://localhost:3000/api`
- **请求头**:
```
Authorization: Bearer <jwtToken>
Content-Type: application/json
```
- **请求参数**:

| 参数名     | 类型   | 必填 | 描述           |
| ---------- | ------ | ---- | -------------- |
| categoryId | number | 是   | 分类ID（路径）  |
| name       | string | 否   | 分类名称       |
| icon       | string | 否   | 图标类名       |
| color      | string | 否   | 分类颜色       |

- **请求示例**:
```
PUT /categories/1
```
```json
{
  "name": "Java 进阶",
  "color": "#f89820"
}
```

- **响应示例 (成功)**:
```json
{
  "category": {
    "id": 1,
    "name": "Java 进阶",
    "key": "java",
    "icon": "bi bi-cup-hot-fill",
    "color": "#f89820",
    "articleCount": 12
  }
}
```

---

### 5. 删除分类

- **URL**: `DELETE /categories/{categoryId}`
- **描述**: 删除分类，该分类下文章的 category 字段将被清空
- **基础URL**: `http://localhost:3000/api`
- **请求头**:
```
Authorization: Bearer <jwtToken>
```
- **请求参数**:

| 参数名     | 类型   | 必填 | 描述   |
| ---------- | ------ | ---- | ------ |
| categoryId | number | 是   | 分类ID |

- **请求示例**:
```
DELETE /categories/1
```

- **响应示例 (成功)**:
```json
{
  "success": true
}
```

---

## 四、文章相关 API

### 1. 获取所有文章列表

- **URL**: `GET /articles`
- **描述**: 获取所有文章列表
- **基础URL**: `http://localhost:3000/api`
- **请求参数**: 无

- **响应示例**:
```json
{
  "articles": [
    {
      "id": 1,
      "title": "文章标题",
      "summary": "文章摘要",
      "date": "2024-01-01",
      "category": "java",
      "image": "https://example.com/image.jpg",
      "theme": "java"
    }
  ]
}
```

---

### 2. 根据主题获取文章列表

- **URL**: `GET /articles?theme={theme}`
- **描述**: 根据主题/分类获取文章列表
- **基础URL**: `http://localhost:3000/api`
- **请求参数**:

| 参数名 | 类型   | 必填 | 描述                    |
| ------ | ------ | ---- | ----------------------- |
| theme  | string | 是   | 主题分类 (java/python/c++/vue) |

- **请求示例**:
```
GET /articles?theme=java
```

- **响应示例**:
```json
{
  "articles": [
    {
      "id": 1,
      "title": "Java基础知识",
      "summary": "Java基础知识介绍",
      "date": "2024-06-28",
      "category": "java",
      "image": "https://picsum.photos/200/300",
      "theme": "java"
    }
  ]
}
```

---

### 3. 根据 ID 获取文章详情

- **URL**: `GET /articles/{articleId}`
- **描述**: 获取单篇文章详情
- **基础URL**: `http://localhost:3000/api`
- **请求参数**:

| 参数名    | 类型   | 必填 | 描述   |
| --------- | ------ | ---- | ------ |
| articleId | number | 是   | 文章ID |

- **请求示例**:
```
GET /articles/1
```

- **响应示例**:
```json
{
  "article": {
    "id": 1,
    "title": "文章标题",
    "content": "文章内容...",
    "summary": "文章摘要",
    "date": "2024-01-01",
    "category": "java",
    "image": "https://example.com/image.jpg",
    "theme": "java"
  }
}
```

---

### 4. 创建文章

- **URL**: `POST /articles`
- **描述**: 创建新文章
- **基础URL**: `http://localhost:3000/api`
- **请求头**:
```
Content-Type: application/json
```

- **请求参数**:

| 参数名    | 类型   | 必填 | 描述       |
| --------- | ------ | ---- | ---------- |
| title     | string | 是   | 文章标题   |
| content   | string | 是   | 文章内容   |
| summary   | string | 否   | 文章摘要   |
| category  | string | 是   | 文章分类   |
| theme     | string | 是   | 文章主题   |
| image     | string | 否   | 文章封面图 |

- **请求示例**:
```json
{
  "title": "新文章标题",
  "content": "文章内容...",
  "summary": "文章摘要",
  "category": "java",
  "theme": "java",
  "image": "https://example.com/image.jpg"
}
```

- **响应示例**:
```json
{
  "article": {
    "id": 2,
    "title": "新文章标题",
    "content": "文章内容...",
    "summary": "文章摘要",
    "category": "java",
    "theme": "java",
    "image": "https://example.com/image.jpg"
  }
}
```

---

### 5. 更新文章

- **URL**: `PUT /articles/{articleId}`
- **描述**: 更新文章信息
- **基础URL**: `http://localhost:3000/api`
- **请求头**:
```
Content-Type: application/json
```

- **请求参数**:

| 参数名    | 类型   | 必填 | 描述       |
| --------- | ------ | ---- | ---------- |
| articleId | number | 是   | 文章ID     |
| title     | string | 否   | 文章标题   |
| content   | string | 否   | 文章内容   |
| summary   | string | 否   | 文章摘要   |
| category  | string | 否   | 文章分类   |
| image     | string | 否   | 文章封面图 |

- **请求示例**:
```
PUT /articles/1
```
```json
{
  "title": "更新后的标题",
  "content": "更新后的内容..."
}
```

- **响应示例**:
```json
{
  "article": {
    "id": 1,
    "title": "更新后的标题",
    "content": "更新后的内容..."
  }
}
```

---

### 6. 删除文章

- **URL**: `DELETE /articles/{articleId}`
- **描述**: 删除文章
- **基础URL**: `http://localhost:3000/api`
- **请求参数**:

| 参数名    | 类型   | 必填 | 描述   |
| --------- | ------ | ---- | ------ |
| articleId | number | 是   | 文章ID |

- **请求示例**:
```
DELETE /articles/1
```

- **响应示例**:
```json
{
  "success": true
}
```

---

### 7. 上传图片

- **URL**: `POST /upload/image`
- **描述**: 上传图片到阿里云 OSS，返回图片 URL
- **基础URL**: `http://localhost:3000/api`
- **认证方式**: JWT Token (请求头 `Authorization: Bearer <token>`)
- **请求格式**: `multipart/form-data`
- **请求参数**:

| 参数名 | 类型   | 必填 | 描述     |
| ------ | ------ | ---- | -------- |
| file   | File   | 是   | 图片文件 |

- **请求示例**:
```
POST /upload/image
Content-Type: multipart/form-data
Authorization: Bearer <jwtToken>

file: (binary)
```

- **响应示例 (成功)**:
```json
{
  "code": "0000",
  "url": "https://your-bucket.oss-cn-hangzhou.aliyuncs.com/images/2024/example.jpg"
}
```

- **响应示例 (失败)**:
```json
{
  "code": "0001",
  "info": "上传失败，文件类型不支持"
}
```

**前端处理**: md-editor-v3 的 `onUploadImg` 回调会自动将返回的 URL 插入为 Markdown 图片语法

**后端实现要点**:
1. 接收 `file` 字段
2. 生成唯一文件名（如 `UUID + 原始扩展名`）
3. 使用阿里云 OSS SDK 上传到指定 Bucket
4. 返回文件的公开访问 URL
5. 建议限制文件类型为 `image/jpeg`, `image/png`, `image/gif`, `image/webp`
6. 建议限制文件大小不超过 5MB

---

## 五、图片管理相关 API

> **注意**: 以下图片管理 API 需要管理员权限，请求头须携带 JWT Token：`Authorization: Bearer <token>`

### 1. 获取图片列表

- **URL**: `GET /images`
- **描述**: 获取所有已上传图片列表
- **基础URL**: `http://localhost:3000/api`
- **请求头**:
```
Authorization: Bearer <jwtToken>
```
- **请求参数**: 无

- **响应示例 (成功)**:
```json
{
  "code": "0000",
  "data": [
    {
      "id": 1,
      "name": "博客封面.jpg",
      "url": "https://your-bucket.oss-cn-hangzhou.aliyuncs.com/images/2024/example.jpg",
      "size": "245 KB",
      "uploadedAt": "2024-06-20T10:30:00Z"
    }
  ]
}
```

- **响应示例 (失败)**:
```json
{
  "code": "0001",
  "info": "未授权，请先登录"
}
```

---

### 2. 删除图片

- **URL**: `DELETE /images/{imageId}`
- **描述**: 删除指定图片
- **基础URL**: `http://localhost:3000/api`
- **请求头**:
```
Authorization: Bearer <jwtToken>
```
- **请求参数**:

| 参数名  | 类型   | 必填 | 描述       |
| ------- | ------ | ---- | ---------- |
| imageId | number | 是   | 图片ID     |

- **请求示例**:
```
DELETE /images/1
```

- **响应示例 (成功)**:
```json
{
  "code": "0000",
  "info": "删除成功"
}
```

- **响应示例 (失败)**:
```json
{
  "code": "0001",
  "info": "图片不存在"
}
```

---

## 六、项目管理相关 API

> **注意**: 以下项目管理 API 需要管理员权限，请求头须携带 JWT Token：`Authorization: Bearer <token>`

### 1. 获取项目列表

- **URL**: `GET /projects`
- **描述**: 获取所有项目列表
- **基础URL**: `http://localhost:3000/api`
- **请求头**:
```
Authorization: Bearer <jwtToken>
```
- **请求参数**: 无

- **响应示例 (成功)**:
```json
{
  "code": "0000",
  "data": [
    {
      "id": 1,
      "name": "个人博客系统",
      "description": "基于 Vue 3 + Node.js 的全栈博客系统",
      "techStack": "Vue 3, Node.js, Express, MongoDB",
      "projectUrl": "https://myblog.example.com",
      "githubUrl": "https://github.com/username/myblog",
      "previewUrl": "https://demo.myblog.example.com",
      "coverImage": "https://example.com/cover.jpg",
      "status": "已完成",
      "sortOrder": 1,
      "startDate": "2024-01-15",
      "endDate": "2024-03-20",
      "isPublic": true
    }
  ]
}
```

- **响应示例 (失败)**:
```json
{
  "code": "0001",
  "info": "未授权，请先登录"
}
```

---

### 2. 根据 ID 获取项目详情

- **URL**: `GET /projects/{projectId}`
- **描述**: 获取单个项目详情
- **基础URL**: `http://localhost:3000/api`
- **请求头**:
```
Authorization: Bearer <jwtToken>
```
- **请求参数**:

| 参数名    | 类型   | 必填 | 描述   |
| --------- | ------ | ---- | ------ |
| projectId | number | 是   | 项目ID |

- **请求示例**:
```
GET /projects/1
```

- **响应示例 (成功)**:
```json
{
  "code": "0000",
  "data": {
    "id": 1,
    "name": "个人博客系统",
    "description": "基于 Vue 3 + Node.js 的全栈博客系统",
    "techStack": "Vue 3, Node.js, Express, MongoDB",
    "projectUrl": "https://myblog.example.com",
    "githubUrl": "https://github.com/username/myblog",
    "previewUrl": "https://demo.myblog.example.com",
    "coverImage": "https://example.com/cover.jpg",
    "status": "已完成",
    "sortOrder": 1,
    "startDate": "2024-01-15",
    "endDate": "2024-03-20",
    "isPublic": true
  }
}
```

- **响应示例 (失败)**:
```json
{
  "code": "0001",
  "info": "项目不存在"
}
```

---

### 3. 创建项目

- **URL**: `POST /projects`
- **描述**: 创建新项目
- **基础URL**: `http://localhost:3000/api`
- **请求头**:
```
Authorization: Bearer <jwtToken>
Content-Type: application/json
```
- **请求参数**:

| 参数名      | 类型    | 必填 | 描述                    |
| ----------- | ------- | ---- | ----------------------- |
| name        | string  | 是   | 项目名称                |
| description | string  | 否   | 项目描述                |
| techStack   | string  | 否   | 技术栈（逗号分隔）      |
| projectUrl  | string  | 否   | 项目链接                |
| githubUrl   | string  | 否   | GitHub 仓库地址         |
| previewUrl  | string  | 否   | 在线预览地址            |
| coverImage  | string  | 否   | 封面图片 URL            |
| status      | string  | 否   | 状态（进行中/已完成/暂停）|
| sortOrder   | number  | 否   | 排序权重，默认 0        |
| startDate   | string  | 否   | 开始日期（ISO格式）     |
| endDate     | string  | 否   | 结束日期（ISO格式）     |
| isPublic    | boolean | 否   | 是否公开，默认 true     |

- **请求示例**:
```json
{
  "name": "个人博客系统",
  "description": "基于 Vue 3 的全栈博客系统",
  "techStack": "Vue 3, Node.js, MongoDB",
  "status": "进行中",
  "isPublic": true
}
```

- **响应示例 (成功)**:
```json
{
  "code": "0000",
  "data": {
    "id": 5,
    "name": "个人博客系统",
    "description": "基于 Vue 3 的全栈博客系统",
    "techStack": "Vue 3, Node.js, MongoDB",
    "projectUrl": "",
    "githubUrl": "",
    "previewUrl": "",
    "coverImage": "",
    "status": "进行中",
    "sortOrder": 0,
    "startDate": "",
    "endDate": "",
    "isPublic": true
  }
}
```

- **响应示例 (失败)**:
```json
{
  "code": "0001",
  "info": "项目名称不能为空"
}
```

---

### 4. 更新项目

- **URL**: `PUT /projects/{projectId}`
- **描述**: 更新项目信息
- **基础URL**: `http://localhost:3000/api`
- **请求头**:
```
Authorization: Bearer <jwtToken>
Content-Type: application/json
```
- **请求参数**:

| 参数名      | 类型    | 必填 | 描述                    |
| ----------- | ------- | ---- | ----------------------- |
| projectId   | number  | 是   | 项目ID（路径参数）      |
| name        | string  | 否   | 项目名称                |
| description | string  | 否   | 项目描述                |
| techStack   | string  | 否   | 技术栈                  |
| projectUrl  | string  | 否   | 项目链接                |
| githubUrl   | string  | 否   | GitHub 仓库地址         |
| previewUrl  | string  | 否   | 在线预览地址            |
| coverImage  | string  | 否   | 封面图片 URL            |
| status      | string  | 否   | 状态                    |
| sortOrder   | number  | 否   | 排序权重                |
| startDate   | string  | 否   | 开始日期                |
| endDate     | string  | 否   | 结束日期                |
| isPublic    | boolean | 否   | 是否公开                |

- **请求示例**:
```
PUT /projects/1
```
```json
{
  "name": "个人博客系统 V2",
  "status": "已完成"
}
```

- **响应示例 (成功)**:
```json
{
  "code": "0000",
  "data": {
    "id": 1,
    "name": "个人博客系统 V2",
    "status": "已完成"
  }
}
```

- **响应示例 (失败)**:
```json
{
  "code": "0001",
  "info": "项目不存在"
}
```

---

### 5. 删除项目

- **URL**: `DELETE /projects/{projectId}`
- **描述**: 删除指定项目
- **基础URL**: `http://localhost:3000/api`
- **请求头**:
```
Authorization: Bearer <jwtToken>
```
- **请求参数**:

| 参数名    | 类型   | 必填 | 描述   |
| --------- | ------ | ---- | ------ |
| projectId | number | 是   | 项目ID |

- **请求示例**:
```
DELETE /projects/1
```

- **响应示例 (成功)**:
```json
{
  "code": "0000",
  "info": "删除成功"
}
```

- **响应示例 (失败)**:
```json
{
  "code": "0001",
  "info": "项目不存在"
}
```

---

## 七、API 服务函数

### articleService.js

位置: `src/services/articleService.js`

| 函数名            | 参数           | 返回值  | 描述           |
| ----------------- | -------------- | ------- | -------------- |
| getArticlesByTheme | theme (string) | Promise<Array> | 根据主题获取文章 |
| getAllArticles    | 无             | Promise<Array> | 获取所有文章     |
| getArticleById    | articleId (number) | Promise<Object> | 获取文章详情   |
| createArticle     | articleData (Object) | Promise<Object> | 创建文章     |
| updateArticle     | articleId (number), articleData (Object) | Promise<Object> | 更新文章 |
| deleteArticle     | articleId (number) | Promise<boolean> | 删除文章   |

### userService.js

位置: `src/services/userService.js`

| 函数名            | 参数           | 返回值  | 描述           |
| ----------------- | -------------- | ------- | -------------- |
| getUsers          | params (Object) | Promise<Object> | 获取用户列表（支持 keyword, loginType, status, page, pageSize 参数） |
| getUserById       | userId (number) | Promise<Object> | 获取用户详情   |
| updateUserStatus  | userId (number), status (string) | Promise<Object> | 更新用户状态（active/disabled） |
| deleteUser        | userId (number) | Promise<boolean> | 删除用户   |

### categoryService.js

位置: `src/services/categoryService.js`

| 函数名            | 参数           | 返回值  | 描述           |
| ----------------- | -------------- | ------- | -------------- |
| getCategories     | 无             | Promise<Array> | 获取分类列表   |
| getCategoryById   | categoryId (number) | Promise<Object> | 获取分类详情   |
| createCategory    | categoryData (Object) | Promise<Object> | 创建分类   |
| updateCategory    | categoryId (number), categoryData (Object) | Promise<Object> | 更新分类 |
| deleteCategory    | categoryId (number) | Promise<boolean> | 删除分类   |

### uploadService.js

位置: `src/services/uploadService.js`

| 函数名            | 参数           | 返回值  | 描述           |
| ----------------- | -------------- | ------- | -------------- |
| uploadImage       | file (File)    | Promise<string> | 上传图片，返回图片URL |
| getImages         | 无             | Promise<Array>  | 获取图片列表   |
| deleteImage       | imageId (number) | Promise<boolean> | 删除图片   |

### projectService.js

位置: `src/services/projectService.js`

| 函数名            | 参数           | 返回值  | 描述           |
| ----------------- | -------------- | ------- | -------------- |
| getProjects       | 无             | Promise<Array>  | 获取项目列表   |
| getProjectById    | projectId (number) | Promise<Object> | 获取项目详情   |
| createProject     | projectData (Object) | Promise<Object> | 创建项目   |
| updateProject     | projectId (number), projectData (Object) | Promise<Object> | 更新项目 |
| deleteProject     | projectId (number) | Promise<boolean> | 删除项目   |

---

## 八、GitHub 页面使用的第三方 API

> **说明**: 以下接口用于 `src/views/github/GitHubStarView.vue`，属于第三方 GitHub 接口，不是本项目后端提供的 API。

### 1. 获取 GitHub 用户资料

- **URL**: `GET https://api.github.com/users/{username}`
- **描述**: 获取 GitHub 用户公开资料
- **请求参数**:

| 参数名   | 类型   | 必填 | 描述        |
| -------- | ------ | ---- | ----------- |
| username | string | 是   | GitHub 用户名 |

- **请求示例**:
```
GET https://api.github.com/users/your-github-name
```

- **前端用途**:
  - 读取头像、昵称、简介、位置、博客、公司
  - 读取公开仓库数、关注数、被关注数

---

### 2. 获取 GitHub Star 列表

- **URL**: `GET https://api.github.com/users/{username}/starred`
- **描述**: 获取用户公开 Star 的仓库列表
- **查询参数**:

| 参数名    | 类型   | 必填 | 描述 |
| --------- | ------ | ---- | ---- |
| per_page  | number | 否   | 每页数量，项目中使用 `30` |
| sort      | string | 否   | 排序字段，项目中使用 `stars` |
| direction | string | 否   | 排序方向，项目中使用 `desc` |

- **请求示例**:
```
GET https://api.github.com/users/your-github-name/starred?per_page=30&sort=stars&direction=desc
```

- **前端用途**:
  - 展示 Star 仓库名称、描述、语言、Star 数、Fork 数

---

### 3. 获取 GitHub 仓库列表

- **URL**: `GET https://api.github.com/users/{username}/repos`
- **描述**: 获取用户公开仓库列表
- **查询参数**:

| 参数名    | 类型   | 必填 | 描述 |
| --------- | ------ | ---- | ---- |
| per_page  | number | 否   | 每页数量，项目中使用 `30` |
| sort      | string | 否   | 排序字段，项目中使用 `updated` |
| direction | string | 否   | 排序方向，项目中使用 `desc` |

- **请求示例**:
```
GET https://api.github.com/users/your-github-name/repos?per_page=30&sort=updated&direction=desc
```

- **前端用途**:
  - 展示自己的仓库列表
  - 汇总所有仓库 `stargazers_count` 计算 `totalStars`

---

### 4. 获取 GitHub Contributions HTML

- **目标 URL**: `GET https://github.com/users/{username}/contributions?from={year}-01-01&to={year}-12-31`
- **描述**: 获取 GitHub 年度 contributions 热力图 HTML
- **说明**: 由于浏览器跨域限制，项目中不会直接请求该 URL，而是通过代理获取 HTML 再解析。

- **请求示例**:
```
GET https://github.com/users/your-github-name/contributions?from=2024-01-01&to=2024-12-31
```

- **前端用途**:
  - 解析返回的 HTML
  - 生成年度 contributions 热力图数据

---

### 5. Contributions 代理接口

项目中按顺序尝试以下两个公开代理：

#### 5.1 CodeTabs Proxy

- **URL**: `GET https://api.codetabs.com/v1/proxy?quest={encodedUrl}`
- **描述**: 代理请求 GitHub contributions 页面 HTML

- **请求示例**:
```
GET https://api.codetabs.com/v1/proxy?quest=https%3A%2F%2Fgithub.com%2Fusers%2Fyour-github-name%2Fcontributions%3Ffrom%3D2024-01-01%26to%3D2024-12-31
```

#### 5.2 AllOrigins Proxy

- **URL**: `GET https://api.allorigins.win/raw?url={encodedUrl}`
- **描述**: 作为备用代理请求 GitHub contributions 页面 HTML

- **请求示例**:
```
GET https://api.allorigins.win/raw?url=https%3A%2F%2Fgithub.com%2Fusers%2Fyour-github-name%2Fcontributions%3Ffrom%3D2024-01-01%26to%3D2024-12-31
```

---

## 九、API 服务函数与直接调用补充

### LoginView.vue 中的直接 API 调用

位置: `src/views/user/LoginView.vue`

| 调用位置 | 方法 | URL | 描述 |
| -------- | ---- | --- | ---- |
| 用户名密码登录 | POST | `http://localhost:8080/api/r1/login/token` | 获取 JWT Token |
| 获取微信二维码 ticket | GET | `http://localhost:8080/api/v1/login/weixin_qrcode_ticket` | 获取二维码票据 |
| 轮询微信登录状态 | GET | `http://localhost:8080/api/v1/login/check_login?ticket=...` | 检查扫码登录状态 |
| 获取微信用户信息 | GET | `http://localhost:8080/api/v1/login/weixin_user_information?openid=...` | 获取微信昵称和头像 |

### RegisterView.vue 中的直接 API 调用

位置: `src/views/user/RegisterView.vue`

| 调用位置 | 方法 | URL | 描述 |
| -------- | ---- | --- | ---- |
| 用户注册 | POST | `http://localhost:8080/api/r1/register` | 注册账号 |

### GitHubStarView.vue 中的直接 API 调用

位置: `src/views/github/GitHubStarView.vue`

| 调用位置 | 方法 | URL | 描述 |
| -------- | ---- | --- | ---- |
| 获取用户资料 | GET | `https://api.github.com/users/{username}` | 获取 GitHub 用户资料 |
| 获取 Star 列表 | GET | `https://api.github.com/users/{username}/starred?per_page=30&sort=stars&direction=desc` | 获取用户 Star 仓库 |
| 获取仓库列表 | GET | `https://api.github.com/users/{username}/repos?per_page=30&sort=updated&direction=desc` | 获取用户仓库 |
| 获取 Contributions HTML | GET | `https://api.codetabs.com/v1/proxy?quest={encodedUrl}` / `https://api.allorigins.win/raw?url={encodedUrl}` | 通过代理抓取 contributions 页面 |

---

## 十、错误码说明

| 错误码 | 描述           |
| ------ | -------------- |
| 0000   | 成功           |
| 0001   | 失败/错误      |

---

## 十一、本地存储说明

### localStorage

| 键名           | 描述               |
| -------------- | ------------------ |
| jwtToken       | JWT Token (24小时) |
| jwtTokenExpiry | Token 过期时间戳   |
| weixinName     | 微信用户昵称       |
| weixinImageUrl | 微信用户头像       |

### Cookie

| 键名         | 描述                   |
| ------------ | ---------------------- |
| openIdToken  | 微信 OpenID (30天)     |

---

## 十二、前端路由

### 主要路由

| 路由名称     | 路径                       | 描述         |
| ------------ | -------------------------- | ------------ |
| blog         | /                          | 博客首页     |
| me           | /me                        | 个人介绍     |
| githubstar   | /githubstar                | GitHub项目   |
| dlpaper      | /dlpaper                   | 深度学习论文 |
| login        | /user/login                | 登录页       |
| register     | /user/register             | 注册页       |
| article      | /blog/article/:theme       | 文章列表     |
| articleDetail| /blog/article/:theme/:id   | 文章详情     |

### 后台管理路由

| 路由名称              | 路径                          | 描述           | 需要认证 |
| --------------------- | ----------------------------- | -------------- | -------- |
| backend               | /backend                      | 后台管理（重定向到仪表盘） | 是 |
| backend-dashboard     | /backend/dashboard           | 仪表盘概览     | 是       |
| backend-articles      | /backend/articles            | 文章列表管理   | 是       |
| backend-article-new   | /backend/articles/new         | 新建文章       | 是       |
| backend-article-edit  | /backend/articles/:id/edit   | 编辑文章       | 是       |
| backend-categories    | /backend/categories           | 分类管理       | 是       |
| backend-projects      | /backend/projects            | 项目管理       | 是       |
| backend-images        | /backend/images              | 图片管理       | 是       |
| backend-users         | /backend/users              | 用户管理       | 是       |

---

*文档生成时间: 2024年*
