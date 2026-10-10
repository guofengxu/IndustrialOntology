# 02 · API 契约

> 所有新接口在 `/api/v1`；兼容接口在 `/data`（旧 Jersey 路径）与 `/download`。
> OpenAPI 由 springdoc 生成，`/v3/api-docs` 是唯一权威；本文是设计稿，实现后以生成文档为准并回填差异。

---

## 1. 通用约定

| 项 | 约定 |
|---|---|
| 认证 | `Authorization: Bearer <jwt>` 或 `Authorization: ApiKey <key>`；无认证 → 401 |
| 授权失败 | 403，body 为 Problem JSON |
| 错误格式 | RFC 7807 `application/problem+json`：`{type, title, status, detail, instance, code, errors?}`；`code` 为稳定字符串，如 `PROJECT_NOT_FOUND`、`PERMISSION_DENIED`、`INVALID_IRI`、`CONFLICT_FRAME_CHANGED` |
| 项目 ID | 路径参数 `{projectId}`，UUID 正则校验；未知 → 404 `PROJECT_NOT_FOUND` |
| IRI 参数 | **一律放 query**（`?iri=`），URL 编码；不放 path |
| OWL 实体 JSON | `{"type":"owl:Class"\|"owl:ObjectProperty"\|"owl:DatatypeProperty"\|"owl:AnnotationProperty"\|"owl:NamedIndividual"\|"rdfs:Datatype","iri":"…"}`，即旧 `ObjectMapperProvider` 的格式（07 4-6；S6 起 REST 的 `ObjectMapper` 注册了同一套序列化器，`JsonConfigurationTest` 固定）。本稿原写 `{"@type":"Class",…}` |
| 字面量 JSON | `{"value":"23.5","lang":"", "datatype":"http://www.w3.org/2001/XMLSchema#decimal"}` |
| 分页 | `?page=1&size=50`（1 基，与旧 `PageRequest` 一致）；响应 `{pageNumber, pageCount, pageSize, totalElements, pageElements:[…]}` |
| 写操作提交说明 | 可选 `commitMessage` 字段；缺省由服务端生成（沿用旧 change description） |
| 写操作返回 | `{result: <T>, revision: {number, timestamp, userId}, eventTag: {…}}`；客户端可用 `eventTag` 判断是否已收到对应 SSE |
| 时间 | ISO-8601 UTC 字符串；兼容层保持旧的 epoch 毫秒 |
| 幂等 | PUT 幂等；POST 创建类操作支持 `Idempotency-Key` 头（P2 可后置） |

---

