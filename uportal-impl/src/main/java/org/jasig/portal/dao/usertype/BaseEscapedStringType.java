/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.dao.usertype;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang.Validate;
import org.hibernate.HibernateException;
import org.hibernate.type.NullableType;
import org.hibernate.usertype.UserType;

/**
 * Quotes all non-null strings to handle the case of null and "" being treated the same way in some
 * databases.
 * 
 * Based on http://www.hibernate.org/169.html
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class BaseEscapedStringType implements UserType {
    private static final char QUOTING_CHAR = '\"';

    private final NullableType type;
    
    public BaseEscapedStringType(NullableType type) {
        Validate.notNull(type);

        if (!String.class.isAssignableFrom(type.getReturnedClass())) {
            throw new IllegalArgumentException(type + " returns " + type.getReturnedClass() + " which cannot be cast to a " + String.class);
        }
        
        this.type = type;
    }

    
    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#assemble(java.io.Serializable, java.lang.Object)
     */
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return deepCopy(cached);
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#deepCopy(java.lang.Object)
     */
    public Object deepCopy(Object value) throws HibernateException {
        if (value == null) {
            return null;
        }

        return new String((String) value);
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#disassemble(java.lang.Object)
     */
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) deepCopy(value);
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#equals(java.lang.Object, java.lang.Object)
     */
    public boolean equals(Object x, Object y) throws HibernateException {
        if (x == y) {
            return true;
        }
        if (x == null) {
            return false;
        }
        return x.equals(y);
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#hashCode(java.lang.Object)
     */
    public int hashCode(Object x) throws HibernateException {
        if (x == null) {
            return 0;
        }

        return x.hashCode();
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#isMutable()
     */
    public boolean isMutable() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#nullSafeGet(java.sql.ResultSet, java.lang.String[], java.lang.Object)
     */
    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
        final String dbValue = (String) this.type.nullSafeGet(rs, names[0]);
        if (dbValue == null) {
            return null;
        }

        return this.unescape(dbValue);
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#nullSafeSet(java.sql.PreparedStatement, java.lang.Object, int)
     */
    public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        if (value != null) {
            final String escapedValue = this.escape((String) value);
            this.type.nullSafeSet(st, escapedValue, index);
        }
        else {
            this.type.nullSafeSet(st, value, index);
        }
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#replace(java.lang.Object, java.lang.Object, java.lang.Object)
     */
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return this.deepCopy(original);
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#returnedClass()
     */
    public Class<?> returnedClass() {
        return String.class;
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#sqlTypes()
     */
    public int[] sqlTypes() {
        return new int[] { this.type.sqlType() };
    }

    /**
     * Escape a string by quoting the string.
     */
    protected String escape(String string) {
        return QUOTING_CHAR + string + QUOTING_CHAR;
    }

    /**
     * Unescape by removing the quotes
     */
    protected Object unescape(String string) throws HibernateException {
        if (!(string.charAt(0) == QUOTING_CHAR) || !(string.charAt(string.length() - 1) == QUOTING_CHAR)) {
            throw new HibernateException("Persistent storage of " + this.getClass().getName() + " corrupted, database contained string [" + string + "] which should be surrouned by " + QUOTING_CHAR + " characters.");
        }

        return string.substring(1, string.length() - 1);
    }
}
