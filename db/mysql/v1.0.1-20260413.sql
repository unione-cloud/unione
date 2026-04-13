-- 升级页面配置，增加组件配置模块，支持用户动态配置组件信息
drop index normal on SYS_WIDGET_SETTING;
drop table if exists SYS_WIDGET_SETTING;
/*==============================================================*/
/* Table: SYS_WIDGET_SETTING                                    */
/*==============================================================*/
create table SYS_WIDGET_SETTING
(
   ID                   bigint not null,
   TENANT_ID            bigint not null comment '租户ID',
   ORG_ID               bigint not null comment '机构ID',
   USER_ID              bigint not null comment '用户ID',
   PAGE_ID              bigint not null comment '页面ID',
   WID                  varchar(50) not null comment '组件ID',
   WNAME                varchar(20) not null comment '组件名称',
   CONFIGS              longtext not null comment '页面设置，json结构{}',
   LEVELS               int(2) not null comment '设置级别',
   DESCS                varchar(200) comment '备注',
   CREATED              timestamp not null,
   CREATED_BY           bigint not null,
   LAST_UPDATED         timestamp not null,
   LAST_UPDATED_BY      bigint not null,
   primary key (ID)
);
alter table SYS_WIDGET_SETTING comment '系统管理：组件设置';
/*==============================================================*/
/* Index: normal                                                */
/*==============================================================*/
create index normal on SYS_WIDGET_SETTING
(
   WID,
   WNAME
);