## 2. 身份与管理

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/api/v1/me` | 登录 | `{userId, displayName, email, applicationActions[]}`，替代旧 `userInSession` |
| GET/POST/DELETE | `/api/v1/me/api-keys`、`/{apiKeyId}` | 登录 | 列表 `{apiKeyId, purpose, createdAt}`；POST 返回一次明文 |
| GET/PUT | `/api/v1/admin/settings` | EDIT_APPLICATION_SETTINGS | `ApplicationSettings` 同旧 DTO |
| GET | `/api/v1/admin/users?q=` | USER_ADMIN | 用户列表（新功能） |
| POST | `/api/v1/admin/permissions/rebuild` | REBUILD_PERMISSIONS | |

已实现（S5；`permissions/rebuild` 随 `rebuild-permissions` 在 S6 实现，成功返回 204，要求 `RebuildPermissions`，`SYSTEM_ADMIN` 带有），与上表的差别和补充：
- `applicationActions` 是动作 id（`EditApplicationSettings`、`CreateEmptyProject` 等，即 `BuiltInAction` 的 `ActionId`，与 `RoleAssignments.actionClosure` 中的字符串相同），按 id 排序；`email` 未知时为 `null`；`displayName` 是姓名，没有姓名时用用户名。Keycloak 管理员（realm 角色 `webprotege-admin`）的 `applicationActions` 包含 `SystemAdmin` 的全部动作，但不写入 `RoleAssignments`。
- `POST /api/v1/me/api-keys` 的请求体为 `{purpose}`，`purpose` 为空时返回 400 `INVALID_REQUEST`。成功时返回 201、`Location` 和 `Cache-Control: no-store`，响应体为 `{apiKeyId, apiKey, purpose, createdAt}`；`createdAt` 是 ISO-8601 格式。`DELETE` 一个不属于自己或不存在的 Key 返回 404 `API_KEY_NOT_FOUND`。`POST` 和 `DELETE` 只接受 bearer 令牌，用 API Key 调用返回 403；`GET` 列表两种凭证都可以。
- `/admin/settings` 的读和写都要求 `EDIT_APPLICATION_SETTINGS`（同旧 handler）。字段为 `{applicationName, systemNotificationEmailAddress, applicationLocation:{scheme,host,path,port}, accountCreationSetting, projectCreationSetting, projectUploadSetting, notificationEmailsSetting, maxUploadSize}`，旧 DTO 中始终为空的三个用户列表已去掉；任一字段缺失返回 400。`PUT` 返回保存后的设置。
- `/admin/users` 要求 `VIEW_ANY_USER_DETAILS`（`USER_ADMIN` 及 `SYSTEM_ADMIN` 角色带有），按用户名包含 `q`（不区分大小写）查找，返回 `[{userId, displayName, email}]`，按用户名排序；`limit` 默认 50，最多 100。
- 本地兜底登录（`webprotege.auth.local-login.enabled`）：`POST /login`，表单字段 `username`、`password`，返回 `{access_token, token_type: "Bearer", expires_in}`。用户名或密码错误返回 401 `BAD_CREDENTIALS`；同一用户名连续失败 5 次后，15 分钟内返回 429 `TOO_MANY_ATTEMPTS`；凭证放在查询串里返回 400。
- 401 的 `code` 为 `UNAUTHENTICATED`，并带 `WWW-Authenticate: Bearer`。

---

## 3. 项目

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/api/v1/projects?filter=owned\|shared\|trash` | 登录 | `ProjectDetails[]` |
| POST | `/api/v1/projects` | CREATE_EMPTY_PROJECT / UPLOAD_PROJECT | body `NewProjectSettings{displayName, description, language, sourceDocumentId?}`；201 + Location |
| POST | `/api/v1/uploads` | UPLOAD_PROJECT | multipart `file` → `{documentId, fileName, size}` |
| GET | `/api/v1/projects/{projectId}` | VIEW_PROJECT | `ProjectDetails` |
| POST | `/api/v1/projects/{projectId}/trash` / DELETE 同路径 | 所有者 / MOVE_ANY_PROJECT_TO_TRASH | 移入 / 移出回收站 |
| GET | `/api/v1/projects/{projectId}/permissions` | 登录 | 当前用户在该项目的 `BuiltInAction[]` |
| GET/PUT | `/api/v1/projects/{projectId}/settings` | VIEW / EDIT_PROJECT_SETTINGS | `ProjectSettings`（去 Slack 字段） |
| GET/PUT | `/api/v1/projects/{projectId}/prefixes` | VIEW / EDIT_PROJECT_PREFIXES | `{prefixes: {"ex:":"http://…"}}` |
| GET/PUT | `/api/v1/projects/{projectId}/crud-settings` | VIEW / EDIT_NEW_ENTITY_SETTINGS | `EntityCrudKitSettings`；GET `/api/v1/crud-kits` 列出可用 kit |
| GET/PUT | `/api/v1/projects/{projectId}/sharing` | VIEW / EDIT_SHARING_SETTINGS | `{sharingSettings:[{userId, permission: VIEW\|COMMENT\|EDIT\|MANAGE}], linkSharing: NONE\|VIEW\|COMMENT\|EDIT}` |
| GET/PUT | `/api/v1/projects/{projectId}/languages` | VIEW / EDIT_PROJECT_SETTINGS | `{defaultLanguage, displayNameSettings{primary[], secondary[]}}`；GET `/lang-tags` 列项目内语言标签 |
| GET/PUT | `/api/v1/projects/{projectId}/search-settings` | VIEW / EDIT_PROJECT_SETTINGS | 搜索过滤器 |
| GET/PUT | `/api/v1/projects/{projectId}/webhooks` | EDIT_PROJECT_SETTINGS | 通用 webhook |
| GET | `/api/v1/projects/{projectId}/export/settings` / POST `/import/settings` | EDIT_PROJECT_SETTINGS | `AllProjectSettings` JSON（兼容旧导出文件） |
| GET | `/download?project=&revision=&format=` | DOWNLOAD_PROJECT | 兼容路径，`format ∈ owl\|ttl\|owx\|omn\|ofn`，zip 流 |
| GET | `/api/v1/projects/{projectId}/download?revision=&format=` | DOWNLOAD_PROJECT | 同上新路径 |

