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


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Enumeration;

import org.w3c.dom.Element;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.DocumentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.AttributeList;
import org.xml.sax.Attributes;


public class PalmHTMLSerializer
    extends BaseMarkupSerializer
{


    /**
     * True if serializing in XHTML format.
     */
    private static boolean _xhtml;


    public static String XHTMLNamespace = "";




    /**
     * Constructs a new HTML/XHTML serializer depending on the value of
     * <tt>xhtml</tt>. The serializer cannot be used without calling
     * {@link #init} first.
     *
     * @param xhtml True if XHTML serializing
     */
    protected PalmHTMLSerializer( boolean xhtml, OutputFormat format )
    {
        super( format );
        _xhtml = xhtml;
    }


    /**
     * Constructs a new serializer. The serializer cannot be used without
     * calling {@link #setOutputCharStream} or {@link #setOutputByteStream}
     * first.
     */
    public PalmHTMLSerializer()
    {
        this( false, new OutputFormat( Method.HTML, null, false ) );
    }


    /**
     * Constructs a new serializer. The serializer cannot be used without
     * calling {@link #setOutputCharStream} or {@link #setOutputByteStream}
     * first.
     */
    public PalmHTMLSerializer( OutputFormat format )
    {
        this( false, format != null ? format : new OutputFormat( Method.HTML, null, false ) );
    }



    /**
     * Constructs a new serializer that writes to the specified writer
     * using the specified output format. If <tt>format</tt> is null,
     * will use a default output format.
     *
     * @param writer The writer to use
     * @param format The output format to use, null for the default
     */
    public PalmHTMLSerializer( Writer writer, OutputFormat format )
    {
        this( false, format != null ? format : new OutputFormat( Method.HTML, null, false ) );
        setOutputCharStream( writer );
    }


    /**
     * Constructs a new serializer that writes to the specified output
     * stream using the specified output format. If <tt>format</tt>
     * is null, will use a default output format.
     *
     * @param output The output stream to use
     * @param format The output format to use, null for the default
     */
    public PalmHTMLSerializer( OutputStream output, OutputFormat format )
    {
        this( false, format != null ? format : new OutputFormat( Method.HTML, null, false ) );
        setOutputByteStream( output );
    }
    
    
    public void setOutputFormat( OutputFormat format )
    {
        super.setOutputFormat( format != null ? format : new OutputFormat( Method.HTML, null, false ) );
    }


    //-----------------------------------------//
    // SAX content handler serializing methods //
    //-----------------------------------------//


    public void startElement( String namespaceURI, String localName,
                              String rawName, Attributes attrs )
    {
        int          i;
        boolean      preserveSpace;
        ElementState state;
        String       name;
        String       value;
        String       htmlName;
        boolean      addNSAttr = false;
        
        if ( _printer == null )
            throw new IllegalStateException( "SER002 No writer supplied for serializer" );
        
        state = getElementState();
        if ( isDocumentState() ) {
            // If this is the root element handle it differently.
            // If the first root element in the document, serialize
            // the document's DOCTYPE. Space preserving defaults
            // to that of the output format.
            if ( ! _started )
                startDocument( localName == null ? rawName : localName );
        } else {
            // For any other element, if first in parent, then
            // close parent's opening tag and use the parnet's
            // space preserving.
            if ( state.empty )
                _printer.printText( '>' );
            // Indent this element on a new line if the first
            // content of the parent element or immediately
            // following an element.
            if ( _indenting && ! state.preserveSpace &&
                 ( state.empty || state.afterElement ) )
                _printer.breakLine();
        }
        preserveSpace = state.preserveSpace;

        // Do not change the current element state yet.
        // This only happens in endElement().
        
        if ( rawName == null ) {
            rawName = localName;
            if ( namespaceURI != null ) {
                String prefix;
                prefix = getPrefix( namespaceURI );
                if ( prefix.length() > 0 )
                    rawName = prefix + ":" + localName;
            }
            addNSAttr = true;
        }
        if ( namespaceURI == null )
            htmlName = rawName;
        else {
            if ( namespaceURI.equals( XHTMLNamespace ) )
                htmlName = localName;
            else
                htmlName = null;
        }
        
        // XHTML: element names are lower case, DOM will be different
        _printer.printText( '<' );
        if ( _xhtml )
            _printer.printText( rawName.toLowerCase() );
        else
            _printer.printText( rawName );
        _printer.indent();
        
        // For each attribute serialize it's name and value as one part,
        // separated with a space so the element can be broken on
        // multiple lines.
        if ( attrs != null ) {
            for ( i = 0 ; i < attrs.getLength() ; ++i ) {
                _printer.printSpace();
                name = attrs.getQName( i ).toLowerCase();;
                value = attrs.getValue( i );
                if ( _xhtml || namespaceURI != null ) {
                    // XHTML: print empty string for null values.
                    if ( value == null ) {
                        _printer.printText( name );
                        _printer.printText( "=\"\"" );
                    } else {
                        _printer.printText( name );
                        _printer.printText( "=\"" );
                        printEscaped( value );
                        _printer.printText( '"' );
                    }
                } else {
                    // HTML: Empty values print as attribute name, no value.
                    // HTML: URI attributes will print unescaped
                    if ( value == null || value.length() == 0 )
                        _printer.printText( name );
                    else if ( PalmHTMLdtd.isURI( rawName, name ) ) {
                        _printer.printText( name );
                        _printer.printText( "=\"" ); 
                        _printer.printText( escapeURI( value ) );
                        _printer.printText( '"' );
                    } else if ( PalmHTMLdtd.isBoolean( rawName, name ) )
                        _printer.printText( name );
                    else {
                        _printer.printText( name );
                        _printer.printText( "=\"" );
                        printEscaped( value );
                        _printer.printText( '"' );
                    }
                }
            }
        }
        if ( htmlName != null && PalmHTMLdtd.isPreserveSpace( htmlName ) )
            preserveSpace = true;
        
        if ( addNSAttr ) {
            Enumeration enum;
            
            enum = _prefixes.keys();
            while ( enum.hasMoreElements() ) {
                _printer.printSpace();
                value = (String) enum.nextElement();
                name = (String) _prefixes.get( value );
                if ( name.length() == 0 ) {
                    _printer.printText( "xmlns=\"" );
                    printEscaped( value );
                    _printer.printText( '"' );
                } else {
                    _printer.printText( "xmlns:" );
                    _printer.printText( name );
                    _printer.printText( "=\"" );
                    printEscaped( value );
                    _printer.printText( '"' );
                }
            }
        }
        
        // Now it's time to enter a new element state
        // with the tag name and space preserving.
        // We still do not change the curent element state.
        state = enterElementState( namespaceURI, localName, rawName, preserveSpace );
        
        // Prevents line breaks inside A/TD
        
        if ( htmlName != null && ( htmlName.equalsIgnoreCase( "A" ) ||
                                   htmlName.equalsIgnoreCase( "TD" ) ) ) {
            state.empty = false;
            _printer.printText( '>' );
        }
        
        // Handle SCRIPT and STYLE specifically by changing the
        // state of the current element to CDATA (XHTML) or
        // unescaped (HTML).
        if ( htmlName != null && ( rawName.equalsIgnoreCase( "SCRIPT" ) ||
                                   rawName.equalsIgnoreCase( "STYLE" ) ) ) {
            if ( _xhtml ) {
                // XHTML: Print contents as CDATA section
                state.doCData = true;
            } else {
                // HTML: Print contents unescaped
                state.unescaped = true;
            }
        }
    }
    
    
    public void endElement( String namespaceURI, String localName,
                            String rawName )
    {
        ElementState state;
        String       htmlName;
        
        // Works much like content() with additions for closing
        // an element. Note the different checks for the closed
        // element's state and the parent element's state.
        _printer.unindent();
        state = getElementState();
        
        if ( state.namespaceURI == null )
            htmlName = state.rawName;
        else {
            if ( state.namespaceURI.equals( XHTMLNamespace ) )
                htmlName = state.localName;
            else
                htmlName = null;
        }
        
        if ( _xhtml) {
            if ( state.empty ) {
                _printer.printText( " />" );
            } else {
                // Must leave CData section first
                if ( state.inCData )
                    _printer.printText( "]]>" );
                // XHTML: element names are lower case, DOM will be different
                _printer.printText( "</" );
                _printer.printText( state.rawName.toLowerCase() );
                _printer.printText( '>' );
            }
        } else {
            if ( state.empty )
                _printer.printText( '>' );
            // This element is not empty and that last content was
            // another element, so print a line break before that
            // last element and this element's closing tag.
            // [keith] Provided this is not an anchor.
            // HTML: some elements do not print closing tag (e.g. LI)
            if ( htmlName == null || ! PalmHTMLdtd.isOnlyOpening( htmlName ) ) {
                if ( _indenting && ! state.preserveSpace && state.afterElement )
                    _printer.breakLine();
                // Must leave CData section first (Illegal in HTML, but still)
                if ( state.inCData )
                    _printer.printText( "]]>" );
                _printer.printText( "</" );
                _printer.printText( state.rawName );
                _printer.printText( '>' );
            }
        }
        // Leave the element state and update that of the parent
        // (if we're not root) to not empty and after element.
        state = leaveElementState();
        // Temporary hack to prevent line breaks inside A/TD
        if ( htmlName == null || ( ! htmlName.equalsIgnoreCase( "A" ) &&
                                   ! htmlName.equalsIgnoreCase( "TD" ) ) )
            
            state.afterElement = true;
        state.empty = false;
        if ( isDocumentState() )
            _printer.flush();
    }
    
    
    //------------------------------------------//
    // SAX document handler serializing methods //
    //------------------------------------------//


    public void characters( char[] chars, int start, int length )
    {
        ElementState state;
        
        // HTML: no CDATA section
        state = content();
        state.doCData = false;
        super.characters( chars, start, length );
    }
    
    
    public void startDocument()
    {
        // Do nothing for HTML/XHTML, browser might not respond
        // well to <?xml ...?>
        if ( _printer == null )
            throw new IllegalStateException( "SER002 No writer supplied for serializer" );
    }
    
    
    public void startElement( String tagName, AttributeList attrs )
    {
        int          i;
        boolean      preserveSpace;
        ElementState state;
        String       name;
        String       value;
        
        if ( _printer == null )
            throw new IllegalStateException( "SER002 No writer supplied for serializer" );
        
        state = getElementState();
        if ( isDocumentState() ) {
            // If this is the root element handle it differently.
            // If the first root element in the document, serialize
            // the document's DOCTYPE. Space preserving defaults
            // to that of the output format.
            if ( ! _started )
                startDocument( tagName );
        } else {
            // For any other element, if first in parent, then
            // close parent's opening tag and use the parnet's
            // space preserving.
            if ( state.empty )
                _printer.printText( '>' );
            // Indent this element on a new line if the first
            // content of the parent element or immediately
            // following an element.
            if ( _indenting && ! state.preserveSpace &&
                 ( state.empty || state.afterElement ) )
                _printer.breakLine();
        }
        preserveSpace = state.preserveSpace;

        // Do not change the current element state yet.
        // This only happens in endElement().
        
        // XHTML: element names are lower case, DOM will be different
        _printer.printText( '<' );
        if ( _xhtml )
            _printer.printText( tagName.toLowerCase() );
        else
            _printer.printText( tagName );
        _printer.indent();
        
        // For each attribute serialize it's name and value as one part,
        // separated with a space so the element can be broken on
        // multiple lines.
        if ( attrs != null ) {
            for ( i = 0 ; i < attrs.getLength() ; ++i ) {
                _printer.printSpace();
                name = attrs.getName( i ).toLowerCase();;
                value = attrs.getValue( i );
                if ( _xhtml ) {
                    // XHTML: print empty string for null values.
                    if ( value == null ) {
                        _printer.printText( name );
                        _printer.printText( "=\"\"" );
                    } else {
                        _printer.printText( name );
                        _printer.printText( "=\"" ); 
                        printEscaped( value );
                        _printer.printText( '"' );
                    }
                } else {
                    // HTML: Empty values print as attribute name, no value.
                    // HTML: URI attributes will print unescaped
                    if ( value == null || value.length() == 0 )
                        _printer.printText( name );
                    else if ( PalmHTMLdtd.isURI( tagName, name ) ) {
                        _printer.printText( name );
                        _printer.printText( "=\"" ); 
                        _printer.printText( escapeURI( value ) );
                        _printer.printText( '"' );
                    } else if ( PalmHTMLdtd.isBoolean( tagName, name ) )
                        _printer.printText( name );
                    else {
                        _printer.printText( name );
                        _printer.printText( "=\"" ); 
                        printEscaped( value );
                        _printer.printText( '"' );
                    }
                }
            }
        }
        if ( PalmHTMLdtd.isPreserveSpace( tagName ) )
            preserveSpace = true;
        
        // Now it's time to enter a new element state
        // with the tag name and space preserving.
        // We still do not change the curent element state.
        state = enterElementState( null, null, tagName, preserveSpace );
        
        // Prevents line breaks inside A/TD
        if ( tagName.equalsIgnoreCase( "A" ) || tagName.equalsIgnoreCase( "TD" ) ) {
            state.empty = false;
            _printer.printText( '>' );
        }
        
        // Handle SCRIPT and STYLE specifically by changing the
        // state of the current element to CDATA (XHTML) or
        // unescaped (HTML).
        if ( tagName.equalsIgnoreCase( "SCRIPT" ) ||
             tagName.equalsIgnoreCase( "STYLE" ) ) {
            if ( _xhtml ) {
                // XHTML: Print contents as CDATA section
                state.doCData = true;
            } else {
                // HTML: Print contents unescaped
                state.unescaped = true;
            }
        }
    }
    
    
    public void endElement( String tagName )
    {
        endElement( null, null, tagName );
    }


    //------------------------------------------//
    // Generic node serializing methods methods //
    //------------------------------------------//


    /**
     * Called to serialize the document's DOCTYPE by the root element.
     * The document type declaration must name the root element,
     * but the root element is only known when that element is serialized,
     * and not at the start of the document.
     * <p>
     * This method will check if it has not been called before ({@link #_started}),
     * will serialize the document type declaration, and will serialize all
     * pre-root comments and PIs that were accumulated in the document
     * (see {@link #serializePreRoot}). Pre-root will be serialized even if
     * this is not the first root element of the document.
     */
    protected void startDocument( String rootTagName )
    {
        StringBuffer buffer;
        
        // Not supported in HTML/XHTML, but we still have to switch
        // out of DTD mode.
        _printer.leaveDTD();
        if ( ! _started ) {
            // If the public and system identifiers were not specified
            // in the output format, use the appropriate ones for HTML
            // or XHTML.
            if ( _docTypePublicId == null && _docTypeSystemId == null ) {
                if ( _xhtml ) {
                    _docTypePublicId = OutputFormat.DTD.XHTMLPublicId;
                    _docTypeSystemId = OutputFormat.DTD.XHTMLSystemId;
                } else {
                    _docTypePublicId = OutputFormat.DTD.HTMLPublicId;
                    _docTypeSystemId = OutputFormat.DTD.HTMLSystemId;
                }
            }

            // XHTML: If public idnentifier and system identifier
            //  specified, print them, else print just system identifier
            // HTML: If public identifier specified, print it with
            //  system identifier, if specified.
            if ( _docTypePublicId != null && ( ! _xhtml || _docTypeSystemId != null )  ) {
                _printer.printText( "<!DOCTYPE HTML PUBLIC " );
                printDoctypeURL( _docTypePublicId );
                if ( _docTypeSystemId != null ) {
                    if ( _indenting ) {
                        _printer.breakLine();
                        _printer.printText( "                      " );
                    } else
                        _printer.printText( ' ' );
                    printDoctypeURL( _docTypeSystemId );
                }
                _printer.printText( '>' );
                _printer.breakLine();
            } else if ( _docTypeSystemId != null ) {
                _printer.printText( "<!DOCTYPE HTML SYSTEM " );
                printDoctypeURL( _docTypeSystemId );
                _printer.printText( '>' );
                _printer.breakLine();
            }
        }
        
        _started = true;
        // Always serialize these, even if not te first root element.
        serializePreRoot();
    }


    /**
     * Called to serialize a DOM element. Equivalent to calling {@link
     * #startElement}, {@link #endElement} and serializing everything
     * inbetween, but better optimized.
     */
    protected void serializeElement( Element elem )
    {
        Attr         attr;
        NamedNodeMap attrMap;
        int          i;
        Node         child;
        ElementState state;
        boolean      preserveSpace;
        String       name;
        String       value;
        String       tagName;
        
        tagName = elem.getTagName();
        state = getElementState();
        if ( isDocumentState() ) {
            // If this is the root element handle it differently.
            // If the first root element in the document, serialize
            // the document's DOCTYPE. Space preserving defaults
            // to that of the output format.
            if ( ! _started )
                startDocument( tagName );
        } else {
            // For any other element, if first in parent, then
            // close parent's opening tag and use the parnet's
            // space preserving.
            if ( state.empty )
                _printer.printText( '>' );
            // Indent this element on a new line if the first
            // content of the parent element or immediately
            // following an element.
            if ( _indenting && ! state.preserveSpace &&
                 ( state.empty || state.afterElement ) )
                _printer.breakLine();
        }
        preserveSpace = state.preserveSpace;

        // Do not change the current element state yet.
        // This only happens in endElement().
        
        // XHTML: element names are lower case, DOM will be different
        _printer.printText( '<' );
        if ( _xhtml )
            _printer.printText( tagName.toLowerCase() );
        else
            _printer.printText( tagName );
        _printer.indent();
        
        // Lookup the element's attribute, but only print specified
        // attributes. (Unspecified attributes are derived from the DTD.
        // For each attribute print it's name and value as one part,
        // separated with a space so the element can be broken on
        // multiple lines.
        attrMap = elem.getAttributes();
        if ( attrMap != null ) {
            for ( i = 0 ; i < attrMap.getLength() ; ++i ) {
                attr = (Attr) attrMap.item( i );
                name = attr.getName().toLowerCase();
                value = attr.getValue();
                if ( attr.getSpecified() ) {
                    _printer.printSpace();
                    if ( _xhtml ) {
                        // XHTML: print empty string for null values.
                        if ( value == null ) {
                            _printer.printText( name );
                            _printer.printText( "=\"\"" );
                        } else {
                            _printer.printText( name );
                            _printer.printText( "=\"" );
                            printEscaped( value );
                            _printer.printText( '"' );
                        }
                    } else {
                        // HTML: Empty values print as attribute name, no value.
                        // HTML: URI attributes will print unescaped
                        if ( value == null || value.length() == 0 )
                            _printer.printText( name );
                        else if ( PalmHTMLdtd.isURI( tagName, name ) ) {
                            _printer.printText( name );
                            _printer.printText( "=\"" );
                            _printer.printText( escapeURI( value ) );
                            _printer.printText( '"' );
                        } else if ( PalmHTMLdtd.isBoolean( tagName, name ) )
                            _printer.printText( name );
                        else {
                            _printer.printText( name );
                            _printer.printText( "=\"" );
                            printEscaped( value );
                            _printer.printText( '"' );
                        }
                    }
                }
            }
        }
        if ( PalmHTMLdtd.isPreserveSpace( tagName ) )
            preserveSpace = true;
        
        // If element has children, or if element is not an empty tag,
        // serialize an opening tag.
        if ( elem.hasChildNodes() || ! PalmHTMLdtd.isEmptyTag( tagName ) ) {
            // Enter an element state, and serialize the children
            // one by one. Finally, end the element.
            state = enterElementState( null, null, tagName, preserveSpace );
            
            // Prevents line breaks inside A/TD
            if ( tagName.equalsIgnoreCase( "A" ) || tagName.equalsIgnoreCase( "TD" ) ) {
                state.empty = false;
                _printer.printText( '>' );
            }
            
            // Handle SCRIPT and STYLE specifically by changing the
            // state of the current element to CDATA (XHTML) or
            // unescaped (HTML).
            if ( tagName.equalsIgnoreCase( "SCRIPT" ) ||
                 tagName.equalsIgnoreCase( "STYLE" ) ) {
                if ( _xhtml ) {
                    // XHTML: Print contents as CDATA section
                    state.doCData = true;
                } else {
                    // HTML: Print contents unescaped
                    state.unescaped = true;
                }
            }
            child = elem.getFirstChild();
            while ( child != null ) {
                serializeNode( child );
                child = child.getNextSibling();
            }
            endElement( null, null, tagName );
        } else {
            _printer.unindent();
            // XHTML: Close empty tag with ' />' so it's XML and HTML compatible.
            // HTML: Empty tags are defined as such in DTD no in document.
            if ( _xhtml )
                _printer.printText( " />" );
            else
                _printer.printText( '>' );
            // After element but parent element is no longer empty.
            state.afterElement = true;
            state.empty = false;
            if ( isDocumentState() )
                _printer.flush();
        }
    }



    protected void characters( String text )
    {
        ElementState state;
        
        // HTML: no CDATA section
        state = content();
        state.doCData = false;
        super.characters( text );
    }
    
    
    protected String getEntityRef( char ch )
    {
        return PalmHTMLdtd.fromChar( ch );
    }


    protected String escapeURI( String uri )
    {
        int index;
        
        // XXX  Apparently Netscape doesn't like if we escape the URI
        //      using %nn, so we leave it as is, just remove any quotes.
        index = uri.indexOf( "\"" );
        if ( index >= 0 )
            return uri.substring( 0, index );
        else
            return uri;
    }


}




