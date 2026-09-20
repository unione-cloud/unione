package com.unione.cloud.beetsql.style;

import org.beetl.sql.clazz.kit.KeyWordHandler;

/** PostgreSQL 大写字段引用处理器，避免未加引号的字段名被折叠为小写。 */
public final class PostgreSqlQuotedColumnHandler implements KeyWordHandler {

    @Override
    public String getTable(String tableName) {
        return tableName;
    }

    @Override
    public String getCol(String columnName) {
        if (columnName == null || columnName.isEmpty() || "*".equals(columnName)) {
            return columnName;
        }
        if (columnName.startsWith("\"") && columnName.endsWith("\"")) {
            return columnName;
        }
        return "\"" + columnName.replace("\"", "\"\"") + "\"";
    }
}
