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

import org.xml.sax.XMLReader;
import org.xml.sax.XMLFilter;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.DTDHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.ext.LexicalHandler;
import java.util.Vector;
import java.util.Enumeration;


/**
 * A basic XML buffer implementation.
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 */

public class SAX2BufferImpl extends SAX2FilterImpl
{
    // holding vectors
    protected Vector eventTypes;
    protected Vector eventArguments;

    // control flags
    protected boolean buffering;
    protected boolean outputAtDocumentEnd;

    // types of SAX events
    public static final int STARTDOCUMENT = 0;
    public static final int ENDDOCUMENT = 1;
    public static final int STARTELEMENT = 2;
    public static final int ENDELEMENT = 3;
    public static final int CHARACTERS = 4;
    public static final int IGNORABLEWHITESPACE = 5;
    public static final int PROCESSINGINSTRUCTION = 6;
    public static final int NOTATIONDECL = 7;
    public static final int UNPARSEDENTITYDECL = 8;
    public static final int STARTPREFIXMAPPING = 9;
    public static final int ENDPREFIXMAPPING = 10;
    public static final int SKIPPEDENTITY = 11;
    public static final int WARNING = 12;
    public static final int ERROR = 13;
    public static final int FATALERROR = 14;
    public static final int COMMENT = 15;
    public static final int STARTCDATA = 16;
    public static final int ENDCDATA = 17;
    public static final int STARTDTD = 18;
    public static final int ENDDTD = 19;
    public static final int STARTENTITY = 20;
    public static final int ENDENTITY = 21;
    

    // constructors

    /**
     * Construct an empty XML buffer, with no parent.
     *
     * <p>This filter will have no parent: you must assign a parent
     * before you start a parse or do any configuration with
     * setFeature or setProperty.</p>
     * <p>By default, buffering is on, outputAtDocumentEnd is off.</p>
     *
     * @see org.xml.sax.XMLReader#setFeature
     * @see org.xml.sax.XMLReader#setProperty
     */
    public SAX2BufferImpl () {
	super();
        buffering = true;
        outputAtDocumentEnd=false;
        eventTypes = new Vector ();
        eventArguments = new Vector ();
    }


    /**
     * Construct an XML filter with the specified parent.
     * <p> Same default flag settings as with empty constructor</p>
     * @see #setParent
     * @see #getParent
     */
    public SAX2BufferImpl(XMLReader parent) {
        this();
	setParent(parent);
    }

    /**
     * Construct an XML filter with the specified children.
     * <p> Same default flag settings as with empty constructor</p>
     * @see #setContentHandler
     * @see #setDTDHandler
     * @see #setErrorHandler
     * @see #setEntityResolver
     * @see #setLexicalHandler
     */
    public SAX2BufferImpl(ContentHandler ch, EntityResolver er, ErrorHandler eh, LexicalHandler lh, DTDHandler dh) {
        super(ch,er,eh,lh,dh);
        buffering = true;
        outputAtDocumentEnd=false;
        eventTypes = new Vector ();
        eventArguments = new Vector ();        
    }

    /**
     * Construct an XML filter based on a unified handler
     * <p> Same default flag settings as with empty constructor</p>
     */
    public SAX2BufferImpl(ContentHandler ch) {
        super(ch);
        buffering = true;
        outputAtDocumentEnd=false;
        eventTypes = new Vector ();
        eventArguments = new Vector ();        
    }

    /**
     * Tells buffer to automatically output itself once
     * an end of a document is reached.
     * @param setting a <code>boolean</code> value
     */
    public void setOutputAtDocumentEnd(boolean setting) {
        this.outputAtDocumentEnd=setting;
    }

    public synchronized void clearBuffer() {
        // clean out the vectors
        eventTypes.clear();
        eventArguments.clear();
    }

    public synchronized void stopBuffering () throws SAXException
    {
        buffering = false;
    }

    public synchronized void startBuffering ()  {
        buffering=true;
    }

    public synchronized boolean isEmpty() {
        return eventTypes.isEmpty();
    }

