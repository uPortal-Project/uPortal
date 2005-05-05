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
import java.io.OutputStream;
import java.io.Writer;
import java.util.Enumeration;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.AttributeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/**
 * Implements an XHTML serializer supporting both DOM and SAX
 * pretty serializing. For usage instructions see either {@link
 * Serializer} or {@link BaseMarkupSerializer}.
 *
 *
 * @version $Revision$ $Date$
 * @author <a href="mailto:arkin@intalio.com">Assaf Arkin</a>
 * @author <a href="mailto:flopez@unicon.net">Freddy Lopez</a> 
 * @author <a href="mailto:bart.grebowiec@rutgers.edu">Bart Grebowiec</a> 
 *  Bart :: Simplified mechanism to support XHTML compliant markup
 * 
 * 
 * @see Serializer
 */
public class XHTMLSerializer extends BaseMarkupSerializer
{

    public static String XHTMLNamespace = "";

    /**
     * Constructs a new serializer. The serializer cannot be used without
     * calling {@link #setOutputCharStream(Writer)} or {@link #setOutputByteStream(OutputStream)}
     * first.
     */
    public XHTMLSerializer()
    {
        super(new OutputFormat( Method.XHTML, null, false ) );
    }


    /**
     * Constructs a new serializer. The serializer cannot be used without
     * calling {@link #setOutputCharStream(Writer)} or {@link #setOutputByteStream(OutputStream)}
     * first.
     */
    public XHTMLSerializer( OutputFormat format )
    {
        super(format != null ? format : new OutputFormat( Method.XHTML, null, false ) );
    }


    /**
     * Constructs a new serializer that writes to the specified writer
     * using the specified output format. If <tt>format</tt> is null,
     * will use a default output format.
     *
     * @param writer The writer to use
     * @param format The output format to use, null for the default
     */
    public XHTMLSerializer( Writer writer, OutputFormat format )
    {
        super(format != null ? format : new OutputFormat( Method.XHTML, null, false ) );
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
    public XHTMLSerializer( OutputStream output, OutputFormat format )
    {
        super(format != null ? format : new OutputFormat( Method.XHTML, null, false ) );
        setOutputByteStream( output );
    }


    public void setOutputFormat( OutputFormat format )
    {
        super.setOutputFormat( format != null ? format : new OutputFormat( Method.XHTML, null, false ) );
    }
    
    public void characters( char[] chars, int start, int length )
           throws SAXException
       {
           ElementState state;

           try {
               state = content();
               state.doCData = false;
               super.characters( chars, start, length );
           } catch ( IOException except ) {
               throw new SAXException( except );
           }
    }    

    public void startElement( String namespaceURI, String localName,
                              String rawName, Attributes attrs )
        throws SAXException
    {
        int          i;
        boolean      preserveSpace;
        ElementState state;
        String       name;
        String       value;
        String       htmlName;
        boolean      addNSAttr = false;

        try {
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
            _printer.printText( rawName.toLowerCase() );
            _printer.indent();

            // For each attribute serialize it's name and value as one part,
            // separated with a space so the element can be broken on
            // multiple lines.
            if ( attrs != null ) {
                for ( i = 0 ; i < attrs.getLength() ; ++i ) {
                    _printer.printSpace();
                    name = attrs.getQName( i ).toLowerCase();;
                    value = attrs.getValue( i );
                    // XHTML: print empty string for null values.
                    if ( value == null ) {
                        _printer.printText( name );
                        _printer.printText( "=\"\"" );
                    } else {
                        _printer.printText( name );
                        _printer.printText( "=\"" );
                        value = ProxyWriter.considerProxyRewrite(name,localName,value); 
                        value = appendAnchorIfNecessary(rawName.toLowerCase(),name,value);
                        printEscaped( value );
                        _printer.printText( '"' );
                    }
                }
            }
            if ( htmlName != null && HTMLdtd.isPreserveSpace( htmlName ) )
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
            } else {
                if (shouldNotExpandEndTagForEmptyElement(rawName)) {
                    _printer.printText(" />");
                } else {
                    _printer.printText(">");
                }
                
            }

            // Handle SCRIPT and STYLE specifically by changing the
            // state of the current element to CDATA 
            if ( htmlName != null && ( rawName.equalsIgnoreCase( "SCRIPT" ) ||
                                       rawName.equalsIgnoreCase( "STYLE" ) ) ) { 
                    // XHTML: Print contents as CDATA section
                    state.doCData = true;
            }
        } catch ( IOException except ) {
            throw new SAXException( except );
        }
    }

    public void endElementIO( String namespaceURI, String localName,
                              String rawName )
        throws IOException
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

        if ( state.inCData )
          _printer.printText( "]]>" );

