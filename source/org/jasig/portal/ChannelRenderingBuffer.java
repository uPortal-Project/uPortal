/**
 * Channel Rendering buffer allows portal to accumulate a list
 * of all channels that will have to be rendered. This is done
 * by accumulating layout content (full page content minus content
 * provided by the channels).
 * The entire document is accumulated in a buffer. Information about
 * channel elements is passed to a ChannelManager. Once the end of the
 * document is reached, the entire buffer is released to a provided 
 * Document Handler.
 *
 * @author Peter Kharchenko
 * @version $Revision$
 */

package org.jasig.portal;

import org.xml.sax.DocumentHandler;
import org.xml.sax.SAXException;
import java.util.Hashtable;


public class ChannelRenderingBuffer extends SAXBufferImpl
{
    protected DocumentHandler out;
    protected ChannelManager cm;
    
    // information about the current channel
    private boolean insideChannelElement=false;
    private Hashtable params;
    private String channelClassName;
    private String channelID;
    private long timeOut;
    
    /**
     * Default constructor.
     * @param handler output document handler
     * @param chanman channel manager
     */
    public ChannelRenderingBuffer(DocumentHandler handler, ChannelManager chanman) {
	super();
	this.out=handler;
	this.cm=chanman;
    }
    
    public void startDocument ()
	throws SAXException
    {
	insideChannelElement=false;
	super.startDocument();

    }
    
    
    public void endDocument ()
	throws SAXException
    {
	super.endDocument();
	// at this point we can release the buffer
	this.outputBuffer(out);
    }
    
    public void startElement(java.lang.String name, org.xml.sax.AttributeList atts)
	throws SAXException
    {

	if(!insideChannelElement) {
	    // recognizing "channel"   
	    if(name.equals("channel")) {
		insideChannelElement=true;
		// get class attribute
		channelClassName=atts.getValue("class");
		channelID=atts.getValue("ID");
		timeOut=java.lang.Long.parseLong(atts.getValue("timeout"));
		params=new Hashtable();
	    } 
	} else 
	    if(name.equals("parameter")) { 
		params.put(atts.getValue("name"),atts.getValue("value")); 
	    }

	super.startElement(name,atts);
    }
    
    public void endElement(java.lang.String name)
	throws SAXException
    {
	if(insideChannelElement){
	    if(name.equals("channel")) {
		cm.startChannelRendering(channelID,channelClassName,timeOut,params);
		insideChannelElement=false;
	    }
	}
	super.endElement(name);
    }
};