已实现（S6，除 `search-settings` 随搜索设置在 S7、`export/settings` 与 `import/settings` 在 S9 实现外；`AllProjectSettings` 包含表单、标签和搜索过滤器，要等这些服务），与上表的差别和补充：
- 路径里的项目 id 不是 UUID 时返回 400 `INVALID_REQUEST`，是 UUID 但项目不存在时返回 404 `PROJECT_NOT_FOUND`，对所有调用者都一样（先查项目是否存在，再查权限）。
- `GET /api/v1/projects`：`filter` 可省略，省略时列出全部可用项目（含回收站中的）；`owned` 是自己的、不在回收站，`shared` 是别人的、不在回收站，`trash` 是自己的、在回收站，与旧客户端的三个视图相同；其他值返回 400。每项为 `{projectId, displayName, description, owner, inTrash, createdAt, createdBy, modifiedAt, modifiedBy, downloadable, trashable, lastOpenedAt}`（旧 `AvailableProject`，`lastOpenedAt` 没打开过时为 `null`），按显示名排序。与旧版一样，只经链接共享可见的项目不在列表中，自己的项目即使没有角色也在列表中。
- 项目详情 `ProjectDto` 为 `{projectId, displayName, description, owner, inTrash, defaultLanguage, defaultDisplayNameSettings, createdAt, createdBy, modifiedAt, modifiedBy}`，语言字段是领域 JSON（如 `{"type":"AnnotationAssertion","propertyIri":"…","lang":"en"}`）。
- `POST /api/v1/projects` 的请求体为 `{displayName, description, language, sourceDocumentId}`（`language` 即上表的 `language`，用作默认显示名的语言标签），成功返回 201、`Location` 和 `ProjectDto`。显示名为空返回 400 `INVALID_REQUEST`；上传文档不存在或已被使用返回 400 `UPLOAD_NOT_FOUND`，无法解析返回 400 `INVALID_UPLOAD`。创建者得到 `CanManage` 和 `ProjectDownloader`，任意登录用户得到 `LayoutEditor`（同旧版）。
- `GET /api/v1/projects/{projectId}` 只读详情，不加载项目。新增 `POST /api/v1/projects/{projectId}/open`（要求 `ViewProject`），即旧 `LoadProject`：加载项目、记录访问（`ProjectAccess`）、加入调用者的最近项目，返回 `ProjectDto`；前端打开项目时调用它。
- 回收站 `POST`/`DELETE …/trash` 返回 `ProjectDto`；所有者，或在应用（或项目）上有 `MoveAnyProjectToTrash` 的用户才能操作。旧版移入回收站不检查权限，移出只许所有者。
- `GET …/permissions`：按 id 排序的动作 id 列表（如 `ViewProject`），任何登录用户都能查自己的。
- `…/settings`：`{displayName, description, defaultLanguage, defaultDisplayNameSettings, webhooks:[{payloadUrl, eventTypes[]}]}`。读写都要求 `EditProjectSettings`（同旧 handler；设置里有 webhook 地址，所以读也不放宽到 `ViewProject`）。`PUT` 同时替换 webhook，各字段缺失返回 400；webhook 必须是 http(s) URL。`…/webhooks` 读写同一组 webhook（`[{payloadUrl, eventTypes[]}]`）。
- `…/languages`：`GET`（`ViewProject`）返回 `{defaultLanguage, displayNameSettings, languageUsage:[{language, referenceCount}]}`（后者对应旧 `GetProjectInfo` 的语言使用情况，用得最多的在前）；`PUT`（`EditProjectSettings`）请求体为 `{defaultLanguage, displayNameSettings}`。`…/lang-tags`（`ViewProject`）返回项目注解中用到的语言标签，如 `["en","zh"]`。
- `…/crud-settings`：`GET`（`ViewProject`）返回旧 `EntityCrudKitSettings` JSON（`{prefixSettings:{iriPrefix, conditionalIriPrefixes}, suffixSettings:{_class, …}}`，项目还没有设置时返回并保存默认值）；`PUT`（`EditNewEntitySettings`）请求体相同，`?prefixUpdateStrategy=FIND_AND_REPLACE` 时还把 IRI 以旧前缀开头的实体改到新前缀下（另需 `EditOntology`，产生一条修订），默认 `LEAVE_INTACT`。`GET /api/v1/crud-kits` 返回 `[{kitId, displayName, defaultPrefixSettings, defaultSuffixSettings}]`，kit 为 `UUID`、`OBO`、`SuppliedNameSuffix`。
- `…/sharing`：读写都要求 `EditSharingSettings`（同旧 handler，读也不放宽）。`linkSharing` 取 `NONE`、`VIEW`、`COMMENT`、`EDIT`、`MANAGE`（旧客户端四种都提供，所以保留 `MANAGE`），`sharingSettings` 按用户名排序。`PUT` 替换项目上的全部角色分配：不在列表中的用户失去访问，非共享角色（如 `ProjectDownloader`）也被替换，同旧版。`userId` 也可以是邮箱；既不是已知用户名也不是已知邮箱、且在项目上还没有角色的，返回 400 `USER_NOT_FOUND`，什么都不改（旧版悄悄跳过）。Keycloak 中的用户要先登录一次，或由管理员用 `wp-cli set-permissions` 设置。
- `POST /api/v1/uploads`：multipart 字段 `file`，返回 201 `{documentId, fileName, size}`。大小上限是应用设置的 `maxUploadSize`，zip 按解压后的内容计算（边解压边计数，不信任压缩包头）；超出返回 413 `UPLOAD_TOO_LARGE`，超过 `spring.servlet.multipart.max-file-size` 时同样返回 413 `UPLOAD_TOO_LARGE`；以 zip 开头却读不了的文件返回 400 `INVALID_UPLOAD`。文档用于创建项目后即被删除。用上传文档建项目时，zip 中没有 `root-ontology.owl`（`detail` 给出这句提示）或含有解压目录之外的条目，也返回 400 `INVALID_UPLOAD`。可接受的格式是 OWL API 自己能读的：RDF/XML、OWL/XML、函数式、Manchester、Turtle（含 N-Triples）、OBO、KRSS2，以及 BinaryOWL；N-Quads、TriG、JSON-LD、RDF/JSON、TriX、RDFa 不接受（旧版经 Rio 解析器接受，其中 JSON-LD 解析器会读取文档指定的远程或本机上下文）。`owl:imports` 只解析到同一次上传（zip）中的其他文档；上传中没有的导入不会被获取，项目里只保留导入声明。旧版会按 IRI 从网络或本机文件读取被导入的本体并入项目，上传者可以借此读取服务器上的文件或访问内网地址。需要被导入的本体时，把它们一起打进 zip。
- 下载：两条路径都返回 `application/zip`，`Content-Disposition` 的文件名沿用旧规则（显示名的空白换成 `-`，非 head 修订加 `-revision-<n>`，如 `pizza-ontologies.owl.zip`）。`/download` 保留旧参数的宽松解析（修订号缺失或不合法当作 head，未知格式当作 RDF/XML，见旧 `FileDownloadParameters`），项目参数缺失或不是 UUID 返回 400；`/api/v1/…/download` 对不合法的 `revision`（数字或 `HEAD`）和 `format` 返回 400。超过 head 或小于 0 的修订返回 404 `REVISION_NOT_FOUND`。下载在下载缓存中生成一次，之后直接返回。

