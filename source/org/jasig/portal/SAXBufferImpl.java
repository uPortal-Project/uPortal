/**
 * SAXBufferImpl acts like a SAXFilter, but it can also buffer
 * SAX events to be released at a later time.
 * @author Peter Kharchenko
 * @version $Revision$
 */

package org.jasig.portal;

import org.xml.sax.DocumentHandler;
import org.xml.sax.SAXException;
import java.util.Vector;
import java.util.Enumeration;


public class SAXBufferImpl implements DocumentHandler
{
    protected DocumentHandler outDocumentHandler;

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
    
    /**
     * Empty constructor
     */
    public SAXBufferImpl() {
	buffering=true;
	eventTypes=new Vector();
	eventArguments=new Vector();
    }
    
    /**
     * Constructor with a defined DocumentHandler.
     * @param handler output DocumentHandler
     */
    public SAXBufferImpl(DocumentHandler handler)  {
	buffering=false;
	this.outDocumentHandler=handler;
    }
    
    public SAXBufferImpl(DocumentHandler handler,boolean bufferSetting)  {
	this(handler);
	this.buffering=bufferSetting;
	this.outDocumentHandler=handler;
    }
    
    public synchronized void startBuffering(){ 
	buffering=true;
    }
    
    /**
     * Equivalent to sequential call to setDocumentHander(dh) and stopBuffering()
     */
    public synchronized void outputBuffer(DocumentHandler dh) throws SAXException {
	this.setDocumentHandler(dh);
	this.stopBuffering();
    }


    public synchronized void stopBuffering() throws SAXException  { 
	// unqueue all of the buffered events
	if(outDocumentHandler!=null) {
	    Enumeration args=eventArguments.elements();
	    for (Enumeration types = eventTypes.elements() ; types.hasMoreElements() ;) {
		int type= ((Integer)types.nextElement()).intValue();
		
		switch(type) {
		case STARTDOCUMENT:
		    outDocumentHandler.startDocument();
		    break;
		case ENDDOCUMENT:
		    outDocumentHandler.endDocument();
		    break;
		case STARTELEMENT:
		    StartElementData sed=(StartElementData) args.nextElement();
		    outDocumentHandler.startElement(sed.getName(),sed.getAtts());
		    break;
		case ENDELEMENT:
		    String elname=(String) args.nextElement();
		    outDocumentHandler.endElement(elname);
		    break;
		case CHARACTERS:
		    CharactersData cd=(CharactersData) args.nextElement();
		    outDocumentHandler.characters(cd.getCh(),cd.getStart(),cd.getLength());
		    break;
		case IGNORABLEWHITESPACE:
		    CharactersData ws=(CharactersData) args.nextElement();
		    outDocumentHandler.ignorableWhitespace(ws.getCh(),ws.getStart(),ws.getLength());
		    break;
		case PROCESSINGINSTRUCTION:
		    ProcessingInstructionData pid=(ProcessingInstructionData) args.nextElement();
		    outDocumentHandler.processingInstruction(pid.getTarget(),pid.getData());
		    break;
		case SETDOCUMENTLOCATOR:
		    org.xml.sax.Locator loc=(org.xml.sax.Locator) args.nextElement(); 
		    outDocumentHandler.setDocumentLocator(loc);
		    break;
		}
	    }
       
	} else Logger.log(Logger.ERROR,"SAXBufferImpl:stopBuffering() : trying to ouput buffer to a null DocumentHandler.");
	
	buffering=false; 
    }

    public DocumentHandler getDocumentHandler() { return outDocumentHandler; }
    
    public void setDocumentHandler(DocumentHandler handler)
    {
	this.outDocumentHandler=handler;
    }



    
    public void characters (char ch[], int start, int length) 
	throws SAXException
    {
	if(buffering) {
	    eventTypes.add(new Integer(CHARACTERS));
	    eventArguments.add(new CharactersData(ch,start,length));
	} else {
	    if (outDocumentHandler != null) {
		outDocumentHandler.characters(ch, start, length);
	    }
	}
    }
    
    public void startDocument ()
	throws SAXException
    {
	if(buffering) {
	    eventTypes.add(new Integer(STARTDOCUMENT));
	} else {
	    if (outDocumentHandler != null) {
		outDocumentHandler.startDocument();
	    }
	}
    }
    
    
    public void endDocument ()
	throws SAXException
    {
	if(buffering) {
	    eventTypes.add(new Integer(ENDDOCUMENT));
	} else {
	    if (outDocumentHandler != null) {
		outDocumentHandler.endDocument();
	    }
	}
    }
    
    public void startElement(java.lang.String name, org.xml.sax.AttributeList atts)
	throws SAXException
    {
	if(buffering) {
	    eventTypes.add(new Integer(STARTELEMENT));
	    eventArguments.add(new StartElementData(name,atts));
	} else {
	    if (outDocumentHandler != null) {
		outDocumentHandler.startElement(name,atts);
	    }
	}
    }
    
    public void endElement(java.lang.String name)
	throws SAXException
    {
	if(buffering) {
	    eventTypes.add(new Integer(ENDELEMENT));
	    eventArguments.add(name);
	} else {
	    if (outDocumentHandler != null) {
		outDocumentHandler.endElement(name);
	    }
	}
    }
    
    public void ignorableWhitespace(char[] ch, int start, int length)
	throws SAXException
    {
	if(buffering) {
	    eventTypes.add(new Integer(IGNORABLEWHITESPACE));
	    eventArguments.add(new CharactersData(ch,start,length));
	} else {
	    if (outDocumentHandler != null) {
		outDocumentHandler.ignorableWhitespace(ch,start,length);
	    }
	}
    } 
    
    public void processingInstruction(java.lang.String target, java.lang.String data)
	throws SAXException
    {
	if(buffering) {
	    eventTypes.add(new Integer(PROCESSINGINSTRUCTION));
	    eventArguments.add(new ProcessingInstructionData(target,data));
	} else {
	    if (outDocumentHandler != null) {
		outDocumentHandler.processingInstruction(target,data);
	    }
	}
    }
    
    public void setDocumentLocator(org.xml.sax.Locator locator)
    {
	if(buffering) {
	    eventTypes.add(new Integer(SETDOCUMENTLOCATOR));
	    eventArguments.add(locator);
	} else {
	    if (outDocumentHandler != null) {
		outDocumentHandler.setDocumentLocator(locator);
	    }
	}
    }


    // supporting utility classes

    private class ProcessingInstructionData {
	public String s_target;
	public String s_data;
	
	ProcessingInstructionData(String target, String data) {
	    this.s_target=target; this.s_data=data;
	}

	public String getTarget() { return s_target; }
	public String getData() { return s_data; }
    };

    private class CharactersData {
	public char[] ca_ch;
	public int i_start;
	public int i_length;
	
	CharactersData(char ch[],int start, int length) {
	    this.ca_ch=ch;
	    this.i_start=start;
	    this.i_length=length;
	}
	
	public char[] getCh() { return ca_ch; };
	public int getStart() { return i_start; };
	public int getLength() { return i_length; };
    };

    private class StartElementData {
	private String s_name;
	private org.xml.sax.AttributeList al_atts;

	org.xml.sax.helpers.AttributeListImpl li;

	StartElementData(String name, org.xml.sax.AttributeList atts) {
	    this.s_name=name;
	    li=new org.xml.sax.helpers.AttributeListImpl(atts);
	}
	public String getName() {return s_name; }
	public  org.xml.sax.AttributeList getAtts() { return li; }
    };
    
};
