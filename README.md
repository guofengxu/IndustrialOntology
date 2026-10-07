# IndustrialOntology

WebProtégé-based industrial ontology applications.

基于 WebProtégé 内核，用 Spring Boot 3.5 + React 18 重写的本体平台。设计与约定见 [`docs/`](docs/)，任务入口是 [`docs/05-任务拆分与验收.md`](docs/05-任务拆分与验收.md)。

## 仓库结构

| 路径 | 内容 |
|---|---|
| `wp-domain` | 纯 DTO（record）：帧、表单描述符、匹配条件、设置、事件 |
| `wp-kernel-api` | OWL 变更模型、索引、修订、层级接口 |
| `wp-kernel` | 索引实现、ChangeManager、RevisionStore、ProjectContext、Lucene 字典、渲染（不依赖 Spring） |
| `wp-app` | 按功能切分的应用服务、Mongo 仓库、权限、事件总线 |
| `wp-api` | `/api/v1` 控制器、`/data` 兼容层、SSE、OpenAPI、Security |
| `wp-integration` | 集成 REST（兼容路径 + v1 路径） |
| `wp-governance` | P3：版本标签、对比回滚、发布校验、模型注册 |
| `wp-ai` | P4（可选）：本体助手、MCP Server |
| `wp-server` | Spring Boot 可执行应用 |
| `wp-cli` | 管理命令（独立 jar） |
| `wp-web` | React 前端（pnpm） |
| `deploy` | Dockerfile、docker-compose、Keycloak realm |
| `docs` | 设计文档 00–06、ADR |
| `tools` | Checkstyle 配置 |
| `.cursor` | Cursor 规则与技能 |

依赖方向只能向下，由每个模块 POM 的 enforcer 规则强制：

```
wp-server → wp-api, wp-integration, wp-governance, wp-ai
wp-api / wp-integration / wp-governance / wp-ai / wp-cli → wp-app → wp-kernel → wp-kernel-api → wp-domain
```

`spring-ai` / `spring-ai-alibaba` 只允许出现在 `wp-governance` 与 `wp-ai`（父 POM 的 `ban-spring-ai` 规则）。

## 环境要求

- JDK 17（Temurin）、Maven 3.9+（或直接用仓库自带的 `./mvnw`）
- Node 22、pnpm 10（`corepack enable` 后按 `wp-web/package.json` 的 `packageManager` 自动获取）
- Docker（本地 mongo 与 keycloak）

## 本地开发

```bash
docker compose -f deploy/docker-compose.dev.yml up -d
```

```bash
./mvnw -pl wp-server -am spring-boot:run -Dspring-boot.run.profiles=dev
```

```bash
pnpm -C wp-web install
```

```bash
pnpm -C wp-web dev
```

- 后端 `http://localhost:8080`（`/actuator/health`），前端 `http://localhost:5173`（代理 `/api`、`/data`、`/download`）。
- Keycloak `http://localhost:8180`，realm `webprotege`；测试用户 `admin` / `editor` / `viewer`，密码同用户名，仅限本地开发。

## 构建与检查

CI（`.github/workflows/build-and-test.yml`）顺序：format → checkstyle → test → build → web lint/test。本地对应：

```bash
./mvnw spotless:apply
```

```bash
./mvnw verify
```

```bash
pnpm -C wp-web lint && pnpm -C wp-web typecheck && pnpm -C wp-web test
```

提交前 husky 会对 `wp-web` 的暂存文件运行 lint-staged。

## 约定

- 编码、Git、完成定义：[`docs/00-总览与开发约定.md`](docs/00-总览与开发约定.md)
- 分支 `<type>/<ticket>-<kebab>`，提交与 PR 标题使用 Conventional Commits，评论使用 Conventional Comments
- 架构决策：[`docs/adr/`](docs/adr/)
