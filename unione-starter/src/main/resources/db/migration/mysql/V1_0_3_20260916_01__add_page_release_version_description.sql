-- Add version descriptions for page release snapshots and histories.
ALTER TABLE `sys_page_release`
    ADD COLUMN `VERT` varchar(200) NULL COMMENT '版本说明' AFTER `VERS`;

ALTER TABLE `sys_page_his`
    ADD COLUMN `VERT` varchar(200) NULL COMMENT '版本说明' AFTER `VERS`;
