---
name: git-naming-conventions
description: 分支、提交、PR 标题与评论的命名约定。创建分支、写提交信息、开 PR 或写 Review 评论时使用。
---

# Git 命名约定

> 依据 docs/00 §6。旧仓库的同名技能若可获取，按 docs/00 §7 要求原样迁入并覆盖本文件。

## 分支

格式：`<type>/<ticket>-<kebab>`

- `type` ∈ `feature` | `fix` | `hotfix` | `test` | `refactor` | `docs` | `chore` | `release`
- `ticket` 用 docs/05 的任务号，例如 `P0-03`
- 示例：`feature/P0-03-index-port`、`fix/P1-09-sse-replay-gap`

## 提交与 PR 标题：Conventional Commits

```
<type>(<scope>): <summary>
```

- `type`：feat、fix、refactor、test、docs、build、ci、chore、perf、style、revert
- `scope`：模块名去掉 `wp-` 前缀（`kernel`、`app`、`api`、`web`……），或任务号
- 示例：`feat(kernel): port BinaryOwlRevisionStore`、`ci: add web lint job`
- 破坏性变更：`feat(api)!: ...`，并在正文写 `BREAKING CHANGE:`

## Issue / PR 评论：Conventional Comments

```
<label> [decorations]: <subject>
```

- `label`：praise、nitpick、suggestion、issue、todo、question、thought、chore、note
- `decorations`：`(blocking)`、`(non-blocking)`、`(if-minor)`
- 示例：`issue (blocking): applyChanges is called while holding the read lock`

## 合并规则

- 默认分支 `main`，禁止直推。
- PR 需 CI 全绿：format → checkstyle → test → build → web lint/test。
