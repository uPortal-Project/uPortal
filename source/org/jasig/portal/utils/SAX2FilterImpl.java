/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
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
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal.utils;

import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

/**
 * This is a remake of <code>org.xml.sax.helpers.XMLFilterImpl</code>
 * that allows for downward chaining of LexicalHandlers, and further extensions.
 * <p>Some of the behavior is slightly different from that of hte XMLFilterImpl.
 * For example this implementation allows to set null handlers. It also redirects parent's handlers 
 * right after instantiation or setParent() invokation, not just at the parse().</p>
 * @see org.xml.sax.helpers.XMLFilterImpl
 */
public class SAX2FilterImpl implements XMLFilter, EntityResolver, DTDHandler, ContentHandler, ErrorHandler, LexicalHandler {

    // Internal state

    private XMLReader parent = null;
    protected Locator locator = null;
    protected EntityResolver entityResolver = null;
    protected DTDHandler dtdHandler = null;
    protected ContentHandler contentHandler = null;
    protected ErrorHandler errorHandler = null;
    protected LexicalHandler lexicalHandler = null;


    // Constructors

    /**
     * Construct an empty XML filter, with no parent.
     *
     * <p>This filter will have no parent: you must assign a parent
     * before you start a parse or do any configuration with
     * setFeature or setProperty.</p>
     *
     * @see org.xml.sax.XMLReader#setFeature
     * @see org.xml.sax.XMLReader#setProperty
     */
    public SAX2FilterImpl()
    {
	super();
    }


    /**
     * Construct an XML filter with the specified parent.
     *
     * @see #setParent
     * @see #getParent
     */
    public SAX2FilterImpl(XMLReader parent)
    {
        super();
	setParent(parent);
    }


    /**
     * Construct an XML filter with the specified children handlers.
     * @see #setContentHandler
     * @see #setDTDHandler
     * @see #setErrorHandler
     * @see #setEntityResolver
     * @see #setLexicalHandler
     */
    public SAX2FilterImpl(ContentHandler ch, EntityResolver er, ErrorHandler eh, LexicalHandler lh, DTDHandler dh) {
        this();
        setContentHandler(ch);
        setEntityResolver(er);
        setErrorHandler(eh);
        setDTDHandler(dh);
        setLexicalHandler(lh);
    }

    /**
     * Try to imply all of the handlers from ContentHandler alone.
     */
    public SAX2FilterImpl(ContentHandler ch) {
        this();
        setAllHandlers(ch);
    }


    /**
     * Try to imply all of the handlers from ContentHandler alone.
     */
    public void setAllHandlers (ContentHandler ch) {
        setContentHandler(ch);
        if(ch instanceof EntityResolver) {
            setEntityResolver((EntityResolver)ch);
        }
        if(ch instanceof ErrorHandler) {
            setErrorHandler((ErrorHandler)ch);
        }
        if(ch instanceof DTDHandler) {
            setDTDHandler((DTDHandler)ch);
        }
        if(ch instanceof LexicalHandler) {
            setLexicalHandler((LexicalHandler)ch);
        }
    }

    ////////////////////////////////////////////////////////////////////
    // Implementation of org.xml.sax.XMLFilter.
    ////////////////////////////////////////////////////////////////////


    /**
     * Set the parent reader.
     *
     * <p>This is the {@link org.xml.sax.XMLReader XMLReader} from which 
     * this filter will obtain its events and to which it will pass its 
     * configuration requests.  The parent may itself be another filter.</p>
     *
     * <p>If there is no parent reader set, any attempt to parse
     * or to set or get a feature or property will fail.</p>
     *
     * @param parent The parent XML reader.
     * @exception java.lang.NullPointerException If the parent is null.
     * @see #getParent
     */
    public void setParent (XMLReader parent)
    {
	if (parent == null) {
	    throw new NullPointerException("Null parent");
	}
	this.parent = parent;
        setupParse();
    }


    /**
     * Get the parent reader.
     *
     * @return The parent XML reader, or null if none is set.
     * @see #setParent
     */
    public XMLReader getParent ()
    {
	return parent;
    }



    ////////////////////////////////////////////////////////////////////
    // Implementation of org.xml.sax.XMLReader.
    ////////////////////////////////////////////////////////////////////


