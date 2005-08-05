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
//  Fixed comments to preserve whitespaces and add a line break
//  when indenting. Reported by Gervase Markham <GRM@dataconnection.com>
// Sep 14, 2000:
//  Fixed serializer to report IO exception directly, instead at
//  the end of document processing.
//  Reported by Patrick Higgins <phiggins@transzap.com>
// Sep 13, 2000:
//   CR in character data will print as &#0D;
// Aug 25, 2000:
//   Fixed processing instruction printing inside element content
//   to not escape content. Reported by Mikael Staldal
//   <d96-mst@d.kth.se>
// Aug 25, 2000:
//   Added ability to omit comments.
//   Contributed by Anupam Bagchi <abagchi@jtcsv.com>
// Aug 26, 2000:
//   Fixed bug in newline handling when preserving spaces.
//   Contributed by Mike Dusseault <mdusseault@home.com>
// Aug 29, 2000:
//   Fixed state.unescaped not being set to false when
//   entering element state.
//   Reported by Lowell Vaughn <lvaughn@agillion.com>


package org.jasig.portal.serialize;


import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.transform.Result;

import org.jasig.portal.IAnchoringSerializer;
import org.jasig.portal.properties.PropertiesManager;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.DocumentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;


/**
 * Base class for a serializer supporting both DOM and SAX pretty
 * serializing of XML/HTML/XHTML documents. Derives classes perform
 * the method-specific serializing, this class provides the common
 * serializing mechanisms.
 * <p>
 * The serializer must be initialized with the proper writer and
 * output format before it can be used by calling <code>init</code>.
 * The serializer can be reused any number of times, but cannot
 * be used concurrently by two threads.
 * <p>
 * If an output stream is used, the encoding is taken from the
 * output format (defaults to <tt>UTF-8</tt>). If a writer is
 * used, make sure the writer uses the same encoding (if applies)
 * as specified in the output format.
 * <p>
 * The serializer supports both DOM and SAX. DOM serializing is done
 * by calling {@link #serialize(Document)} and SAX serializing is done by firing
 * SAX events and using the serializer as a document handler.
 * This also applies to derived class.
 * <p>
 * If an I/O exception occurs while serializing, the serializer
 * will not throw an exception directly, but only throw it
 * at the end of serializing (either DOM or SAX's {@link
 * org.xml.sax.DocumentHandler#endDocument()}.
 * <p>
 * For elements that are not specified as whitespace preserving,
 * the serializer will potentially break long text lines at space
 * boundaries, indent lines, and serialize elements on separate
 * lines. Line terminators will be regarded as spaces, and
 * spaces at beginning of line will be stripped.
 * <p>
 * When indenting, the serializer is capable of detecting seemingly
 * element content, and serializing these elements indented on separate
 * lines. An element is serialized indented when it is the first or
 * last child of an element, or immediate following or preceding
 * another element.
 *
 *
 * @version $Revision$ $Date$
 * @author <a href="mailto:arkin@intalio.com">Assaf Arkin</a>
 * @see Serializer
 * @see DOMSerializer
 */
