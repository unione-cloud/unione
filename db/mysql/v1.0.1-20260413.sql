-- 升级页面配置，增加组件配置模块，支持用户动态配置组件信息
drop index normal on SYS_WIDGET_TEMPL;
drop table if exists SYS_WIDGET_TEMPL;
/*==============================================================*/
/* Table: SYS_WIDGET_TEMPL                                      */
/*==============================================================*/
create table SYS_WIDGET_TEMPL
(
   ID                   bigint not null,
   TENANT_ID            bigint not null comment '租户ID',
   PAGE_ID              bigint not null comment '页面ID',
   WID                  varchar(50) not null comment '组件ID',
   WNAME                varchar(20) not null comment '组件名称',
   CONFIGS              longtext not null comment '页面设置，json结构{}',
   BIZ_TYPE             int(2) not null comment '业务分类，字典WIDGETTMPLTYPE 9其他',
   DESCS                varchar(200) comment '备注',
   CREATED              timestamp not null,
   CREATED_BY           bigint not null,
   LAST_UPDATED         timestamp not null,
   LAST_UPDATED_BY      bigint not null,
   primary key (ID)
);
alter table SYS_WIDGET_TEMPL comment '系统管理：组件模版';
/*==============================================================*/
/* Index: normal                                                */
/*==============================================================*/
create index normal on SYS_WIDGET_TEMPL
(
   WID,
   WNAME
);
