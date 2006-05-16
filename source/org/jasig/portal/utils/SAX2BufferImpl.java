/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.utils;

import java.util.Enumeration;
import java.util.Vector;

import org.jasig.portal.properties.PropertiesManager;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

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
    private static final int ISTARTDOCUMENT = 0;
    private static final int IENDDOCUMENT = 1;
    private static final int ISTARTELEMENT = 2;
    private static final int IENDELEMENT = 3;
    private static final int ICHARACTERS = 4;
    private static final int IIGNORABLEWHITESPACE = 5;
    private static final int IPROCESSINGINSTRUCTION = 6;
    private static final int INOTATIONDECL = 7;
    private static final int IUNPARSEDENTITYDECL = 8;
    private static final int ISTARTPREFIXMAPPING = 9;
    private static final int IENDPREFIXMAPPING = 10;
    private static final int ISKIPPEDENTITY = 11;
    private static final int IWARNING = 12;
    private static final int IERROR = 13;
    private static final int IFATALERROR = 14;
    private static final int ICOMMENT = 15;
    private static final int ISTARTCDATA = 16;
    private static final int IENDCDATA = 17;
    private static final int ISTARTDTD = 18;
    private static final int IENDDTD = 19;
    private static final int ISTARTENTITY = 20;
    private static final int IENDENTITY = 21;

    public static final Integer STARTDOCUMENT = new Integer(ISTARTDOCUMENT);
    public static final Integer ENDDOCUMENT = new Integer(IENDDOCUMENT);
    public static final Integer STARTELEMENT = new Integer(ISTARTELEMENT);
    public static final Integer ENDELEMENT = new Integer(IENDELEMENT);
    public static final Integer CHARACTERS = new Integer(ICHARACTERS);
    public static final Integer IGNORABLEWHITESPACE = new Integer(IIGNORABLEWHITESPACE);
    public static final Integer PROCESSINGINSTRUCTION = new Integer(IPROCESSINGINSTRUCTION);
    public static final Integer NOTATIONDECL = new Integer(INOTATIONDECL);
    public static final Integer UNPARSEDENTITYDECL = new Integer(IUNPARSEDENTITYDECL);
    public static final Integer STARTPREFIXMAPPING = new Integer(ISTARTPREFIXMAPPING);
    public static final Integer ENDPREFIXMAPPING = new Integer(IENDPREFIXMAPPING);
    public static final Integer SKIPPEDENTITY = new Integer(ISKIPPEDENTITY);
    public static final Integer WARNING = new Integer(IWARNING);
    public static final Integer ERROR = new Integer(IERROR);
    public static final Integer FATALERROR = new Integer(IFATALERROR);
    public static final Integer COMMENT = new Integer(ICOMMENT);
    public static final Integer STARTCDATA = new Integer(ISTARTCDATA);
    public static final Integer ENDCDATA = new Integer(IENDCDATA);
    public static final Integer STARTDTD = new Integer(ISTARTDTD);
    public static final Integer ENDDTD = new Integer(IENDDTD);
    public static final Integer STARTENTITY = new Integer(ISTARTENTITY);
    public static final Integer ENDENTITY = new Integer(IENDENTITY);

    protected boolean copyCharBlock
        = PropertiesManager.getPropertyAsBoolean(SAX2BufferImpl.class.getName() + ".copyCharBlock", true);

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
    public SAX2BufferImpl() {
        super();
        buffering = true;
        outputAtDocumentEnd=false;
        eventTypes = new Vector ();
        eventArguments = new Vector ();
    }

    /**
     * Construct an XML filter with the specified parent.
     * <p> Same default flag settings as with empty constructor</p>
     * @see #setParent(XMLReader)
     * @see #getParent()
     */
    public SAX2BufferImpl(XMLReader parent) {
        this();
        setParent(parent);
    }

    /**
     * Construct an XML filter with the specified children.
     * <p> Same default flag settings as with empty constructor</p>
     * @see #setContentHandler(ContentHandler)
     * @see #setDTDHandler(DTDHandler)
     * @see #setErrorHandler(ErrorHandler)
     * @see #setEntityResolver(EntityResolver)
     * @see #setLexicalHandler(LexicalHandler)
     */
    public SAX2BufferImpl(ContentHandler ch, EntityResolver er, ErrorHandler eh, LexicalHandler lh, DTDHandler dh) {
        super(ch,er,eh,lh,dh);
        buffering = true;
        outputAtDocumentEnd=false;
        eventTypes = new Vector ();
        eventArguments = new Vector ();
    }

    /**
     * Construct an XML filter based on a unified handler.
     * <p>
     * Same default flag settings as with empty constructor.
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


    /**
     * Outputs buffer's content to a current set of handlers.
     * Please note that this method is not thread-safe if
     * handlers are being reset by different threads.
     * Use the other outputBuffer(ContentHandler) method in such cases.
     *
     * @exception SAXException if an error occurs
     */
    public void outputBuffer() throws SAXException {
        // for speed purposes, we don't allow contentHandler to be null
        if(contentHandler!=null) {
            outputBuffer(contentHandler, dtdHandler, errorHandler, lexicalHandler);
        } else {
            // Logger.log (Logger.ERROR, "SAX2BufferImpl:stopBuffering() : trying to ouput buffer to a null ContentHandler.");
        }
    }


    /**
     * Outputs buffer's content to a specified content handler.
     * Please note that the content handler can also represent
     * lexical handler, dtd handler and error handler.
     * This method is thread-safe.
     *
     * @param ch a <code>ContenteHandler</code> value
     * @exception SAXException if an error occurs
     */
    public void outputBuffer(ContentHandler ch) throws SAXException {
        // unqueue all of the buffered events

        // for speed purposes, we don't allow contentHandler to be null
        if(ch!=null) {
            // determine what a given content handler represents
            DTDHandler dtdh=null;
            if(ch instanceof DTDHandler) {
                dtdh=(DTDHandler) ch;
            }

            ErrorHandler erh=null;
            if(ch instanceof ErrorHandler) {
                erh=(ErrorHandler)ch;
            }

            LexicalHandler lh=null;
            if(ch instanceof LexicalHandler) {
                lh=(LexicalHandler)ch;
            }

            outputBuffer(ch, dtdh, erh, lh);
        } else {
            // Logger.log (Logger.ERROR, "SAX2BufferImpl:stopBuffering() : trying to ouput buffer to a null ContentHandler.");
        }
    }

    private void outputBuffer(final ContentHandler ch, final DTDHandler dtdh,
      final ErrorHandler erh, final LexicalHandler lh) throws SAXException {

      final Enumeration args = eventArguments.elements ();

      for (final Enumeration types = eventTypes.elements (); types.hasMoreElements ();) {
          final int type = ((Integer)types.nextElement()).intValue();

          // ContentHandler events
          if (ISTARTELEMENT == type) {
              final StartElementData sed = (StartElementData) args.nextElement ();
              ch.startElement (sed.getURI(), sed.getLocalName(), sed.getQName(),sed.getAtts());
          } else  if (IENDELEMENT == type) {
              final ThreeString ths = (ThreeString) args.nextElement ();
              ch.endElement (ths.first,ths.second,ths.third);
          } else if (ISTARTPREFIXMAPPING == type) {
              final TwoString ts=(TwoString) args.nextElement();
              ch.startPrefixMapping(ts.first,ts.second);
          } else if (ICHARACTERS == type) {
              final CharBlock cd = (CharBlock) args.nextElement ();
              ch.characters (cd.getCh(), cd.getStart(), cd.getLength());
          } else if (ISTARTDOCUMENT == type) {
              ch.startDocument ();
          } else if (IENDDOCUMENT == type) {
              ch.endDocument ();

          } else if (IENDPREFIXMAPPING == type) {
              ch.endPrefixMapping((String)args.nextElement());
          } else if (IIGNORABLEWHITESPACE == type) {
              final CharBlock cd = (CharBlock) args.nextElement ();
              ch.ignorableWhitespace (cd.getCh(), cd.getStart(), cd.getLength());
          } else if (IPROCESSINGINSTRUCTION == type) {
              final TwoString ts=(TwoString) args.nextElement();
              ch.processingInstruction (ts.first,ts.second);
          } else {
            switch (type) {
            case INOTATIONDECL:
            case IUNPARSEDENTITYDECL:
              // DTDHandler events
              if (dtdh != null) {
                if (INOTATIONDECL == type) {
                  final ThreeString ths = (ThreeString) args.nextElement();
                  dtdh.notationDecl(ths.first, ths.second, ths.third);
                } else if (IUNPARSEDENTITYDECL == type) {
                  final FourString fs = (FourString) args.nextElement();
                  dtdh.unparsedEntityDecl(fs.first, fs.second, fs.third, fs.fourth);
                }
              }
              break;

            case IWARNING:
            case IERROR:
            case IFATALERROR:
              // ErrorHandler events
              if (erh != null) {
                final SAXParseException e = (SAXParseException) args.nextElement();;
                if (IWARNING == type) {
                  erh.warning(e);
                } else if (IERROR == type) {
                  erh.error(e);
                } else if (IFATALERROR == type) {
                  erh.fatalError(e);
                }
              }
              break;

            case ISTARTDTD:
            case IENDDTD:
            case ISTARTENTITY:
            case IENDENTITY:
            case ISTARTCDATA:
            case IENDCDATA:
            case ICOMMENT:
              // LexicalHandler events
              if (lh != null) {
                if (ISTARTDTD == type) {
                  final ThreeString ths = (ThreeString) args.nextElement();
                  lh.startDTD(ths.first, ths.second, ths.third);
                } else if (IENDDTD == type) {
                  lh.endDTD();
                } else if (ISTARTENTITY == type) {
                  final String n = (String) args.nextElement();
                  lh.startEntity(n);
                } else if (IENDENTITY == type) {
                  final String n = (String) args.nextElement();
                  lh.endEntity(n);
                } else if (ISTARTCDATA == type) {
                  lh.startCDATA();
                } else if (IENDCDATA == type) {
                  lh.endCDATA();
                } else if (ICOMMENT == type) {
                  final CharBlock ccd = (CharBlock) args.nextElement();
                  lh.comment(ccd.getCh(), ccd.getStart(), ccd.getLength());
                }
              }
              break;

             default:
               throw new RuntimeException("org.jasig.portal.utils.SAX2BufferImpl::outputBuffer: unexpected element type " +
                   type + " encountered in SAX stream");
            }
          }
      }
    }

    // Implementation of org.xml.sax.ext.LexicalHandler

    public void startDTD (String name, String publicId, String systemId) throws SAXException {
        if(buffering) {
            eventTypes.add(STARTDTD);
            eventArguments.add(new ThreeString(name,publicId,systemId));
        } else if(lexicalHandler!=null) {
            lexicalHandler.startDTD(name,publicId,systemId);
        }
    }

    public void endDTD () throws SAXException {
        if(buffering) {
            eventTypes.add(ENDDTD);
        } else if(lexicalHandler!=null) {
            lexicalHandler.endDTD();
        }
    }

    public void startEntity (String name) throws SAXException {
        if(buffering) {
            eventTypes.add(STARTENTITY);
            eventArguments.add(name);
        } else if(lexicalHandler!=null) {
            lexicalHandler.startEntity(name);
        }
    }

    public void endEntity (String name) throws SAXException {
        if(buffering) {
            eventTypes.add(ENDENTITY);
            eventArguments.add(name);
        } else if(lexicalHandler!=null) {
            lexicalHandler.endEntity(name);
        }
    }

    public void startCDATA () throws SAXException {
        if(buffering) {
            eventTypes.add(STARTCDATA);
        } else if(lexicalHandler!=null) {
            lexicalHandler.startCDATA();
        }
    }

    public void endCDATA () throws SAXException {
        if(buffering) {
            eventTypes.add(ENDCDATA);
        } else if(lexicalHandler!=null) {
            lexicalHandler.endCDATA();
        }
    }

    public void comment (char ch[], int start, int length) throws SAXException {
        if(buffering) {
            eventTypes.add(COMMENT);
            eventArguments.add(new CharBlock(ch,start,length));
        } else if(lexicalHandler!=null) {
            lexicalHandler.comment(ch,start,length);
        }
    }

    // Implementation of org.xml.sax.DTDHandler

    public void notationDecl (String name, String publicId, String systemId) throws SAXException
    {
        if(buffering) {
            eventTypes.add(NOTATIONDECL);
            eventArguments.add(new ThreeString(name,publicId,systemId));
        } else if(dtdHandler != null) {
	    dtdHandler.notationDecl(name, publicId, systemId);
	}
    }

    public void unparsedEntityDecl (String name, String publicId, String systemId, String notationName)	throws SAXException
    {
        if(buffering) {
            eventTypes.add(UNPARSEDENTITYDECL);
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
            eventTypes.add(STARTDOCUMENT);
        } else if (contentHandler != null) {
	    contentHandler.startDocument();
	}
    }

    public void endDocument ()	throws SAXException
    {
        if(buffering) {
            eventTypes.add(ENDDOCUMENT);
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
            eventTypes.add(STARTPREFIXMAPPING);
            eventArguments.add(new TwoString(prefix,uri));
        } else if (contentHandler != null) {
            contentHandler.startPrefixMapping(prefix, uri);
        }
    }


    public void endPrefixMapping (String prefix) throws SAXException
    {
        if(buffering) {
            eventTypes.add(ENDPREFIXMAPPING);
            eventArguments.add(prefix);
        } else if (contentHandler != null) {
            contentHandler.endPrefixMapping(prefix);
        }
    }


    public void startElement (String uri, String localName, String qName, Attributes atts) throws SAXException
    {
        if(buffering) {
            eventTypes.add(STARTELEMENT);
            eventArguments.add(new StartElementData(uri,localName,qName,atts));
        } else if (contentHandler != null) {
            contentHandler.startElement(uri, localName, qName, atts);
        }
    }


    public void endElement (String uri, String localName, String qName)	throws SAXException
    {
        if(buffering) {
            eventTypes.add(ENDELEMENT);
            eventArguments.add(new ThreeString(uri,localName,qName));
        } else if (contentHandler != null) {
            contentHandler.endElement(uri, localName, qName);
        }
    }


    public void characters (char ch[], int start, int length) throws SAXException
    {
        if(buffering) {
            eventTypes.add(CHARACTERS);
            eventArguments.add(new CharBlock(ch,start,length));
        } else if (contentHandler != null) {
            contentHandler.characters(ch, start, length);
        }
    }


    public void ignorableWhitespace (char ch[], int start, int length)	throws SAXException
    {
        if(buffering) {
            eventTypes.add(IGNORABLEWHITESPACE);
            eventArguments.add(new CharBlock(ch,start,length));
        } else if (contentHandler != null) {
            contentHandler.ignorableWhitespace(ch, start, length);
        }
    }

    public void processingInstruction (String target, String data) throws SAXException
    {
        if(buffering) {
            eventTypes.add(PROCESSINGINSTRUCTION);
            eventArguments.add(new TwoString(target,data));
        } else if (contentHandler != null) {
            contentHandler.processingInstruction(target, data);
        }
    }

    public void skippedEntity (String name) throws SAXException
    {
        if(buffering) {
            eventTypes.add(SKIPPEDENTITY);
            eventArguments.add(name);
        } else if (contentHandler != null) {
            contentHandler.skippedEntity(name);
        }
    }


    // Implementation of org.xml.sax.ErrorHandler.

    public void warning (SAXParseException e) throws SAXException
    {
        if(buffering) {
            eventTypes.add(WARNING);
            eventArguments.add(e);
        } else if (errorHandler != null) {
            errorHandler.warning(e);
        }
    }

    public void error (SAXParseException e) throws SAXException
    {
        if(buffering) {
            eventTypes.add(ERROR);
            eventArguments.add(e);
        } else if (errorHandler != null) {
            errorHandler.error(e);
        }
    }


    public void fatalError (SAXParseException e) throws SAXException
    {
        if(buffering) {
            eventTypes.add(FATALERROR);
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
            if (copyCharBlock) {
                this.ca_ch = new char[length+start];
                System.arraycopy(ch, 0, this.ca_ch, 0, length+start);
            } else {
                this.ca_ch = ch;
            }
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

