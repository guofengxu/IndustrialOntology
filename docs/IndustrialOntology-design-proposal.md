# WebProtégé 新技术栈重构 —— 设计思路（待确认稿）

> 版本：v0.1（2026-10-06）
> 状态：**已确认（2026-10-07，D1–D8 全部按建议值）**。详细开发文档见 `docs/00-总览与开发约定.md` 起的 7 份文件；Cursor 任务入口为 `docs/05-任务拆分与验收.md`。
> 读者：项目负责人（决策）、Cursor（后续开发依据）。
> 上游参照：本仓库 `master`（5.0.0-SNAPSHOT）与 `origin/feature/integration-ontology`；`D:\00.workspace\00.ProjectCode\spring-ai-alibaba`（main = 1.1.2.2 + 137 commits，`origin/2.x` = 2.0.0.0-RC1）。

---

## 0. 一页结论

1. **现有工程的本质**：一个"命令总线 + 内存索引即本体 + 追加式修订日志"的单体。服务端设计（ChangeManager / 62 个索引接口 / BinaryOWL 修订日志 / Mongo 元数据 / Lucene 短名字典）是**可移植的资产**；GWT 客户端（92k LOC）与 GWT-RPC、Dagger、Morphia 1.3、Jersey 2.27、JUnit 4、Java 8 约束是**必须整体替换的负担**。
2. **"参考 spring-ai-alibaba 技术栈"在工程上等于**：Java 17、Spring Boot 3.5.x（或跟 2.x 线走 Boot 4.1 / Spring AI 2.0）、Spring MVC + SSE 流式接口、Starter/AutoConfiguration 组织方式、Micrometer/OTel 可观测、JUnit 5 + Testcontainers、Checkstyle/Spotless，前端 React + TypeScript + Ant Design 5（Admin 平台）或 Next.js 15 + Tailwind + shadcn（Studio）。
3. **两种选择的真实差别只有一处**：spring-ai-alibaba 是"本体内核的依赖"还是"内核之外可选模块的依赖"。其余（Boot、MVC、React 等）两种方案完全一样。
4. **推荐：方案一为基座，方案二按模块叠加（"内核零 AI 依赖，治理与 AI 模块用 spring-ai-alibaba"）**。理由：本体编辑内核与 LLM 无关；spring-ai-alibaba 迭代极快（1.1 → 2.0 伴随 Boot 3.5 → 4.1 的大版本跳跃），把内核绑上去会重演"技术栈无法迭代"的问题；而你方案 v0.4 中的"发布校验流水线"和未来的"本体助手 / MCP 对外暴露"恰好是 Graph 工作流与 Agent 的典型场景，值得完全基于它实现。
5. **需要你先拍板的 8 个决策**见第 9 节；其中最影响工作量的是**功能裁剪范围**与**数据/接口兼容要求**。

---

## 1. 现有工程解读（阅读结论）

### 1.1 规模

| 模块 | 主代码文件 | 约 LOC | 说明 |
|---|---|---|---|
| webprotege-shared-core | 37 | 2.4k | 基元类型、作用域注解 |
| webprotege-shared | 1014 | 49.6k | Action/Result DTO（约 168 个 Action）、Place、Event；**Java 8 + GWT 可编译** |
| webprotege-server-api | 131 | 4.9k | 62 个索引接口、OntologyChange 模型、仓库接口 |
| webprotege-server-lucene | 45 | 2.8k | 短名字典、实体搜索 |
| webprotege-server-core | 1008 | 74.2k | 151 个 ActionHandler、ChangeManager、51 个索引实现、Morphia 仓库 |
| webprotege-server | 53 | 3.7k | WAR、Dagger 根、Jersey `/data/*`、3 个 Servlet |
| webprotege-client | 1332 + 268 ui.xml | 92k + 7.9k | GWT 2.8.2 MVP、28 个 Portlet |
| webprotege-integration | 10（分支上更多） | — | **自研**运行数据 REST，见 1.6 |

### 1.2 核心设计（必须在新架构中保留的"思想"）