public abstract class BaseMarkupSerializer
    implements ContentHandler, DocumentHandler, LexicalHandler,
               DTDHandler, DeclHandler, DOMSerializer, Serializer, IAnchoringSerializer
{

    protected String anchorId = null;
    private EncodingInfo _encodingInfo;


    /**
     * Holds array of all element states that have been entered.
     * The array is automatically resized. When leaving an element,
     * it's state is not removed but reused when later returning
     * to the same nesting level.
     */
    private ElementState[]  _elementStates;


    /**
     * The index of the next state to place in the array,
     * or one plus the index of the current state. When zero,
     * we are in no state.
     */
    private int             _elementStateCount;


    /**
     * Vector holding comments and PIs that come before the root
     * element (even after it), see {@link #serializePreRoot}.
     */
    private Vector          _preRoot;


    /**
     * If the document has been started (header serialized), this
     * flag is set to true so it's not started twice.
     */
    protected boolean       _started;


    /**
     * True if the serializer has been prepared. This flag is set
     * to false when the serializer is reset prior to using it,
     * and to true after it has been prepared for usage.
     */
    private boolean         _prepared;


    /**
     * Association between namespace URIs (keys) and prefixes (values).
     * Accumulated here prior to starting an element and placing this
     * list in the element state.
     */
    protected Hashtable     _prefixes;


    /**
     * The system identifier of the document type, if known.
     */
    protected String        _docTypePublicId;


    /**
     * The system identifier of the document type, if known.
     */
    protected String        _docTypeSystemId;


    /**
     * The output format associated with this serializer. This will never
     * be a null reference. If no format was passed to the constructor,
     * the default one for this document type will be used. The format
     * object is never changed by the serializer.
     */
    protected OutputFormat   _format;


    /**
     * The printer used for printing text parts.
     */
    protected Printer       _printer;


    /**
     * True if indenting printer.
     */
    protected boolean       _indenting;


    /**
     * The underlying writer.
     */
    private Writer          _writer;


    /**
     * The output stream.
     */
    private OutputStream    _output;


    /**
     * A portal property indicating whether or not to allow the disabling
     * of output escaping.  When allowed, XSLT stylesheets can request
     * to disable output escaping, therefore enabling the direct pass-through
     * of markup such as HTML.
     */
    private boolean         _allowDisableOutputEscaping;
    

    //--------------------------------//
    // Constructor and initialization //
    //--------------------------------//


    /**
     * Protected constructor can only be used by derived class.
     * Must initialize the serializer before serializing any document.
     */
    protected BaseMarkupSerializer( OutputFormat format )
    {
        int i;

        _elementStates = new ElementState[ 10 ];
        for ( i = 0 ; i < _elementStates.length ; ++i )
            _elementStates[ i ] = new ElementState();
        _format = format;
       
       try { 
         _allowDisableOutputEscaping = PropertiesManager.getPropertyAsBoolean("org.jasig.portal.serialize.BaseMarkupSerializer.allow_disable_output_escaping");
       } catch ( Exception e ) {
       	 _allowDisableOutputEscaping = false;  
       }
    }


    public DocumentHandler asDocumentHandler()
        throws IOException
    {
        prepare();
        return this;
    }


    public ContentHandler asContentHandler()
        throws IOException
    {
        prepare();
        return this;
    }


    public DOMSerializer asDOMSerializer()
        throws IOException
    {
        prepare();
        return this;
    }


    public void setOutputByteStream( OutputStream output )
    {
        if ( output == null )
            throw new NullPointerException( "SER001 Argument 'output' is null." );
        _output = output;
        _writer = null;
        reset();
    }


    public void setOutputCharStream( Writer writer )
    {
        if ( writer == null )
            throw new NullPointerException( "SER001 Argument 'writer' is null." );
        _writer = writer;
        _output = null;
        reset();
    }


    public void setOutputFormat( OutputFormat format )
    {
        if ( format == null )
            throw new NullPointerException( "SER001 Argument 'format' is null." );
        _format = format;
        reset();
    }

    public boolean reset()
    {
        if ( _elementStateCount > 1 )
            throw new IllegalStateException( "Serializer reset in the middle of serialization" );
        _prepared = false;
        return true;
    }


    protected void prepare()
        throws IOException
    {
        if ( _prepared )
            return;

        if ( _writer == null && _output == null )
            throw new IOException( "SER002 No writer supplied for serializer" );
        // If the output stream has been set, use it to construct
        // the writer. It is possible that the serializer has been
        // reused with the same output stream and different encoding.

        _encodingInfo = _format.getEncodingInfo();

        if ( _output != null ) {
            _writer = _encodingInfo.getWriter(_output);
        }

        if ( _format.getIndenting() ) {
            _indenting = true;
            _printer = new IndentPrinter( _writer, _format );
        } else {
            _indenting = false;
            _printer = new Printer( _writer, _format );
        }

        ElementState state;

        _elementStateCount = 0;
        state = _elementStates[ 0 ];
        state.namespaceURI = null;
        state.localName = null;
        state.rawName = null;
        state.preserveSpace = _format.getPreserveSpace();
        state.empty = true;
        state.afterElement = false;
        state.afterComment = false;
        state.doCData = state.inCData = false;
        state.prefixes = null;

        _docTypePublicId = _format.getDoctypePublic();
        _docTypeSystemId = _format.getDoctypeSystem();
        _started = false;
        _prepared = true;
    }



    //----------------------------------//
    // DOM document serializing methods //
    //----------------------------------//


    /**
     * Serializes the DOM element using the previously specified
     * writer and output format. Throws an exception only if
     * an I/O exception occured while serializing.
     *
     * @param elem The element to serialize
     * @throws IOException An I/O exception occured while
     *   serializing
     */
    public void serialize( Element elem )
        throws IOException
    {
        prepare();
        serializeNode( elem );
        _printer.flush();
        if ( _printer.getException() != null )
            throw _printer.getException();
    }


    /**
     * Serializes the DOM document fragmnt using the previously specified
     * writer and output format. Throws an exception only if
     * an I/O exception occured while serializing.
     *
     * @param frag the document fragment to serialize
     * @throws IOException An I/O exception occured while
     *   serializing
     */
    public void serialize( DocumentFragment frag )
        throws IOException
    {
        prepare();
        serializeNode( frag );
        _printer.flush();
        if ( _printer.getException() != null )
            throw _printer.getException();
    }


    /**
     * Serializes the DOM document using the previously specified
     * writer and output format. Throws an exception only if
     * an I/O exception occured while serializing.
     *
     * @param doc The document to serialize
     * @throws IOException An I/O exception occured while
     *   serializing
     */
    public void serialize( Document doc )
        throws IOException
    {
        prepare();
        serializeNode( doc );
        serializePreRoot();
        _printer.flush();
        if ( _printer.getException() != null )
            throw _printer.getException();
    }


    //------------------------------------------//
    // SAX document handler serializing methods //
    //------------------------------------------//


    public void startDocument()
        throws SAXException
    {
        try {
            prepare();
        } catch ( IOException except ) {
            throw new SAXException( except.toString() );
        }
        // Nothing to do here. All the magic happens in startDocument(String)
    }
    
    
    public void characters( char[] chars, int start, int length )
        throws SAXException
    {
        ElementState state;

        try {
        state = content();

        // Check if text should be print as CDATA section or unescaped
        // based on elements listed in the output format (the element
        // state) or whether we are inside a CDATA section or entity.

        if ( state.inCData || state.doCData ) {
            int          saveIndent;

            // Print a CDATA section. The text is not escaped, but ']]>'
            // appearing in the code must be identified and dealt with.
            // The contents of a text node is considered space preserving.
            if ( ! state.inCData ) {
                _printer.printText( "<![CDATA[" );
                state.inCData = true;
            }
            saveIndent = _printer.getNextIndent();
            _printer.setNextIndent( 0 );
            for ( int index = 0 ; index < length ; ++index ) {
                if ( index + 2 < length && chars[ index ] == ']' &&
                     chars[ index + 1 ] == ']' && chars[ index + 2 ] == '>' ) {

                    printText( chars, start, index + 2, true, true );
                    _printer.printText( "]]><![CDATA[" );
                    start += index + 2;
                    length -= index + 2;
                    index = 0;
                }
            }
            if ( length > 0 )
                printText( chars, start, length, true, true );
            _printer.setNextIndent( saveIndent );

        } else {

            int saveIndent;

            if ( state.preserveSpace ) {
                // If preserving space then hold of indentation so no
                // excessive spaces are printed at line breaks, escape
                // the text content without replacing spaces and print
                // the text breaking only at line breaks.
                saveIndent = _printer.getNextIndent();
                _printer.setNextIndent( 0 );
                printText( chars, start, length, true, state.unescaped );
                _printer.setNextIndent( saveIndent );
            } else {
                printText( chars, start, length, false, state.unescaped );
            }
        }
        } catch ( IOException except ) {
            throw new SAXException( except );
        }
    }


    public void ignorableWhitespace( char[] chars, int start, int length )
        throws SAXException
    {
        int i;

        try {
        content();

        // Print ignorable whitespaces only when indenting, after
        // all they are indentation. Cancel the indentation to
        // not indent twice.
        if ( _indenting ) {
            _printer.setThisIndent( 0 );
            for ( i = start ; length-- > 0 ; ++i )
                _printer.printText( chars[ i ] );
        }
        } catch ( IOException except ) {
            throw new SAXException( except );
        }
    }


    public final void processingInstruction( String target, String code )
        throws SAXException
    {
        try {
            processingInstructionIO( target, code );
        } catch ( IOException except ) {
        throw new SAXException( except );
        }
    }

    public void processingInstructionIO( String target, String code )
        throws IOException
    {
        int          index;
        StringBuffer buffer;
        ElementState state;

        state = content();
        buffer = new StringBuffer( 40 );

        // Create the processing instruction textual representation.
        // Make sure we don't have '?>' inside either target or code.
        index = target.indexOf( "?>" );
        if ( index >= 0 )
            buffer.append( "<?" ).append( target.substring( 0, index ) );
        else
            buffer.append( "<?" ).append( target );
        if ( code != null ) {
            buffer.append( ' ' );
            index = code.indexOf( "?>" );
            if ( index >= 0 )
                buffer.append( code.substring( 0, index ) );
            else
                buffer.append( code );
        }
        buffer.append( "?>" );

        // If before the root element (or after it), do not print
        // the PI directly but place it in the pre-root vector.
        if ( isDocumentState() ) {
            if ( _preRoot == null )
                _preRoot = new Vector();
            _preRoot.addElement( buffer.toString() );
        } else {
            _printer.indent();
            printText( buffer.toString(), true, true );
            _printer.unindent();
            if ( _indenting )
            state.afterElement = true;
        }
        
        if (_allowDisableOutputEscaping) {
            if (target.equals(Result.PI_DISABLE_OUTPUT_ESCAPING))
                startNonEscaping();
            else if (target.equals(Result.PI_ENABLE_OUTPUT_ESCAPING))
                endNonEscaping();
        }        
    }


    public void comment( char[] chars, int start, int length )
        throws SAXException
    {
        try {
        comment( new String( chars, start, length ) );
        } catch ( IOException except ) {
            throw new SAXException( except );
    }
    }


    public void comment( String text )
        throws IOException
    {
        StringBuffer buffer;
        int          index;
        ElementState state;
        
        if ( _format.getOmitComments() )
            return;

        state  = content();
        buffer = new StringBuffer( 40 );
        // Create the processing comment textual representation.
        // Make sure we don't have '-->' inside the comment.
        index = text.indexOf( "-->" );
        if ( index >= 0 )
            buffer.append( "<!--" ).append( text.substring( 0, index ) ).append( "-->" );
        else
            buffer.append( "<!--" ).append( text ).append( "-->" );

        // If before the root element (or after it), do not print
        // the comment directly but place it in the pre-root vector.
        if ( isDocumentState() ) {
            if ( _preRoot == null )
                _preRoot = new Vector();
            _preRoot.addElement( buffer.toString() );
        } else {
            // Indent this element on a new line if the first
            // content of the parent element or immediately
            // following an element.
            if ( _indenting && ! state.preserveSpace)
                _printer.breakLine();
						_printer.indent();
            printText( buffer.toString(), true, true );
						_printer.unindent();
            if ( _indenting )
                state.afterElement = true;
        }
				state.afterComment = true;
				state.afterElement = false;
    }


    public void startCDATA()
    {
        ElementState state;

        state = getElementState();
        state.doCData = true;
    }


    public void endCDATA()
    {
        ElementState state;

        state = getElementState();
        state.doCData = false;
    }


    public void startNonEscaping()
    {
        ElementState state;

        state = getElementState();
        state.unescaped = true;
    }


    public void endNonEscaping()
    {
        ElementState state;

        state = getElementState();
        state.unescaped = false;
    }


    public void startPreserving()
    {
        ElementState state;

        state = getElementState();
        state.preserveSpace = true;
    }


    public void endPreserving()
    {
        ElementState state;

        state = getElementState();
        state.preserveSpace = false;
    }


    /**
     * Called at the end of the document to wrap it up.
     * Will flush the output stream and throw an exception
     * if any I/O error occured while serializing.
     *
     * @throws SAXException An I/O exception occured during
     *  serializing
     */
    public void endDocument()
        throws SAXException
    {
        try {
        // Print all the elements accumulated outside of
        // the root element.
        serializePreRoot();
        // Flush the output, this is necessary for buffered output.
        _printer.flush();
        } catch ( IOException except ) {
            throw new SAXException( except );
    }
    }


    public void startEntity( String name )
    {
        // ???
    }


    public void endEntity( String name )
    {
        // ???
    }


    public void setDocumentLocator( Locator locator )
    {
        // Nothing to do
    }


    //-----------------------------------------//
    // SAX content handler serializing methods //
    //-----------------------------------------//


    public void skippedEntity ( String name )
        throws SAXException
    {
        try {
        endCDATA();
        content();
        _printer.printText( '&' );
        _printer.printText( name );
        _printer.printText( ';' );
        } catch ( IOException except ) {
            throw new SAXException( except );
    }
    }


    public void startPrefixMapping( String prefix, String uri )
        throws SAXException
    {
        if ( _prefixes == null )
            _prefixes = new Hashtable();
        _prefixes.put( uri, prefix == null ? "" : prefix );
    }


    public void endPrefixMapping( String prefix )
        throws SAXException
    {
    }


    //------------------------------------------//
    // SAX DTD/Decl handler serializing methods //
    //------------------------------------------//


    public final void startDTD( String name, String publicId, String systemId )
        throws SAXException
    {
        try {
        _printer.enterDTD();
        _docTypePublicId = publicId;
        _docTypeSystemId = systemId;
        } catch ( IOException except ) {
            throw new SAXException( except );
        }
    }


    public void endDTD()
    {
        // Nothing to do here, all the magic occurs in startDocument(String).
    }


    public void elementDecl( String name, String model )
        throws SAXException
    {
        try {
        _printer.enterDTD();
        _printer.printText( "<!ELEMENT " );
        _printer.printText( name );
        _printer.printText( ' ' );
        _printer.printText( model );
        _printer.printText( '>' );
        if ( _indenting )
            _printer.breakLine();
        } catch ( IOException except ) {
            throw new SAXException( except );
        }
    }


    public void attributeDecl( String eName, String aName, String type,
                               String valueDefault, String value )
        throws SAXException
    {
        try {
        _printer.enterDTD();
        _printer.printText( "<!ATTLIST " );
        _printer.printText( eName );
        _printer.printText( ' ' );
        _printer.printText( aName );
        _printer.printText( ' ' );
        _printer.printText( type );
        if ( valueDefault != null ) {
            _printer.printText( ' ' );
            _printer.printText( valueDefault );
        }
        if ( value != null ) {
            _printer.printText( " \"" );
            printEscaped( value );
            _printer.printText( '"' );
        }
        _printer.printText( '>' );
        if ( _indenting )
            _printer.breakLine();
        } catch ( IOException except ) {
            throw new SAXException( except );
    }
    }


    public void internalEntityDecl( String name, String value )
        throws SAXException
    {
        try {
        _printer.enterDTD();
        _printer.printText( "<!ENTITY " );
        _printer.printText( name );
        _printer.printText( " \"" );
        printEscaped( value );
        _printer.printText( "\">" );
        if ( _indenting )
            _printer.breakLine();
        } catch ( IOException except ) {
            throw new SAXException( except );
        }
    }


    public void externalEntityDecl( String name, String publicId, String systemId )
        throws SAXException
    {
        try {
        _printer.enterDTD();
        unparsedEntityDecl( name, publicId, systemId, null );
        } catch ( IOException except ) {
            throw new SAXException( except );
        }
    }


    public void unparsedEntityDecl( String name, String publicId,
                                    String systemId, String notationName )
        throws SAXException
    {
        try {
        _printer.enterDTD();
        if ( publicId == null ) {
            _printer.printText( "<!ENTITY " );
            _printer.printText( name );
            _printer.printText( " SYSTEM " );
            printDoctypeURL( systemId );
        } else {
            _printer.printText( "<!ENTITY " );
            _printer.printText( name );
            _printer.printText( " PUBLIC " );
            printDoctypeURL( publicId );
            _printer.printText( ' ' );
            printDoctypeURL( systemId );
        }
        if ( notationName != null ) {
            _printer.printText( " NDATA " );
            _printer.printText( notationName );
        }
        _printer.printText( '>' );
        if ( _indenting )
            _printer.breakLine();
        } catch ( IOException except ) {
            throw new SAXException( except );
    }
    }


    public void notationDecl( String name, String publicId, String systemId )
        throws SAXException
    {
        try {
        _printer.enterDTD();
        if ( publicId != null ) {
            _printer.printText( "<!NOTATION " );
            _printer.printText( name );
            _printer.printText( " PUBLIC " );
            printDoctypeURL( publicId );
            if ( systemId != null ) {
                _printer.printText( ' ' );
                printDoctypeURL( systemId );
            }
        } else {
            _printer.printText( "<!NOTATION " );
            _printer.printText( name );
            _printer.printText( " SYSTEM " );
            printDoctypeURL( systemId );
        }
        _printer.printText( '>' );
        if ( _indenting )
            _printer.breakLine();
        } catch ( IOException except ) {
            throw new SAXException( except );
        }
    }


    //------------------------------------------//
    // Generic node serializing methods methods //
    //------------------------------------------//


    /**
     * Serialize the DOM node. This method is shared across XML, HTML and XHTML
     * serializers and the differences are masked out in a separate {@link
     * #serializeElement}.
     *
     * @param node The node to serialize
     * @see #serializeElement
     * @throws IOException An I/O exception occured while
     *   serializing
     */
    protected void serializeNode( Node node )
        throws IOException
    {
        // Based on the node type call the suitable SAX handler.
        // Only comments entities and documents which are not
        // handled by SAX are serialized directly.
        switch ( node.getNodeType() ) {
        case Node.TEXT_NODE : {
            String text;

            text = node.getNodeValue();
            if ( text != null )
                if ( !_indenting || getElementState().preserveSpace
                     || (text.replace('\n',' ').trim().length() != 0))
                    characters( text );
            break;
        }

        case Node.CDATA_SECTION_NODE : {
            String text;

            text = node.getNodeValue();
            if ( text != null ) {
                startCDATA();
                characters( text );
                endCDATA();
            }
            break;
        }

        case Node.COMMENT_NODE : {
            String text;

            if ( ! _format.getOmitComments() ) {
                text = node.getNodeValue();
                if ( text != null )
                    comment( text );
            }
            break;
        }

        case Node.ENTITY_REFERENCE_NODE : {
            Node         child;

            endCDATA();
            content();
            child = node.getFirstChild();
            while ( child != null ) {
                serializeNode( child );
                child = child.getNextSibling();
            }
            break;
        }

        case Node.PROCESSING_INSTRUCTION_NODE :
            processingInstructionIO( node.getNodeName(), node.getNodeValue() );
            break;

        case Node.ELEMENT_NODE :
            serializeElement( (Element) node );
            break;

        case Node.DOCUMENT_NODE : {
            DocumentType      docType;
            DOMImplementation domImpl;

            // If there is a document type, use the SAX events to
            // serialize it.
            docType = ( (Document) node ).getDoctype();
            if (docType != null) {
                // DOM Level 2 (or higher)
                domImpl = ( (Document) node ).getImplementation();
                try {
                    String internal;

                    _printer.enterDTD();
                    _docTypePublicId = docType.getPublicId();
                    _docTypeSystemId = docType.getSystemId();
                    internal = docType.getInternalSubset();
                    if ( internal != null && internal.length() > 0 )
                        _printer.printText( internal );
                    endDTD();
                }
                // DOM Level 1 -- does implementation have methods?
                catch (NoSuchMethodError nsme) {
                    Class docTypeClass = docType.getClass();

                    String docTypePublicId = null;
                    String docTypeSystemId = null;
                    try {
                        java.lang.reflect.Method getPublicId = docTypeClass.getMethod("getPublicId", (Class[])null);
                        if (getPublicId.getReturnType().equals(String.class)) {
                            docTypePublicId = (String)getPublicId.invoke(docType, (Object[])null);
                        }
                    }
                    catch (Exception e) {
                        // ignore
                    }
                    try {
                        java.lang.reflect.Method getSystemId = docTypeClass.getMethod("getSystemId", (Class[])null);
                        if (getSystemId.getReturnType().equals(String.class)) {
                            docTypeSystemId = (String)getSystemId.invoke(docType, (Object[])null);
                        }
                    }
                    catch (Exception e) {
                        // ignore
                    }
                    _printer.enterDTD();
                    _docTypePublicId = docTypePublicId;
                    _docTypeSystemId = docTypeSystemId;
                    endDTD();
                }
            }
            // !! Fall through
        }
        case Node.DOCUMENT_FRAGMENT_NODE : {
            Node         child;

            // By definition this will happen if the node is a document,
            // document fragment, etc. Just serialize its contents. It will
            // work well for other nodes that we do not know how to serialize.
            child = node.getFirstChild();
            while ( child != null ) {
                serializeNode( child );
                child = child.getNextSibling();
            }
            break;
        }

        default:
            break;
        }
    }


    /**
     * Must be called by a method about to print any type of content.
     * If the element was just opened, the opening tag is closed and
     * will be matched to a closing tag. Returns the current element
     * state with <tt>empty</tt> and <tt>afterElement</tt> set to false.
     *
     * @return The current element state
     * @throws IOException An I/O exception occured while
     *   serializing
     */
    protected ElementState content()
        throws IOException
    {
        ElementState state;

        state = getElementState();
        if ( ! isDocumentState() ) {
            // Need to close CData section first
            if ( state.inCData && ! state.doCData ) {
                _printer.printText( "]]>" );
                state.inCData = false;
            }
            // If this is the first content in the element,
            // change the state to not-empty and close the
            // opening element tag.
            if ( state.empty ) {
               _printer.printText( ">" );
                state.empty = false;
            }
            // Except for one content type, all of them
            // are not last element. That one content
            // type will take care of itself.
            state.afterElement = false;
            // Except for one content type, all of them
            // are not last comment. That one content
            // type will take care of itself.
            state.afterComment = false;
        }
        return state;
    }


    /**
     * Called to print the text contents in the prevailing element format.
     * Since this method is capable of printing text as CDATA, it is used
     * for that purpose as well. White space handling is determined by the
     * current element state. In addition, the output format can dictate
     * whether the text is printed as CDATA or unescaped.
     *
     * @param text The text to print
     * @throws IOException An I/O exception occured while serializing
     */
    protected void characters( String text )
        throws IOException
    {
        ElementState state;

        state = content();
        // Check if text should be print as CDATA section or unescaped
        // based on elements listed in the output format (the element
        // state) or whether we are inside a CDATA section or entity.

        if ( state.inCData || state.doCData ) {
            StringBuffer buffer;
            int          index;
            int          saveIndent;

            // Print a CDATA section. The text is not escaped, but ']]>'
            // appearing in the code must be identified and dealt with.
            // The contents of a text node is considered space preserving.
            buffer = new StringBuffer( text.length() );
            if ( ! state.inCData ) {
                buffer.append( "<![CDATA[" );
                state.inCData = true;
            }
            index = text.indexOf( "]]>" );
            while ( index >= 0 ) {
                buffer.append( text.substring( 0, index + 2 ) ).append( "]]><![CDATA[" );
                text = text.substring( index + 2 );
                index = text.indexOf( "]]>" );
            }
            buffer.append( text );
            saveIndent = _printer.getNextIndent();
            _printer.setNextIndent( 0 );
            printText( buffer.toString(), true, true );
            _printer.setNextIndent( saveIndent );

        } else {

            int saveIndent;

            if ( state.preserveSpace ) {
                // If preserving space then hold of indentation so no
                // excessive spaces are printed at line breaks, escape
                // the text content without replacing spaces and print
                // the text breaking only at line breaks.
                saveIndent = _printer.getNextIndent();
                _printer.setNextIndent( 0 );
                printText( text, true, state.unescaped );
                _printer.setNextIndent( saveIndent );
            } else {
                printText( text, false, state.unescaped );
            }
        }
    }


    /**
     * Returns the suitable entity reference for this character value,
     * or null if no such entity exists. Calling this method with <tt>'&amp;'</tt>
     * will return <tt>"&amp;amp;"</tt>.
     *
     * @param ch Character value
     * @return Character entity name, or null
     */
    protected abstract String getEntityRef( int ch );


    /**
     * Called to serializee the DOM element. The element is serialized based on
     * the serializer's method (XML, HTML, XHTML).
     *
     * @param elem The element to serialize
     * @throws IOException An I/O exception occured while
     *   serializing
     */
    protected abstract void serializeElement( Element elem )
        throws IOException;


    /**
     * Comments and PIs cannot be serialized before the root element,
     * because the root element serializes the document type, which
     * generally comes first. Instead such PIs and comments are
     * accumulated inside a vector and serialized by calling this
     * method. Will be called when the root element is serialized
     * and when the document finished serializing.
     *
     * @throws IOException An I/O exception occured while
     *   serializing
     */
    protected void serializePreRoot()
        throws IOException
    {
        int i;

        if ( _preRoot != null ) {
            for ( i = 0 ; i < _preRoot.size() ; ++i ) {
                printText( (String) _preRoot.elementAt( i ), true, true );
                if ( _indenting )
                _printer.breakLine();
            }
            _preRoot.removeAllElements();
        }
    }


    //---------------------------------------------//
    // Text pretty printing and formatting methods //
    //---------------------------------------------//


    /**
     * Called to print additional text with whitespace handling.
     * If spaces are preserved, the text is printed as if by calling
     * {@link #printText(String, boolean, boolean)} with a call to breakLine()
     * for each new line. If spaces are not preserved, the text is
     * broken at space boundaries if longer than the line width;
     * Multiple spaces are printed as such, but spaces at beginning
     * of line are removed.
     *
     * @param preserveSpace Space preserving flag
     * @param unescaped Print unescaped
     */
    protected final void printText( char[] chars, int start, int length,
                                    boolean preserveSpace, boolean unescaped )
        throws IOException
    {
        char ch;

        if ( preserveSpace ) {
            // Preserving spaces: the text must print exactly as it is,
            // without breaking when spaces appear in the text and without
            // consolidating spaces. If a line terminator is used, a line
            // break will occur.
            while ( length-- > 0 ) {
                ch = chars[ start ];
                ++start;
                if ( ch == '\n' || ch == '\r' || unescaped )
                    _printer.printText( ch );
                else
                    printEscaped( ch );
            }
        } else {
            // Not preserving spaces: print one part at a time, and
            // use spaces between parts to break them into different
            // lines. Spaces at beginning of line will be stripped
            // by printing mechanism. Line terminator is treated
            // no different than other text part.
            while ( length-- > 0 ) {
                ch = chars[ start ];
                ++start;
                if ( ch == ' ' || ch == '\f' || ch == '\t' || ch == '\n' || ch == '\r' )
                    _printer.printSpace();
                else if ( unescaped )
                    _printer.printText( ch );
                else
                    printEscaped( ch );
            }
        }
    }


    protected final void printText( String text, boolean preserveSpace, boolean unescaped )
        throws IOException
    {
        int index;
        char ch;

        if ( preserveSpace ) {
            // Preserving spaces: the text must print exactly as it is,
            // without breaking when spaces appear in the text and without
            // consolidating spaces. If a line terminator is used, a line
            // break will occur.
            for ( index = 0 ; index < text.length() ; ++index ) {
                ch = text.charAt( index );
                if ( ch == '\n' || ch == '\r' || unescaped )
                    _printer.printText( ch );
                else
                    printEscaped( ch );
            }
        } else {
            // Not preserving spaces: print one part at a time, and
            // use spaces between parts to break them into different
            // lines. Spaces at beginning of line will be stripped
            // by printing mechanism. Line terminator is treated
            // no different than other text part.
            for ( index = 0 ; index < text.length() ; ++index ) {
                ch = text.charAt( index );
                if ( ch == ' ' || ch == '\f' || ch == '\t' || ch == '\n' || ch == '\r' )
                    _printer.printSpace();
                else if ( unescaped )
                    _printer.printText( ch );
                else
                    printEscaped( ch );
            }
        }
    }


    /**
     * Print a document type public or system identifier URL.
     * Encapsulates the URL in double quotes, escapes non-printing
     * characters and print it equivalent to {@link #printText(char[], int, int, boolean, boolean)}.
     *
     * @param url The document type url to print
     */
    protected void printDoctypeURL( String url )
        throws IOException
    {
        int                i;

        _printer.printText( '"' );
        for( i = 0 ; i < url.length() ; ++i ) {
            if ( url.charAt( i ) == '"' ||  url.charAt( i ) < 0x20 || url.charAt( i ) > 0x7F ) {
                _printer.printText( '%' );
                _printer.printText( Integer.toHexString( url.charAt( i ) ) );
            } else
                _printer.printText( url.charAt( i ) );
        }
        _printer.printText( '"' );
    }


    protected void printEscaped( int ch )
        throws IOException
    {
        String charRef;

        // If there is a suitable entity reference for this
        // character, print it. The list of available entity
        // references is almost but not identical between
        // XML and HTML.
        charRef = getEntityRef( ch );
        if ( charRef != null ) {
            _printer.printText( '&' );
            _printer.printText( charRef );
            _printer.printText( ';' );
        } else if ( ( ch >= ' ' && _encodingInfo.isPrintable(ch) && ch != 0xF7 ) ||
                    ch == '\n' || ch == '\r' || ch == '\t' ) {
            // If the character is not printable, print as character reference.
            // Non printables are below ASCII space but not tab or line
            // terminator, ASCII delete, or above a certain Unicode threshold.
            if (ch < 0x10000) {
                _printer.printText((char)ch );
            } else {
                _printer.printText((char)(((ch-0x10000)>>10)+0xd800));
                _printer.printText((char)(((ch-0x10000)&0x3ff)+0xdc00));
            }

        } else {
            _printer.printText( "&#x" );
            _printer.printText(Integer.toHexString(ch));
            _printer.printText( ';' );
        }
    }


    /**
     * Escapes a string so it may be printed as text content or attribute
     * value. Non printable characters are escaped using character references.
     * Where the format specifies a deault entity reference, that reference
     * is used (e.g. <tt>&amp;lt;</tt>).
     *
     * @param source The string to escape
     */
    protected void printEscaped( String source )
        throws IOException
    {
        for ( int i = 0 ; i < source.length() ; ++i ) {
            int ch = source.charAt(i);
            if ((ch & 0xfc00) == 0xd800 && i+1 < source.length()) {
                int lowch = source.charAt(i+1);
                if ((lowch & 0xfc00) == 0xdc00) {
                    ch = 0x10000 + ((ch-0xd800)<<10) + lowch-0xdc00;
                    i++;
                }
            }
            printEscaped(ch);
        }
    }


    //--------------------------------//
    // Element state handling methods //
    //--------------------------------//


    /**
     * Return the state of the current element.
     *
     * @return Current element state
     */
    protected ElementState getElementState()
    {
        return _elementStates[ _elementStateCount ];
    }


    /**
     * Enter a new element state for the specified element.
     * Tag name and space preserving is specified, element
     * state is initially empty.
     *
     * @return Current element state, or null
     */
    protected ElementState enterElementState( String namespaceURI, String localName,
                                              String rawName, boolean preserveSpace )
    {
        ElementState state;

        if ( _elementStateCount + 1 == _elementStates.length ) {
            ElementState[] newStates;

            // Need to create a larger array of states. This does not happen
            // often, unless the document is really deep.
            newStates = new ElementState[ _elementStates.length + 10 ];
            for ( int i = 0 ; i < _elementStates.length ; ++i )
                newStates[ i ] = _elementStates[ i ];
            for ( int i = _elementStates.length ; i < newStates.length ; ++i )
                newStates[ i ] = new ElementState();
            _elementStates = newStates;
        }

        ++_elementStateCount;
        state = _elementStates[ _elementStateCount ];
        state.namespaceURI = namespaceURI;
        state.localName = localName;
        state.rawName = rawName;
        state.preserveSpace = preserveSpace;
        state.empty = true;
        state.afterElement = false;
        state.afterComment = false;
        state.doCData = state.inCData = false;
        state.unescaped = false;
        state.prefixes = _prefixes;

        _prefixes = null;
        return state;
    }


    /**
     * Leave the current element state and return to the
     * state of the parent element. If this was the root
     * element, return to the state of the document.
     *
     * @return Previous element state
     */
    protected ElementState leaveElementState()
    {
        if ( _elementStateCount > 0 ) {
            /*Corrected by David Blondeau (blondeau@intalio.com)*/
		_prefixes = null;
		//_prefixes = _elementStates[ _elementStateCount ].prefixes;
            -- _elementStateCount;
            return _elementStates[ _elementStateCount ];
        } else
            throw new IllegalStateException( "Internal error: element state is zero" );
    }


    /**
     * Returns true if in the state of the document.
     * Returns true before entering any element and after
     * leaving the root element.
     *
     * @return True if in the state of the document
     */
    protected boolean isDocumentState()
    {
        return _elementStateCount == 0;
    }


    /**
     * Returns the namespace prefix for the specified URI.
     * If the URI has been mapped to a prefix, returns the
     * prefix, otherwise returns null.
     *
     * @param namespaceURI The namespace URI
     * @return The namespace prefix if known, or null
     */
    protected String getPrefix( String namespaceURI )
    {
        String    prefix;

        if ( _prefixes != null ) {
            prefix = (String) _prefixes.get( namespaceURI );
            if ( prefix != null )
                return prefix;
        }
        if ( _elementStateCount == 0 )
            return null;
        else {
            for ( int i = _elementStateCount ; i > 0 ; --i ) {
                if ( _elementStates[ i ].prefixes != null ) {
                    prefix = (String) _elementStates[ i ].prefixes.get( namespaceURI );
                    if ( prefix != null )
                        return prefix;
                }
            }
        }
        return null;
    }
    
    public void startAnchoring(String anchorId) {
        this.anchorId = anchorId;
    }
    
    public void stopAnchoring() {
        this.anchorId = null;
    }

    protected String appendAnchorIfNecessary(String elementName, String attributeName, String attributeValue) {
        if (anchorId != null) {
            // looking for an <a> or <form> tag element
            if (elementName.equalsIgnoreCase("a") || elementName.equalsIgnoreCase("form")) {
                // found an <a> or <form>, let's peek at the attributes it contains
                // does it contain either an "href" or "action" attribute
                if (attributeName.equalsIgnoreCase("href") || attributeName.equalsIgnoreCase("action")) {
                    // found the attribute, now lets make sure it points back to a channel
                    if (attributeValue.indexOf(".render.") != -1 && attributeValue.indexOf("javascript:") == -1) {
                        // this link points back to a channel, so let's
                        // rewrite it and place back into the Attribute Object
                        attributeValue += "#" + anchorId;
                    }
                }
            }
        }
        return attributeValue;
    }    
}