    public synchronized void outputBuffer() throws SAXException {
        // unqueue all of the buffered events

        // for speed purposes, we don't allow contentHandler to be null
        if(contentHandler!=null) {
            Enumeration args = eventArguments.elements ();
            ThreeString ths;
            CharBlock cd;
            TwoString ts;
            FourString fs;
            SAXParseException e;
        
            for (Enumeration types = eventTypes.elements (); types.hasMoreElements ();) {
                int type = ((Integer)types.nextElement ()).intValue ();

                switch (type) {
                    // ContentHandler events
                case STARTDOCUMENT:
                    contentHandler.startDocument ();
                    break;
                case ENDDOCUMENT:
                    contentHandler.endDocument ();
                    break;
                case STARTPREFIXMAPPING:
                    ts=(TwoString) args.nextElement();
                    contentHandler.startPrefixMapping(ts.first,ts.second);
                    break;
                case ENDPREFIXMAPPING:
                    contentHandler.endPrefixMapping((String)args.nextElement());
                    break;
                case STARTELEMENT:
                    StartElementData sed = (StartElementData) args.nextElement ();
                    contentHandler.startElement (sed.getURI(), sed.getLocalName(), sed.getQName(),sed.getAtts());
                    break;
                case ENDELEMENT:
                    ths = (ThreeString) args.nextElement ();
                    contentHandler.endElement (ths.first,ths.second,ths.third);
                    break;
                case CHARACTERS:
                    cd = (CharBlock) args.nextElement ();
                    contentHandler.characters (cd.getCh(), cd.getStart(), cd.getLength());
                    break;
                case IGNORABLEWHITESPACE:
                    cd = (CharBlock) args.nextElement ();
                    contentHandler.ignorableWhitespace (cd.getCh(), cd.getStart(), cd.getLength());
                    break;
                case PROCESSINGINSTRUCTION:
                    ts=(TwoString) args.nextElement();
                    contentHandler.processingInstruction (ts.first,ts.second);
                    break;
                    
                    // DTDHandler events
                case NOTATIONDECL:
                    if(dtdHandler!=null) {
                        ths=(ThreeString) args.nextElement();
                        dtdHandler.notationDecl(ths.first,ths.second,ths.third);
                    }
                    break;
                case UNPARSEDENTITYDECL:
                    if(dtdHandler!=null) {
                        fs=(FourString) args.nextElement();
                        dtdHandler.unparsedEntityDecl(fs.first,fs.second,fs.third,fs.fourth);
                    }
                    break;

                    // ErrorHandler events
                case WARNING:
                    if(errorHandler!=null) {
                        e=(SAXParseException) args.nextElement();
                        errorHandler.warning(e);
                    }
                    break;
                case ERROR:
                    if(errorHandler!=null) {
                        e=(SAXParseException) args.nextElement();
                        errorHandler.error(e);
                    }
                    break;
                case FATALERROR:
                    if(errorHandler!=null) {
                        e=(SAXParseException) args.nextElement();
                        errorHandler.fatalError(e);
                    }
                    break;
                    
                    // LexicalHandler events
                case STARTDTD:
                    if(lexicalHandler!=null) {
                        ths=(ThreeString) args.nextElement();
                        lexicalHandler.startDTD(ths.first,ths.second,ths.third);
                    }
                    break;
                case ENDDTD:
                    if(lexicalHandler!=null) {
                        lexicalHandler.endDTD();
                    }
                    break;
                case STARTENTITY:
                    if(lexicalHandler!=null) {
                        String n=(String) args.nextElement();
                        lexicalHandler.startEntity(n);
                    } 
                    break;
                case ENDENTITY:
                    if(lexicalHandler!=null) {
                        String n=(String) args.nextElement();
                        lexicalHandler.endEntity(n);
                    } 
                    break;
                case STARTCDATA:
                    if(lexicalHandler!=null) {
                        lexicalHandler.startCDATA();
                    } 
                    break;
                case ENDCDATA:
                    if(lexicalHandler!=null) {
                        lexicalHandler.endCDATA();
                    } 
                    break;
                case COMMENT:
                    if(lexicalHandler!=null) {
                        CharBlock ccd = (CharBlock) args.nextElement ();
                        lexicalHandler.comment (ccd.getCh(), ccd.getStart(), ccd.getLength());
                    } 
                    break;
                }
            }
        } else {
            // Logger.log (Logger.ERROR, "SAX2BufferImpl:stopBuffering() : trying to ouput buffer to a null ContentHandler.");
        }
    }

    // Implementation of org.xml.sax.ext.LexicalHandler