- **命令总线**：客户端与 REST 都走同一组 `Action → ActionHandler → Result`，读走索引、写走 `HasApplyChanges`。REST 只是命令总线的门面（`ActionExecutor`）。
- **索引即本体**：项目加载时把 `change-data.binary` 里的全部修订重放进 17 个主索引（`UpdatableIndex`），其余 40 余个索引是派生/查询索引，按依赖图分层并行更新。**内存中没有 OWLOntology 对象**；下载/合并/导出时才用 `RevisionManager.getOntologyManagerForRevision` 临时重建 OWL API 本体。
- **写路径**（`ChangeManager.applyChanges`，项目级写锁）：权限校验 → 生成变更 → 为临时 IRI 铸造真实 IRI（EntityCrudKit）→ 最小化变更 → 更新索引 → 更新 Lucene 字典 → 追加修订（异步落盘）→ 更新层级缓存 → 产生高层事件 → 投递事件 → 触发 webhook。
- **事件**：`EventManager` 内存桶 + 单调 `EventTag`，事件只保留 60 秒；客户端每 10 秒轮询 `GetProjectEventsAction`。**没有 WebSocket/SSE**。
- **项目生命周期**：`ProjectCache` 持有每项目的 Dagger `ProjectComponent`（`@ProjectSingleton`），空闲 3 分钟（分支改为 1 小时）后整体释放。
- **存储分工**：本体公理只在 BinaryOWL 修订日志（`data-store/project-data/<id>/change-data/change-data.binary`）；Mongo 只存元数据（22 个集合：用户、项目详情、角色分配、透视图布局、表单、标签、关注、讨论串、webhook、API Key、应用偏好等）；Lucene 为可重建的派生索引（`lucene-indexes/<projectId>`）。
- **权限**：52 个 `BuiltInAction` × 层级化 `BuiltInRole`，闭包反规范化进 `RoleAssignments`；分享设置就是角色分配；REST 用 `Authorization: ApiKey <key>`（SHA-256 无盐）。登录是 CHAP 挑战应答 + 盐化 MD5。
- **表单子系统**：声明式 `FormDescriptor`（文本/数字/单多选/实体名/图片/网格/子表单）通过 `OwlBinding` 直接映射为 OWL 公理，是最大的功能子系统（服务端 83 文件 + 共享 145 文件 + 客户端 237 文件）。

### 1.3 客户端功能清单（重写时的功能对账表）

- **壳与路由**：15 个 URL hash Place（`projects/{uuid}/perspectives/{id}?selection=…` 等），选择状态由 URL 驱动。
- **透视图/Portlet**：widgetmap 树形分栏布局；28 个 `@Portlet`，分组为：层级树 2、实体列表 2、实体编辑 8（含 Manchester 语法编辑器、表单、用法）、OBO 6、本体级 2、协作 4（评论、被评论实体、关注、动态）、历史 2、查询与可视化 2。Portlet 注册表由 Maven 插件在构建期生成，布局 JSON 按 portlet id 引用。
- **主要页面**：登录/注册/改密、项目列表（我的/共享/回收站）、项目设置（新实体 IRI 策略、显示名语言、Slack/Webhook）、分享、前缀、标签、表单管理与表单设计器、搜索设置、透视图管理、应用设置（管理员）。
- **其他**：全文搜索、40 余种匹配条件的查询构造器、修订历史与差异/回滚、上传合并、CSV 导入、多格式下载、实体关系图（d3 + dagre）。
- **没有的**：用户管理界面、SSO、实时推送、推理、SPARQL。

### 1.4 旧技术栈无法迭代的具体原因

| 问题 | 事实 |
|---|---|
| GWT 2.8.2 | 全量编译 10–15 分钟；`shared`/`client` 被锁在 Java 8 语法；只支持 gecko/safari 两个 user agent；前端生态隔绝 |
| GWT-RPC | 序列化 Java 对象，无法被非 GWT 客户端调用；第三方只能走 `/data/*` 这条窄门 |
| Dagger 2.20 + AutoFactory beta + AutoValue | 大量生成代码，`ProjectModule` 724 行、`ProjectActionHandlersModule` 713 行手工注册 |
| Morphia 1.3 + 原生 Document 混用 | 两套持久化风格，Jackson `convertValue` 兜底 |
| Jersey 2.27 / javax.* | 与 Jakarta 生态脱节 |
| JUnit 4 + Hamcrest + Mockito 2 | 需要 `--add-opens` 才能跑 |
| 事件 10 秒轮询、60 秒过期 | 多人协作体验差，无法做服务端推送 |
| 上游维护模式 | 上游已迁往 webprotege-next-gen（Spring Boot 微服务 + Keycloak + 消息总线，UI 仍为 GWT，通过 `webprotege-gwt-api-gateway` 接入），本仓库不再演进 |

