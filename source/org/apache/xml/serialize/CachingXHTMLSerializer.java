/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xerces" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xml.serialize;


import java.io.OutputStream;
import java.io.Writer;
import java.io.UnsupportedEncodingException;
import java.io.IOException;


/**
 * This should really extend CachingXMLSerializer instead of reimplementing :(
 * @author Peter Kharchenko
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see Serializer
 */
public final class CachingXHTMLSerializer
    extends HTMLSerializer implements CachingSerializer
{

    CharacterCachingWriter cacher;
    String encoding;

    /**
     * Constructs a new serializer. The serializer cannot be used without
     * calling {@link #setOutputCharStream} or {@link #setOutputByteStream}
     * first.
     */
    public CachingXHTMLSerializer()
    {
        super( true, new OutputFormat( Method.XHTML, null, false ) );
    }


    /**
     * Constructs a new serializer. The serializer cannot be used without
     * calling {@link #setOutputCharStream} or {@link #setOutputByteStream}
     * first.
     */
    public CachingXHTMLSerializer( OutputFormat format )
    {
        super( true, format != null ? format : new OutputFormat( Method.XHTML, null, false ) );
        this.encoding=format.getEncoding();
    }


    /**
     * Constructs a new serializer that writes to the specified writer
     * using the specified output format. If <tt>format</tt> is null,
     * will use a default output format.
     *
     * @param writer The writer to use
     * @param format The output format to use, null for the default
     */
    public CachingXHTMLSerializer( Writer writer, OutputFormat format )
    {
        super( true, format != null ? format : new OutputFormat( Method.XHTML, null, false ) );
        CachingWriter cw=new CachingWriter(writer);
        this.cacher=cw;
        setOutputCharStream(cw);
        this.encoding=format.getEncoding();
    }

    public void setOutputCharStream( Writer writer ) {
        CachingWriter cw=new CachingWriter(writer);
        this.cacher=cw;
        super.setOutputCharStream(cw);
    }

    /**
     * Constructs a new serializer that writes to the specified output
     * stream using the specified output format. If <tt>format</tt>
     * is null, will use a default output format.
     *
     * @param output The output stream to use
     * @param format The output format to use, null for the default
     */
    public CachingXHTMLSerializer( OutputStream output, OutputFormat format )
    {
        super( true, format != null ? format : new OutputFormat( Method.XHTML, null, false ) );
        CachingOutputStream cos=new CachingOutputStream(output);
        this.cacher=cos;
        setOutputByteStream( cos );
        this.encoding=format.getEncoding();
    }

    public void setOutputByteStream( OutputStream output ) {
        CachingOutputStream cos=new CachingOutputStream(output);
        this.cacher=cos;
        super.setOutputByteStream(cos);
    }

    public void setOutputFormat( OutputFormat format )
    {
        super.setOutputFormat( format != null ? format : new OutputFormat( Method.XHTML, null, false ) );
        this.encoding=format.getEncoding();
    }

    // caching methods
    public boolean startCaching() {
        _printer.flush();
        return cacher.startCaching();
    }
    public boolean stopCaching() {
        _printer.flush();
        return cacher.stopCaching(); 
    }

    public String getCache() throws UnsupportedEncodingException, IOException {
                _printer.flush();
        return cacher.getCache(this.encoding);
    }
    
    /**
     * Allows one to print a <code>String</code> of characters directly to the output stream.
     *
     * @param text a <code>String</code> value
     */
    public void printRawCharacters(String text) {
        _printer.printText(text);
    }
}
