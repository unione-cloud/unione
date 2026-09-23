ALTER TABLE `sys_data_define`
    MODIFY COLUMN `CATEGORY` varchar(10) NOT NULL COMMENT '类别，sql：关系型存储，nosql：非关系型存储，api：接口存储，view：视图表单（不创建物理表，根据绑定主数据进行数据加载存储）',
    ADD COLUMN `REF_ID` bigint COMMENT '引用ID，当类型为view时，保存绑定主数据id' AFTER `DS_ID`;

ALTER TABLE `sys_data_define_his`
    MODIFY COLUMN `CATEGORY` varchar(10) NOT NULL COMMENT '类别，sql：关系型存储，nosql：非关系型存储，api：接口存储，view：视图表单（不创建物理表，根据绑定主数据进行数据加载存储）',
    ADD COLUMN `REF_ID` bigint COMMENT '引用ID，当类型为view时，保存绑定主数据id' AFTER `DS_ID`;

ALTER TABLE `sys_data_define_release`
    MODIFY COLUMN `CATEGORY` varchar(10) NOT NULL COMMENT '类别，sql：关系型存储，nosql：非关系型存储，api：接口存储，view：视图表单（不创建物理表，根据绑定主数据进行数据加载存储）',
    ADD COLUMN `REF_ID` bigint COMMENT '引用ID，当类型为view时，保存绑定主数据id' AFTER `DS_ID`;