### 1.5 与你《工业数据采集平台 v0.4》方案的关系（重要）

v0.4 第 5.3 节把 WebProtégé 定位为**统一本体服务**，包含三个模块：建模与版本管理、发布校验（ROBOT / Jena SHACL / WIDOCO / 映射回归）、模型注册（`/registry/*`、webhook）；统一认证 Keycloak；并明确要求"**少改上游，以独立扩展模块方式加入**"。

本次重构决定与该约束**相冲突**：一旦换技术栈重写，就不再有"跟进上游升级"这回事（上游本身也已停止在本仓库演进）。这是一个需要在 v0.4 中同步修订的决策（见第 9 节 D1）。好处是三个治理模块可以作为一等公民设计进新架构，而不是外挂。

### 1.6 自研集成模块（必须保留的契约）

- 协议文档：`webprotege-doc/webprotege-integration-protocol.md`（仅在 `origin/feature/integration-ontology` 分支，869 行）。
- 端点（`/data/integration/projects/{uuid}/…`）：`individuals/runtime-data` GET/PUT/PATCH/DELETE；分支新增 `classes`、`properties` 只读接口。
- 设计规则：只依赖 `webprotege-shared` 的 Action，通过 `ActionDispatch` 端口调用命令总线，**不碰索引**；每次写入都是一条修订。
- 已知缺陷（重写时应修正）：扁平 `Map<String,String>` 丢多值属性、丢字面量数据类型与语言标签、新键一律当数据属性、未知个体返回 200、列表 O(N) 取帧、`updatedAt` 不落库。
- 分支上另有：SMTP 环境变量配置、内置透视图补 id/label、`project.dormant.time` 调为 1 小时、Cursor 技能 `webprotege-code-dev`。**重写应以该分支为功能基线。**

---

## 2. spring-ai-alibaba 技术栈盘点（"参考"的具体含义）

| 维度 | 框架库（main 分支） | 备注 |
|---|---|---|
| Java / 构建 | Java 17（`<release>17</release>`、`-parameters`）、Maven 3.9.6、`${revision}` + flatten | 无 Gradle |
| Spring | Spring Boot **3.5.8** BOM、Spring AI **1.1.2** BOM、MCP SDK 0.14.0 BOM | `origin/2.x`：Boot **4.1.0** + Spring AI **2.0.0** |
| Web | **Spring MVC**（非 WebFlux），控制器返回 `Flux<ServerSentEvent<String>>` 做 SSE 流式 | `/apps/{app}/users/{user}/threads/{id}` 风格路径 |
| 组织方式 | `@AutoConfiguration` + `@ConditionalOn*` + `@EnableConfigurationProperties`、`AutoConfiguration.imports`、构造器注入、Builder API、**不用 Lombok/MapStruct** | Admin 平台例外（Lombok + MyBatis-Plus，Boot 3.3.6，独立工程，依赖旧版 graph-core） |
| 可观测 | Micrometer Observation、OTel OTLP、Actuator；Reactor 上下文传播 | |
| 测试 | JUnit 5、Mockito、AssertJ、Testcontainers、`@EnabledIfDockerAvailable` | |
| 质量 | Checkstyle 9.3（含 Javadoc、ImportOrder）、spring-javaformat、Spotless、license-eye、gitleaks、codespell | GitHub Actions：format → checkstyle → test → build |
| 前端 A（Admin） | Umi 4 + React 18 + TS 5 + **Ant Design 5** + ahooks + CodeMirror + @xyflow 流程画布 + lerna workspaces | nginx 镜像 |
| 前端 B（Studio） | Next.js 15 + React 19 + Tailwind 4 + shadcn/ui + react-markdown/mermaid/recharts；静态导出打进 jar | |
| AI 能力 | `StateGraph`/`CompiledGraph`（条件边、并行、子图、中断/人工介入、检查点、Mermaid 导出）、`ReactAgent` 与 Sequential/Parallel/LlmRouting/Loop 多智能体、Hook/Interceptor、Skills、工具调用、MCP 客户端、A2A + Nacos、轻量 RAG | 无 MCP Server 模块 |

