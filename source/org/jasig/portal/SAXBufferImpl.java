/**
 * Copyright (c) 2000 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal;

import org.xml.sax.DocumentHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.SAXException;
import java.util.Vector;
import java.util.Enumeration;

/**
 * SAXBufferImpl acts like a SAXFilter, but it can also buffer
 * SAX events to be released at a later time.
 * @author Peter Kharchenko
 * @version $Revision$
 */
public class SAXBufferImpl implements DocumentHandler, LexicalHandler
{
    protected DocumentHandler outDocumentHandler;
    protected LexicalHandler outLexicalHandler;

    protected Vector eventTypes;
    protected Vector eventArguments;
    
    protected boolean buffering;
    
    // types of SAX events
    public static final int STARTDOCUMENT = 0;
    public static final int ENDDOCUMENT = 1;
    public static final int STARTELEMENT = 2;
    public static final int ENDELEMENT = 3;
    public static final int CHARACTERS = 4;
    public static final int IGNORABLEWHITESPACE = 5;
    public static final int PROCESSINGINSTRUCTION = 6;
    public static final int SETDOCUMENTLOCATOR = 7;
    public static final int STARTDTD = 8;
    public static final int ENDDTD = 9;
    public static final int STARTENTITY = 10;
    public static final int ENDENTITY = 11;
    public static final int STARTCDATA = 12;
    public static final int ENDCDATA = 13;
    public static final int COMMENT = 14;


  /**
   * Empty constructor
   */
  public SAXBufferImpl ()
  {
    buffering = true;
    eventTypes = new Vector ();
    eventArguments = new Vector ();
  }

  /**
   * Constructor with a defined DocumentHandler.
   * @param handler output DocumentHandler
   */
  public SAXBufferImpl (DocumentHandler handler)
  {
    buffering = false;
    this.outDocumentHandler = handler;
    if(handler instanceof LexicalHandler) 
	this.outLexicalHandler=(LexicalHandler) handler;
  }

  public SAXBufferImpl (DocumentHandler handler, boolean bufferSetting)
  {
    this (handler);
    this.buffering = bufferSetting;
  }

  public synchronized void startBuffering ()
  {
    buffering=true;
  }

  /**
   * Equivalent to sequential call to setDocumentHander(dh) and stopBuffering()
   */
  public synchronized void outputBuffer (DocumentHandler dh) throws SAXException
  {
    this.setDocumentHandler (dh);
    this.stopBuffering ();
  }