---

## 4. 实体、层级、帧

### 4.1 实体

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/api/v1/projects/{projectId}/entities/lookup?q=&types=Class,NamedIndividual&limit=20` | VIEW_PROJECT | 自动补全，`EntityLookupResult[]{entity, displayName, matches[]}` |
| POST | `/api/v1/projects/{projectId}/entities/search` | VIEW_PROJECT | body `{searchString, entityTypes[], langTagFilter?, filters[], page, size}` → 分页 `EntitySearchResult` |
| POST | `/api/v1/projects/{projectId}/entities/match` | VIEW_PROJECT | body `{criteria: RootCriteria, page, size}`（查询构造器） |
| GET | `/api/v1/projects/{projectId}/entities/rendering?iri=&type=` | VIEW_PROJECT | `OWLEntityData` |
| GET | `/api/v1/projects/{projectId}/entities/html?iri=&type=` | VIEW_PROJECT | Manchester HTML 渲染（只读描述浏览器） |
| GET | `/api/v1/projects/{projectId}/entities/usage?iri=&type=&page=` | VIEW_PROJECT | |
| GET | `/api/v1/projects/{projectId}/entities/deprecated?page=` | VIEW_PROJECT | |
| POST | `/api/v1/projects/{projectId}/entities` | CREATE_CLASS 等 | body `{entityType, sourceText, langTag, parent?: entity, commitMessage?}`，多行 sourceText 创建多个；返回 `{result:{entities[]}, revision, eventTag}` |
| DELETE | `/api/v1/projects/{projectId}/entities` | DELETE_* | body `{entities[]}` |
| POST | `/api/v1/projects/{projectId}/entities/merge` | MERGE_ENTITIES | `{sourceEntities[], targetEntity, treatment: DELETE\|DEPRECATE}` |
| POST | `/api/v1/projects/{projectId}/entities/change-iri` | EDIT_ONTOLOGY | `{entity, newIri}` |
| POST | `/api/v1/projects/{projectId}/entities/bulk/move-to-parent` / `set-annotation` / `edit-annotations` / `delete-annotations` | EDIT_ONTOLOGY | 旧 bulkop 四种 |
| GET | `/api/v1/projects/{projectId}/individuals?type=&mode=ALL\|DIRECT&q=&page=&size=` | VIEW_PROJECT | 分页个体列表；`/individuals/page-containing?iri=` |

### 4.2 层级

`hierarchyId ∈ Class | ObjectProperty | DataProperty | AnnotationProperty`。

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/v1/projects/{projectId}/hierarchies/{hierarchyId}/roots` | `GraphNode[]`（含 `entity`, `displayName`, `tags[]`, `deprecated`, `childCount`） |
| GET | `.../children?iri=&page=&size=` | 子节点分页 |
| GET | `.../siblings?iri=` | |
| GET | `.../paths-to-root?iri=` | `Path[]` |
| POST | `.../move` | `{entity, fromParent, toParent, dropType: MOVE\|ADD}` → 写结果 |

