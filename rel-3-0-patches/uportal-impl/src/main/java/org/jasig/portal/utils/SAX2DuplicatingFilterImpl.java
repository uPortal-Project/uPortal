/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
import org.xml.sax.SAXParseException;
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
public class SAX2DuplicatingFilterImpl extends SAX2FilterImpl {

    // Internal state
    protected EntityResolver entityResolver2 = null;
    protected DTDHandler dtdHandler2 = null;
    protected ContentHandler contentHandler2 = null;
    protected ErrorHandler errorHandler2 = null;
    protected LexicalHandler lexicalHandler2 = null;


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
    public SAX2DuplicatingFilterImpl()
    {
	super();
    }


    /**
     * Construct an XML filter with the specified parent.
     *
     * @see #setParent(XMLReader)
     * @see #getParent()
     */
    public SAX2DuplicatingFilterImpl(XMLReader parent)
    {
        super();
	setParent(parent);
    }


    /**
     * Construct an XML filter with the specified children handlers.
     * @see #setContentHandler(ContentHandler)
     * @see #setDTDHandler(DTDHandler)
     * @see #setErrorHandler(ErrorHandler)
     * @see #setEntityResolver(EntityResolver)
     * @see #setLexicalHandler(LexicalHandler)
     */
    public SAX2DuplicatingFilterImpl(ContentHandler ch, EntityResolver er, ErrorHandler eh, LexicalHandler lh, DTDHandler dh, ContentHandler ch2, EntityResolver er2, ErrorHandler eh2, LexicalHandler lh2, DTDHandler dh2) {
        super(ch,er,eh,lh,dh);
        setContentHandler2(ch2);
        setErrorHandler2(eh2);
        setDTDHandler2(dh2);
        setLexicalHandler2(lh2);
    }

    /**
     * Try to imply all of the handlers from ContentHandler alone.
     */
    public SAX2DuplicatingFilterImpl(ContentHandler ch, ContentHandler ch2) {
        super(ch);
        setAllHandlers(ch,ch2);
    }


    /**
     * Try to imply all of the handlers from ContentHandler alone.
     */
    public void setAllHandlers(ContentHandler ch, ContentHandler ch2) {
        super.setAllHandlers(ch);
        setContentHandler2(ch2);
        if(ch2 instanceof ErrorHandler) {
            setErrorHandler2((ErrorHandler)ch2);
        }
        if(ch2 instanceof DTDHandler) {
            setDTDHandler2((DTDHandler)ch2);
        }
        if(ch2 instanceof LexicalHandler) {
            setLexicalHandler2((LexicalHandler)ch2);
        }
    }

    ////////////////////////////////////////////////////////////////////
    // Implementation of org.xml.sax.XMLReader.
    ////////////////////////////////////////////////////////////////////


    /**
     * Set the DTD event handler.
     *
     * @param handler The new DTD handler.
     * @see org.xml.sax.XMLReader#setDTDHandler
     */
    public void setDTDHandler2(DTDHandler handler)
    {
        dtdHandler2 = handler;
    }


    /**
     * Get the current DTD event handler.
     *
     * @return The current DTD handler, or null if none was set.
     * @see org.xml.sax.XMLReader#getDTDHandler
     */
    public DTDHandler getDTDHandler2()
    {
	return dtdHandler2;
    }


    /**
     * Set the content event handler.
     *
     * @param handler The new content handler.
     * @see org.xml.sax.XMLReader#setContentHandler
     */
    public void setContentHandler2(ContentHandler handler)
    {
        contentHandler2 = handler;
    }


    /**
     * Get the content event handler.
     *
     * @return The current content handler, or null if none was set.
     * @see org.xml.sax.XMLReader#getContentHandler
     */
    public ContentHandler getContentHandler2()
    {
	return contentHandler2;
    }


    /**
     * Set the error event handler.
     *
     * @param handler The new error handler.
     * @see org.xml.sax.XMLReader#setErrorHandler
     */
    public void setErrorHandler2(ErrorHandler handler)
    {
        errorHandler2 = handler;
    }