  public synchronized void stopBuffering () throws SAXException
  {
    // unqueue all of the buffered events
    if (outDocumentHandler != null)
    {
      Enumeration args = eventArguments.elements ();
      
      for (Enumeration types = eventTypes.elements (); types.hasMoreElements () ;)
      {
        int type = ((Integer)types.nextElement ()).intValue ();

        switch (type) {
	case STARTDOCUMENT:
            outDocumentHandler.startDocument ();
            break;
	case ENDDOCUMENT:
            outDocumentHandler.endDocument ();
            break;
	case STARTELEMENT:
            StartElementData sed = (StartElementData) args.nextElement ();
            outDocumentHandler.startElement (sed.getName (), sed.getAtts ());
            break;
	case ENDELEMENT:
            String elname = (String) args.nextElement ();
            outDocumentHandler.endElement (elname);
            break;
	case CHARACTERS:
            CharactersData cd = (CharactersData) args.nextElement ();
            outDocumentHandler.characters (cd.getCh (), cd.getStart (), cd.getLength ());
            break;
	case IGNORABLEWHITESPACE:
            CharactersData ws = (CharactersData) args.nextElement ();
            outDocumentHandler.ignorableWhitespace (ws.getCh (), ws.getStart (), ws.getLength ());
            break;
	case PROCESSINGINSTRUCTION:
            ProcessingInstructionData pid = (ProcessingInstructionData) args.nextElement ();
            outDocumentHandler.processingInstruction (pid.getTarget (), pid.getData ());
            break;
	case SETDOCUMENTLOCATOR:
            org.xml.sax.Locator loc = (org.xml.sax.Locator) args.nextElement ();
            outDocumentHandler.setDocumentLocator (loc);
            break;
	case STARTDTD:
	    if(outLexicalHandler!=null) {
		StartDTDData dd=(StartDTDData) args.nextElement();
		outLexicalHandler.startDTD(dd.getName(),dd.getPublicId(),dd.getSystemId());
	    } else 
		Logger.log(Logger.WARN,"SAXBufferImpl.stopBuffering() : trying to output lexical events while LexicalHandler is null");
	    break;
	case ENDDTD:
	    if(outLexicalHandler!=null) {
		outLexicalHandler.endDTD();
	    } else 
		Logger.log(Logger.WARN,"SAXBufferImpl.stopBuffering() : trying to output lexical events while LexicalHandler is null");
	    break;
	case STARTENTITY:
	    if(outLexicalHandler!=null) {
		String n=(String) args.nextElement();
		outLexicalHandler.startEntity(n);
	    } else 
		Logger.log(Logger.WARN,"SAXBufferImpl.stopBuffering() : trying to output lexical events while LexicalHandler is null");
	    break;
	case ENDENTITY:
	    if(outLexicalHandler!=null) {
		String n=(String) args.nextElement();
		outLexicalHandler.endEntity(n);
	    } else 
		Logger.log(Logger.WARN,"SAXBufferImpl.stopBuffering() : trying to output lexical events while LexicalHandler is null");
	    break;
	case STARTCDATA:
	    if(outLexicalHandler!=null) {
		outLexicalHandler.startCDATA();
	    } else 
		Logger.log(Logger.WARN,"SAXBufferImpl.stopBuffering() : trying to output lexical events while LexicalHandler is null");
	    break;
	case ENDCDATA:
	    if(outLexicalHandler!=null) {
		outLexicalHandler.endCDATA();
	    } else 
		Logger.log(Logger.WARN,"SAXBufferImpl.stopBuffering() : trying to output lexical events while LexicalHandler is null");
	    break;
	case COMMENT:
	    if(outLexicalHandler!=null) {
		CharactersData ccd = (CharactersData) args.nextElement ();
		outLexicalHandler.comment (ccd.getCh (), ccd.getStart (), ccd.getLength ());
	    } else 
		Logger.log(Logger.WARN,"SAXBufferImpl.stopBuffering() : trying to output lexical events while LexicalHandler is null");
	    break;		
        }
      }
    } 
    else 
      Logger.log (Logger.ERROR, "SAXBufferImpl:stopBuffering() : trying to ouput buffer to a null DocumentHandler.");

    buffering = false;
  }

  public DocumentHandler getDocumentHandler ()
  { 
    return outDocumentHandler; 
  }

  public void setDocumentHandler (DocumentHandler handler)
  {
    this.outDocumentHandler=handler;
    if(handler instanceof LexicalHandler) 
	this.outLexicalHandler=(LexicalHandler) handler;
  }

    public LexicalHandler getLexicalHandler() { return outLexicalHandler; }
    
    public void setLexicalHandler(LexicalHandler handler) {
	this.outLexicalHandler=handler;
    }

  public void characters (char ch[], int start, int length) throws SAXException
  {
      if (buffering) {
	  eventTypes.add (new Integer (CHARACTERS));
	  eventArguments.add (new CharactersData (ch, start, length));
      } else {
	  if (outDocumentHandler != null) {
	      outDocumentHandler.characters (ch, start, length);
	  }
      }
  }

  public void startDocument () throws SAXException
  {
    if (buffering)
    {
      eventTypes.add (new Integer (STARTDOCUMENT));
    }
    else
    {
      if (outDocumentHandler != null)
      {
        outDocumentHandler.startDocument ();
      }
    }
  }

  public void endDocument () throws SAXException
  {
    if (buffering)
    {
      eventTypes.add (new Integer (ENDDOCUMENT));
    } 
    else
    {
      if (outDocumentHandler != null)
        outDocumentHandler.endDocument ();
    }
  }

  public void startElement (java.lang.String name, org.xml.sax.AttributeList atts) throws SAXException
  {
    if (buffering)
    {
      eventTypes.add (new Integer (STARTELEMENT));
      eventArguments.add (new StartElementData (name, atts));
    } 
    else
    {
      if (outDocumentHandler != null)
        outDocumentHandler.startElement (name, atts);
    }
  }

  public void endElement (java.lang.String name) throws SAXException
  {
    if (buffering)
    {
      eventTypes.add (new Integer (ENDELEMENT));
      eventArguments.add (new String(name));
    } 
    else
    {
      if (outDocumentHandler != null)
        outDocumentHandler.endElement (name);
    }
  }

  public void ignorableWhitespace (char[] ch, int start, int length) throws SAXException
  {
    if (buffering)
    {
      eventTypes.add (new Integer (IGNORABLEWHITESPACE));
      eventArguments.add (new CharactersData (ch, start, length));
    } 
    else
    {
      if (outDocumentHandler != null)
        outDocumentHandler.ignorableWhitespace (ch, start, length);
    }
  }

