package org.apereo.portal.utils.hibernate4.dialects;

import java.sql.Types;
import org.hibernate.dialect.Oracle10gDialect;

/**
 * When using Oracle 10g dialect with Oracle 12, ensure strings over 4000 characters are handled
 * correctly.
 *
 * <p>See also https://groups.google.com/a/apereo.org/forum/#!topic/uportal-user/j1Opk6Knv_k
 *
 * @since 5.3
 */
public class Oracle12ForceClobDialect extends Oracle10gDialect {

    public Oracle12ForceClobDialect() {

        registerColumnType(Types.CHAR, "char(1 char)");
        registerColumnType(Types.VARCHAR, 4000, "varchar2($l char)");
        registerColumnType(Types.VARCHAR, "clob");
    }
}
