package com.unione.cloud.common.dto;

import org.beetl.sql.annotation.entity.Table;

import com.unione.cloud.common.model.DocFile;

/** 文件详情使用DTO构建SQL，不能依赖未继承的父类表注解。 */
public class DocFileDtoMappingTest {
    public static void main(String[] args) {
        Table dtoTable = DocFileDto.class.getAnnotation(Table.class);
        Table modelTable = DocFile.class.getAnnotation(Table.class);
        if (dtoTable == null || modelTable == null
                || !modelTable.name().equals(dtoTable.name())) {
            throw new AssertionError("文件详情DTO必须映射至文件模型使用的同一张表");
        }
        System.out.println("File detail DTO table mapping: passed");
    }
}
