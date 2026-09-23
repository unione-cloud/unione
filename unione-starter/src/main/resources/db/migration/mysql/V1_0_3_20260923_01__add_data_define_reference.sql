ALTER TABLE `sys_data_define`
    MODIFY COLUMN `TYPES` varchar(10) NOT NULL COMMENT '数据定义类型 setting：数据配置，form：表单设计,inner:内嵌数据（内嵌在某个表单中的数据），view：视图表单（不创建物理表，根据绑定主数据进行数据加载存储）',
    ADD COLUMN `REF_ID` bigint COMMENT '引用ID，当类型为view时，保存绑定主数据id' AFTER `DS_ID`;

ALTER TABLE `sys_data_define_his`
    MODIFY COLUMN `TYPES` varchar(10) NOT NULL COMMENT '数据定义类型 setting：数据配置，form：表单设计,inner:内嵌数据（内嵌在某个表单中的数据），view：视图表单（不创建物理表，根据绑定主数据进行数据加载存储）',
    ADD COLUMN `REF_ID` bigint COMMENT '引用ID，当类型为view时，保存绑定主数据id' AFTER `DS_ID`;

ALTER TABLE `sys_data_define_release`
    MODIFY COLUMN `TYPES` varchar(10) NOT NULL COMMENT '数据定义类型 setting：数据配置，form：表单设计,inner:内嵌数据（内嵌在某个表单中的数据），view：视图表单（不创建物理表，根据绑定主数据进行数据加载存储）',
    ADD COLUMN `REF_ID` bigint COMMENT '引用ID，当类型为view时，保存绑定主数据id' AFTER `DS_ID`;
