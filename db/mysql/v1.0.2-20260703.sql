
/**
*版本：1.0.2-V0.1.0
*时间：2026-07-03
*1、新增：第三方系统认证
*/
/*==============================================================*/
/* Table: SYS_3RD_AUTH                                          */
/*==============================================================*/
create table SYS_3RD_AUTH
(
   ID                   bigint not null,
   TENANT_ID            bigint not null comment '租户ID',
   SCENE                varchar(50) not null comment '场景，字典3RDSCENE ai-llm：AI大模型，asr-rt：实时语音识别， other：其他',
   TITLE                varchar(200) not null comment '标题',
   SN                   varchar(50) not null comment '编码，唯一验证',
   ICON                 varchar(100) comment '图标（字体图标）',
   PIC_MAX              varchar(250) comment '大图标(图片图标)',
   PIC_MID              varchar(250) comment '中图标(图片图标)',
   PIC_MIX              varchar(250) comment '小图标(图片图标)',
   URL                  varchar(250) comment '认证接口',
   DATA_JSON            text comment '认证信息，json存储',
   SCRIPT_TXT           text comment '认证脚本',
   DOC_LINK             varchar(200) comment '技术文档url',
   DESCS                varchar(500) comment '说明',
   IS_GLOBAL            int(1) not null comment '字典TUREORFALSE 1是，0否',
   STATUS               int(2) not null comment '状态，字典USEORNOT 1使用，0停用',
   ORDERED              int(5) not null comment '显示顺序',
   DEL_FLAG             int not null comment '删除标记，字典TUREORFLASE 1是，0否',
   CREATED              datetime not null,
   CREATED_BY           bigint not null,
   LAST_UPDATED         datetime not null,
   LAST_UPDATED_BY      bigint not null,
   primary key (ID)
);
alter table SYS_3RD_AUTH comment '系统管理：第三方系统认证，所有请求外部系统认证管理，前端接口动态加载认证信息（后端加密，前端解密）';
create unique index SN_UNIQUE on SYS_3RD_AUTH
(
   SN
);

