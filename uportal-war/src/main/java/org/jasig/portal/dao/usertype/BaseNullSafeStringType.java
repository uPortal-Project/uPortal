/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.dao.usertype;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.type.descriptor.java.StringTypeDescriptor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

/**
 * Adds a prefx to all non-null strings to handle the case of null and an empty string being treated the
 * same way in some databases.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class BaseNullSafeStringType extends BaseUserType<String> {
    private static final long serialVersionUID = 1L;
    
    public static final char NOT_NULL_PREFIX = '_';
    

    public BaseNullSafeStringType(SqlTypeDescriptor sqlTypeDescriptor) {
        super(sqlTypeDescriptor, StringTypeDescriptor.INSTANCE);
    }

    @Override
    public final String nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
        final String value = super.nullSafeGet(rs, names, owner);
        return this.unescape(value);
    }

    @Override
    public final void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        value = this.escape((String)value);
        super.nullSafeSet(st, value, index);
    }

    /**
     * Escape a string by quoting the string.
     */
    protected final String escape(String string) {
        if (string == null) {
            return null;
        }
        
        return NOT_NULL_PREFIX + string;
    }

    /**
     * Unescape by removing the quotes
     */
    protected final String unescape(String string) throws HibernateException {
        if (string == null) {
            return null;
        }
        
        if (string.charAt(0) != NOT_NULL_PREFIX) {
            throw new HibernateException("Persistent storage of " + this.getClass().getName() + " corrupted, database contained string [" + string + "] which should be prefixed by: " + NOT_NULL_PREFIX);
        }

        return string.substring(1);
    }
}
