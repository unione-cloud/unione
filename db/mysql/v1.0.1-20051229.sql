-- 新增表格：我的收藏

drop index uni_uid_tid on SYS_MINE_LIKE;
drop table if exists SYS_MINE_LIKE;
/*==============================================================*/
/* Table: SYS_MINE_LIKE                                         */
/*==============================================================*/
create table SYS_MINE_LIKE
(
   ID                   bigint not null,
   TENANT_ID            bigint comment '租户ID',
   ORG_ID                 bigint not null comment '机构ID',
   USER_ID              bigint not null comment '用户ID',
   TARGET_TYPE          varchar(20) not null comment '目标类型',
   TARGET_ID            bigint not null comment '目标ID',
   ORDERED              int(4) not null comment '显示顺序',
   DEL_FLAG             int(2) not null comment '删除标记',
   DESCS                varchar(500) comment '描述',
   CONFIGS              varchar(5000) comment '配置，json格式存储',
   CREATED              timestamp not null,
   CREATED_BY           bigint not null,
   LAST_UPDATED         timestamp not null,
   LAST_UPDATED_BY      bigint not null,
   primary key (ID)
);
alter table SYS_MINE_LIKE comment '系统管理：我的收藏';
create unique index uni_uid_tid on SYS_MINE_LIKE
(
   USER_ID,
   TARGET_ID
);

-- 升级：系统资源，增加引用ID字段
ALTER TABLE `unione`.`sys_resource` 
MODIFY COLUMN `TYPES` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '资源类型，字典SYSRESTYPE menu：菜单，form：动态表单，flow：流程，btn：按钮，tool：工具' AFTER `ALIAS`,
ADD COLUMN `REF_ID` bigint NULL COMMENT '引用ID，保存：表单ID、流程ID' AFTER `TYPES`;