# P0 性能基线：加载 50k 公理项目

> 对应 05 文档 P0 退出标准：“加载 50k 公理项目 ≤ 旧系统 1.2 倍”。
> 测量时间：2026-10-07；由 `wp-legacy-compat` 模块的 `ProjectLoadBaselineIT` 生成（原始报告在 `wp-legacy-compat/target/perf/P0-baseline.md`）。

## 结论

新内核加载耗时与旧系统持平，比值 **1.01**（上限 1.2）。

| 加载 | 合计 | 读取修订 | 重放到索引 | 类层级 |
|---|---|---|---|---|
| 旧系统 `webprotege-server-core` | 131 ms | 48 ms | 43 ms | 37 ms |
| 新内核 `wp-kernel` | 132 ms | 46 ms | 46 ms | 38 ms |

另外单独测了新内核完整的 `ProjectContextFactory.create`（含 Lucene 字典、渲染、事件、`ChangeManager`）：

| 场景 | 中位数 |
|---|---|
| Lucene 索引已存在（日常打开项目） | 173 ms |
| 首次加载，需要全量构建 Lucene 索引 | 2286 ms |

## 测量方法

- **数据**：生成的本体，10,000 个类，每个类有声明、`rdfs:label`、`rdfs:comment`、一个父类和一个存在限制，合计 50,000 条公理；由旧系统的 `RevisionStoreImpl` 写成一条修订的 `change-data.binary`，两个内核读的是同一个文件。
- **范围相同**：两边都做“读取修订 → 用四线程池把修订重放进 17 个主索引 → 构建类层级（取 `owl:Thing` 的子类）”。旧系统的 Lucene 模块是 Lucene 8，不能和新内核的 Lucene 9 放在同一个类路径里，所以 Lucene 部分只测新内核。
- **计时**：先预热 5 轮，再交替测 9 轮，每次计时前做一次完整 GC，取中位数。
- **环境**：OpenJDK 64-Bit Server VM 17.0.20.1，8 个处理器，Windows 11。CI 环境的绝对值会不同，比值才有意义。

## 测量中发现并修复的问题

第一次测量的比值是 1.35，原因是新内核“读取修订”比旧系统慢一倍。日志显示每次加载都读了两遍 `change-data.binary`：`RevisionStoreFactory.createRevisionStore()` 已经调用了 `load()`（与旧系统相同），`ProjectContextFactory` 又调用了一次。结果虽然正确（`load()` 是整体替换），但每次打开项目都多读一遍文件。去掉多余的调用后，两边持平。`ProjectLoadBaselineIT` 会持续守住这个基线。

## 复现

先按 `wp-legacy-compat/README.md` 安装旧系统 jar，然后运行：

```bash
./mvnw -Plegacy-compat -pl wp-legacy-compat -am verify -Dit.test=ProjectLoadBaselineIT -Dfailsafe.failIfNoSpecifiedTests=false
```
