-- 升级页面配置，增加组件配置模块，支持用户动态配置组件信息
drop index normal on SYS_WIDGET_TMPL;
drop table if exists SYS_WIDGET_TMPL;
/*==============================================================*/
/* Table: SYS_WIDGET_TMPL                                      */
/*==============================================================*/
create table SYS_WIDGET_TMPL
(
   ID                   bigint not null,
   TENANT_ID            bigint not null comment '租户ID',
   BIZ_ID               bigint not null comment '业务ID',
   BIZ_TYPE             int(2) not null comment '业务分类，字典WIDGETTMPLTYPE 9其他',
   OWNER_ID             bigint comment '归属ID',
   WID                  varchar(50) not null comment '组件ID',
   WNAME                varchar(20) not null comment '组件名称',
   WTITLE               varchar(100) not null comment '模版标题',
   CONFIGS              longtext not null comment '页面设置，json结构{}',
   DESCS                varchar(200) comment '备注',
   CREATED              timestamp not null,
   CREATED_BY           bigint not null,
   LAST_UPDATED         timestamp not null,
   LAST_UPDATED_BY      bigint not null,
   primary key (ID)
);
alter table SYS_WIDGET_TMPL comment '系统管理：组件模版';
/*==============================================================*/
/* Index: normal                                                */
/*==============================================================*/
create index normal on SYS_WIDGET_TMPL
(
   WID,
   WNAME
);


-- 升级动态表单：数据定义模块增加版本发布说明
ALTER TABLE `unione`.`sys_data_define_his` 
ADD COLUMN `VERT` varchar(200) NULL COMMENT '版本说明' AFTER `VERS`;
ALTER TABLE `unione`.`sys_data_define_release` 
ADD COLUMN `VERT` varchar(200) NULL COMMENT '版本说明' AFTER `VERS`;

-- 升级动态表单：增加数据编码模块，用于实现业务编码
drop index UNI_NAME on SYS_DATA_CODER;
drop table if exists SYS_DATA_CODER;
/*==============================================================*/
/* Table: SYS_DATA_CODER                                        */
/*==============================================================*/
create table SYS_DATA_CODER
(
   ID                   bigint not null comment '主键',
   TENANT_ID            bigint not null comment '租户ID',
   TITLE                varchar(100) not null comment '标题',
   NAME                 varchar(50) not null comment '名称',
   CODE_PREFIX          varchar(1000) not null comment '前缀，支持表达式获取数据属性，语法：${row.type}',
   CODE_LENGTH          int(2) not null comment '长度，动态流水编码长度',
   STATUS               int(2) not null comment '使用状态，字典USEORNOT 1使用，0停用',
   DEL_FLAG             int(2) not null comment '删除标记，字典TUREORFALSE 1是，0否',
   DESCS                varchar(500) comment '说明',
   CREATED              timestamp not null,
   CREATED_BY           bigint not null,
   LAST_UPDATED         timestamp not null,
   LAST_UPDATED_BY      bigint not null,
   primary key (ID)
);
alter table SYS_DATA_CODER comment '系统管理：数据编码器，规则：前缀+{YYYYMMDD}+{流水号}，流水号规则：根据前缀和日期从1开始生成';
/*==============================================================*/
/* Index: UNI_NAME                                              */
/*==============================================================*/
create unique index UNI_NAME on SYS_DATA_CODER
(
   NAME
);
drop index UNI_NAME_PREFIX on SYS_DATA_CODER_VALUE;
drop table if exists SYS_DATA_CODER_VALUE;
/*==============================================================*/
/* Table: SYS_DATA_CODER_VALUE                                  */
/*==============================================================*/
create table SYS_DATA_CODER_VALUE
(
   ID                   bigint not null comment '主键',
   TENANT_ID            bigint not null comment '租户ID',
   CODER_ID             bigint not null comment '编码器ID',
   NAME                 varchar(50) not null comment '名称',
   CODE_PREFIX          varchar(20) not null comment '前缀',
   CODE_DATE            date not null comment '日期',
   CODE_VALUE           int not null comment '当前值（每天凌晨重置）',
   CREATED              timestamp not null,
   CREATED_BY           bigint not null,
   LAST_UPDATED         timestamp not null,
   LAST_UPDATED_BY      bigint not null,
   primary key (ID)
);
alter table SYS_DATA_CODER_VALUE comment '系统管理：数据编码值';
/*==============================================================*/
/* Index: UNI_NAME_PREFIX                                       */
/*==============================================================*/
create unique index UNI_NAME_PREFIX on SYS_DATA_CODER_VALUE
(
   NAME,
   CODE_PREFIX
);

-- 添加系统信息模块
drop index NUI_CTX on SYS_SYSTEM;
drop table if exists SYS_SYSTEM;
/*==============================================================*/
/* Table: SYS_SYSTEM                                            */
/*==============================================================*/
create table SYS_SYSTEM
(
   ID                   bigint not null,
   TENANT_ID            bigint not null comment '租户ID',
   NAME                 varchar(100) not null comment '系统名称',
   ALIAS                varchar(50) comment '系统简称',
   TYPES                varchar(10) not null comment '系统类型，字典APPTYPES pc：PC，app：APP',
   CTX                  varchar(20) not null comment '系统ctx',
   LOGO_LARGE           varchar(100) comment '系统logo大',
   LOGO_SMALL           varchar(100) comment '系统logo小',
   THEME_NAME           varchar(20) comment '系统主题',
   VERS_NO              varchar(30) comment '版本号',
   VERS_DESC            text comment '版本说明',
   SECRET               varchar(50) comment '系统秘钥，令牌加密秘钥，用于实现不同系统之间token隔离',
   FOOTER               varchar(200) comment '底部信息',
   CONFIGS              text comment '系统配置,json对象存储，{}',
   APP_LIST             text comment '应用列表,json数组存储，[{title,name,id}]',
   NAV_LIST             text comment '导航配置，导航条组件，json存储{
               barStyle:{},//导航条样式
               itemStyle:{},   //导航项样式
               activeStyle:{},  // 活动项样式
               itemList:[{title,name,icon,iconActive,enable,roles}]  //导航项
            }',
   ORDERED              int(2) not null comment '显示顺序',
   STATUS               int(2) not null comment '系统状态，字典SYSSTATUS 1开发，2内测，3发布，4撤销',
   DEL_FLAG             int(2) not null comment '删除状态，1是，0否',
   DESCS                varchar(200) comment '备注',
   CREATED              timestamp not null,
   CREATED_BY           bigint not null,
   LAST_UPDATED         timestamp not null,
   LAST_UPDATED_BY      bigint not null,
   primary key (ID)
);
alter table SYS_SYSTEM comment '系统管理：系统信息';
/*==============================================================*/
/* Index: NUI_CTX                                               */
/*==============================================================*/
create unique index NUI_CTX on SYS_SYSTEM
(
   CTX
);