  public void processingInstruction (java.lang.String target, java.lang.String data) throws SAXException
  {
    if (buffering)
    {
      eventTypes.add (new Integer (PROCESSINGINSTRUCTION));
      eventArguments.add (new ProcessingInstructionData (target, data));
    } 
    else
    {
      if (outDocumentHandler != null)
        outDocumentHandler.processingInstruction (target, data);
    }
  }

  public void setDocumentLocator (org.xml.sax.Locator locator)
  {
    if (buffering)
    {
      eventTypes.add (new Integer (SETDOCUMENTLOCATOR));
      eventArguments.add (locator);
    } 
    else
    {
      if (outDocumentHandler != null)
         outDocumentHandler.setDocumentLocator (locator);
    }
  }


    public  void startDTD (String name, String publicId, String systemId) throws SAXException  {
	if (buffering) {
	    eventTypes.add(new Integer(STARTDTD));
	    eventArguments.add(new StartDTDData(name,publicId,systemId));
	} else {
	    if(outLexicalHandler!=null)
		outLexicalHandler.startDTD(name,publicId,systemId);
	}
    }


    public  void endDTD () throws SAXException {
	if (buffering) {
	    eventTypes.add(new Integer(ENDDTD));
	} else {
	    if(outLexicalHandler!=null) 
		outLexicalHandler.endDTD();
	}
    }


    public  void startEntity (String name) throws SAXException {
	if (buffering) {
	    eventTypes.add(new Integer(STARTENTITY));
	    eventArguments.add(new String(name));
	} else {
	    if(outLexicalHandler!=null) 
		outLexicalHandler.startEntity(name);
	}
    }


    public  void endEntity (String name) throws SAXException {
	if (buffering) {
	    eventTypes.add(new Integer(ENDENTITY));
	    eventArguments.add(new String(name));
	} else {
	    if(outLexicalHandler!=null) 
		outLexicalHandler.endEntity(name);
	}
    }


    public  void startCDATA () throws SAXException {
	if (buffering) {
	    eventTypes.add(new Integer(STARTCDATA));
	} else {
	    if(outLexicalHandler!=null)
		outLexicalHandler.startCDATA();
	}
    }



    public  void endCDATA () throws SAXException {
	if (buffering) {
	    eventTypes.add(new Integer(ENDCDATA));
	} else {
	    if(outLexicalHandler!=null) 
		outLexicalHandler.endCDATA();
	}
    }


    public  void comment (char ch[], int start, int length) throws SAXException {
	if (buffering) {
	    eventTypes.add (new Integer (COMMENT));
	    eventArguments.add (new CharactersData (ch, start, length));
	} else {
	    if(outLexicalHandler!=null)
		outLexicalHandler.comment(ch,start,length);
	}
    }
    

  // supporting utility classes

  private class ProcessingInstructionData
  {
    public String s_target;
    public String s_data;

    ProcessingInstructionData (String target, String data)
    {
      this.s_target = target; 
      this.s_data = data;
    }

    public String getTarget () { return s_target; }
    public String getData () { return s_data; }
  }

  private class CharactersData
  {
    public char[] ca_ch;
    public int i_start;
    public int i_length;

    CharactersData (char ch[],int start, int length)
    {
      this.ca_ch=ch;
      this.i_start=start;
      this.i_length=length;
    }

    public char[] getCh () { return ca_ch; }
    public int getStart () { return i_start; }
    public int getLength () { return i_length; }
  }

  private class StartElementData
  {
    private String s_name;
    private org.xml.sax.AttributeList al_atts;

    org.xml.sax.helpers.AttributeListImpl li;

    StartElementData (String name, org.xml.sax.AttributeList atts)
    {
      this.s_name = name;
      li = new org.xml.sax.helpers.AttributeListImpl (atts);
    }
    
    public String getName () {return s_name; }
    public  org.xml.sax.AttributeList getAtts () { return li; }
  }

  private class StartDTDData
  {
      public String s_name;
      public String s_publicId;
      public String s_systemId;

    StartDTDData (String name, String publicId, String systemId)  {
	this.s_name=name;
	this.s_publicId=publicId;
	this.s_systemId=systemId;
    }

    public String getName () { return s_name; }
    public String getPublicId () { return s_publicId; }
    public String getSystemId () { return s_systemId; }
  }
}
