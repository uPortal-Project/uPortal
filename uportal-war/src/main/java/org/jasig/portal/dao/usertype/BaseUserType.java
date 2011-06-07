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

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang.Validate;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.jdbc.LobCreator;
import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;
import org.hibernate.usertype.UserType;

/**
 * Base class for custom UserType impls
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class BaseUserType<T> implements UserType, Serializable {
    private static final WrapperOptions OPTIONS = new WrapperOptions() {
        @Override
        public boolean useStreamForLobBinding() {
            return Environment.useStreamsForBinary();
        }

        @Override
        public LobCreator getLobCreator() {
            //TODO How to deal with LobCreator
            //Should I use: NonContextualLobCreator.INSTANCE
            //This seems better but need a Session reference: Hibernate.getLobCreator( session );
            throw new UnsupportedOperationException();
        }
    };
    
    protected final SqlTypeDescriptor sqlTypeDescriptor;
    protected final JavaTypeDescriptor<T> javaTypeDescriptor;
    
    public BaseUserType(SqlTypeDescriptor sqlTypeDescriptor,
            JavaTypeDescriptor<T> javaTypeDescriptor) {
        
        Validate.notNull(sqlTypeDescriptor);
        Validate.notNull(javaTypeDescriptor);
        
        this.sqlTypeDescriptor = sqlTypeDescriptor;
        this.javaTypeDescriptor = javaTypeDescriptor;
    }

    
    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#assemble(java.io.Serializable, java.lang.Object)
     */
    @Override
    public T assemble(Serializable cached, Object owner) throws HibernateException {
        return this.javaTypeDescriptor.getMutabilityPlan().assemble(cached);
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#deepCopy(java.lang.Object)
     */
    @Override
    @SuppressWarnings("unchecked")
    public T deepCopy(Object value) throws HibernateException {
        return this.javaTypeDescriptor.getMutabilityPlan().deepCopy((T)value);
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#disassemble(java.lang.Object)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Serializable disassemble(Object value) throws HibernateException {
        return this.javaTypeDescriptor.getMutabilityPlan().disassemble((T)value);
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#equals(java.lang.Object, java.lang.Object)
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object x, Object y) throws HibernateException {
        return this.javaTypeDescriptor.areEqual((T)x, (T)y);
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#hashCode(java.lang.Object)
     */
    @Override
    @SuppressWarnings("unchecked")
    public int hashCode(Object x) throws HibernateException {
        return this.javaTypeDescriptor.extractHashCode((T)x);
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#isMutable()
     */
    @Override
    public boolean isMutable() {
        return this.javaTypeDescriptor.getMutabilityPlan().isMutable();
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#replace(java.lang.Object, java.lang.Object, java.lang.Object)
     */
    @Override
    @SuppressWarnings("unchecked")
    public T replace(Object original, Object target, Object owner) throws HibernateException {
        if (!isMutable()) {
            return (T)original;
        }
        else if (this.equals(original, target)) {
            return (T)original;
        }
        else {
            return deepCopy(original);
        }
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#nullSafeGet(java.sql.ResultSet, java.lang.String[], java.lang.Object)
     */
     @Override
     public T nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
         final ValueExtractor<T> extractor = this.sqlTypeDescriptor.getExtractor(this.javaTypeDescriptor);
         return extractor.extract(rs, names[0], OPTIONS);
     }

     /* (non-Javadoc)
      * @see org.hibernate.usertype.UserType#nullSafeSet(java.sql.PreparedStatement, java.lang.Object, int)
      */
     @Override
     @SuppressWarnings("unchecked")
     public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
         final ValueBinder<T> binder = this.sqlTypeDescriptor.getBinder(this.javaTypeDescriptor);
         binder.bind(st, (T)value, index, OPTIONS);
     }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#returnedClass()
     */
    @Override
    public Class<T> returnedClass() {
        return this.javaTypeDescriptor.getJavaTypeClass();
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#sqlTypes()
     */
    @Override
    public int[] sqlTypes() {
        return new int[] { this.sqlTypeDescriptor.getSqlType() };
    }
}
