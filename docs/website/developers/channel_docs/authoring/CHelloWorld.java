package edu.virginia.uportal.channels.helloworld;

// A channel needs these eight classes no matter what:
import org.jasig.portal.IChannel;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.utils.XSLT;
import org.xml.sax.ContentHandler;

// This is only useful if you will be using Commons Logging (which you should!!):
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/** 
 *  A simple example channel for the uPortal.
 *  @author Owen Gunden (nog7g@virginia.edu)
 */
public class CHelloWorld implements IChannel {

  private static final int NORMAL_MODE =    42; // two of my
  private static final int ABOUT_MODE  = 31337; // favorite numbers
  private int mode; // whether we're in NORMAL_MODE or ABOUT_MODE

  private ChannelStaticData staticData;
  private ChannelRuntimeData runtimeData;
  
  private String name; // the name to say hello to
  private String name_prev; // the name that was previously submitted, to go
                            // in the text box by default.
  
  /** 
   *  Construct0r. 
   */ 
  public CHelloWorld() { 
    this.name = "World"; // default to "Hello World!"
    this.name_prev = ""; // start with the text box empty
    this.mode = NORMAL_MODE; // start in normal mode
  }
  
  //
  //  Implementing the IChannel Interface
  //

  /** 
   *  Returns channel runtime properties.
   *  Satisfies implementation of Channel Interface.
   *
   *  @return handle to runtime properties 
   */ 
  public ChannelRuntimeProperties getRuntimeProperties() { 
    return new ChannelRuntimeProperties();
  }
  
  /** 
   *  Process layout-level events coming from the portal.
   *  Satisfies implementation of IChannel Interface.
   *
   *  @param <b>PortalEvent</b> ev a portal layout event
   */
  public void receiveEvent(PortalEvent ev) {
      if (ev.getEventNumber() == PortalEvent.ABOUT_BUTTON_EVENT) {
          mode = ABOUT_MODE;
      }
  }
  
  /** 
   *  Receive static channel data from the portal.
   *  Satisfies implementation of IChannel Interface.
   *
   *  @param <b>ChannelStaticData</b> sd static channel data
   */
  public void setStaticData(ChannelStaticData sd) {
    this.staticData = sd;
  }
  
  /** 
   *  Receive channel runtime data from the portal.
   *  Satisfies implementation of IChannel Interface.
   *
   *  @param <b>ChannelRuntimeData</b> rd handle to channel runtime data
   */
  public void setRuntimeData(ChannelRuntimeData rd) {
    // Most of the processing is usually done here.
    this.runtimeData = rd;
    
    // process the form submissions
    if (runtimeData.getParameter("submit") != null) {
        name = runtimeData.getParameter("name"); 
        name_prev = name;
    } 

    if (runtimeData.getParameter("clear") != null) {
        name_prev = "";
    }

    if (runtimeData.getParameter("back") != null) {
        mode = NORMAL_MODE;
    }
  }
  
  /** Output channel content to the portal
   *  @param out a sax document handler
   */
  public void renderXML(ContentHandler out) throws PortalException {
    String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"; 
    String stylesheet = "normal"; 
    if (mode == NORMAL_MODE) {
        xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<name>"+name+"</name>";
        stylesheet = "normal";
    } else if (mode == ABOUT_MODE) {
        xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<about channel=\"Hello World\">This channel was created "
            + "for demonstrative purposes, by Owen Gunden &lt;nog7g@"
            + "virginia.edu&gt;</about>";
        stylesheet = "about";
    }

    // Create a new XSLT styling engine
    XSLT xslt = new XSLT(this);
    
    // pass the result XML to the styling engine.
    xslt.setXML(xml);
    
    // specify the stylesheet selector
    xslt.setXSL("CHelloWorld.ssl", stylesheet, runtimeData.getBrowserInfo());
    
    // set parameters that the stylesheet needs.
    xslt.setStylesheetParameter("baseActionURL",
                                    runtimeData.getBaseActionURL());
    xslt.setStylesheetParameter("name_prev", name_prev);

    
    // set the output Handler for the output.
    xslt.setTarget(out);
    
    // do the deed
    xslt.transform();
  }
}