    public void startDTD (String name, String publicId, String systemId) throws SAXException {
        if(buffering) {
            eventTypes.add(new Integer(STARTDTD));
            eventArguments.add(new ThreeString(name,publicId,systemId));
        } else if(lexicalHandler!=null) {
            lexicalHandler.startDTD(name,publicId,systemId);
        }
    }

    public void endDTD () throws SAXException {
        if(buffering) {
            eventTypes.add(new Integer(ENDDTD));
        } else if(lexicalHandler!=null) {
            lexicalHandler.endDTD();
        }
    }

    public void startEntity (String name) throws SAXException {
        if(buffering) {
            eventTypes.add(new Integer(STARTENTITY));
            eventArguments.add(name);
        } else if(lexicalHandler!=null) {
            lexicalHandler.startEntity(name);
        }
    }

    public void endEntity (String name) throws SAXException {
        if(buffering) {
            eventTypes.add(new Integer(ENDENTITY));
            eventArguments.add(name);
        } else if(lexicalHandler!=null) {
            lexicalHandler.endEntity(name);
        }
    }

    public void startCDATA () throws SAXException {
        if(buffering) {
            eventTypes.add(new Integer(STARTCDATA));
        } else if(lexicalHandler!=null) {
            lexicalHandler.startCDATA();
        }
    }

    public void endCDATA () throws SAXException {
        if(buffering) {
            eventTypes.add(new Integer(ENDCDATA));
        } else if(lexicalHandler!=null) {
            lexicalHandler.endCDATA();
        }
    }

    public void comment (char ch[], int start, int length) throws SAXException {
        if(buffering) {
            eventTypes.add(new Integer(COMMENT));
            eventArguments.add(new CharBlock(ch,start,length));
        } else if(lexicalHandler!=null) {
            lexicalHandler.comment(ch,start,length);
        }
    }

    // Implementation of org.xml.sax.DTDHandler
    
    public void notationDecl (String name, String publicId, String systemId) throws SAXException
    {
        if(buffering) {
            eventTypes.add(new Integer(NOTATIONDECL));
            eventArguments.add(new ThreeString(name,publicId,systemId));
        } else if(dtdHandler != null) {
	    dtdHandler.notationDecl(name, publicId, systemId);
	}
    }

    public void unparsedEntityDecl (String name, String publicId, String systemId, String notationName)	throws SAXException
    {
        if(buffering) {
            eventTypes.add(new Integer(UNPARSEDENTITYDECL));
            eventArguments.add(new FourString(name,publicId,systemId,notationName));
        } else if (dtdHandler != null) {
	    dtdHandler.unparsedEntityDecl(name, publicId, systemId, notationName);
	}
    }



    // Implementation of org.xml.sax.ContentHandler.

    public void setDocumentLocator (Locator locator)
    {
        this.locator = locator;
        if(!buffering) {
            if (contentHandler != null) {
                contentHandler.setDocumentLocator(locator);
            }
        }
    }

    public void startDocument () throws SAXException
    {
        if(buffering) {
            eventTypes.add(new Integer(STARTDOCUMENT));
        } else if (contentHandler != null) {
	    contentHandler.startDocument();
	}
    }

    public void endDocument ()	throws SAXException
    {
        if(buffering) {
            eventTypes.add(new Integer(ENDDOCUMENT));
            if(outputAtDocumentEnd) {
                this.stopBuffering();
                this.outputBuffer();
            }
        } else if (contentHandler != null) {
	    contentHandler.endDocument();
	}
    }

    public void startPrefixMapping (String prefix, String uri)	throws SAXException
    {
        if(buffering) {
            eventTypes.add(new Integer(STARTPREFIXMAPPING));
            eventArguments.add(new TwoString(prefix,uri));
        } else if (contentHandler != null) {
	    contentHandler.startPrefixMapping(prefix, uri);
	}
    }


    public void endPrefixMapping (String prefix) throws SAXException
    {
        if(buffering) {
            eventTypes.add(new Integer(ENDPREFIXMAPPING));
            eventArguments.add(prefix);
        } else if (contentHandler != null) {
	    contentHandler.endPrefixMapping(prefix);
	}
    }


    public void startElement (String uri, String localName, String qName, Attributes atts) throws SAXException
    {
        if(buffering) {
            eventTypes.add(new Integer(STARTELEMENT));
            eventArguments.add(new StartElementData(uri,localName,qName,atts));
        } else if (contentHandler != null) {
	    contentHandler.startElement(uri, localName, qName, atts);
	}
    }