    /**
     * Set the state of a feature.
     *
     * <p>This will always fail if the parent is null.</p>
     *
     * @param name The feature name.
     * @param state The requested feature state.
     * @exception org.xml.sax.SAXNotRecognizedException When the
     *            XMLReader does not recognize the feature name.
     * @exception org.xml.sax.SAXNotSupportedException When the
     *            XMLReader recognizes the feature name but 
     *            cannot set the requested value.
     * @see org.xml.sax.XMLReader#setFeature
     */
    public void setFeature (String name, boolean state)
	throws SAXNotRecognizedException, SAXNotSupportedException
    {
	if (parent != null) {
	    parent.setFeature(name, state);
	} else {
	    throw new SAXNotRecognizedException("Feature: " + name);
	}
    }


    /**
     * Look up the state of a feature.
     *
     * <p>This will always fail if the parent is null.</p>
     *
     * @param name The feature name.
     * @return The current state of the feature.
     * @exception org.xml.sax.SAXNotRecognizedException When the
     *            XMLReader does not recognize the feature name.
     * @exception org.xml.sax.SAXNotSupportedException When the
     *            XMLReader recognizes the feature name but 
     *            cannot determine its state at this time.
     * @see org.xml.sax.XMLReader#getFeature
     */
    public boolean getFeature (String name)
	throws SAXNotRecognizedException, SAXNotSupportedException
    {
	if (parent != null) {
	    return parent.getFeature(name);
	} else {
	    throw new SAXNotRecognizedException("Feature: " + name);
	}
    }


    /**
     * Set the value of a property.
     *
     * <p>This will always fail if the parent is null.</p>
     *
     * @param name The property name.
     * @param state The requested property value.
     * @exception org.xml.sax.SAXNotRecognizedException When the
     *            XMLReader does not recognize the property name.
     * @exception org.xml.sax.SAXNotSupportedException When the
     *            XMLReader recognizes the property name but 
     *            cannot set the requested value.
     * @see org.xml.sax.XMLReader#setProperty
     */
    public void setProperty (String name, Object value)
	throws SAXNotRecognizedException, SAXNotSupportedException
    {
        // monitor for lexical handler property
        if(name.equals("http://xml.org/sax/handlers/LexicalHandler")) { 
            this.lexicalHandler=(LexicalHandler)value; 
            if(parent!=null) {
                parent.setProperty(name,this);
            }
        } else 	if (parent != null) {
	    parent.setProperty(name, value);
	} else {
	    throw new SAXNotRecognizedException("Property: " + name);
	}
    }


    /**
     * Look up the value of a property.
     *
     * @param name The property name.
     * @return The current value of the property.
     * @exception org.xml.sax.SAXNotRecognizedException When the
     *            XMLReader does not recognize the feature name.
     * @exception org.xml.sax.SAXNotSupportedException When the
     *            XMLReader recognizes the property name but 
     *            cannot determine its value at this time.
     * @see org.xml.sax.XMLReader#setFeature
     */
    public Object getProperty (String name)
	throws SAXNotRecognizedException, SAXNotSupportedException
    {
	if (parent != null) {
	    return parent.getProperty(name);
	} else {
	    throw new SAXNotRecognizedException("Property: " + name);
	}
    }


    /**
     * Set the entity resolver.
     *
     * @param resolver The new entity resolver.
     * @see org.xml.sax.XMLReader#setEntityResolver
     */
    public void setEntityResolver (EntityResolver resolver)
    {
        entityResolver = resolver;
    }


    /**
     * Get the current entity resolver.
     *
     * @return The current entity resolver, or null if none was set.
     * @see org.xml.sax.XMLReader#getEntityResolver
     */
    public EntityResolver getEntityResolver ()
    {
	return entityResolver;
    }


    /**
     * Set the DTD event handler.
     *
     * @param resolver The new DTD handler.
     * @see org.xml.sax.XMLReader#setDTDHandler
     */
    public void setDTDHandler (DTDHandler handler)
    {
        dtdHandler = handler;
    }


    /**
     * Get the current DTD event handler.
     *
     * @return The current DTD handler, or null if none was set.
     * @see org.xml.sax.XMLReader#getDTDHandler
     */
    public DTDHandler getDTDHandler ()
    {
	return dtdHandler;
    }


