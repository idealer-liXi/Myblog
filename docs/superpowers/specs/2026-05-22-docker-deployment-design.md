# MyBlog Docker Deployment Design

**Date:** 2026-05-22

**Goal:** Package the MyBlog frontend and backend into Docker images, publish the business images to Alibaba Cloud Container Registry, and deploy the full production stack on the server through a single `docker-compose.yml` that brings up `nginx`, frontend static assets, Spring Boot backend, MySQL, and Redis for `https://idealer01.cn`.

## Scope

- Build a production frontend image that serves the Vue app through `nginx`.
- Build a production backend image for the Spring Boot application.
- Remove `frpc` from the deployment topology.
- Add a production `docker-compose.yml` that uses `image:` references instead of server-side builds.
- Serve the site through `https://idealer01.cn` with mounted certificate files.
- Route browser API traffic through `nginx` using `/api/...` and forward it to the backend container.
- Containerize MySQL and Redis as part of the same compose stack.
- Persist MySQL data, Redis data, Redis config, and TLS certificates on the host.
- Convert frontend API access away from hard-coded `http://localhost:8080` URLs.
- Move backend production connectivity and secrets to environment-variable driven configuration.
- Document the image push and server pull workflow for Alibaba Cloud Container Registry.

## Non-Goals

- no CI/CD pipeline in this phase
- no blue-green or rolling deployment strategy
- no multi-server high availability
- no managed database migration away from Docker-hosted MySQL in this phase
- no centralized log platform or metrics platform
- no automatic certificate issuance or renewal

## Current Problems

The repository already contains useful deployment pieces, but they do not yet form a production-ready full-stack Docker deployment.

- `backend/backend-app/Dockerfile` already exists and can run the backend jar.
- `backend/docs/dev-ops-myblog/local/docker-compose.yml` already brings up MySQL and Redis, but it is a local environment file and still includes `frpc`.
- the frontend currently has multiple service files and views with hard-coded `http://localhost:8080` API addresses.
- `application-dev.yml` contains local hostnames and sensitive credentials embedded in config, which is not suitable for production deployment.
- the current backend production config file does not yet fully express production MySQL and Redis connectivity through container service names and environment variables.

Without addressing these gaps, the site may build successfully but still fail after deployment because the frontend would call the wrong host, the backend would not connect cleanly to containerized dependencies, and secrets would remain mixed into source-controlled config.

## Options Considered

### Option 1: Single full-stack compose project

Use one production `docker-compose.yml` for `frontend`, `backend`, `mysql`, and `redis`, with frontend and backend images pushed to Alibaba Cloud Container Registry.

Pros:

- matches the target operating model exactly
- simplest server workflow: `docker compose pull && docker compose up -d`
- keeps deployment assets in one place
- easy to reason about for future maintenance and rollback

Cons:

- database lifecycle remains coupled to the same compose project
- later infrastructure separation would require follow-up refactoring

### Option 2: Containerize application only, keep database outside the main compose project

Pros:

- database operations can be isolated earlier
- easier to adopt managed DB services later

Cons:

- does not match the current goal of one compose file pulling the full runtime
- increases deployment coordination and environment-specific setup

### Option 3: Split frontend and backend into separate compose projects

Pros:

- frontend and backend release cadence can diverge

Cons:

- adds operational overhead without solving a current problem
- unnecessarily heavy for the present project stage

## Recommended Design

Use Option 1: a single production compose project that runs the full stack and pulls business images from Alibaba Cloud Container Registry.

This approach reuses the existing backend Docker baseline, aligns with the desired server workflow, and keeps the deployment surface area small enough to implement safely in one phase.

## Architecture

### External Access

- users access `https://idealer01.cn`
- port `80` exists only to redirect HTTP traffic to HTTPS
- port `443` terminates TLS in the `nginx` container

### Runtime Services

- `frontend-nginx`
  - serves the built Vue static assets
  - owns TLS termination
  - owns SPA routing fallback
  - proxies `/api/` to the backend container
- `backend-app`
  - runs the Spring Boot jar
  - is reachable only inside the Docker network
- `mysql`
  - stores relational application data
  - persists data on the host
- `redis`
  - stores login and cache state
  - persists config and optionally append-only data on the host

### Request Flow

1. browser requests `https://idealer01.cn`
2. `nginx` returns frontend static assets
3. frontend sends API requests to relative paths such as `/api/public/...`
4. `nginx` proxies `/api/...` to `http://backend:8091/api/...`
5. backend connects to `mysql:3306` and `redis:6379` through the internal Docker network

