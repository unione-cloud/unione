-- 升级动态表单模块
-- 页面定义：增加页面类型 form:表单设计
ALTER TABLE `unione`.`sys_page_define` 
MODIFY COLUMN `TYPES` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '页面分类，字典PAGETYPE  code:编码页面，setting：配置页面，design：设计页面，form：表单设计' AFTER `PIC_MIX`;
ALTER TABLE `unione`.`sys_page_release` 
MODIFY COLUMN `TYPES` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '页面分类，字典PAGETYPE  code:编码页面，setting：配置页面，design：设计页面，form：表单设计' AFTER `PIC_MIX`;
ALTER TABLE `unione`.`sys_page_his` 
MODIFY COLUMN `TYPES` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '页面分类，字典PAGETYPE  code:编码页面，setting：配置页面，design：设计页面，form：表单设计' AFTER `PIC_MIX`;

-- 数据定义：增加数据定义类型字段
ALTER TABLE `unione`.`sys_data_define` 
ADD COLUMN `TYPES` varchar(10) NULL COMMENT '数据定义类型 setting：数据配置，form：表单设计,inner:内嵌数据（内嵌在某个表单中的数据）' AFTER `VERS`;
UPDATE sys_data_define SET TYPES='setting' WHERE TYPES IS NULL;
ALTER TABLE `unione`.`sys_data_define` 
MODIFY COLUMN `TYPES` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '数据定义类型 setting：数据配置，form：表单设计,inner:内嵌数据（内嵌在某个表单中的数据）' AFTER `VERS`;

ALTER TABLE `unione`.`sys_data_define_release` 
ADD COLUMN `TYPES` varchar(10) NULL COMMENT '数据定义类型 setting：数据配置，form：表单设计,inner:内嵌数据（内嵌在某个表单中的数据）' AFTER `VERS`;
UPDATE sys_data_define_release SET TYPES='setting' WHERE TYPES IS NULL;
ALTER TABLE `unione`.`sys_data_define_release` 
MODIFY COLUMN `TYPES` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '数据定义类型 setting：数据配置，form：表单设计,inner:内嵌数据（内嵌在某个表单中的数据）' AFTER `VERS`;

ALTER TABLE `unione`.`sys_data_define_his` 
ADD COLUMN `TYPES` varchar(10) NULL COMMENT '数据定义类型 setting：数据配置，form：表单设计,inner:内嵌数据（内嵌在某个表单中的数据）' AFTER `VERS`;
UPDATE sys_data_define_his SET TYPES='setting' WHERE TYPES IS NULL;
ALTER TABLE `unione`.`sys_data_define_his` 
MODIFY COLUMN `TYPES` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '数据定义类型 setting：数据配置，form：表单设计,inner:内嵌数据（内嵌在某个表单中的数据）' AFTER `VERS`;

-- 组件定义：增加自定义组件字段，租户信息字段
ALTER TABLE `unione`.`sys_page_widget` 
CHANGE COLUMN `IS_BASE` `IS_CUSTOM` int(0) NOT NULL COMMENT '自定义组件，字典TUREORFALSE 1是，0否' AFTER `ORDERED`,
ADD COLUMN `TENANT_ID` bigint NULL COMMENT '租户ID' AFTER `ID`,
ADD COLUMN `ORG_ID` bigint NULL COMMENT '机构ID' AFTER `TENANT_ID`,
ADD COLUMN `USER_ID` bigint NULL COMMENT '用户ID' AFTER `ORG_ID`,
ADD COLUMN `APP` varchar(50) NULL COMMENT '应用，字典WIDGETAPP form：表单，visual：可视化' AFTER `USER_ID`;
ALTER TABLE `unione`.`sys_page_widget` 
ADD COLUMN `IS_PLATFORM` int(0) NOT NULL COMMENT '是否平台组件，字典 TUREORFALSE 1是，0否' AFTER `ORDERED`;

-- 数据定义：增加无列表字段
ALTER TABLE `unione`.`sys_data_define` 
ADD COLUMN `IS_NO_LIST` int(2) NULL COMMENT '无列表，开启后无列表页面，字典TUREORFALSE 1是，0否' AFTER `IS_CUSTOM`;
ALTER TABLE `unione`.`sys_data_define_his` 
ADD COLUMN `IS_NO_LIST` int(2) NULL COMMENT '无列表，开启后无列表页面，字典TUREORFALSE 1是，0否' AFTER `IS_CUSTOM`;
ALTER TABLE `unione`.`sys_data_define_release` 
ADD COLUMN `IS_NO_LIST` int(2) NULL COMMENT '无列表，开启后无列表页面，字典TUREORFALSE 1是，0否' AFTER `IS_CUSTOM`;