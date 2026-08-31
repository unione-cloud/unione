# Flyway 数据库迁移规范

`unione-starter` 启动时会自动执行此目录中的 Flyway 迁移。

## 当前基线

- 历史初始化脚本：`db/mysql/unione-init.zip`
- 最后一份人工增量：`db/mysql/v1.0.2-20260806.sql`
- Flyway 基线版本：`1.0.2.20260806`
- 首个 Flyway 管理版本：`1.0.2.20260831`

已有数据库首次启动时，如果库中存在业务表但没有
`flyway_platform_schema_history`，Flyway 会建立 `1.0.2.20260806` 基线，并只执行
版本号更高的迁移。

空数据库仍需先导入 `db/mysql/unione-init.zip`，并确认历史增量脚本已经执行，
再启动应用交给 Flyway 接管。历史脚本完成整合前，不要直接使用 Flyway 创建空库。


## 模块隔离

平台使用独立目录 `db/migration/mysql` 和历史表 `flyway_platform_schema_history`。
AI 等其他模块必须使用各自的迁移目录和历史表，禁止复用平台版本序列。

## 新增迁移

文件名格式：`V<产品版本>_<日期>_<序号>__<英文描述>.sql`。

例如：`V1_0_3_20260901_01__add_user_status_index.sql`。

规则：

1. 版本号必须严格递增，禁止复用已经执行过的版本号。
2. 已提交或已在任一环境执行的迁移文件禁止修改、删除或重命名。
3. 表结构、索引和基础数据变更必须通过新迁移文件发布。
4. MySQL DDL 可能隐式提交，一个迁移文件只处理一个关联变更。
5. 生产环境禁止执行 `clean`；项目配置已启用 `clean-disabled`。
6. 启动校验失败应先核对校验和与数据库记录，不能直接删除历史记录。

## 配置开关

- `UNIONE_FLYWAY_ENABLED=false`：临时禁用 Flyway。
- `UNIONE_FLYWAY_BASELINE_VERSION`：仅用于首次接管不同历史版本的数据库，设置前必须核实该库完成的最后一份脚本。

查询迁移状态：

```sql
SELECT installed_rank, version, description, type, installed_on, success
FROM flyway_platform_schema_history
ORDER BY installed_rank;
```
