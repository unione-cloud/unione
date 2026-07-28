/**
 * 版本：1.0.2-v0.1.0
 * 时间：2026-07-27
 * 新增：评论模块
 */
drop table if exists COMM_COMMENT_ITEM;
/*==============================================================*/
/* Table: COMM_COMMENT_ITEM                                     */
/*==============================================================*/
create table COMM_COMMENT_ITEM
(
   ID                   bigint not null,
   TENANT_ID            bigint comment '租户ID',
   ORG_ID               bigint comment '机构ID',
   USER_ID              bigint not null comment '用户ID',
   PARENT_ID            bigint not null comment '引用ID',
   TARGET_TYPE          varchar(20) not null comment '目标类型，字典COMMENTTARGET ',
   TARGET_ID            bigint not null comment '目标ID',
   LEVEL                int(2) not null comment '所在层级',
   LVSN                 varchar(40) not null comment '层级编码',
   CONTENTS             longtext comment '评论内容',
   FILES                text comment '附件列表，json数组存储',
   LIKE_COUNT           int(5) not null comment '点赞数量',
   IS_TOP               int(2) not null comment '是否置顶，字典：TRUEORFALSE 1是，0否',
   STATUS               int(2) not null comment '评论状态，字典COMMENTSTS 1正常，2待审，3拒绝',
   ORDERED              int(5) not null comment '显示顺序',
   DEL_FLAG             int(2) not null comment '删除标记，字典：TRUEORFALSE 1是，0否',
   CREATED              datetime not null,
   CREATED_BY           bigint not null,
   LAST_UPDATED         datetime not null,
   LAST_UPDATED_BY      bigint not null,
   primary key (ID)
);
alter table COMM_COMMENT_ITEM comment '通用：评论明细，通用评论';
/*==============================================================*/
/* Index: normal                                                */
/*==============================================================*/
create index normal on COMM_COMMENT_ITEM
(
   DEL_FLAG,
   TARGET_ID,
   STATUS,
   LVSN
);


drop table if exists COMM_COMMENT_SETTING;
/*==============================================================*/
/* Table: COMM_COMMENT_SETTING                                  */
/*==============================================================*/
create table COMM_COMMENT_SETTING
(
   ID                   bigint not null,
   TENANT_ID            bigint comment '租户ID',
   TARGET_TYPE          varchar(20) not null comment '目标类型，字典COMMENTTARGET ',
   TARGET_ID            bigint not null comment '目标ID',
   CLOSE_FLAG           int(2) not null comment '是否关闭评论，字典TUREORFALSE 1是，0否，默认开启',
   REF_LEVEL            int(2) comment '引用评论层级，默认4级',
   ASYNC_FLAG           int(2) comment '是否同步加载，字典TUREORFALSE 1是，0否，默认同步',
   LIKE_ENABLE          int(2) comment '是否开启点赞，字典TUREORFALSE 1是，0否，默认开启',
   FILE_ENABLE          int(2) comment '是否开启附件，字典TUREORFALSE 1是，0否，默认开启',
   FILE_COUNT           int(2) comment '附件数量，默认2个',
   FILE_TYPE            varchar(50) comment '附件类型，多个'',''号分隔，默认所有',
   CREATED              datetime not null,
   CREATED_BY           bigint not null,
   LAST_UPDATED         datetime not null,
   LAST_UPDATED_BY      bigint not null,
   primary key (ID)
);
alter table COMM_COMMENT_SETTING comment '通用：评论配置，具体目标个性化设置，如，是否关闭评论，是否开启点赞等等';
/*==============================================================*/
/* Index: uni_target_id                                         */
/*==============================================================*/
create unique index uni_target_id on COMM_COMMENT_SETTING
(
   TARGET_ID
);
/*==============================================================*/
/* Index: normal                                                */
/*==============================================================*/
create index normal on COMM_COMMENT_SETTING
(
   TARGET_TYPE,
   TARGET_ID
);


drop table if exists COMM_COMMENT_TARGET;
/*==============================================================*/
/* Table: COMM_COMMENT_TARGET                                   */
/*==============================================================*/
create table COMM_COMMENT_TARGET
(
   ID                   bigint not null,
   TENANT_ID            bigint comment '租户ID',
   TARGET_TYPE          varchar(20) not null comment '目标类型，字典COMMENTTARGET ',
   AUDIT_FLAG           int(2) not null comment '是否开启审核，字典TUREORFALSE 1是，0否，默认不审核',
   CLOSE_FLAG           int(2) not null comment '是否关闭评论，字典TUREORFALSE 1是，0否，默认开启',
   REF_LEVEL            int(2) comment '引用评论层级，默认4级',
   ASYNC_FLAG           int(2) comment '是否同步加载，字典TUREORFALSE 1是，0否，默认同步',
   LIKE_ENABLE          int(2) comment '是否开启点赞，字典TUREORFALSE 1是，0否，默认开启',
   FILE_ENABLE          int(2) comment '是否开启附件，字典TUREORFALSE 1是，0否，默认开启',
   FILE_COUNT           int(2) comment '附件数量，默认2个',
   FILE_TYPE            varchar(50) comment '附件类型，多个'',''号分隔，默认所有',
   CREATED              datetime not null,
   CREATED_BY           bigint not null,
   LAST_UPDATED         datetime not null,
   LAST_UPDATED_BY      bigint not null,
   primary key (ID)
);
alter table COMM_COMMENT_TARGET comment '通用：评论目标，如，是否关闭评论，是否开启点赞，是否审核等等';
/*==============================================================*/
/* Index: normal                                                */
/*==============================================================*/
create unique index normal on COMM_COMMENT_TARGET
(
   TARGET_TYPE
);
