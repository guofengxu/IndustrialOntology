# wp-legacy-compat

只有测试的模块：把同一份数据分别交给旧系统 `webprotege-server-core` 和新内核 `wp-kernel` 处理，比较结果是否一致。对应 05 文档 P0-03、P0-05、P0-06 的新旧对比验收，以及 P0 退出标准中的性能基线。

模块只在 `-Plegacy-compat` 下参与构建：它依赖的旧系统 jar 不在 Maven Central 上，默认构建和 `backend` CI 作业都不需要它。

## 1. 安装旧系统 jar

基线版本是 `guofengxu/webprotege` 的 `master` 分支提交 `543c306`（已合并 `feature/integration-ontology`）。用 JDK 11 构建：

```bash
git clone https://github.com/guofengxu/webprotege.git ../webprotege
```

```bash
git -C ../webprotege checkout 543c30600666f6ab73fbc85cb9c977bace55e449
```

```bash
JAVA_HOME=<JDK 11> ./mvnw -B -f ../webprotege/pom.xml -pl webprotege-server-core -am install -DskipTests -Dgwt.skipCompilation=true
```

这会把 `webprotege-shared-core`、`webprotege-shared`、`webprotege-server-api`、`webprotege-server-lucene`、`webprotege-server-core`（版本均为 `5.0.0-SNAPSHOT`）装进本地 Maven 仓库。

## 2. 运行

用 JDK 17：

```bash
./mvnw -Plegacy-compat -pl wp-legacy-compat -am verify
```

| 测试 | 验收项 | 内容 |
|---|---|---|
| `LegacyRevisionCompatibilityIT` | P0-03 | 旧系统写的修订被新内核读出：HEAD 修订号与旧系统 `GetHeadRevisionNumber` 一致，每条修订的编号、作者、时间戳、描述、变更都相同；新内核追加的修订能被旧系统读出 |
| `IndexReplayConsistencyIT` | P0-05 | 同一 `change-data.binary` 分别由旧内核和新内核重放，`OntologyAxiomsIndex`、`ClassFrameAxiomsIndex`、`SubClassOfAxiomsBySubClassIndex`、`AnnotationAssertionAxiomsBySubjectIndex` 对全部签名实体的结果相同；另有一个反向用例，确认两份不同的历史会被报告为不同 |
| `ClassHierarchyCompatibilityIT` | P0-06 | 类层级的根，以及每个类的子类、父类、祖先、是否叶子、到根的路径都与旧系统相同 |
| `ProjectLoadBaselineIT` | P0 退出标准 | 加载 50k 公理项目的耗时不超过旧系统的 1.2 倍，结果见 `docs/perf/P0-baseline.md` |
| `LegacyRoleClosuresIT` | S5（07 文档 5.1-16） | `wp-app/src/test/resources/legacy-access/role-closures.json` 中 19 个内置角色单独及两两组合（共 361 行）的 `roleClosure`、`actionClosure`，必须与旧 `AccessManagerImpl` 用 `RoleOracleImpl` 算出的完全相同（包括哈希集合的顺序和重复项）。`wp-app` 的 `RoleOracleTest` 再验证新 `RoleOracle` 算出的与该文件逐行相同，所以新访问管理器写出的 `RoleAssignments` 与旧版一致 |
| `LegacyMongoDocumentsIT` | S4（07 文档 5.3-3） | `wp-app/src/test/resources/legacy-mongo/` 下 19 个集合的样本和 `indexes.json`，必须与旧持久化代码（Morphia 1.3.2、旧 Jackson `ObjectMapperProvider`、旧 `UserRecordConverter`、各仓库的 `ensureIndexes()`）现在写出的完全相同。`wp-app` 的 `LegacyMongoRoundTripIT` 再验证新仓库读写这些样本不变 |

数据集：

- **pizza**：`wp-kernel` 测试资源中的 pizza.owl（377 条公理）导入为一条修订。
- **edited**：pizza 导入后再加上几百条确定性的编辑（新建、改名、移动、删除类，增加个体，使用第二个本体，增删本体注解和 import），代替真实项目。
- **真实项目（可选）**：加上 `-Dwp.compat.projectDirectory=<旧系统项目目录>`，该目录下要有 `change-data/change-data.binary`。测试只复制这个文件到临时目录，不会修改原目录。没设置时这些用例会跳过。

比较报告写在 `target/compat-reports/`，性能报告写在 `target/perf/`。

改动 `LegacyMongoDocuments` 或内置角色后，用 `-Dwp.compat.writeMongoSamples=true` 运行 `LegacyMongoDocumentsIT` 和 `LegacyRoleClosuresIT` 重新生成样本，再把 `wp-app/src/test/resources/legacy-mongo/` 和 `legacy-access/` 的改动一起提交。旧版 Morphia 通过 cglib 生成代理类，在 JDK 17 上需要 `--add-opens java.base/java.lang=ALL-UNNAMED`，模块的 failsafe 配置已经加上。

## 3. 类路径说明

- `wp-kernel` 排在旧系统依赖之前，所以 OWL API、Guava、Lucene 用的是新内核的版本。
- 旧系统的 Lucene 8 模块被排除，因为被比较的路径（修订、索引、层级）不用它，而且它和新内核的 Lucene 9 冲突。
- 旧系统的 `ProjectOntologiesIndexImpl.init` 放在重放之后调用，与 `ProjectContextFactory` 一致；见 `LegacyKernel` 的说明。
