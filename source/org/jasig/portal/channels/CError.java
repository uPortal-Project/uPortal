/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels;

import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.AuthorizationException;
import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelManager;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.ICacheable;
import org.jasig.portal.IChannel;
import org.jasig.portal.ICharacterChannel;
import org.jasig.portal.IPrivilegedChannel;
import org.jasig.portal.InternalTimeoutException;
import org.jasig.portal.MediaManager;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalException;
import org.jasig.portal.ResourceMissingException;
import org.jasig.portal.ThemeStylesheetDescription;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.serialize.BaseMarkupSerializer;
import org.jasig.portal.serialize.OutputFormat;
import org.jasig.portal.serialize.XMLSerializer;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;

/**
 * CError is the error channel, also known as the null channel, it is
 * designed to render in place of other channels when something goes wrong.
 * <p>
 * Possible conditions when CError is invoked are:
 * <ul>
 * <li> Channel has thrown a {@link PortalException} from one of the IChannel or IPrivilegedChannel methods.</li>
 * <li> Channel has thrown a Runtime exception from one of the methods.<li>
 * <li> Channel has timed out on rendering and was terminated.</li>
 * <li> uPortal has rejected a channel for some reason.
 * In this case a general message is constructed by the portal.</li>
 * </ul>
 * 
 * @author Peter Kharchenko, pkharchenko@interactivebusiness.com
 * @version $Revision$
 */
public class CError extends BaseChannel implements IPrivilegedChannel, ICacheable, ICharacterChannel
{
    private static final Log log = LogFactory.getLog(CError.class);

    // codes defining the stage at which the exception was thrown
    public static final int GENERAL_ERROR=0;
    public static final int RENDER_TIME_EXCEPTION=1;
    public static final int SET_STATIC_DATA_EXCEPTION=2;
    public static final int SET_RUNTIME_DATA_EXCEPTION=3;
    public static final int TIMEOUT_EXCEPTION=4;
    public static final int SET_PCS_EXCEPTION=5;
    public static final int CHANNEL_AUTHORIZATION_EXCEPTION=6;
    public static final int CHANNEL_MISSING_EXCEPTION=7;

    // codes defining exception types
    public static final int GENERAL_RENDERING_EXCEPTION=1;
    public static final int INTERNAL_TIMEOUT_EXCEPTION=2;
    public static final int AUTHORIZATION_EXCEPTION=3;
    public static final int RESOURCE_MISSING_EXCEPTION=4;

    protected Throwable channelException=null;
    protected String str_channelSubscribeId=null;
    protected String str_message=null;
    protected IChannel the_channel=null;
    protected int errorID=-1;
    protected boolean placeHolder = false;

    private boolean showStackTrace=false;
    private String ssTitle = null;

    private PortalControlStructures portcs;
    private static final String sslLocation = "CError/CError.ssl";
    protected static MediaManager mediaM=new MediaManager();

    public CError() {
    }

    public CError(int errorCode, Throwable exception, String channelSubscribeId,IChannel channelInstance) {
        this();
        str_channelSubscribeId=channelSubscribeId;
        this.channelException=exception;
        this.the_channel=channelInstance;
        this.errorID=errorCode;
    }

    public CError(int errorCode, String message,String channelSubscribeId,IChannel channelInstance) {
        this();
        this.str_channelSubscribeId=channelSubscribeId;
        this.str_message=message;
        this.the_channel=channelInstance;
        this.errorID=errorCode;
    }

    public CError(int errorCode, Throwable exception, String channelSubscribeId,IChannel channelInstance, String message) {
        this(errorCode,exception,channelSubscribeId,channelInstance);
        this.setMessage(message);
    }

    public void setMessage(String m) {
        this.str_message=m;
    }

    private void resetCError(int errorCode, Throwable exception, String channelSubscribeId,IChannel channelInstance,String message) {
        str_channelSubscribeId=channelSubscribeId;
        this.channelException=exception;
        this.the_channel=channelInstance;
        this.errorID=errorCode;
        this.str_message=message;
    }

    public void setPortalControlStructures(PortalControlStructures pcs) {
        this.portcs=pcs;
    }


    /**
     * This is so CError can be used by getUserLayout() as a placeholder for
     * channels that have either been deleted from the portal database or
     * the users permission to use the channel has been removed (permanently or
     * temporarily).
     */
    public void setStaticData(ChannelStaticData sd) {
      this.str_message = sd.getParameter("CErrorMessage");
      this.str_channelSubscribeId = sd.getParameter("CErrorChanId");
      String value;
      if ((value = sd.getParameter("CErrorErrorId")) != null) {
        this.errorID = Integer.parseInt(value);
      }
      placeHolder = true;  // Should only get here if we are a "normal channel"
    }

