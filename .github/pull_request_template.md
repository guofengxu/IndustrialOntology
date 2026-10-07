<!-- 标题使用 Conventional Commits，例如：feat(kernel): port BinaryOwlRevisionStore -->

## 任务

- 任务号：P?-??（docs/05）
- 分支：`<type>/<ticket>-<kebab>`

## 变更

-

## 验收证据（docs/05 中该任务的验收项逐条对应）

| 验收项 | 证据（测试名或复现步骤） |
|---|---|
|  |  |

## 完成定义（docs/00 §10）

- [ ] CI 全绿
- [ ] 验收项逐条有证据
- [ ] 新增/变更的 REST 端点出现在 `/v3/api-docs`，且 docs/02 已同步
- [ ] 迁移代码的旧测试已转为 JUnit 5 并通过（`@Disabled("not ported: …")` 的用例列在下方）
- [ ] 无新增 Checkstyle / ESLint 告警

## 本地验证输出

```
mvn -q verify -pl <module> -am
pnpm -C wp-web test
```
