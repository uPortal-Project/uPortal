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

import java.io.Reader;
import java.io.StringReader;
import java.sql.Clob;
import java.sql.SQLException;

import javax.xml.namespace.QName;

import org.hibernate.HibernateException;
import org.hibernate.type.descriptor.CharacterStream;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.hibernate.type.descriptor.java.CharacterStreamImpl;
import org.hibernate.type.descriptor.java.DataHelper;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class QNameTypeDescriptor extends AbstractTypeDescriptor<QName> {
    public static final QNameTypeDescriptor INSTANCE = new QNameTypeDescriptor();
    
    private static final long serialVersionUID = 1L;

    private QNameTypeDescriptor() {
        super(QName.class);
    }

    @Override
    public String toString(QName value) {
        return value.toString();
    }

    @Override
    public QName fromString(String string) {
        return QName.valueOf(string);
    }

    @Override
    public <X> X unwrap(QName value, Class<X> type, WrapperOptions options) {
        if ( value == null ) {
            return null;
        }
        if ( String.class.isAssignableFrom( type ) ) {
            return type.cast(value.toString());
        }
        if ( Reader.class.isAssignableFrom( type ) ) {
            return type.cast(new StringReader( value.toString() ));
        }
        if ( CharacterStream.class.isAssignableFrom( type ) ) {
            return type.cast(new CharacterStreamImpl( value.toString() ));
        }
        if ( Clob.class.isAssignableFrom( type ) ) {
            return type.cast(options.getLobCreator().createClob( value.toString() ));
        }
        if ( DataHelper.isNClob( type ) ) {
            return type.cast(options.getLobCreator().createNClob( value.toString() ));
        }

        throw unknownUnwrap( type );
    }

    @Override
    public <X> QName wrap(X value, WrapperOptions options) {
        if ( value == null ) {
            return null;
        }
        
        final String strValue;
        if ( String.class.isInstance( value ) ) {
            strValue = (String)value;
        }
        else if ( Reader.class.isInstance( value ) ) {
            strValue = DataHelper.extractString( (Reader) value );
        }
        else if ( Clob.class.isInstance( value ) || DataHelper.isNClob( value.getClass() ) ) {
            try {
                strValue = DataHelper.extractString( ( (Clob) value ).getCharacterStream() );
            }
            catch ( SQLException e ) {
                throw new HibernateException( "Unable to access lob stream", e );
            }
        }
        else {
            throw unknownWrap( value.getClass() );
        }
        
        return this.fromString(strValue);
    }

}
