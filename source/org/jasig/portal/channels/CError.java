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

package org.jasig.portal.channels;

import org.xml.sax.DocumentHandler;
import org.jasig.portal.channels.BaseChannel;
import org.jasig.portal.IChannel;
import org.jasig.portal.IPrivilegedChannel;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelManager;
import org.jasig.portal.PortalException;
import org.jasig.portal.ResourceMissingException;
import org.jasig.portal.InternalTimeoutException;
import org.jasig.portal.UtilitiesBean;
import org.jasig.portal.utils.XSLT;
import org.jasig.portal.services.LogService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.net.URL;
import java.util.Hashtable;

/**
 * Error channel (aka null channel) is designed to render in
 * place of other channels when something goes wrong.<br/>
 * Possible conditions when CError is invoked are:
 * <ul>
 * <li> Channel has thrown a {@link PortalException} from one of the IChannel or IPrivilegedChannel methods.</li>
 * <li> Channel has thrown a Runtime exception from one of the methods.<li>
 * <li> Channel has timed out on rendering and was terminated.</li>
 * <li> uPortal has rejected a channel for some reason.
 * In this case a general message is constructed by the portal.</li>
 * </ul>
 * @author Peter Kharchenko, pkharchenko@interactivebusiness.com
 * @version $Revision$
 */
public class CError extends BaseChannel implements IPrivilegedChannel
{
    public static final int GENERAL_ERROR=0;
    public static final int RENDER_TIME_EXCEPTION=1;
    public static final int SET_STATIC_DATA_EXCEPTION=2;
    public static final int SET_RUNTIME_DATA_EXCEPTION=3;
    public static final int TIMEOUT_EXCEPTION=4;
    public static final int SET_PCS_EXCEPTION=5;

    protected Exception channelException=null;
    protected String str_channelID=null;
    protected String str_message=null;
    protected IChannel the_channel=null;
    protected int errorID=-1;

    private boolean showStackTrace=false;

    private PortalControlStructures portcs;
    private static final String sslLocation = UtilitiesBean.fixURI("webpages/stylesheets/org/jasig/portal/channels/CError/CError.ssl");

    public CError() {
    }
    
    public CError(int errorCode, Exception exception, String channelID,IChannel channelInstance) {
        this();
        str_channelID=channelID;
        this.channelException=exception;
        this.the_channel=channelInstance;
        this.errorID=errorCode;
    }

    public CError(int errorCode, String message,String channelID,IChannel channelInstance) {
        this();
        this.str_channelID=channelID;
        this.str_message=message;
        this.the_channel=channelInstance;
        this.errorID=errorCode;
    }

    public void setMessage(String m) {
        this.str_message=m;
    }

    private void resetCError(int errorCode, Exception exception, String channelID,IChannel channelInstance,String message) {
        str_channelID=channelID;
        this.channelException=exception;
        this.the_channel=channelInstance;
        this.errorID=errorCode;
        this.str_message=message;
    }

    public void setPortalControlStructures(PortalControlStructures pcs) {
        this.portcs=pcs;
    }