    /**
     * Set the lexical handler.
     *
     * @param handler The new lexical handler.
     */
    public void setLexicalHandler2(LexicalHandler handler) {
        this.lexicalHandler2 = handler;
        /*
        if(lexicalHandler2!=null) {
            try {
                parent.setProperty("http://xml.org/sax/handlers/LexicalHandler",this);
            } catch (SAXNotRecognizedException e1) {}
            catch (SAXNotSupportedException e2) {};
        }
        */
    }


    

    /**
     * Get the current error event handler.
     *
     * @return The current error handler, or null if none was set.
     * @see org.xml.sax.XMLReader#getErrorHandler
     */
    public ErrorHandler getErrorHandler2()
    {
	return errorHandler2;
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
        if(lexicalHandler2!=null) {
            lexicalHandler2.startDTD(name,publicId,systemId);
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
        if(lexicalHandler2!=null) {
            lexicalHandler2.endDTD();
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
        if(lexicalHandler2!=null) {
            lexicalHandler2.startEntity(name);
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
        if(lexicalHandler2!=null) {
            lexicalHandler2.endEntity(name);
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
        if(lexicalHandler2!=null) {
            lexicalHandler2.startCDATA();
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
        if(lexicalHandler2!=null) {
            lexicalHandler2.endCDATA();
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
        if(lexicalHandler2!=null) {
            lexicalHandler2.comment(ch,start,length);
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
	if (dtdHandler2 != null) {
	    dtdHandler2.notationDecl(name, publicId, systemId);
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
    public void unparsedEntityDecl (String name, String publicId,  String systemId, String notationName)
	throws SAXException
    {
	if (dtdHandler != null) {
	    dtdHandler.unparsedEntityDecl(name, publicId, systemId,  notationName);
	}
	if (dtdHandler2 != null) {
	    dtdHandler.unparsedEntityDecl(name, publicId, systemId,  notationName);
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
	if (contentHandler2 != null) {
	    contentHandler2.setDocumentLocator(locator);
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
	if (contentHandler2 != null) {
	    contentHandler2.startDocument();
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
    	if (log.isTraceEnabled()) {
    		log.trace("ending document");
    	}
    	
	if (contentHandler != null) {
	    contentHandler.endDocument();
	}
	if (contentHandler2 != null) {
	    contentHandler2.endDocument();
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
	if (contentHandler2 != null) {
	    contentHandler2.startPrefixMapping(prefix, uri);
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
	if (contentHandler2 != null) {
	    contentHandler2.endPrefixMapping(prefix);
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
	if (contentHandler2 != null) {
	    contentHandler2.startElement(uri, localName, qName, atts);
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
	if (contentHandler2 != null) {
	    contentHandler2.endElement(uri, localName, qName);
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
	if (contentHandler2 != null) {
	    contentHandler2.characters(ch, start, length);
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
	if (contentHandler2 != null) {
	    contentHandler2.ignorableWhitespace(ch, start, length);
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
	if (contentHandler2 != null) {
	    contentHandler2.processingInstruction(target, data);
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
	if (contentHandler2 != null) {
	    contentHandler2.skippedEntity(name);
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
    	log.warn("SAX2DuplicatingFilterImpl.warning()", e);
    	
	if (errorHandler != null) {
	    errorHandler.warning(e);
	}
	if (errorHandler2 != null) {
	    errorHandler2.warning(e);
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
    	log.error("SAX2DuplicatingFilterImpl.error()", e);
	if (errorHandler != null) {
	    errorHandler.error(e);
	}
	if (errorHandler2 != null) {
	    errorHandler2.error(e);
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
    	log.fatal("SAX2DuplicatingFilterImpl.fatalError()", e);
    	
	if (errorHandler != null) {
	    errorHandler.fatalError(e);
	}
	if (errorHandler2 != null) {
	    errorHandler2.fatalError(e);
	}
    }


}

