/**
 *
 * 版本：1.0.2-V0.1.1
 * 时间：2026-07-29
 * 1、修改操作日志，增加设备id字段
 *
 * 版本：UniOneV1.0.2-4-动态表单V0.1.0
 * 时间：2026-08-06
 * 1、增加表单数据溯源模块
 * 2、修改数据授权表，增加目标标题，目标编码字段
 * 3、修改数据权限表，增加权限类型字段
 *
 * 版本：UniOneV1.0.2-5-系统管理V0.1.2
 * 时间：2026-08-09
 * 1、修改常用工具表，增加系统ID，资源ID，分组名称，删除标记字段
 */

ALTER TABLE `unione`.`sys_logs` 
ADD COLUMN `DEVICE_ID` varchar(50) NULL COMMENT '设备ID' AFTER `PREQUEST_ID`;


CREATE TABLE `sys_data_tracing_row` (
  `ID` bigint NOT NULL COMMENT '主键',
  `TENANT_ID` bigint NOT NULL COMMENT '租户ID',
  `ORG_ID` bigint NOT NULL COMMENT '机构ID',
  `USER_ID` bigint NOT NULL COMMENT '修改人ID',
  `DEFINE_ID` bigint NOT NULL COMMENT '数据定义ID',
  `DATA_ID` bigint NOT NULL COMMENT '数据记录ID',
  `ACTION_ID` bigint NOT NULL COMMENT '业务操作ID',
  `OLD_VALUE` longtext COLLATE utf8mb4_unicode_ci COMMENT '原值，json对象，更新字段原始内容',
  `NEW_VALUE` longtext COLLATE utf8mb4_unicode_ci COMMENT '新值，json对象，更新字段新值内容',
  `VERS` int NOT NULL COMMENT '版本',
  `USER_ACCOUNT` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '修改人帐号',
  `USER_REALNAME` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '修改人姓名',
  `DEL_FLAG` int NOT NULL COMMENT '删除标记,字典TRUEORFALSE 1是0否',
  `CREATED` timestamp NOT NULL,
  `CREATED_BY` bigint NOT NULL,
  `LAST_UPDATED` timestamp NOT NULL,
  `LAST_UPDATED_BY` bigint NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `normal` (`DEL_FLAG`,`DEFINE_ID`,`DATA_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统管理：数据溯源-记录，分表存储，规则SYS_DATA_TRACING_ROW_{数据定义id}';
CREATE TABLE `sys_data_tracing_info` (
  `ID` bigint NOT NULL COMMENT '主键',
  `TENANT_ID` bigint NOT NULL COMMENT '租户ID',
  `ORG_ID` bigint NOT NULL COMMENT '机构ID',
  `USER_ID` bigint NOT NULL COMMENT '修改人ID',
  `DEFINE_ID` bigint NOT NULL COMMENT '数据定义ID',
  `DATA_ID` bigint NOT NULL COMMENT '数据记录ID',
  `ACTION_ID` bigint NOT NULL COMMENT '业务操作ID',
  `FIELDS` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段版本，json存储，eg：{name:1,sex:2}',
  `VERS` int NOT NULL COMMENT '数据版本，当前行数据版本',
  `USER_ACCOUNT` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '修改人帐号',
  `USER_REALNAME` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '修改人姓名',
  `DEL_FLAG` int NOT NULL COMMENT '删除标记,字典TRUEORFALSE 1是0否',
  `CREATED` timestamp NOT NULL,
  `CREATED_BY` bigint NOT NULL,
  `LAST_UPDATED` timestamp NOT NULL,
  `LAST_UPDATED_BY` bigint NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `normal` (`DATA_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统管理：数据溯源-信息，分表存储，规则SYS_DATA_TRACING_INFO_{数据定义id}';
CREATE TABLE `sys_data_tracing_field` (
  `ID` bigint NOT NULL COMMENT '主键',
  `TENANT_ID` bigint NOT NULL COMMENT '租户ID',
  `ORG_ID` bigint NOT NULL COMMENT '机构ID',
  `USER_ID` bigint NOT NULL COMMENT '修改人ID',
  `DEFINE_ID` bigint NOT NULL COMMENT '数据定义ID',
  `DATA_ID` bigint NOT NULL COMMENT '数据记录ID',
  `ACTION_ID` bigint NOT NULL COMMENT '业务操作ID',
  `TITLE` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `NAME` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '名称',
  `OLD_VALUE` longtext COLLATE utf8mb4_unicode_ci COMMENT '原值',
  `NEW_VALUE` longtext COLLATE utf8mb4_unicode_ci COMMENT '新值',
  `VERS` int NOT NULL COMMENT '版本',
  `USER_ACCOUNT` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '修改人帐号',
  `USER_REALNAME` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '修改人姓名',
  `DEL_FLAG` int NOT NULL COMMENT '删除标记,字典TRUEORFALSE 1是0否',
  `CREATED` timestamp NOT NULL,
  `CREATED_BY` bigint NOT NULL,
  `LAST_UPDATED` timestamp NOT NULL,
  `LAST_UPDATED_BY` bigint NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `normal` (`DEL_FLAG`,`DEFINE_ID`,`DATA_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统管理：数据溯源-字段，分表存储，规则SYS_DATA_TRACING_FIELD_{数据定义id}';

ALTER TABLE `unione`.`sys_data_permis` 
ADD COLUMN `TYPES` varchar(50) NULL COMMENT '权限类型，字典DATAPERMISTYPE add：新增，view：查看，edit：编辑，delete：删除，sensitive：脱敏，多个逗号分隔' AFTER `DEFINE_ID`;
ALTER TABLE `unione`.`sys_data_auth` 
ADD COLUMN `TARGET_SN` varchar(50) NULL COMMENT '目标编码' AFTER `TARGET_ID`,
ADD COLUMN `TARGET_TITLE` varchar(200) NULL COMMENT '目标标题' AFTER `TARGET_SN`;

ALTER TABLE `unione`.`sys_tool` 
ADD COLUMN `SYS_ID` bigint NULL COMMENT '系统ID' AFTER `USER_ID`,
ADD COLUMN `RES_ID` bigint NULL COMMENT '资源ID' AFTER `SYS_ID`,
ADD COLUMN `GNAME` varchar(50) NULL COMMENT '分组名称' AFTER `RES_ID`,
ADD COLUMN `DEL_FLAG` int(2) NULL COMMENT '删除标记，字典TUREORFALSE 1是，0否' AFTER `CONFIGS`;