    public void endElement (String uri, String localName, String qName)	throws SAXException
    {
        if(buffering) {
            eventTypes.add(new Integer(ENDELEMENT));
            eventArguments.add(new ThreeString(uri,localName,qName));
        } else if (contentHandler != null) {
	    contentHandler.endElement(uri, localName, qName);
	}
    }


    public void characters (char ch[], int start, int length) throws SAXException
    {
        if(buffering) {
            eventTypes.add(new Integer(CHARACTERS));
            eventArguments.add(new CharBlock(ch,start,length));
        } else if (contentHandler != null) {
	    contentHandler.characters(ch, start, length);
	}
    }


    public void ignorableWhitespace (char ch[], int start, int length)	throws SAXException
    {
        if(buffering) {
            eventTypes.add(new Integer(IGNORABLEWHITESPACE));
            eventArguments.add(new CharBlock(ch,start,length));
        } else if (contentHandler != null) {
	    contentHandler.ignorableWhitespace(ch, start, length);
	}
    }

    public void processingInstruction (String target, String data) throws SAXException
    {
        if(buffering) {
            eventTypes.add(new Integer(PROCESSINGINSTRUCTION));
            eventArguments.add(new TwoString(target,data));
        } else if (contentHandler != null) {
	    contentHandler.processingInstruction(target, data);
	}
    }

    public void skippedEntity (String name) throws SAXException
    {
        if(buffering) {
            eventTypes.add(new Integer(SKIPPEDENTITY));
            eventArguments.add(name);
        } else if (contentHandler != null) {
	    contentHandler.skippedEntity(name);
	}
    }


    // Implementation of org.xml.sax.ErrorHandler.

    public void warning (SAXParseException e)
	throws SAXException
    {
        if(buffering) {
            eventTypes.add(new Integer(WARNING));
            eventArguments.add(e);
        } else if (errorHandler != null) {
	    errorHandler.warning(e);
	}
    }

    public void error (SAXParseException e) throws SAXException
    {
        if(buffering) {
            eventTypes.add(new Integer(ERROR));
            eventArguments.add(e);
        } else if (errorHandler != null) {
	    errorHandler.error(e);
	}
    }


    public void fatalError (SAXParseException e) throws SAXException
    {
        if(buffering) {
            eventTypes.add(new Integer(FATALERROR));
            eventArguments.add(e);
        } else if (errorHandler != null) {
	    errorHandler.fatalError(e);
	}
    }


    // supporting utility classes

    private class TwoString {
        public String first;
        public String second;
        TwoString(String first, String second)   {
            this.first = first;
            this.second = second;
        }

        public String getFirst() { return this.first; }
        public String getSecond() { return this.second; }
    }

    private class ThreeString {
        public String first;
        public String second;
        public String third;
        ThreeString(String first, String second, String third)   {
            this.first=first;
            this.second=second;
            this.third=third;
        }

        public String getFirst() { return this.first; }
        public String getSecond() { return this.second; }
        public String getThird() { return this.third; }
    }

    private class FourString {
        public String first;
        public String second;
        public String third;
        public String fourth;
        FourString(String first, String second, String third, String fourth)   {
            this.first=first;
            this.second=second;
            this.third=third;
            this.fourth=fourth;
        }

        public String getFirst() { return this.first; }
        public String getSecond() { return this.second; }
        public String getThird() { return this.third; }
        public String getFourth() { return this.fourth; }
    }

    private class CharBlock  {
        public char[] ca_ch;
        public int i_start;
        public int i_length;
        
        CharBlock(char ch[],int start, int length) {
            this.ca_ch=ch;
            this.i_start=start;
            this.i_length=length;
        }

        public char[] getCh() { return ca_ch; }
        public int getStart() { return i_start; }
        public int getLength() { return i_length; }
    }

    private class StartElementData  extends ThreeString {
        public org.xml.sax.helpers.AttributesImpl ai;

        StartElementData (String uri, String localName, String qName, Attributes atts)  {
            super(uri,localName,qName);
            ai = new org.xml.sax.helpers.AttributesImpl(atts);
        }

        public String getURI () {return super.getFirst(); }
        public String getLocalName() { return super.getSecond(); }
        public String getQName() { return super.getThird(); }
        public Attributes getAtts () { return ai; }
  }
}

