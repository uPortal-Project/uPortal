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


// Sep 14, 2000:
//  Fixed serializer to report IO exception directly, instead at
//  the end of document processing.
//  Reported by Patrick Higgins <phiggins@transzap.com>


package org.jasig.portal.serialize;


import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;


/**
 * The printer is responsible for sending text to the output stream
 * or writer. This class performs direct writing for efficiency.
 * {@link IndentPrinter} supports indentation and line wrapping by
 * extending this class.
 *
 * @version $Revision$ $Date$
 * @author <a href="mailto:arkin@intalio.com">Assaf Arkin</a>
 */
public class Printer
{


    /**
     * The output format associated with this serializer. This will never
     * be a null reference. If no format was passed to the constructor,
     * the default one for this document type will be used. The format
     * object is never changed by the serializer.
     */
    protected final OutputFormat _format;


    /**
     * The writer to which the document is written.
     */
    protected Writer             _writer;


    /**
     * The DTD writer. When we switch to DTD mode, all output is
     * accumulated in this DTD writer. When we switch out of it,
     * the output is obtained as a string. Must not be reset to
     * null until we're done with the document.
     */
    protected StringWriter       _dtdWriter;


    /**
     * Holds a reference to the document writer while we are
     * in DTD mode.
     */
    protected Writer          _docWriter;


    /**
     * Holds the exception thrown by the serializer.  Exceptions do not cause
     * the serializer to quit, but are held and one is thrown at the end.
     */
    protected IOException     _exception;


    /**
     * The size of the output buffer.
     */
    private static final int BufferSize = 4096;


    /**
     * Output buffer.
     */
    private final char[]  _buffer = new char[ BufferSize ];


    /**
     * Position within the output buffer.
     */
    private int           _pos = 0;


    public Printer( Writer writer, OutputFormat format)
    {
        _writer = writer;
        _format = format;
        _exception = null;
        _dtdWriter = null;
        _docWriter = null;
        _pos = 0;
    }


    public IOException getException()
    {
        return _exception;
    }


    /**
     * Called by any of the DTD handlers to enter DTD mode.
     * Once entered, all output will be accumulated in a string
     * that can be printed as part of the document's DTD.
     * This method may be called any number of time but will only
     * have affect the first time it's called. To exist DTD state
     * and get the accumulated DTD, call {@link #leaveDTD}.
     */
    public void enterDTD()
        throws IOException
    {
        // Can only enter DTD state once. Once we're out of DTD
        // state, can no longer re-enter it.
        if ( _dtdWriter == null ) {
	    flushLine( false );

			_dtdWriter = new StringWriter();
            _docWriter = _writer;
            _writer = _dtdWriter;
        }
    }


    /**
     * Called by the root element to leave DTD mode and if any
     * DTD parts were printer, will return a string with their
     * textual content.
     */
    public String leaveDTD()
        throws IOException
    {
        // Only works if we're going out of DTD mode.
        if ( _writer == _dtdWriter ) {
	    flushLine( false );

			_writer = _docWriter;
            return _dtdWriter.toString();
        } else
            return null;
    }


    public void printText( String text )
        throws IOException
    {
        try {
            int length = text.length();
            for ( int i = 0 ; i < length ; ++i ) {
                if ( _pos == BufferSize ) {
                    _writer.write( _buffer );
                    _pos = 0;
                }
                _buffer[ _pos ] = text.charAt( i );
                ++_pos;
            }
        } catch ( IOException except ) {
            // We don't throw an exception, but hold it
            // until the end of the document.
            if ( _exception == null )
                _exception = except;
            throw except;
        }
    }


    public void printText( StringBuffer text )
        throws IOException
    {
        try {
            int length = text.length();
            for ( int i = 0 ; i < length ; ++i ) {
                if ( _pos == BufferSize ) {
                    _writer.write( _buffer );
                    _pos = 0;
                }
                _buffer[ _pos ] = text.charAt( i );
                ++_pos;
            }
        } catch ( IOException except ) {
            // We don't throw an exception, but hold it
            // until the end of the document.
            if ( _exception == null )
                _exception = except;
            throw except;
        }
    }


    public void printText( char[] chars, int start, int length )
        throws IOException
    {
        try {
            while ( length-- > 0 ) {
                if ( _pos == BufferSize ) {
                    _writer.write( _buffer );
                    _pos = 0;
                }
                _buffer[ _pos ] = chars[ start ];
                ++start;
                ++_pos;
            }
        } catch ( IOException except ) {
            // We don't throw an exception, but hold it
            // until the end of the document.
            if ( _exception == null )
                _exception = except;
            throw except;
        }
    }


    public void printText( char ch )
        throws IOException
    {
        try {
            if ( _pos == BufferSize ) {
                _writer.write( _buffer );
                _pos = 0;
            }
            _buffer[ _pos ] = ch;
            ++_pos;
        } catch ( IOException except ) {
            // We don't throw an exception, but hold it
            // until the end of the document.
            if ( _exception == null )
                _exception = except;
            throw except;
        }
    }


    public void printSpace()
        throws IOException
    {
        try {
            if ( _pos == BufferSize ) {
                _writer.write( _buffer );
                _pos = 0;
            }
            _buffer[ _pos ] = ' ';
            ++_pos;
        } catch ( IOException except ) {
            // We don't throw an exception, but hold it
            // until the end of the document.
            if ( _exception == null )
                _exception = except;
            throw except;
        }
    }


    public void breakLine()
        throws IOException
    {
        try {
            if ( _pos == BufferSize ) {
                _writer.write( _buffer );
                _pos = 0;
            }
            _buffer[ _pos ] = '\n';
            ++_pos;
        } catch ( IOException except ) {
            // We don't throw an exception, but hold it
            // until the end of the document.
            if ( _exception == null )
                _exception = except;
            throw except;
        }
    }


    public void breakLine( boolean preserveSpace )
        throws IOException
    {
        breakLine();
    }


    public void flushLine( boolean preserveSpace )
        throws IOException
    {
        // Write anything left in the buffer into the writer.
        try {
            _writer.write( _buffer, 0, _pos );
        } catch ( IOException except ) {
            // We don't throw an exception, but hold it
            // until the end of the document.
            if ( _exception == null )
                _exception = except;
        }
        _pos = 0;
    }


    /**
     * Flush the output stream. Must be called when done printing
     * the document, otherwise some text might be buffered.
     */
    public void flush()
        throws IOException
    {
        try {
            _writer.write( _buffer, 0, _pos );
            _writer.flush();
        } catch ( IOException except ) {
            // We don't throw an exception, but hold it
            // until the end of the document.
            if ( _exception == null )
                _exception = except;
            throw except;
        }
        _pos = 0;
    }


    public void indent()
    {
        // NOOP
    }


    public void unindent()
    {
        // NOOP
    }


    public int getNextIndent()
    {
        return 0;
    }


    public void setNextIndent( int indent )
    {
    }


    public void setThisIndent( int indent )
    {
    }


}