    public void renderXML(ContentHandler out) {
        // runtime data processing needs to be done here, otherwise replaced
        // channel will get duplicated setRuntimeData() calls
        if(str_channelSubscribeId!=null) {
            String chFate=runtimeData.getParameter("action");
            if(chFate!=null) {
                // a fate has been chosen
                if(chFate.equals("retry")) {
                    log.debug("CError:setRuntimeData() : going for retry");
                    // clean things up for the channel
                    ChannelRuntimeData crd = (ChannelRuntimeData) runtimeData.clone();
                    crd.clear(); // Remove parameters
                    try {
                        if(the_channel instanceof IPrivilegedChannel)
                            ((IPrivilegedChannel)the_channel).setPortalControlStructures(portcs);
                        the_channel.setRuntimeData (crd);
                        ChannelManager cm=portcs.getChannelManager();
                        cm.setChannelInstance(this.str_channelSubscribeId,this.the_channel);
                        the_channel.renderXML(out);
                        return;
                    } catch (Exception e) {
                        // if any of the above didn't work, fall back to the error channel
                        resetCError(CError.SET_RUNTIME_DATA_EXCEPTION,e,this.str_channelSubscribeId,this.the_channel,"Channel failed a refresh attempt.");
                    }
                } else if(chFate.equals("restart")) {
                    log.debug("CError:setRuntimeData() : going for reinstantiation");

                    ChannelManager cm=portcs.getChannelManager();

                    ChannelRuntimeData crd = (ChannelRuntimeData) runtimeData.clone();
                    crd.clear();
                    try {
                        if((the_channel=cm.instantiateChannel(str_channelSubscribeId))==null) {
                            resetCError(CError.GENERAL_ERROR,null,this.str_channelSubscribeId,null,"Channel failed to reinstantiate!");
                        } else {
                            try {
                                if(the_channel instanceof IPrivilegedChannel) {
                                    ((IPrivilegedChannel)the_channel).setPortalControlStructures(portcs);
                                }
                                the_channel.setRuntimeData (crd);
                                the_channel.renderXML(out);
                                return;
                            } catch (Exception e) {
                                // if any of the above didn't work, fall back to the error channel
                                resetCError(CError.SET_RUNTIME_DATA_EXCEPTION,e,this.str_channelSubscribeId,this.the_channel,"Channel failed a reload attempt.");
                                cm.setChannelInstance(str_channelSubscribeId,this);
                                log.error("CError::setRuntimeData() : an error occurred during channel reinitialization. "+e);
                            }
                        }
                    } catch (Exception e) {
                        resetCError(CError.GENERAL_ERROR,e,this.str_channelSubscribeId,null,"Channel failed to reinstantiate!");
                        log.error("CError::setRuntimeData() : an error occurred during channel reinstantiation. "+e);
                    }
                } else if(chFate.equals("toggle_stack_trace")) {
                    showStackTrace=!showStackTrace;
                }
            }
        }
        // if channel's render XML method was to be called, we would've returned by now
        localRenderXML(out);
    }