### 4.3 帧

`frameType ∈ class | object-property | data-property | annotation-property | individual`。

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/v1/projects/{projectId}/frames/{frameType}?iri=` | 返回 `{frame: Plain*Frame, rendered: EntityFrame(带显示名)}`；两种形态同时给，前端编辑用 plain，展示用 rendered |
| PUT | `/api/v1/projects/{projectId}/frames/{frameType}?iri=` | body `{from: Plain*Frame, to: Plain*Frame, commitMessage?}`；`from` 与当前不一致 → 409 `CONFLICT_FRAME_CHANGED` |
| GET | `/api/v1/projects/{projectId}/frames/manchester?iri=&type=` | `{frameText, entity}` |
| POST | `.../frames/manchester/check` | `{iri, type, from, to}` → `{ok, error?:{line,col,message}, expectedTokens[]}` |
| PUT | `.../frames/manchester?iri=&type=` | `{from, to, commitMessage}` |
| POST | `.../frames/manchester/completions` | `{iri, type, text, cursor}` → 补全列表 |
| GET | `/api/v1/projects/{projectId}/ontologies` | `OWLOntologyID[]`，含 root |
| GET/PUT | `/api/v1/projects/{projectId}/ontologies/annotations?ontologyIri=` | 本体注解 |
| POST | `/api/v1/projects/{projectId}/axioms` | `Content-Type: text/turtle \| application/rdf+xml \| text/owl-functional`，`?commitMessage=` → 201 `{addedAxioms, revision}`；`/axioms/delete` 同形态 |

### 4.4 表单

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET/PUT | `/api/v1/projects/{projectId}/forms` | VIEW / EDIT_FORMS | 全部 `FormDescriptor[]`（PUT 整表替换，兼容旧导入文件） |
| GET/PUT/DELETE | `/api/v1/projects/{projectId}/forms/{formId}` | | 单个描述符 |
| GET | `/api/v1/projects/{projectId}/forms/fresh-id` | | |
| POST | `/api/v1/projects/{projectId}/forms/copy-from/{otherProjectId}` | EDIT_FORMS | |
| GET/PUT | `/api/v1/projects/{projectId}/forms/selectors` | | `EntityFormSelector[]` |
| POST | `/api/v1/projects/{projectId}/entities/forms` | VIEW_PROJECT | body `{entity, formFilter[], pageRequests[], ordering[], regionFilters[], langTagFilter}` → `FormDataDto[]`（POST 因为请求体复杂） |
| PUT | `/api/v1/projects/{projectId}/entities/forms` | EDIT_ONTOLOGY | `{entity, pristine: FormData[], edited: FormData[], commitMessage?}` |

---

## 5. 修订、历史、事件

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/api/v1/projects/{projectId}/revisions?from=&to=&userId=` | VIEW_CHANGES | `RevisionDetails[]` |
| GET | `/api/v1/projects/{projectId}/revisions/head` | VIEW_PROJECT | `{number}` |
| GET | `/api/v1/projects/{projectId}/revisions/{n}` | VIEW_CHANGES | 含变更摘要 |
| GET | `/api/v1/projects/{projectId}/changes?entity=&page=&size=` | VIEW_CHANGES | `ProjectChange[]`（含 diff 元素，旧 `GetProjectChanges`） |
| GET | `/api/v1/projects/{projectId}/changes/watched?page=` | WATCH_CHANGES | |
| POST | `/api/v1/projects/{projectId}/revisions/{n}/revert` | REVERT_CHANGES | 写结果 |
| GET | `/api/v1/projects/{projectId}/events?since=<tag>` | VIEW_PROJECT | 一次性拉取（断线补发） |
| GET | `/api/v1/projects/{projectId}/events/stream?since=<tag>` | VIEW_PROJECT | `text/event-stream`；事件 `id` = eventTag，`event` = 事件类型名，`data` = 事件 JSON；心跳每 25 s `: ping` |