    public void renderXML(DocumentHandler out) {
	    // runtime data processing needs to be done here, otherwise replaced 
	    // channel will get duplicated setRuntimeData() calls
	    if(str_channelID!=null) {
            String chFate=runtimeData.getParameter("action");
            if(chFate!=null) {
                // a fate has been chosen
                if(chFate.equals("retry")) {
                    LogService.instance().log(LogService.DEBUG,"CError:setRuntimeData() : going for retry");
                    // clean things up for the channel
                    ChannelRuntimeData crd = (ChannelRuntimeData) runtimeData.clone();
                    crd.clear(); // Remove parameters
                    try {
                        if(the_channel instanceof IPrivilegedChannel)
                            ((IPrivilegedChannel)the_channel).setPortalControlStructures(portcs);
                        the_channel.setRuntimeData (crd);
                        ChannelManager cm=portcs.getChannelManager();
                        cm.addChannelInstance(this.str_channelID,this.the_channel);
			                  the_channel.renderXML(out);
			                  return;
                    } catch (Exception e) {
                        // if any of the above didn't work, fall back to the error channel
                        resetCError(this.SET_RUNTIME_DATA_EXCEPTION,e,this.str_channelID,this.the_channel,"Channel failed a refresh attempt.");
                    }
                } else if(chFate.equals("restart")) {
                    LogService.instance().log(LogService.DEBUG,"CError:setRuntimeData() : going for reinstantiation");
		    
                    ChannelManager cm=portcs.getChannelManager();
		    
                    ChannelRuntimeData crd = (ChannelRuntimeData) runtimeData.clone();
                    crd.clear();
                    try {
                        if((the_channel=cm.instantiateChannel(str_channelID))==null) {
                            resetCError(this.GENERAL_ERROR,null,this.str_channelID,null,"Channel failed to reinstantiate!");
                        } else {
                            try {
                                if(the_channel instanceof IPrivilegedChannel)
                                    ((IPrivilegedChannel)the_channel).setPortalControlStructures(portcs);
                                the_channel.setRuntimeData (crd);
				                        the_channel.renderXML(out);
				                        return;
                            } catch (Exception e) {
                                // if any of the above didn't work, fall back to the error channel
                                resetCError(this.SET_RUNTIME_DATA_EXCEPTION,e,this.str_channelID,this.the_channel,"Channel failed a reload attempt.");
                                cm.addChannelInstance(str_channelID,this);
                                LogService.instance().log(LogService.ERROR,"CError::setRuntimeData() : an error occurred during channel reinitialization. "+e);
                            }
                        }
                    } catch (Exception e) {
                        resetCError(this.GENERAL_ERROR,e,this.str_channelID,null,"Channel failed to reinstantiate!");
                        LogService.instance().log(LogService.ERROR,"CError::setRuntimeData() : an error occurred during channel reinstantiation. "+e);
                    }
                } else if(chFate.equals("toggle_stack_trace")) {
		                showStackTrace=!showStackTrace;
                }
            }
        }
	      // if channel's render XML method was to be called, we would've returned by now
	      localRenderXML(out);
    }
    
