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


package org.jasig.portal.serialize;


import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;


/**
 * Extends {@link Printer} and adds support for indentation and line
 * wrapping.
 *
 * @version $Revision$ $Date$
 * @author <a href="mailto:arkin@intalio.com">Assaf Arkin</a>
 */
public class IndentPrinter
    extends Printer
{


    /**
     * Holds the currently accumulating text line. This buffer will constantly
     * be reused by deleting its contents instead of reallocating it.
     */
    private StringBuffer    _line;


    /**
     * Holds the currently accumulating text that follows {@link #_line}.
     * When the end of the part is identified by a call to {@link #printSpace}
     * or {@link #breakLine()}, this part is added to the accumulated line.
     */
    private StringBuffer    _text;


    /**
     * Counts how many white spaces come between the accumulated line and the
     * current accumulated text. Multiple spaces at the end of the a line
     * will not be printed.
     */
    private int             _spaces;


    /**
     * Holds the indentation for the current line that is now accumulating in
     * memory and will be sent for printing shortly.
     */
    private int             _thisIndent;
    
    
    /**
     * Holds the indentation for the next line to be printed. After this line is
     * printed, {@link #_nextIndent} is assigned to {@link #_thisIndent}.
     */
    private int             _nextIndent;


    IndentPrinter( Writer writer, OutputFormat format)
    {
        super( writer, format );
        // Initialize everything for a first/second run.
        _line = new StringBuffer( 80 );
        _text = new StringBuffer( 20 );
        _spaces = 0;
        _thisIndent = _nextIndent = 0;
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
    {
        // Can only enter DTD state once. Once we're out of DTD
        // state, can no longer re-enter it.
        if ( _dtdWriter == null ) {
            _line.append( _text );
            _text = new StringBuffer( 20 );
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
    {
        // Only works if we're going out of DTD mode.
        if ( _writer == _dtdWriter ) {
            _line.append( _text );
            _text = new StringBuffer( 20 );
            flushLine( false );
            _writer = _docWriter;
            return _dtdWriter.toString();
        } else
            return null;
    }
    
    
    /**
     * Called to print additional text. Each time this method is called
     * it accumulates more text. When a space is printed ({@link
     * #printSpace}) all the accumulated text becomes one part and is
     * added to the accumulate line. When a line is long enough, it can
     * be broken at its text boundary.
     *
     * @param text The text to print
     */
    public void printText( String text )
    {
        _text.append( text );
    }
    
    
    public void printText( StringBuffer text )
    {
        _text.append( text );
    }


    public void printText( char ch )
    {
        _text.append( ch );
    }


    public void printText( char[] chars, int start, int length )
    {
        _text.append( chars, start, length );
    }
    

    /**
     * Called to print a single space between text parts that may be
     * broken into separate lines. Must not be called to print a space
     * when preserving spaces. The text accumulated so far with {@link
     * #printText(String)} will be added to the accumulated line, and a space
     * separator will be counted. If the line accumulated so far is
     * long enough, it will be printed.
     */
    public void printSpace()
    {
        // The line consists of the text accumulated in _line,
        // followed by one or more spaces as counted by _spaces,
        // followed by more space accumulated in _text:
        // -  Text is printed and accumulated into _text.
        // -  A space is printed, so _text is added to _line and
        //    a space is counted.
        // -  More text is printed and accumulated into _text.
        // -  A space is printed, the previous spaces are added
        //    to _line, the _text is added to _line, and a new
        //    space is counted.
        
        // If text was accumulated with printText(), then the space
        // means we have to move that text into the line and
        // start accumulating new text with printText().
        if ( _text.length() > 0 ) {
            // If the text breaks a line bounary, wrap to the next line.
            // The printed line size consists of the indentation we're going
            // to use next, the accumulated line so far, some spaces and the
            // accumulated text so far.
            if ( _format.getLineWidth() > 0 &&
                 _thisIndent + _line.length() + _spaces + _text.length() > _format.getLineWidth() ) {
                flushLine( false );
                try {
                    // Print line and new line, then zero the line contents.
                    _writer.write( _format.getLineSeparator() );
                } catch ( IOException except ) {
                    // We don't throw an exception, but hold it
                    // until the end of the document.
                    if ( _exception == null )
                        _exception = except;
                }
            }
            
            // Add as many spaces as we accumulaed before.
            // At the end of this loop, _spaces is zero.
            while ( _spaces > 0 ) {
                _line.append( ' ' );
                --_spaces;
            }
            _line.append( _text );
            _text = new StringBuffer( 20 );
        }
        // Starting a new word: accumulate the text between the line
        // and this new word; not a new word: just add another space.
        ++_spaces;
    }


    /**
     * Called to print a line consisting of the text accumulated so
     * far. This is equivalent to calling {@link #printSpace} but
     * forcing the line to print and starting a new line ({@link
     * #printSpace} will only start a new line if the current line
     * is long enough).
     */
    public void breakLine()
    {
        breakLine( false );
    }


    public void breakLine( boolean preserveSpace )
    {
        // Equivalent to calling printSpace and forcing a flushLine.
        if ( _text.length() > 0 ) {
            while ( _spaces > 0 ) {
                _line.append( ' ' );
                --_spaces;
            }
            _line.append( _text );
            _text = new StringBuffer( 20 );
        }
        flushLine( preserveSpace );
        try {
            // Print line and new line, then zero the line contents.
            _writer.write( _format.getLineSeparator() );
        } catch ( IOException except ) {
            // We don't throw an exception, but hold it
            // until the end of the document.
            if ( _exception == null )
                _exception = except;
        }
    }
    

    /**
     * Flushes the line accumulated so far to the writer and get ready
     * to accumulate the next line. This method is called by {@link
     * #printText(String)} and {@link #printSpace} when the accumulated line plus
     * accumulated text are two long to fit on a given line. At the end of
     * this method <code>_line</code> is empty and <code>_spaces</code> is zero.
     */
    public void flushLine( boolean preserveSpace )
    {
        int     indent;
        
        if ( _line.length() > 0 ) {
            try {
                
                if ( _format.getIndenting() && ! preserveSpace ) {
                    // Make sure the indentation does not blow us away.
                    indent = _thisIndent;
                    if ( ( 2 * indent ) > _format.getLineWidth() && _format.getLineWidth() > 0 )
                        indent = _format.getLineWidth() / 2;
                    // Print the indentation as spaces and set the current
                    // indentation to the next expected indentation.
                    while ( indent > 0 ) {
                        _writer.write( ' ' );
                        --indent;
                    }
                }
                _thisIndent = _nextIndent;
                
                // There is no need to print the spaces at the end of the line,
                // they are simply stripped and replaced with a single line
                // separator.
                _spaces = 0;
                _writer.write( _line.toString() );
                
                _line = new StringBuffer( 40 );
            } catch ( IOException except ) {
                // We don't throw an exception, but hold it
                // until the end of the document.
                if ( _exception == null )
                    _exception = except;
            }
        }
    }
    
    
    /**
     * Flush the output stream. Must be called when done printing
     * the document, otherwise some text might be buffered.
     */
    public void flush()
    {
        if ( _line.length() > 0 || _text.length() > 0 )
            breakLine();
        try {
            _writer.flush();
        } catch ( IOException except ) {
            // We don't throw an exception, but hold it
            // until the end of the document.
            if ( _exception == null )
                _exception = except;
        }
    }


    /**
     * Increment the indentation for the next line.
     */
    public void indent()
    {
        _nextIndent += _format.getIndent();
    }


    /**
     * Decrement the indentation for the next line.
     */
    public void unindent()
    {
        _nextIndent -= _format.getIndent();
        if ( _nextIndent < 0 )
            _nextIndent = 0;
        // If there is no current line and we're de-identing then
        // this indentation level is actually the next level.
        if ( ( _line.length() + _spaces + _text.length() ) == 0 )
            _thisIndent = _nextIndent;
    }


    public int getNextIndent()
    {
        return _nextIndent;
    }


    public void setNextIndent( int indent )
    {
        _nextIndent = indent;
    }


    public void setThisIndent( int indent )
    {
        _thisIndent = indent;
    }


}