        // Close all empty tags that require proper closer
        if (!shouldNotExpandEndTagForEmptyElement(state.rawName.toLowerCase())) {
            _printer.printText( "</" );
            _printer.printText( state.rawName.toLowerCase() );
            _printer.printText( ">" );
        }
        // Leave the element state and update that of the parent
        // (if we're not root) to not empty and after element.
        state = leaveElementState();
        // Prevent line breaks inside A/TD
        if ( htmlName == null || ( ! htmlName.equalsIgnoreCase( "A" ) &&
                                   ! htmlName.equalsIgnoreCase( "TD" ) ) ) {
          state.afterElement = true;
        }  

        state.empty = false;
          
        if ( isDocumentState() )
            _printer.flush();
    }

    protected void startDocument( String rootTagName )
        throws IOException
    {
        StringBuffer buffer;

        // Not supported in XHTML, but we still have to switch
        // out of DTD mode.
        _printer.leaveDTD();
        if ( ! _started ) {
            // If the public and system identifiers were not specified
            // in the output format, use the appropriate ones for XHTML.
            if ( _docTypePublicId == null && _docTypeSystemId == null ) {
                    _docTypePublicId = HTMLdtd.XHTMLPublicId;
                    _docTypeSystemId = HTMLdtd.XHTMLSystemId;
            }

            if ( ! _format.getOmitDocumentType() ) {
                // XHTML: If public idnentifier and system identifier
                //  specified, print them, else print just system identifier
                if ( _docTypePublicId != null && _docTypeSystemId != null) {
                    _printer.printText( "<!DOCTYPE html PUBLIC " );
                    printDoctypeURL( _docTypePublicId );
                    if ( _docTypeSystemId != null ) {
                        if ( _indenting ) {
                            _printer.breakLine();
                            _printer.printText( "                      " );
                        } else
                        _printer.printText( ' ' );
                        printDoctypeURL( _docTypeSystemId );
                    }
                    _printer.printText( ">" );
                    _printer.breakLine();
                } else if ( _docTypeSystemId != null ) {
                    _printer.printText( "<!DOCTYPE html SYSTEM " );
                    printDoctypeURL( _docTypeSystemId );
                    _printer.printText( ">" );
                    _printer.breakLine();
                }
            }
        }

        _started = true;
        // Always serialize these, even if not te first root element.
        serializePreRoot();
    }

    protected void serializeElement( Element elem )
        throws IOException
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
        _printer.printText( tagName.toLowerCase() );
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
                }
            }
        }
        if ( HTMLdtd.isPreserveSpace( tagName ) )
            preserveSpace = true;

        // If element has children, or if element is not an empty tag,
        // serialize an opening tag.
        if ( elem.hasChildNodes() || ! HTMLdtd.isEmptyTag( tagName ) ) {
            // Enter an element state, and serialize the children
            // one by one. Finally, end the element.
            state = enterElementState( null, null, tagName, preserveSpace );

             //          Prevents line breaks inside A/TD
             if ( tagName.equalsIgnoreCase( "A" ) || tagName.equalsIgnoreCase( "TD" ) ) {
                 state.empty = false;
                 _printer.printText( '>' );
             }

            // Handle SCRIPT and STYLE specifically by changing the
            // state of the current element to CDATA 
            if ( tagName.equalsIgnoreCase( "SCRIPT" ) ||
                 tagName.equalsIgnoreCase( "STYLE" ) ) {
                 // XHTML: Print contents as CDATA section
                 state.doCData = true;
            }
            child = elem.getFirstChild();
            while ( child != null ) {
                serializeNode( child );
                child = child.getNextSibling();
            }
            endElementIO( null, null, tagName );
        } else {
            _printer.unindent();
            // XHTML: Close empty tag with ' />' so it's XML and HTML compatible.
            _printer.printText( " />" );
            // After element but parent element is no longer empty.
            state.afterElement = true;
            state.empty = false;
            if ( isDocumentState() )
                _printer.flush();
        }
    }

    public boolean shouldNotExpandEndTagForEmptyElement(String elementName) {
        boolean aReturn = false;
        for (int i = 0; !aReturn && i < emptyElementsToNotExpand.length; i++)
          aReturn = emptyElementsToNotExpand[i].equals(elementName);
        return aReturn;
    }

    /** Array of element tag names that are expanded when empty **/
    private static final String[] emptyElementsToNotExpand = {"br","hr","area","base","basefont","col","frame","img","input","isindex","link","meta","param"};
    
    protected String getEntityRef( int ch ){
          return HTMLdtd.fromChar( ch );
    }
    
    public void startElement( String tagName, AttributeList attrs )
            throws SAXException
    {}
    
    public void endElement( String namespaceURI, String localName,
                             String rawName )
         throws SAXException
     {
         try {
             endElementIO( namespaceURI, localName, rawName );
         } catch ( IOException except ) {
             throw new SAXException( except );
         }
     }

    public void endElement( String tagName )
        throws SAXException
    {
        endElement( null, null, tagName );
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
    
}