本体编辑器与这套栈的交集：Java 17 + Boot 3.5 + MVC/SSE + Starter 组织 + 观测/测试/质量门禁 + AntD5 前端。Graph/Agent/MCP 只与"治理流水线"和"AI 助手"相关。

---

## 3. 两种方案

### 方案一：仅参考技术栈，不引入 spring-ai-alibaba 框架

- 新工程 = Spring Boot 3.5.x 模块化单体 + React/AntD5 前端。
- 依赖集合：Spring Web MVC、Spring Security（OIDC/Keycloak + API Key）、Spring Data MongoDB、Lucene 9、OWL API、BinaryOWL（读旧日志）、Caffeine、Micrometer。
- 发布校验流水线用 Spring 自带手段实现（`@Async` + 状态机/简单任务表）。
- AI 能力：不在本期范围；将来可通过 Spring AI 官方 `ChatClient` 接入，不依赖 Alibaba 扩展。

### 方案二：完全基于 spring-ai-alibaba 开发

- 工程引入 `spring-ai-alibaba-bom`，内核之上直接使用：
  - **graph-core**：发布校验流水线建模为 `StateGraph`（robot report → robot reason → 局部名唯一性 → Jena SHACL 测试集 → 调用 mapping-service 试运行 → 生成产物 → 登记），利用条件边做驳回、检查点做可恢复、`interrupt` 做人工确认、SSE 推送每个节点的进度，Mermaid 导出直接当流程文档。
  - **agent-framework**：`ReactAgent` + 工具（查类/属性/个体帧、生成变更、检查 Manchester 语法）实现"本体助手"：自然语言 → 候选公理 → 人工确认 → 走 `applyChanges`。
  - **MCP**：把本体读接口暴露为 MCP Server（需自行基于 MCP SDK 实现，spring-ai-alibaba 只有客户端），让外部 Agent（如 RAG-Openclaw、Industry-RAG）检索本体。
  - **Studio/Admin**：Studio 的 chat UI 可嵌入做助手面板；Admin 平台**不适合作为基座**（独立工程、Boot 3.3.6、依赖旧 graph-core、MyBatis-Plus/MySQL 技术栈与本体服务无关）。
- "完全基于"的另一种理解——把 WebProtégé 做成 spring-ai-alibaba 仓库内的一个应用——不建议：许可、发布节奏、代码规范（Apache 头、Checkstyle）都会被上游绑定。

### 对比

| 维度 | 方案一 | 方案二 |
|---|---|---|
| 内核复杂度 | 低，依赖集最小 | 内核不必依赖；若依赖则引入 Reactor/Spring AI 全家桶 |
| 升级风险 | 只跟 Spring Boot 节奏 | 还要跟 spring-ai-alibaba 节奏（1.1 → 2.0 要同步 Boot 4.1），历史上 Admin 就因此掉队 |
| 发布校验流水线 | 自己写任务编排，可控但平庸 | `StateGraph` 天然适配：条件分支、检查点、人工中断、进度流式 |
| AI 助手 / MCP | 需另起炉灶 | 现成 ReactAgent、工具、Skills、MCP 客户端；MCP Server 需自建 |
| 团队学习成本 | Spring 标准技能 | 额外学习 Graph/Agent 抽象 |
| 与整体平台一致性 | 中 | 高（Industry-RAG、RAG-Openclaw 等 Agent 系统同生态） |

### 推荐：方案一为基座，方案二按模块叠加

```
┌─────────────────────────────────────────────────────────────┐
│  wp-web (React + AntD5)                                      │
├──────────────┬──────────────────┬───────────────────────────┤
│ wp-api (REST │ wp-governance    │ wp-ai (可选)               │
│  + SSE +     │  版本/发布校验/  │  助手 Agent、MCP Server    │
│  OpenAPI)    │  模型注册        │  ← spring-ai-alibaba       │
│              │  ← graph-core    │                           │
├──────────────┴──────────────────┴───────────────────────────┤
│ wp-app：project · access · frame · form · search · hierarchy │
│         issues · tags · watches · perspective · io · integ.  │
├─────────────────────────────────────────────────────────────┤
│ wp-kernel：OntologyChange · ChangeManager · 索引 · 修订存储   │
│            （零 Spring AI 依赖，只依赖 OWL API / Guava）       │
└─────────────────────────────────────────────────────────────┘
```