This removes browser-side cross-origin calls and avoids production dependence on `localhost`.

## Image Strategy

### Frontend Image

Use a multi-stage Docker build:

1. Node stage installs dependencies and runs the Vue production build.
2. `nginx:alpine` stage copies the build output into the standard web root.
3. Production `nginx` config is included in the image.
4. TLS certificate files are not baked into the image and are mounted at runtime.

Recommended image naming:

- `<acr-registry>/myblog/frontend-nginx:latest`
- `<acr-registry>/myblog/frontend-nginx:<release-tag>`

### Backend Image

Retain the current `backend/backend-app/Dockerfile` direction, but standardize runtime behavior around production environment variables.

Expected build flow:

1. run Maven packaging locally or in a dedicated builder workflow
2. produce `backend-app.jar`
3. build a runtime image from the jar
4. run the container with `spring.profiles.active=prod`

Recommended image naming:

- `<acr-registry>/myblog/backend-app:latest`
- `<acr-registry>/myblog/backend-app:<release-tag>`

### Tagging Policy

Do not rely on `latest` alone.

Each release should push at least:

- `latest`
- one immutable version tag such as a git short SHA or release date

This keeps rollback simple and avoids ambiguity on the server.

## Production Compose Design

### Compose Principles

- use `image:` for frontend and backend services
- do not use `build:` on the server
- keep all services on one custom bridge network such as `myblog-network`
- expose only `80` and `443` publicly from the frontend `nginx` service
- keep backend, MySQL, and Redis accessible through the internal network

### Expected Service Definitions

- `frontend`
  - image: frontend `nginx` image from Alibaba Cloud
  - ports: `80:80`, `443:443`
  - mounts: TLS certificate directory
  - depends on: `backend`
- `backend`
  - image: backend image from Alibaba Cloud
  - environment: Spring profile, datasource, Redis, OSS, WeChat, JWT-related settings
  - depends on: healthy `mysql`, healthy `redis`
- `mysql`
  - image: `mysql:8.0`
  - mounted data directory
  - mounted init SQL directory
  - health check enabled
- `redis`
  - image: `redis:6.2`
  - mounted config file
  - mounted data directory
  - health check enabled

### Deployment Directory Layout

Recommended repository layout:

```text
deploy/
  prod/
    docker-compose.yml
    .env.example
    nginx/
      default.conf
      ssl/
        idealer01.cn.pem
        idealer01.cn.key
    mysql/
      init/
    redis/
      redis.conf
    data/
      mysql/
      redis/
```

Notes:

- `.env.example` stays in git as documentation
- real `.env` is created and maintained on the server
- certificate files are uploaded later by the operator and should stay out of git
- `data/` may be excluded from git depending on repository conventions

## Nginx Design

### Responsibilities

The frontend `nginx` container owns:

- HTTP to HTTPS redirect
- TLS termination for `idealer01.cn` and optionally `www.idealer01.cn`
- serving built frontend files
- SPA fallback via `try_files ... /index.html`
- reverse proxy for `/api/`

### Routing Rules

- `http://idealer01.cn` -> 301 redirect to `https://idealer01.cn`
- `https://idealer01.cn/` -> frontend SPA
- `https://idealer01.cn/assets/...` -> static resources
- `https://idealer01.cn/api/...` -> proxied to backend service

### TLS Handling

Certificates are provided as host-mounted files, for example:

- `/etc/nginx/ssl/idealer01.cn.pem`
- `/etc/nginx/ssl/idealer01.cn.key`

This keeps certificate rotation decoupled from image rebuilds.

## Frontend API Design

The frontend must stop using hard-coded absolute local URLs such as `http://localhost:8080`.

### Recommended Rule

All browser-facing API requests should target relative paths rooted at `/api`.

Examples:

- `/api/public/weather/current`
- `/api/public/articles`
- `/api/admin/users`
- `/api/v1/login/...`

### Why This Matters

- works correctly behind production `nginx`
- avoids cross-origin and CORS complexity for the main site
- removes environment-specific browser URL logic
- avoids production failures where the browser would attempt to reach the end user's own `localhost`

### Frontend Refactoring Boundary

Refactor the frontend service layer to centralize API base handling rather than updating ad hoc view-level literals only. Existing direct `axios.post('http://localhost:8080/...')` usage should be normalized into the service layer or equivalent shared configuration.

## Backend Production Configuration

### Connectivity Rules