    /**
     * Set the content event handler.
     *
     * @param resolver The new content handler.
     * @see org.xml.sax.XMLReader#setContentHandler
     */
    public void setContentHandler (ContentHandler handler)
    {
        contentHandler = handler;
    }


    /**
     * Get the content event handler.
     *
     * @return The current content handler, or null if none was set.
     * @see org.xml.sax.XMLReader#getContentHandler
     */
    public ContentHandler getContentHandler ()
    {
	return contentHandler;
    }


    /**
     * Set the error event handler.
     *
     * @param handle The new error handler.
     * @see org.xml.sax.XMLReader#setErrorHandler
     */
    public void setErrorHandler (ErrorHandler handler)
    {
        errorHandler = handler;
    }

    /**
     * Set the lexical handler.
     *
     * @param handle The new lexical handler.
     */
    public void setLexicalHandler (LexicalHandler handler) {
        lexicalHandler = handler;
        if(lexicalHandler!=null && parent!=null) {
            try {
                parent.setProperty("http://xml.org/sax/handlers/LexicalHandler",this);
            } catch (SAXNotRecognizedException e1) {}
            catch (SAXNotSupportedException e2) {};
        }
    }


    

    /**
     * Get the current error event handler.
     *
     * @return The current error handler, or null if none was set.
     * @see org.xml.sax.XMLReader#getErrorHandler
     */
    public ErrorHandler getErrorHandler ()
    {
	return errorHandler;
    }


    /**
     * Parse a document.
     *
     * @param input The input source for the document entity.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @exception java.io.IOException An IO exception from the parser,
     *            possibly from a byte stream or character stream
     *            supplied by the application.
     * @see org.xml.sax.XMLReader#parse(org.xml.sax.InputSource)
     */
    public void parse (InputSource input)
	throws SAXException, IOException
    {
	setupParse();
	parent.parse(input);
    }


    /**
     * Parse a document.
     *
     * @param systemId The system identifier as a fully-qualified URI.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @exception java.io.IOException An IO exception from the parser,
     *            possibly from a byte stream or character stream
     *            supplied by the application.
     * @see org.xml.sax.XMLReader#parse(java.lang.String)
     */
    public void parse (String systemId)
	throws SAXException, IOException
    {
	parse(new InputSource(systemId));
    }


    ////////////////////////////////////////////////////////////////////
    // Implementation of org.xml.sax.ext.LexicalHandler.
    ////////////////////////////////////////////////////////////////////

    /**
     * Filter startDTD event.
     * @param name The document type name.
     * @param publicId The declared public identifier for the
     *        external DTD subset, or null if none was declared.
     * @param systemId The declared system identifier for the
     *        external DTD subset, or null if none was declared.
     * @exception SAXException The application may raise an
     *            exception.
     * @see #endDTD
     * @see #startEntity
     */
    public void startDTD (String name, String publicId, String systemId) throws SAXException {
        if(lexicalHandler!=null) {
            lexicalHandler.startDTD(name,publicId,systemId);
        }
    }


    /**
     * Filter endDTD event
     *
     * @exception SAXException The application may raise an exception.
     * @see #startDTD
     */
    public void endDTD () throws SAXException {
        if(lexicalHandler!=null) {
            lexicalHandler.endDTD();
        }
    }


    /**
     * Filter startEntity event.
     *
     * @param name The name of the entity.  If it is a parameter
     *        entity, the name will begin with '%'.
     * @exception SAXException The application may raise an exception.
     * @see #endEntity
     * @see org.xml.sax.ext.DeclHandler#internalEntityDecl
     * @see org.xml.sax.ext.DeclHandler#externalEntityDecl
     */
    public void startEntity (String name) throws SAXException {
        if(lexicalHandler!=null) {
            lexicalHandler.startEntity(name);
        }
    }


    /**
     * Filter endEntity event.
     *
     * @param name The name of the entity that is ending.
     * @exception SAXException The application may raise an exception.
     * @see #startEntity
     */
    public void endEntity (String name) throws SAXException {
        if(lexicalHandler!=null) {
            lexicalHandler.endEntity(name);
        }
    }