- `wp-kernel` 与 `wp-app` 不引入 spring-ai-alibaba；`wp-governance`、`wp-ai` 通过 BOM 引入 graph-core / agent-framework，可独立升级或关闭（`@ConditionalOnProperty`）。
- 这样"方案二"的全部价值都能拿到，而"方案一"的低风险内核不受影响。如果你坚持纯方案二，也只需把 BOM 提到根 POM，架构不变。

---

## 4. 目标架构要点（推荐路线）

### 4.1 后端

| 关注点 | 现状 | 新设计 |
|---|---|---|
| 运行时 | Java 11 服务端 / Java 8 共享层、Tomcat WAR | Java 17（或 21，见 D2）、Spring Boot 可执行 jar、容器化 |
| DI | Dagger 2，`ServerComponent` / `ProjectComponent` 两级作用域 | Spring 单例 + **`ProjectContext` 注册表**（不用 Spring 自定义 scope；每项目一个显式对象图：索引、ChangeManager、RevisionStore、Lucene、事件总线、空闲驱逐），对应 `ProjectCache` |
| 命令总线 | Action/Result + 151 Handler | 保留"用例"概念：每个 Handler 平移为应用服务方法（读）或 `ChangeListGenerator` + `applyChanges`（写）；DTO 改为 Java record；去掉 GWT 序列化约束。不再需要通用 dispatch 端点 |
| 索引 | 62 接口 / 51 实现，Guava 依赖图并行更新 | **原样移植**（这是最大的可复用资产），接口放 `wp-kernel-api`，去 Dagger `@IntoSet` 改为 Spring `List<UpdatableIndex>` 注入 + 显式依赖声明 |
| 修订存储 | BinaryOWL 追加日志 | 第一期**保留 BinaryOWL 格式**（兼容现有项目数据、零迁移；BinaryOWL 绑定 OWL API 4.x，因此 OWL API 暂留 4.5.x）；抽象 `RevisionStore` 接口，为后续换格式留口 |
| 元数据 | Morphia 1.3 + 原生 Document | Spring Data MongoDB，集合名与字段**保持兼容**，提供一次性迁移脚本处理差异（如 RoleAssignments 闭包字段） |
| 搜索 | Lucene 8.x 短名字典 | Lucene 9.x，索引结构保留；可重建故无迁移问题 |
| 认证 | CHAP + 盐化 MD5、会话 | Spring Security：**Keycloak OIDC**（与 v0.4 一致）为主，API Key 过滤器兼容现有 `Authorization: ApiKey` 头；本地账号仅作兜底（见 D5） |
| 授权 | 52 Action × 角色闭包 | 保留模型；`AccessManager` 平移，用方法级 `@PreAuthorize`/自定义注解替代 `RequestValidator` |
| 事件 | 内存桶 + 10 秒轮询 | Spring `ApplicationEvent` → 每项目 **SSE 流**（与 spring-ai-alibaba 的 `Flux<ServerSentEvent>` 风格一致）；保留 `EventTag` 游标做断线补发 |
| REST | Jersey `/data/*` | Spring MVC `/api/v1/*` + springdoc OpenAPI；**保留 `/data/*` 与 `/data/integration/*` 兼容路径**（见 D6） |
| 文件 IO | 3 个 Servlet | 标准 multipart 上传、下载流式、格式不变（RDF/XML、Turtle、OWL/XML、Manchester、Functional） |
| 邮件/Webhook | javax.mail + Mustache、Slack、ProjectChanged webhook | Spring Mail + Mustache（模板原样）；webhook 平移 |
| 观测 | 无 | Actuator + Micrometer + OTel（与 v0.4 的 Prometheus/Grafana 对齐） |
| 测试 | JUnit 4 | JUnit 5 + Testcontainers(Mongo) + AssertJ；索引与 ChangeManager 的 349 个现有测试大部分可机械转换 |
| 模块边界 | Maven 模块 | Maven 多模块 + Spring Modulith（可选）校验模块依赖方向 |

