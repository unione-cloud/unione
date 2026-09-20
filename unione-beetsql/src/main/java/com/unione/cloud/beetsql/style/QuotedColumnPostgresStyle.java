package com.unione.cloud.beetsql.style;

import org.beetl.sql.core.db.PostgresStyle;

/** 为 BeetlSQL 和平台 SqlBuilder 生成的 PostgreSQL 字段名统一增加双引号。 */
public class QuotedColumnPostgresStyle extends PostgresStyle {

    public QuotedColumnPostgresStyle() {
        setKeyWordHandler(new PostgreSqlQuotedColumnHandler());
    }
}