治理模块（P3）需要的预留：`revisions/{n}` 返回 `{tags[]}`（快照标签）；`GET /revisions/diff?from=&to=` 返回轴向差异（`addedAxioms[]`, `removedAxioms[]` 的 Manchester 文本）；P1 先实现 diff 接口，标签字段留空数组。

---

## 6. 协作：讨论、标签、关注

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/api/v1/projects/{projectId}/threads?entity=` | VIEW_OBJECT_COMMENT | `DiscussionThread[]` |
| POST | `/api/v1/projects/{projectId}/threads` | CREATE_OBJECT_COMMENT | `{entity, body}` |
| POST | `/api/v1/projects/{projectId}/threads/{threadId}/comments` | CREATE_OBJECT_COMMENT | `{body}` |
| PUT/DELETE | `/api/v1/projects/{projectId}/threads/{threadId}/comments/{commentId}` | EDIT_OWN/ANY_OBJECT_COMMENT | |
| PUT | `/api/v1/projects/{projectId}/threads/{threadId}/status` | SET_OBJECT_COMMENT_STATUS | `{status: OPEN\|CLOSED}` |
| GET | `/api/v1/projects/{projectId}/threads/commented-entities?sort=&page=` | VIEW_OBJECT_COMMENT | |
| GET/PUT | `/api/v1/projects/{projectId}/tags` | VIEW / EDIT_PROJECT_TAGS | `Tag[]` |
| GET/PUT | `/api/v1/projects/{projectId}/entities/tags?iri=&type=` | VIEW / EDIT_ENTITY_TAGS | `{tagIds[]}` |
| GET/PUT | `/api/v1/projects/{projectId}/watches?iri=&type=` | WATCH_CHANGES | `{watches:[{type: ENTITY\|BRANCH}]}`；GET 无 iri 列全部 |
| GET | `/api/v1/projects/{projectId}/feed?page=` | VIEW_PROJECT | 项目动态（变更 + 评论 + 用户进出） |

---

## 7. 透视图与布局

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET/PUT | `/api/v1/projects/{projectId}/perspectives` | VIEW / ADD_OR_REMOVE_PERSPECTIVE | 当前用户的标签页 `PerspectiveDescriptor[]`（`{perspectiveId, label{}, favorite}`） |
| GET | `/api/v1/projects/{projectId}/perspectives/details` | | 含是否内置/是否默认 |
| POST | `/api/v1/projects/{projectId}/perspectives/reset` | | 重置为项目默认 |
| GET/PUT/DELETE | `/api/v1/projects/{projectId}/perspectives/{perspectiveId}/layout` | VIEW / ADD_OR_REMOVE_VIEW | **布局 JSON 原样**（widgetmap 格式，见 `03` 文档 §4） |
| PUT | `/api/v1/projects/{projectId}/perspectives/{perspectiveId}/layout/default` | SAVE_DEFAULT_PROJECT_LAYOUT | 保存为项目默认 |
| GET | `/api/v1/portlets` | 登录 | 可用 Portlet 描述 `{id, title, tooltip}`（服务端只做清单，渲染由前端注册表决定） |

---

## 8. 集成接口（`wp-integration`）

### 8.1 兼容路径（与 `webprotege-integration-protocol.md` 完全一致）

```
/data/integration/projects/{projectId}/individuals/runtime-data   GET / PUT / PATCH / DELETE
/data/integration/projects/{projectId}/classes                   GET
/data/integration/projects/{projectId}/properties                GET
```

JSON 契约、状态码、400 文案逐字保留（含 `updatedAt=0`、`updatedBy=null` 的读路径行为，未知 IRI 返回 200 空帧，`types[]`、`kind` 规则）。实现改为调用 `wp-app` 的 `FrameService`/`IndividualsService`，不再经过 Action。

### 8.2 v1 路径（修正旧缺陷）

```
GET    /api/v1/projects/{projectId}/integration/individuals?iri=            → IndividualDataV1
GET    /api/v1/projects/{projectId}/integration/individuals?type=&page=&size=  → 分页（不再拉全）
PUT    /api/v1/projects/{projectId}/integration/individuals?iri=            → 整表替换
PATCH  /api/v1/projects/{projectId}/integration/individuals?iri=            → 合并
DELETE /api/v1/projects/{projectId}/integration/individuals?iri=            → 清空断言
GET    /api/v1/projects/{projectId}/integration/classes?iri= | ?page=
GET    /api/v1/projects/{projectId}/integration/properties?iri=&kind= | ?kind=&page=
```

`IndividualDataV1`：

```json
{
  "projectId": "…",
  "iri": "http://example.org/Sensor1",
  "exists": true,
  "types": ["http://example.org/Sensor"],
  "values": [
    {"property": "http://example.org/hasStatus", "kind": "DATA",
     "literal": {"value": "RUNNING", "lang": "", "datatype": "http://www.w3.org/2001/XMLSchema#string"}},
    {"property": "http://example.org/locatedIn", "kind": "OBJECT", "individual": "http://example.org/Aisle3"},
    {"property": "http://www.w3.org/2000/01/rdf-schema#label", "kind": "ANNOTATION",
     "literal": {"value": "传感器1", "lang": "zh", "datatype": ""}}
  ],
  "revision": {"number": 1234, "timestamp": "2026-10-07T01:02:03Z", "userId": "admin"}
}
```

规则：多值保留；写入时每个值显式 `kind`；`exists=false` 时 GET 返回 404 `INDIVIDUAL_NOT_FOUND`（与兼容路径不同，兼容路径仍 200 空帧）；`revision` 取该个体最近一次修订（从 `EntitiesByRevisionCache`），替代旧的 `updatedAt=0`。

---

## 9. `/data` 兼容层（淘汰期内保留）

| 旧路径 | 新实现 |
|---|---|
| `GET/POST /data/projects` | 代理到 §3 |
| `GET /data/projects/{id}` | 同 |
| `GET /data/projects/{id}/revisions[/{n}]` | 同 §5，时间戳保持 epoch 毫秒 |
| `POST /data/projects/{id}/axioms`、`/delete-axioms` | 同 §4.3 |
| `GET/POST /data/projects/{id}/forms[/{formId}]` | 同 §4.4（GET 列表旧返回 202，新返回 200；写入 ADR 记录该差异） |
| `GET/POST /data/projects/{id}/settings` | 同 §3 `export/import settings` |
| `/data/integration/**` | §8.1 |

开关：`webprotege.compat.legacy-data-api.enabled`。响应头加 `Deprecation: true` 与 `Link: </api/v1/...>; rel="successor-version"`。

---

## 10. 治理与注册（P3，`wp-governance`，路径预留）

```
GET    /api/v1/projects/{projectId}/versions                       → 快照标签列表
POST   /api/v1/projects/{projectId}/versions                       → {tag: "1.4.0", revision, versionIri, notes}
GET    /api/v1/projects/{projectId}/versions/{tag}/diff?against=
POST   /api/v1/projects/{projectId}/versions/{tag}/rollback
POST   /api/v1/projects/{projectId}/releases                       → 触发发布校验流水线，返回 {releaseId}
GET    /api/v1/projects/{projectId}/releases/{releaseId}           → 状态 + 各节点报告
GET    /api/v1/projects/{projectId}/releases/{releaseId}/stream    → SSE 进度
GET/PUT /api/v1/projects/{projectId}/mappings/{type}               → YARRRML 规则（版本化）
GET    /registry/ontology/{ver}/ontology.ttl | shapes.ttl | context.jsonld
GET    /registry/mappings/{type}/{ver}
GET    /registry/mappings/index
GET    /registry/releases
POST   /registry/webhooks/subscribe
```

`/registry/*` 匿名可读（产物不可变，可被静态服务缓存）；`ETag` = 产物 sha256。

---

## 11. OpenAPI 组织

- `@Tag` 按本文章节：`identity`、`projects`、`entities`、`hierarchies`、`frames`、`forms`、`revisions`、`events`、`collaboration`、`perspectives`、`integration`、`legacy`、`governance`。
- 所有 DTO 来自 `wp-domain` record，带 `@Schema(description)`。
- `wp-web` 用 `openapi-typescript` 从 `/v3/api-docs` 生成 `src/api/schema.d.ts`，CI 校验生成物无 diff。
