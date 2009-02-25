/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.serialize;


import java.io.OutputStream;
import java.io.Writer;
import java.io.UnsupportedEncodingException;
import org.apache.xerces.dom.DOMMessageFormatter;

/**
 * Default serializer factory can construct serializers for the three
 * markup serializers (XML, HTML, XHTML ).
 *
 *
 * @version $Revision$ $Date$
 * @author <a href="mailto:Scott_Boag/CAM/Lotus@lotus.com">Scott Boag</a>
 * @author <a href="mailto:arkin@intalio.com">Assaf Arkin</a>
 */
final class SerializerFactoryImpl
    extends SerializerFactory
{


    private String _method;
    
    
    SerializerFactoryImpl( String method )
    {
        _method = method;
        if ( ! _method.equals( Method.XML ) &&
             ! _method.equals( Method.HTML ) &&
             ! _method.equals( Method.XHTML ) &&
             ! _method.equals( Method.TEXT ) ) {
            String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN, "MethodNotSupported", new Object[]{method});
            throw new IllegalArgumentException(msg);
        }
    }


    public Serializer makeSerializer( OutputFormat format )
    {
        Serializer serializer;
        
        serializer = getSerializer( format );
        serializer.setOutputFormat( format );
        return serializer;
    }
    
    
    
    public Serializer makeSerializer( Writer writer,
                                      OutputFormat format )
    {
        Serializer serializer;
        
        serializer = getSerializer( format );
        serializer.setOutputCharStream( writer );
        return serializer;
    }
    
    
    public Serializer makeSerializer( OutputStream output,
                                      OutputFormat format )
        throws UnsupportedEncodingException
    {
        Serializer serializer;
        
        serializer = getSerializer( format );
        serializer.setOutputByteStream( output );
        return serializer;
    }
    
    
    private Serializer getSerializer( OutputFormat format )
    {
        if ( _method.equals( Method.XML ) ) {
            return new XMLSerializer( format );
        } else if ( _method.equals( Method.HTML ) ) {
            return new HTMLSerializer( format );
        }  else if ( _method.equals( Method.XHTML ) ) {
            return new XHTMLSerializer( format );
        }  else if ( _method.equals( Method.TEXT ) ) {
            return new TextSerializer();
        } else {
            String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN, "MethodNotSupported", new Object[]{_method});
            throw new IllegalStateException(msg);
        }
    }
    
    
    protected String getSupportedMethod()
    {
        return _method;
    }


}