### 4.2 前端（整体重写）

- 栈：**React 18 + TypeScript + Vite + Ant Design 5**（与 spring-ai-alibaba Admin 一致，便于团队复用；若更偏好 Next.js/Tailwind 路线见 D3）；状态：TanStack Query + zustand；路由：react-router 6，**保留现有 URL 结构**（`/projects/{uuid}/perspectives/{id}?selection=…`）以兼容深链接。
- 透视图布局：`flexlayout-react` 或 `rc-dock` 替代 widgetmap，**布局 JSON 与 portlet id 保持兼容**，用户已保存的布局可直接加载。
- Portlet 注册：显式 TS 注册表（`portlets.ClassHierarchy` 等 28 个 id），不再构建期生成。
- 关键组件：AntD 虚拟树（层级、拖拽）、CodeMirror 6（Manchester 语法、评论）、AntV G6 或 d3+dagre（实体关系图）、表单运行时与设计器（按 `FormDescriptor` 渲染，最大工作量项）。
- 实时：EventSource 订阅项目 SSE，失效 TanStack Query 缓存。
- i18n：react-i18next，首期中/英。

### 4.3 治理与 AI 模块（来自 v0.4，首次成为一等公民）

- `wp-governance`：快照标签（semver + `owl:versionIRI`）、版本对比/回滚（基于修订历史）、YARRRML 映射规则编辑、发布校验流水线（graph-core `StateGraph`，异步、可恢复、SSE 进度）、模型注册（`/registry/*` 不可变产物、发布记录、webhook）。
- `wp-ai`（可选）：本体助手 Agent（所有写操作必须经人工确认后走 `applyChanges`）、MCP Server 暴露只读本体接口。
- `wp-integration`：平移运行数据 REST，修正 1.6 列出的缺陷（多值、数据类型/语言标签、显式 kind、404、分页、审计时间持久化）。

---

## 5. 兼容性承诺（建议）

| 对象 | 承诺 |
|---|---|
| 项目本体数据 | `change-data.binary` 直接读取，不迁移 |
| Mongo 元数据 | 集合名/字段尽量不变，提供迁移脚本 |
| 用户布局 | 透视图 JSON 与 portlet id 兼容 |
| URL | 项目/透视图/选择/设置页路径兼容 |
| REST | `/data/projects/*`、`/data/integration/*`、API Key 头兼容；新接口在 `/api/v1/*` |
| 登录 | 改 Keycloak 后旧密码不可迁移（MD5 盐化），需用户重置或由 Keycloak 导入 |

---

## 6. 功能范围建议（需你裁决）

| 保留（核心） | 建议后置或裁剪 |
|---|---|
| 项目列表/创建/上传/下载/回收站 | OBO 6 个 Portlet（工业本体不用 OBO 规范） |
| 类/属性层级树、个体列表、实体帧编辑、Manchester 编辑器 | Collections（实验性、ActivityMapper 未接） |
| 表单运行时 + 表单设计器 | Slack webhook（可用通用 webhook 代替） |
| 搜索、查询构造器（条件子集） | CSV 导入（可由 NiFi/mapping 链路替代，或后置） |
| 修订历史、差异、回滚、项目动态 | 实体关系图（后置，第二期） |
| 评论/讨论、标签、关注、邮件通知 | CHAP 本地登录（改 Keycloak） |
| 分享/权限、项目设置、前缀、显示名语言、透视图管理 | 上传合并（merge/merge_add，保留但后置） |
| 集成 REST（运行数据/类/属性） | 应用设置页（并入管理后台） |
| 治理三模块（v0.4） | — |

---

## 7. 分期路线（草案，确认范围后细化）

