---
name: deploy-ng
description: 启动本地开发环境或整套容器（mongo、keycloak、app）。需要跑起后端/前端、联调 Keycloak 登录或复现部署问题时使用。替代旧仓库的 deploy-webprotege 技能。
---

# deploy-ng

## 本地开发（docs/00 §9）

1. 基础设施：`docker compose -f deploy/docker-compose.dev.yml up -d`
   - mongo:7 → `localhost:27017`
   - keycloak:26 → `localhost:8180`，导入 `deploy/keycloak/webprotege-realm.json`
     （client `webprotege-web` public+PKCE、`webprotege-api` bearer-only、realm role `webprotege-admin`、测试用户 admin/editor/viewer，密码同用户名）
2. 后端：`mvn -pl wp-server spring-boot:run -Dspring-boot.run.profiles=dev`
   - `dev` profile 打开本地兜底登录，数据目录为 `./.data`
3. 前端：`pnpm -C wp-web dev`（Vite 代理 `/api`、`/data`、`/download` 到 `:8080`）
4. 检查：`curl http://localhost:8080/actuator/health` 返回 `UP`

## 整套容器

`docker compose -f deploy/docker-compose.yml up -d --build`

## 停止与清理

- `docker compose -f deploy/docker-compose.dev.yml down`（加 `-v` 会删除 Mongo 数据卷，先确认）

API Key 生成由 `wp-cli generate-api-key` 提供（P1-14），替代旧的 generate-apikey 技能。