    /**
     * Filter startCDATA event.
     * 
     * @exception SAXException The application may raise an exception.
     * @see #endCDATA
     */
    public void startCDATA () throws SAXException {
        if(lexicalHandler!=null) {
            lexicalHandler.startCDATA();
        }
    }

    /**
     * Filter endCDATA event.
     *
     * @exception SAXException The application may raise an exception.
     * @see #startCDATA
     */
    public void endCDATA () throws SAXException {
        if(lexicalHandler!=null) {
            lexicalHandler.endCDATA();
        }
    }


    /**
     * Filter comment event.
     *
     * @param ch An array holding the characters in the comment.
     * @param start The starting position in the array.
     * @param length The number of characters to use from the array.
     * @exception SAXException The application may raise an exception.
     */
    public void comment (char ch[], int start, int length) throws SAXException {
        if(lexicalHandler!=null) {
            lexicalHandler.comment(ch,start,length);
        }
    }


    ////////////////////////////////////////////////////////////////////
    // Implementation of org.xml.sax.EntityResolver.
    ////////////////////////////////////////////////////////////////////


    /**
     * Filter an external entity resolution.
     *
     * @param publicId The entity's public identifier, or null.
     * @param systemId The entity's system identifier.
     * @return A new InputSource or null for the default.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @exception java.io.IOException The client may throw an
     *            I/O-related exception while obtaining the
     *            new InputSource.
     * @see org.xml.sax.EntityResolver#resolveEntity
     */
    public InputSource resolveEntity (String publicId, String systemId)
	throws SAXException, IOException
    {
	if (entityResolver != null) {
	    return entityResolver.resolveEntity(publicId, systemId);
	} else {
	    return null;
	}
    }



    ////////////////////////////////////////////////////////////////////
    // Implementation of org.xml.sax.DTDHandler.
    ////////////////////////////////////////////////////////////////////

    
    /**
     * Filter a notation declaration event.
     *
     * @param name The notation name.
     * @param publicId The notation's public identifier, or null.
     * @param systemId The notation's system identifier, or null.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.DTDHandler#notationDecl
     */
    public void notationDecl (String name, String publicId, String systemId)
	throws SAXException
    {
	if (dtdHandler != null) {
	    dtdHandler.notationDecl(name, publicId, systemId);
	}
    }

    
    /**
     * Filter an unparsed entity declaration event.
     *
     * @param name The entity name.
     * @param publicId The entity's public identifier, or null.
     * @param systemId The entity's system identifier, or null.
     * @param notationName The name of the associated notation.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.DTDHandler#unparsedEntityDecl
     */
    public void unparsedEntityDecl (String name, String publicId,
				    String systemId, String notationName)
	throws SAXException
    {
	if (dtdHandler != null) {
	    dtdHandler.unparsedEntityDecl(name, publicId, systemId,
					  notationName);
	}
    }



    ////////////////////////////////////////////////////////////////////
    // Implementation of org.xml.sax.ContentHandler.
    ////////////////////////////////////////////////////////////////////


    /**
     * Filter a new document locator event.
     *
     * @param locator The document locator.
     * @see org.xml.sax.ContentHandler#setDocumentLocator
     */
    public void setDocumentLocator (Locator locator)
    {
	this.locator = locator;
	if (contentHandler != null) {
	    contentHandler.setDocumentLocator(locator);
	}
    }


    /**
     * Filter a start document event.
     *
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.ContentHandler#startDocument
     */
    public void startDocument ()
	throws SAXException
    {
	if (contentHandler != null) {
	    contentHandler.startDocument();
	}
    }


    /**
     * Filter an end document event.
     *
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.ContentHandler#endDocument
     */
    public void endDocument ()
	throws SAXException
    {
	if (contentHandler != null) {
	    contentHandler.endDocument();
	}
    }


    /**
     * Filter a start Namespace prefix mapping event.
     *
     * @param prefix The Namespace prefix.
     * @param uri The Namespace URI.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.ContentHandler#startPrefixMapping
     */
    public void startPrefixMapping (String prefix, String uri)
	throws SAXException
    {
	if (contentHandler != null) {
	    contentHandler.startPrefixMapping(prefix, uri);
	}
    }