| 阶段 | 内容 | 退出标准 |
|---|---|---|
| P0 内核平移 | `wp-kernel`：OntologyChange、索引全量移植、ChangeManager、RevisionStore(BinaryOWL)、ProjectContext、Lucene 字典；JUnit 5 迁移现有索引/变更测试 | 能加载现有项目目录并通过移植后的测试；REST 读取类帧/层级 |
| P1 应用层 + API | project/access/frame/form/search/hierarchy/issues/tags/watches/perspective/io、Spring Security(Keycloak + ApiKey)、SSE 事件、OpenAPI、集成 REST 平移 | 全部保留功能有 REST 覆盖；旧 `/data/*` 兼容测试通过 |
| P2 前端 MVP | 壳、项目列表、透视图布局、层级树、实体编辑、搜索、历史、评论、分享、设置 | 建模人员可脱离旧系统完成日常编辑 |
| P3 治理模块 | 版本标签/对比/回滚、发布校验 `StateGraph`、模型注册 `/registry/*`、映射规则编辑 | 对接 mapping-service 热加载（v0.4 P1/P2 验收项） |
| P4 补全与 AI | 表单设计器、关系图、合并、本体助手 Agent、MCP Server | 按需 |

工作量提示：服务端 ~125k LOC 中索引/变更/表单/帧翻译等约一半可"机械平移"，另一半（DI、dispatch、持久化、认证）是重写；前端 92k LOC 全部重写但功能可裁剪。总量远大于 v0.4 为"扩展模块"方案预留的 6–8 周 MVP，**v0.4 的路线图需要同步调整**。

---

## 8. 主要风险

| 风险 | 应对 |
|---|---|
| BinaryOWL 绑定 OWL API 4.x，阻碍升级到 OWL API 5 | 第一期保留 4.5.x；`RevisionStore` 抽象后评估新格式 + 一次性转换 |
| 索引移植时隐性依赖（Guava 依赖图、并行更新、写锁顺序）出错 | 原样移植 + 带原测试；用现有项目数据做回放一致性校验（旧/新索引对同一日志的查询结果比对） |
| 表单子系统重写量大 | 先做运行时（渲染 + 保存），设计器后置；描述符 JSON 原样兼容 |
| spring-ai-alibaba 大版本跳跃（2.x 要 Boot 4.1） | 只在 `wp-governance`/`wp-ai` 依赖；BOM 版本集中管理，内核不受影响 |
| Keycloak 切换导致旧账号失效 | 迁移期并行支持本地账号登录（仅管理员）或批量导入用户到 Keycloak 并强制重置密码 |
| 与 v0.4"少改上游"约束冲突 | 在 v0.4 修订记录中明确改为"重写为自有服务"，并调整 P1/P2 工期 |

---

## 9. 待你确认的决策清单

| # | 决策 | 建议 |
|---|---|---|
| D1 | 方案选择：纯方案一 / 纯方案二 / **推荐的分层组合** | 分层组合 |
| D2 | Java 与 Boot 版本：Java 17 + Boot 3.5（spring-ai-alibaba main）或 Java 21 + Boot 4.1 + Spring AI 2.0（`origin/2.x`，RC 阶段） | Java 17 + Boot 3.5，待 2.x GA 再升 |
| D3 | 前端栈：React + AntD5（Admin 同款）或 Next.js + Tailwind + shadcn（Studio 同款） | React + Vite + AntD5 |
| D4 | 功能裁剪：是否按第 6 节裁掉 OBO、Collections、Slack、CSV、关系图（后置）、合并（后置） | 同意裁剪 |
| D5 | 认证：是否直接以 Keycloak 为主、本地账号仅兜底 | 是 |
| D6 | 兼容要求：数据零迁移、布局/URL 兼容、旧 `/data/*` 路径保留，是否全部作为硬约束 | 全部保留，`/data/*` 可设淘汰期 |
| D7 | 仓库策略：新建独立仓库（如 `IndustrialOntology`），本仓库只读归档；还是在本仓库新分支并行 | 新仓库，并把 `webprotege-doc/`、`.cursor/` 规范迁过去 |
| D8 | 治理三模块是否纳入首个里程碑（P3）还是与 P1 并行 | P3，但 P1 的修订/事件接口按其需求设计 |

确认以上决策后，下一步输出的详细开发文档将包含：模块与包结构、每模块的接口契约（OpenAPI 草案）、索引/变更内核的移植清单（按文件）、前端组件与路由清单、Cursor 任务拆分（含验收标准与测试要求）、以及对 v0.4 文档的修订建议。