    private void localRenderXML(ContentHandler out) {
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

        Document doc = DocumentFactory.getNewDocument();
        Element errorEl=doc.createElement("error");
        errorEl.setAttribute("code",Integer.toString(errorID));
        if(str_message!=null) {
            Element messageEl=doc.createElement("message");
            messageEl.appendChild(doc.createTextNode(str_message));
            errorEl.appendChild(messageEl);
        }

        if(str_channelSubscribeId!=null) {
            Element channelEl=doc.createElement("channel");
            Element idEl=doc.createElement("id");
            idEl.appendChild(doc.createTextNode(str_channelSubscribeId));
            channelEl.appendChild(idEl);

            // determine channel name
            if(portcs!=null) {
                String chName=null;
                try {
                    chName=portcs.getUserPreferencesManager().getUserLayoutManager().getNode(str_channelSubscribeId).getName();
                } catch (Exception e) {
                    chName="undetermined name";
                }
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
            Element oeEl=doc.createElement("outerException");
            java.io.StringWriter sw=new java.io.StringWriter();
            channelException.printStackTrace(new java.io.PrintWriter(sw));
            sw.flush();
            oeEl.appendChild(doc.createTextNode(sw.toString()));
            stEl.appendChild(oeEl);
            if(channelException instanceof PortalException) {
                PortalException pe=(PortalException) channelException;
                Throwable realException = pe.getRecordedException();
                if (realException != null) {
                    Element ieEl=doc.createElement("innerException");
                    java.io.StringWriter sw2=new java.io.StringWriter();
                    realException.printStackTrace(new java.io.PrintWriter(sw2));
                    ieEl.appendChild(doc.createTextNode(sw2.toString()));
                    stEl.appendChild(ieEl);
                }
            }



            excEl.appendChild(stEl);


            if(channelException instanceof PortalException) {
                PortalException pe=(PortalException) channelException;
                // the Error channel has been invoked as a result of some other
                // channel throwing a PortalException.

                // determine which type of an exception is it
                excEl.setAttribute("code",Integer.toString(0));

                // now specific cases for exceptions containing additional information
                if(pe instanceof ResourceMissingException) {
                    excEl.setAttribute("code",Integer.toString(RESOURCE_MISSING_EXCEPTION));
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
                    excEl.setAttribute("code",Integer.toString(INTERNAL_TIMEOUT_EXCEPTION));
                    Long v=((InternalTimeoutException)pe).getTimeoutValue();
                    if(v!=null) {
                        Element timeoutEl=doc.createElement("timeout");
                        timeoutEl.setAttribute("value",v.toString());
                    }
                } else if(pe instanceof AuthorizationException) {
                    excEl.setAttribute("code",Integer.toString(AUTHORIZATION_EXCEPTION));
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
        if(str_channelSubscribeId==null) {
            allowRel=allowRef="false";
        } else {
            if(channelException!=null && (channelException instanceof PortalException)) {
                if(errorID==SET_STATIC_DATA_EXCEPTION || !((PortalException)channelException).allowRefresh())
                    allowRef="false";
                if(!((PortalException)channelException).allowReinstantiation())
                    allowRel="false";
            }
        }

        if (placeHolder) { // We are just displaying a message. Nothing to restart
          allowRef = "false";
          allowRel = "false";
        }

        // Decide whether to render a friendly or detailed screen
        ssTitle = "friendly";
        try {
          AuthorizationService authService = AuthorizationService.instance();
          EntityIdentifier ei = portcs.getUserPreferencesManager().getPerson().getEntityIdentifier();
          IAuthorizationPrincipal ap = authService.newPrincipal(ei.getKey(), ei.getType());
          if (ap.hasPermission("UP_ERROR_CHAN", "VIEW", "DETAILS"))
            ssTitle = "detailed";
        } catch (Exception e) {
          // stay with friendly stylesheet
        }

        // debug block
        try {
            java.io.StringWriter outString = new java.io.StringWriter ();
            org.apache.xml.serialize.OutputFormat format=new org.apache.xml.serialize.OutputFormat();
            format.setOmitXMLDeclaration(true);
            format.setIndenting(true);
            org.apache.xml.serialize.XMLSerializer xsl = new org.apache.xml.serialize.XMLSerializer (outString,format);
            xsl.serialize (doc);
            log.debug(outString.toString());
        } catch (Exception e) {
            log.debug(e, e);
        }
        // end of debug block

        try {
            XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
            xslt.setXML(doc);
            xslt.setXSL(sslLocation, ssTitle, runtimeData.getBrowserInfo());
            xslt.setTarget(out);
            xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
            xslt.setStylesheetParameter("showStackTrace", String.valueOf(showStackTrace));
            xslt.setStylesheetParameter("allowRefresh", allowRef);
            xslt.setStylesheetParameter("allowReinstantiation", allowRel);
            xslt.transform();
        } catch (Exception e) {
            log.error( "CError::renderXML() : Things are bad. Error channel threw Exception.", e);
        }
    }

    public ChannelCacheKey generateKey() {
        // check if either restart or refresh command has been given, otherwise generate key
        if(runtimeData.getParameter("action")!=null) {
            return null;
        }

        ChannelCacheKey k=new ChannelCacheKey();
        StringBuffer sbKey = new StringBuffer(1024);

        // assume that errors can be cached system-wide
        k.setKeyScope(ChannelCacheKey.SYSTEM_KEY_SCOPE);

        sbKey.append("org.jasig.portal.channels.CError: errorId=").append(Integer.toString(errorID)).append(", channelID=");
        sbKey.append(str_channelSubscribeId).append(", message=").append(str_message).append(" strace=").append(toString(showStackTrace));
        sbKey.append(", mode=").append(ssTitle);

        // part of the key that specifies what kind of exception has been generated
        if(channelException !=null) {
            sbKey.append("exception: ");
            sbKey.append(channelException.getMessage());
            if(channelException instanceof PortalException) {
                PortalException pe= (PortalException) channelException;
                sbKey.append(toString(pe.allowRefresh())).append(toString(pe.allowReinstantiation())).append(" ");
                if(pe.getRecordedException() !=null) {
                    sbKey.append(pe.getRecordedException().getMessage());
                }
                if(channelException instanceof ResourceMissingException) {
                    ResourceMissingException rme=(ResourceMissingException) pe;
                    sbKey.append("resource: ").append(rme.getResourceURI()).append(rme.getResourceDescription());
                } else if(channelException instanceof InternalTimeoutException) {
                    sbKey.append("timeout: ").append(((InternalTimeoutException) channelException).getTimeoutValue().toString());
                }
            }
        }
        sbKey.append(", locales=").append(LocaleManager.stringValueOf(runtimeData.getLocales()));
        k.setKey(sbKey.toString());
        return k;
    }

    public boolean isCacheValid(Object validity) {
        return true;
    }

    private String toString(boolean b) {
        if(b) return("true"); else return ("false");
    }

    public void renderCharacters(PrintWriter out) throws PortalException {
        // runtime data processing needs to be done here, otherwise replaced
        // channel will get duplicated setRuntimeData() calls
        if(str_channelSubscribeId!=null) {
            String chFate=runtimeData.getParameter("action");
            if(chFate!=null) {
                // a fate has been chosen
                if(chFate.equals("retry")) {
                    log.debug("CError:renderCharacters() : going for retry");
                    // clean things up for the channel
                    ChannelRuntimeData crd = (ChannelRuntimeData) runtimeData.clone();
                    crd.clear(); // Remove parameters
                    try {
                        if(the_channel instanceof IPrivilegedChannel)
                            ((IPrivilegedChannel)the_channel).setPortalControlStructures(portcs);
                        the_channel.setRuntimeData (crd);
                        ChannelManager cm=portcs.getChannelManager();
                        cm.setChannelInstance(this.str_channelSubscribeId,this.the_channel);
                        if(the_channel instanceof ICharacterChannel) {
                            ((ICharacterChannel) the_channel).renderCharacters(out);
                        } else {
                            ThemeStylesheetDescription tsd=portcs.getUserPreferencesManager().getThemeStylesheetDescription();
                            BaseMarkupSerializer serOut = mediaM.getSerializerByName(tsd.getSerializerName(), out);
                            the_channel.renderXML(serOut);
                        }
                        return;
                    } catch (Exception e) {
                        // if any of the above didn't work, fall back to the error channel
                        resetCError(CError.SET_RUNTIME_DATA_EXCEPTION,e,this.str_channelSubscribeId,this.the_channel,"Channel failed a refresh attempt.");
                    }
                } else if(chFate.equals("restart")) {
                    log.debug("CError:renderCharacters() : going for reinstantiation");

                    ChannelManager cm=portcs.getChannelManager();

                    ChannelRuntimeData crd = (ChannelRuntimeData) runtimeData.clone();
                    crd.clear();
                    try {
                        if((the_channel=cm.instantiateChannel(str_channelSubscribeId))==null) {
                            resetCError(CError.GENERAL_ERROR,null,this.str_channelSubscribeId,null,"Channel failed to reinstantiate!");
                        } else {
                            try {
                                if(the_channel instanceof IPrivilegedChannel) {
                                    ((IPrivilegedChannel)the_channel).setPortalControlStructures(portcs);
                                }
                                the_channel.setRuntimeData (crd);
                                if(the_channel instanceof ICharacterChannel) {
                                    ((ICharacterChannel) the_channel).renderCharacters(out);
                                } else {
                                    ThemeStylesheetDescription tsd=portcs.getUserPreferencesManager().getThemeStylesheetDescription();
                                    BaseMarkupSerializer serOut = mediaM.getSerializerByName(tsd.getSerializerName(), out);
                                    the_channel.renderXML(serOut);
                                }
                                return;
                            } catch (Exception e) {
                                // if any of the above didn't work, fall back to the error channel
                                resetCError(CError.SET_RUNTIME_DATA_EXCEPTION,e,this.str_channelSubscribeId,this.the_channel,"Channel failed a reload attempt.");
                                cm.setChannelInstance(str_channelSubscribeId,this);
                                log.error("CError::renderCharacters() : an error occurred during channel reinitialization. "+e);
                            }
                        }
                    } catch (Exception e) {
                        resetCError(CError.GENERAL_ERROR,e,this.str_channelSubscribeId,null,"Channel failed to reinstantiate!");
                        log.error("CError::renderCharacters() : an error occurred during channel reinstantiation. "+e);
                    }
                } else if(chFate.equals("toggle_stack_trace")) {
                    showStackTrace=!showStackTrace;
                }
            }
        }
        // if channel's render XML method was to be called, we would've returned by now
        BaseMarkupSerializer serOut=null;
        try {
            ThemeStylesheetDescription tsd=portcs.getUserPreferencesManager().getThemeStylesheetDescription();
            serOut = mediaM.getSerializerByName(tsd.getSerializerName(), out);        
        } catch (Exception e) {
            log.error("CError::renderCharacters() : unable to obtain proper markup serializer : "+e); 
        }

        if(serOut==null) {
            // default to XML serializer
            OutputFormat frmt=new OutputFormat("XML", "UTF-8", true);
            serOut=new XMLSerializer(out, frmt);
        }

        localRenderXML(serOut);
    }

}
