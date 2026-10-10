ALTER TABLE `sys_data_define`
    ADD COLUMN `SYS_ID` bigint NULL COMMENT '所属系统ID' AFTER `APP_ID`,
    ADD INDEX `IDX_DATA_DEFINE_SYS_ID` (`SYS_ID`);

ALTER TABLE `sys_data_define_his`
    ADD COLUMN `SYS_ID` bigint NULL COMMENT '所属系统ID' AFTER `APP_ID`;

ALTER TABLE `sys_data_define_release`
    ADD COLUMN `SYS_ID` bigint NULL COMMENT '所属系统ID' AFTER `APP_ID`,
    ADD INDEX `IDX_DATA_DEFINE_RELEASE_SYS_ID` (`SYS_ID`);
