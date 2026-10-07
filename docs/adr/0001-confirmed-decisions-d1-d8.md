# ADR-0001：采纳设计提案决策 D1–D8

- 状态：已接受
- 日期：2026-10-07
- 相关任务：P0-01

## 背景

[`IndustrialOntology-design-proposal.md`](../IndustrialOntology-design-proposal.md) v0.1 提出八项待决事项，已于 2026-10-07 全部按建议值确认，详细结论见 docs/00 §1。

## 决策

| # | 决策 | 结论 |
|---|---|---|
| D1 | 方案 | 分层组合：内核与应用层零 AI 依赖；治理模块与 AI 模块使用 spring-ai-alibaba |
| D2 | 运行时 | Java 17、Spring Boot 3.5.x（跟随 spring-ai-alibaba main 的 BOM）、Maven |
| D3 | 前端 | React 18 + TypeScript 5 + Vite + Ant Design 5 |
| D4 | 功能裁剪 | 裁掉 OBO 6 个 Portlet、Collections、Slack、CSV 导入；后置实体关系图、上传合并、表单设计器 |
| D5 | 认证 | Keycloak OIDC 为主；API Key 兼容；本地账号仅 dev/admin 兜底 |
| D6 | 兼容 | 项目数据零迁移（BinaryOWL 直读）、Mongo 集合兼容 + 迁移脚本、透视图布局与 portlet id 兼容、URL 兼容、旧 `/data/*` 路径保留并设淘汰期 |
| D7 | 仓库 | 新建 `IndustrialOntology`；旧仓库只读归档；文档与 `.cursor/` 规范迁入新仓库 |
| D8 | 治理模块 | P3 实施；P1 的修订/事件接口按其需求预留 |

## 在仓库中的落地

- D1：父 POM 的 `ban-spring-ai` enforcer 规则；只有 `wp-governance`、`wp-ai`（以及负责装配的 `wp-server`）设 `wp.spring-ai.allowed=true`。
- D2：父 POM 导入 `spring-boot-dependencies` 3.5.8（与 spring-ai-alibaba main 一致），`maven.compiler.release=17`。
- 依赖方向：每个模块 POM 的 `enforce-layering` 规则；`wp-domain`、`wp-kernel-api`、`wp-kernel` 额外禁止 Spring。

## 影响

后续偏离上述结论（例如升级 OWL API 5.x）需新开 ADR。