    /**
     * Filter an end Namespace prefix mapping event.
     *
     * @param prefix The Namespace prefix.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.ContentHandler#endPrefixMapping
     */
    public void endPrefixMapping (String prefix)
	throws SAXException
    {
	if (contentHandler != null) {
	    contentHandler.endPrefixMapping(prefix);
	}
    }


    /**
     * Filter a start element event.
     *
     * @param uri The element's Namespace URI, or the empty string.
     * @param localName The element's local name, or the empty string.
     * @param qName The element's qualified (prefixed) name, or the empty
     *        string.
     * @param atts The element's attributes.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.ContentHandler#startElement
     */
    public void startElement (String uri, String localName, String qName, Attributes atts)
	throws SAXException
    {
	if (contentHandler != null) {
	    contentHandler.startElement(uri, localName, qName, atts);
	}
    }


    /**
     * Filter an end element event.
     *
     * @param uri The element's Namespace URI, or the empty string.
     * @param localName The element's local name, or the empty string.
     * @param qName The element's qualified (prefixed) name, or the empty
     *        string.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.ContentHandler#endElement
     */
    public void endElement (String uri, String localName, String qName)
	throws SAXException
    {
	if (contentHandler != null) {
	    contentHandler.endElement(uri, localName, qName);
	}
    }


    /**
     * Filter a character data event.
     *
     * @param ch An array of characters.
     * @param start The starting position in the array.
     * @param length The number of characters to use from the array.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.ContentHandler#characters
     */
    public void characters (char ch[], int start, int length)
	throws SAXException
    {
	if (contentHandler != null) {
	    contentHandler.characters(ch, start, length);
	}
    }


    /**
     * Filter an ignorable whitespace event.
     *
     * @param ch An array of characters.
     * @param start The starting position in the array.
     * @param length The number of characters to use from the array.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.ContentHandler#ignorableWhitespace
     */
    public void ignorableWhitespace (char ch[], int start, int length)
	throws SAXException
    {
	if (contentHandler != null) {
	    contentHandler.ignorableWhitespace(ch, start, length);
	}
    }


    /**
     * Filter a processing instruction event.
     *
     * @param target The processing instruction target.
     * @param data The text following the target.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.ContentHandler#processingInstruction
     */
    public void processingInstruction (String target, String data)
	throws SAXException
    {
	if (contentHandler != null) {
	    contentHandler.processingInstruction(target, data);
	}
    }


    /**
     * Filter a skipped entity event.
     *
     * @param name The name of the skipped entity.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.ContentHandler#skippedEntity
     */
    public void skippedEntity (String name)
	throws SAXException
    {
	if (contentHandler != null) {
	    contentHandler.skippedEntity(name);
	}
    }



    ////////////////////////////////////////////////////////////////////
    // Implementation of org.xml.sax.ErrorHandler.
    ////////////////////////////////////////////////////////////////////


    /**
     * Filter a warning event.
     *
     * @param e The nwarning as an exception.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.ErrorHandler#warning
     */
    public void warning (SAXParseException e)
	throws SAXException
    {
	if (errorHandler != null) {
	    errorHandler.warning(e);
	}
    }


    /**
     * Filter an error event.
     *
     * @param e The error as an exception.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.ErrorHandler#error
     */
    public void error (SAXParseException e)
	throws SAXException
    {
	if (errorHandler != null) {
	    errorHandler.error(e);
	}
    }


    /**
     * Filter a fatal error event.
     *
     * @param e The error as an exception.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception during processing.
     * @see org.xml.sax.ErrorHandler#fatalError
     */
    public void fatalError (SAXParseException e)
	throws SAXException
    {
	if (errorHandler != null) {
	    errorHandler.fatalError(e);
	}
    }



    ////////////////////////////////////////////////////////////////////
    // Internal methods.
    ////////////////////////////////////////////////////////////////////


    /**
     * Set up before a parse.
     *
     * <p>Before every parse, check whether the parent is
     * non-null, and re-register the filter for all of the 
     * events.</p>
     */
    private void setupParse () 
    {
	if (parent == null) {
	    throw new NullPointerException("No parent for filter");
	}
	if(entityResolver!=null) parent.setEntityResolver(this);
	parent.setDTDHandler(this);
	parent.setContentHandler(this);
	if(errorHandler!=null) parent.setErrorHandler(this);

    }

}

