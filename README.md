# MyBlog

MyBlog 是一个个人博客全栈项目，包含前台内容展示、用户体系、后台管理、图片上传、留言板、音乐盒子、项目展示和生产环境 Docker 部署配置。

## 功能概览

- 博客文章展示、文章详情、主题分类
- 后台文章、主题、项目、音乐、留言、用户和图片管理
- 用户注册、登录、个人资料维护和头像上传
- Spring Security + JWT 权限认证，支持后台管理员访问控制
- 微信登录相关接口与流程文档
- 项目展示页、留言板、天气卡片、音乐播放等前台模块
- 阿里云 OSS 图片上传与静态资源访问
- Docker Compose 生产部署，包含 Nginx、后端、MySQL、Redis

## 技术栈

### 前端

- Vue 3
- Vue Router
- Vuex
- Axios
- Element Plus
- Bootstrap / Bootstrap Icons
- Vitest

### 后端

- Java 8
- Spring Boot 2.7.x
- Spring Security
- JWT
- MyBatis
- MySQL
- Redis
- Aliyun OSS
- Maven 多模块工程

### 部署

- Docker
- Docker Compose
- Nginx
- MySQL
- Redis

## 目录结构

```text
.
├── backend/                 # Spring Boot 后端 Maven 多模块工程
│   ├── backend-api/         # API 契约与接口定义
│   ├── backend-app/         # 应用启动、配置、安全认证
│   ├── backend-domain/      # 领域服务与业务逻辑
│   ├── backend-infrastructure/ # 数据访问、外部服务集成
│   ├── backend-trigger/     # HTTP Controller、任务、监听入口
│   └── backend-types/       # 通用类型、响应对象、枚举
├── myblog/                  # Vue 前端应用
│   ├── src/views/           # 页面视图
│   ├── src/components/      # 页面组件和后台管理组件
│   ├── src/services/        # 前端 API 服务封装
│   ├── src/router/          # 路由和访问控制
│   └── src/store/           # Vuex 状态管理
├── deploy/prod/             # 生产部署配置和初始化脚本
├── docs/                    # 项目文档
└── README.md                # 项目总览文档
```

## 本地开发

### 1. 准备环境

- Node.js 18+ 或兼容 Vue CLI 5 的版本
- JDK 8
- Maven 3.6+
- MySQL 8
- Redis

### 2. 配置后端

后端默认读取 `backend/backend-app/src/main/resources/application.yml`，当前激活 `dev` profile。

本地私有配置文件 `backend/backend-app/src/main/resources/application-dev.yml` 已被 `.gitignore` 忽略，请在本地创建并配置数据库、Redis、OSS、微信等真实参数。

### 3. 启动后端

```bash
mvn -f backend/pom.xml -pl backend-app -am spring-boot:run
```

### 4. 启动前端

```bash
npm --prefix myblog install
npm --prefix myblog run serve
```

前端开发服务默认使用 `myblog/package.json` 中的 `serve` 脚本启动。

## 测试与构建

### 前端单元测试

```bash
npm --prefix myblog run test:unit
```

### 前端构建

```bash
npm --prefix myblog run build
```

### 后端打包

```bash
mvn -f backend/pom.xml -pl backend-app -am package
```

## 生产部署

生产部署配置位于 `deploy/prod/`。

1. 复制 `deploy/prod/.env.example` 为 `deploy/prod/.env`。
2. 填写数据库、Redis、OSS、微信、域名和镜像地址等真实配置。
3. 将 SSL 证书文件放到 `deploy/prod/nginx/ssl/`。
4. 构建并推送前后端镜像。
5. 在服务器执行：

```bash
docker compose -f deploy/prod/docker-compose.yml --env-file deploy/prod/.env pull
docker compose -f deploy/prod/docker-compose.yml --env-file deploy/prod/.env up -d
```

更详细的部署说明见 `deploy/prod/README.md`。

## 安全与提交规范

以下内容不应提交到仓库：

- `.env`、`.env.*` 等真实环境变量文件
- SSL 证书、私钥、`.p12`、`.pfx`
- `backend/data/` 运行数据和日志
- `node_modules/`、`dist/`、`target/` 等依赖和构建产物
- 本地开发配置 `application-dev.yml`
- 本地工作树和临时工具状态

提交前建议执行：

```bash
git status --short
```

确认没有误提交密钥、证书、日志或本地配置。
