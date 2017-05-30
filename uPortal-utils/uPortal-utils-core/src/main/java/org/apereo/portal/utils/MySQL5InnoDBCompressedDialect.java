/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.utils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import org.hibernate.dialect.MySQL5InnoDBDialect;
import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.sql.BasicBinder;
import org.hibernate.type.descriptor.sql.DoubleTypeDescriptor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses the COMPRESSED row format in an InnoDB engine, needed for long index support with UTF-8
 *
 */
public class MySQL5InnoDBCompressedDialect extends MySQL5InnoDBDialect {

    public MySQL5InnoDBCompressedDialect() {
        super();
    }

    public String getTableTypeString() {
        return " ENGINE=InnoDB ROW_FORMAT=COMPRESSED";
    }

    @Override
    protected SqlTypeDescriptor getSqlTypeDescriptorOverride(int sqlCode) {
        switch (sqlCode) {
            case Types.DOUBLE:
                {
                    return MySqlDoubleTypeDescriptor.INSTANCE;
                }
            default:
                {
                    return super.getSqlTypeDescriptorOverride(sqlCode);
                }
        }
    }

    private static class MySqlDoubleTypeDescriptor extends DoubleTypeDescriptor {
        private static final Logger LOGGER =
                LoggerFactory.getLogger(MySqlDoubleTypeDescriptor.class);
        private static final long serialVersionUID = 1L;

        public static final MySqlDoubleTypeDescriptor INSTANCE = new MySqlDoubleTypeDescriptor();

        public <X> ValueBinder<X> getBinder(final JavaTypeDescriptor<X> javaTypeDescriptor) {
            return new BasicBinder<X>(javaTypeDescriptor, this) {
                @Override
                protected void doBind(
                        PreparedStatement st, X value, int index, WrapperOptions options)
                        throws SQLException {
                    Double unwrappedValue = javaTypeDescriptor.unwrap(value, Double.class, options);
                    if (unwrappedValue == Double.NEGATIVE_INFINITY) {
                        LOGGER.debug("Converting double from NEGATIVE_INFINITY to MIN_VALUE");
                        unwrappedValue = Double.MIN_VALUE;
                    } else if (unwrappedValue == Double.POSITIVE_INFINITY) {
                        LOGGER.debug("Converting double from POSITIVE_INFINITY to MAX_VALUE");
                        unwrappedValue = Double.MAX_VALUE;
                    } else if (unwrappedValue.isNaN()) {
                        LOGGER.debug("Converting double from NaN to 0");
                        unwrappedValue = 0d;
                    }
                    st.setDouble(index, unwrappedValue);
                }
            };
        }
    }
}