    private void localRenderXML(DocumentHandler out) {
        // note: this method should be made very robust. Optimally, it should
        // not rely on XSLT to do the job. That means that mime-type dependent
        // output should be generated directly within the method.
        // For now, we'll just do it the usual way.


        // XML of the following type is generated:
        // <error code="$errorID">
        //  <message>$message</messag>
        //  <channel>
        //   <id>$channelID</id>
        //   <name>$channelName</name>
        //  </channel>
        //  <exception code="N">
        //   <resource><uri/><description/></resource>
        //   <timeout value="N"/>
        //  </exception>
        // </error>
        //
        // Note that only two exceptions have detailed elements associated with them.
        // In the future, if the information within exceptions is expanded, it should be
        // placed into this XML for the CError UI to give user a proper feedback.

        Document doc = new org.apache.xerces.dom.DocumentImpl();
        Element errorEl=doc.createElement("error");
        errorEl.setAttribute("code",Integer.toString(errorID));
        if(str_message!=null) {
            Element messageEl=doc.createElement("message");
            messageEl.appendChild(doc.createTextNode(str_message));
            errorEl.appendChild(messageEl);
        }

        if(str_channelID!=null) {
            Element channelEl=doc.createElement("channel");
            Element idEl=doc.createElement("id");
            idEl.appendChild(doc.createTextNode(str_channelID));
            channelEl.appendChild(idEl);

            // determine channel name
            if(portcs!=null) {
                String chName=(portcs.getUserLayoutManager()).getNodeName(str_channelID);
                if(chName!=null) {
                    Element nameEl=doc.createElement("name");
                    nameEl.appendChild(doc.createTextNode(chName));
                    channelEl.appendChild(nameEl);
                }
                errorEl.appendChild(channelEl);
            }
        }

        // if the exception has been generated
        if(channelException!=null) {
            Element excEl=doc.createElement("exception");
            String m;
            if((m=channelException.getMessage())!=null) {
                Element emEl=doc.createElement("message");
                emEl.appendChild(doc.createTextNode(m));
                excEl.appendChild(emEl);
            }

            Element stEl=doc.createElement("stack");
            java.io.StringWriter sw=new java.io.StringWriter();
            channelException.printStackTrace(new java.io.PrintWriter(sw));
            sw.flush();
            stEl.appendChild(doc.createTextNode(sw.toString()));
            excEl.appendChild(stEl);


            if(channelException instanceof PortalException) {
                PortalException pe=(PortalException) channelException;
                // the Error channel has been invoked as a result of some other
                // channel throwing a PortalException.

                // determine which type of an exception is it
                excEl.setAttribute("code",Integer.toString(pe.getExceptionCode()));

                // now specific cases for exceptions containing additional information
                if(pe instanceof ResourceMissingException) {
                    ResourceMissingException rme=(ResourceMissingException) pe;
                    Element resourceEl=doc.createElement("resource");
                    Element uriEl=doc.createElement("uri");
                    uriEl.appendChild(doc.createTextNode(rme.getResourceURI()));
                    resourceEl.appendChild(uriEl);
                    Element descriptionEl=doc.createElement("description");
                    descriptionEl.appendChild(doc.createTextNode(rme.getResourceDescription()));
                    resourceEl.appendChild(descriptionEl);
                    excEl.appendChild(resourceEl);
                } else if(pe instanceof InternalTimeoutException) {
                    Long v=((InternalTimeoutException)pe).getTimeoutValue();
                    if(v!=null) {
                        Element timeoutEl=doc.createElement("timeout");
                        timeoutEl.setAttribute("value",v.toString());
                    }
                }
            } else {
                // runtime exception generated by the channel
                excEl.setAttribute("code","-1");
            }
            errorEl.appendChild(excEl);
        }
        doc.appendChild(errorEl);

        // figure out if we allow for refresh/reload
        String allowRef="true";
        String allowRel="true";
        if(str_channelID==null) {
            allowRel=allowRef="false";
        } else {
            if(channelException!=null && (channelException instanceof PortalException)) {
                if(errorID==SET_STATIC_DATA_EXCEPTION || !((PortalException)channelException).allowRefresh())
                    allowRef="false";
                if(!((PortalException)channelException).allowReinstantiation())
                    allowRel="false";
            }
        }

        // debug block
        try {
            java.io.StringWriter outString = new java.io.StringWriter ();
            org.apache.xml.serialize.OutputFormat format=new org.apache.xml.serialize.OutputFormat();
            format.setOmitXMLDeclaration(true);
            format.setIndenting(true);
            org.apache.xml.serialize.XMLSerializer xsl = new org.apache.xml.serialize.XMLSerializer (outString,format);
            xsl.serialize (doc);
            LogService.instance().log(LogService.DEBUG,outString.toString());
        } catch (Exception e) {
            LogService.instance().log(LogService.DEBUG,e);
        }
        // end of debug block

        try {
            XSLT xslt = new XSLT();
            xslt.setXML(doc);
            xslt.setXSL(sslLocation, runtimeData.getBrowserInfo());
            xslt.setTarget(out);
            xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
            xslt.setStylesheetParameter("showStackTrace", String.valueOf(showStackTrace));
            xslt.setStylesheetParameter("allowRefresh", allowRef);
            xslt.setStylesheetParameter("allowReinstantiation", allowRel);
            xslt.transform();
        } catch (Exception e) { 
            LogService.instance().log(LogService.ERROR, "CError::renderXML() : Things are bad. Error channel threw: " + e); 
        }
    }
}