Within Docker production networking, backend should connect to:

- MySQL at `mysql:3306`
- Redis at `redis:6379`

It should not use host-loopback addresses like `127.0.0.1` or local-development ports like `13306` and `16379` in production.

### Configuration Source

Sensitive or environment-dependent backend values should be injected via environment variables from compose.

Priority configuration categories:

- datasource URL, username, password
- Redis host and port
- OSS endpoint and credentials
- WeChat credentials
- JWT secret or equivalent auth secrets if applicable
- active Spring profile

### Source-Control Hygiene

Current development config includes plaintext credentials. Production implementation should move secrets out of committed config values and into runtime environment injection. Where source-controlled config must remain, it should use placeholders that read from environment variables.

## Persistence Design

### MySQL

- mount a host directory for database data
- mount an initialization SQL directory for first-time boot only
- treat init scripts as bootstrap assets, not recurring migration tools

### Redis

- mount `redis.conf`
- mount a host directory for persistent Redis data if persistence is enabled

### Certificates

- mount certificate files from the host into the `nginx` container
- do not commit real certificate material into the repository

## Release Workflow

### Local or Build-Host Workflow

1. build frontend image
2. build backend jar and backend image
3. tag both images with `latest` and a release tag
4. log in to Alibaba Cloud Container Registry
5. push both business images

### Server Workflow

1. log in to Alibaba Cloud Container Registry
2. prepare `.env`, certificate files, and data directories
3. run `docker compose pull`
4. run `docker compose up -d`
5. inspect `docker compose ps`
6. inspect service logs where needed

The server should not require the application source code to build images.

## Error Handling And Operations

### Startup Ordering

- MySQL and Redis should expose health checks
- backend should wait on healthy dependency state where compose semantics allow it
- frontend can depend on backend startup, but backend readiness remains the more important gate

### Restart Policy

Use a production-safe restart policy such as `unless-stopped` for long-running services.

### Logging

Use container logs as the first operational layer in this phase. Do not add a separate log aggregation stack yet.

### Rollback

Rollback should be performed by pointing compose back to a previously pushed immutable image tag and re-running `docker compose up -d`.

## Validation Strategy

Deployment is not considered successful unless both infrastructure and business behavior work.

### Infrastructure Validation

- all expected containers start successfully
- `http://idealer01.cn` redirects to HTTPS
- `https://idealer01.cn` serves the frontend without static-resource 404s
- `/api` traffic reaches the backend through `nginx`
- backend connects successfully to MySQL and Redis

### Functional Validation

Run at least one manual validation pass for:

- homepage rendering
- article or public content loading
- login flow
- message board flow
- project showcase page
- image upload path if it depends on the production API base and OSS credentials
- any Redis-dependent login or QR-code state flow

### Configuration Validation

- no browser request should target `localhost`
- server deployment should work via image pulls only
- certificate replacement should not require a frontend image rebuild

## Expected File Impact

Expected implementation files likely include:

- `myblog/Dockerfile`
- `myblog/.dockerignore`
- frontend `nginx` config files under a production deployment directory or frontend image context
- frontend service files that currently use `http://localhost:8080`
- `backend/backend-app/Dockerfile` if runtime behavior needs normalization
- `backend/backend-app/src/main/resources/application-prod.yml`
- possibly `backend/backend-app/src/main/resources/application-dev.yml` for cleanup and placeholder alignment
- new deployment files under `deploy/prod/`
- optional `.env.example` and deployment documentation

## Risks And Mitigations

### Risk 1: Frontend still calls `localhost`

Impact:

- production site appears up, but core APIs fail from the browser

Mitigation:

- grep and replace all hard-coded local API origins
- consolidate API base handling into shared service code

### Risk 2: Backend production config remains tied to local development hosts

Impact:

- backend container starts but cannot connect to MySQL or Redis

Mitigation:

- convert production config to service-name based connectivity and environment placeholders

### Risk 3: Secrets remain committed in application config

Impact:

- operational and security exposure

Mitigation:

- move production secrets into `.env` and compose environment injection

### Risk 4: Database initialization assumptions are unclear

Impact:

- first deployment may succeed while schema setup fails or behaves inconsistently on redeploy

Mitigation:

- explicitly separate first-time init SQL from persisted database state
- document expected bootstrap behavior

## Implementation Boundary

This design intentionally stops at a production-ready single-server Docker deployment model. It prepares the codebase for reliable manual releases through Alibaba Cloud Container Registry without introducing additional infrastructure automation.
