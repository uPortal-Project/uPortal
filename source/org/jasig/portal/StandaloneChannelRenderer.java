/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal;


import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;

import org.jasig.portal.channels.BaseChannel;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.serialize.BaseMarkupSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.XSLT;

/**
 * StandaloneChannelRenderer is meant to be used as a base class for channels
 * that might be rendered outside of the standard user-layout driven scheme.
 * (for example CSelectSystemProfile).
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version $Revision$
 */

public class StandaloneChannelRenderer 
    extends BaseChannel
{
    
    private static final Log log = LogFactory.getLog(StandaloneChannelRenderer.class);
    
    private StylesheetSet set;
    private MediaManager mediaM;
    private String channelName;
    private PortalControlStructures pcs;
    private BrowserInfo binfo;
    private LocaleManager lm;
    private boolean hasEdit = false;
    private boolean hasAbout = false;
    private boolean hasHelp = false;
    private long timeOut;                 // 10 seconds is the default timeout value
    private boolean dataIsSet=false;
    private static final String chanID="singleton";
    private static final String fs = File.separator;
    private static final String relativeSSLLocation = "/org/jasig/portal/tools/ChannelServlet/ChannelServlet.ssl";
    private static final IChannelRendererFactory cChannelRendererFactory = 
        ChannelRendererFactory.newInstance(
            StandaloneChannelRenderer.class.getName()
            );

    /**
     * Initializes the channel and calls setStaticData() on the channel.
     * @param params a hastable of channel publish/subscribe parameters (<parameter> elements
     * @param channelName channel name
     * @param hasHelp determines if the channel supports "help" layout event
     * @param hasAbout determines if the channel supports "about" layout event
     * @param hasEdit determines if the channel supports "edit" layout event
     * @param timeOut channel timeout value in milliseconds
     * @param person a user IPerson object
     */
    public void initialize(Hashtable params,String channelName,boolean hasHelp, boolean hasAbout, boolean hasEdit, long timeOut,IPerson person) throws PortalException {
        this.set = new StylesheetSet(ResourceLoader.getResourceAsURLString(this.getClass(), relativeSSLLocation));
        String mediaPropsUrl = ResourceLoader.getResourceAsURLString(this.getClass(), "/properties/media.properties");
        String mimePropsUrl = ResourceLoader.getResourceAsURLString(this.getClass(), "/properties/mime.properties");
        String serializerPropsUrl = ResourceLoader.getResourceAsURLString(this.getClass(), "/properties/serializer.properties");
        this.set.setMediaProps(mediaPropsUrl);
        this.mediaM = new MediaManager(mediaPropsUrl, mimePropsUrl, serializerPropsUrl);
        this.channelName=channelName;
        this.hasHelp=hasHelp;
        this.hasAbout=hasAbout;
        this.hasEdit=hasEdit;
        this.timeOut=timeOut;

        ChannelStaticData sd = new ChannelStaticData ();
        sd.setChannelSubscribeId (chanID);
        sd.setTimeout (timeOut);
        sd.setParameters (params);
        // get person object from UsreLayoutManager
        sd.setPerson(person);
        this.setStaticData(sd);
    }

    /**
     * This request will cause setRuntimeData() method called on the channel. If this method is invoked,
     * the render() method, which usually invokes setRuntimeData() method will omit the call.
     * @param req http request
     */
    public void prepare(HttpServletRequest req) throws Exception {
        if(this instanceof IPrivilegedChannel) {
            ((IPrivilegedChannel) this).setPortalControlStructures(pcs);
        }
        this.setRuntimeData(getRuntimeData(req));
        dataIsSet=true;
    }


    /**
     * This method will output channel content into the HttpServletResponse's
     * out stream. Note that setRuntimeData() method is called only if there was
     * no prior call to prepare() method.
     * @param req http request
     * @param res http response
     */
    public void render(HttpServletRequest req, HttpServletResponse res) throws Throwable {
        ChannelRuntimeData rd=null;
        if(!dataIsSet) {
            if(this instanceof IPrivilegedChannel) {
                ((IPrivilegedChannel) this).setPortalControlStructures(pcs);
            }
            rd=getRuntimeData(req);
        } else {
            dataIsSet=false;
        }

        // start rendering
        IChannelRenderer cr = cChannelRendererFactory.newInstance( this, rd );
        cr.setTimeout (timeOut);
        cr.startRendering ();

        // set the output mime type
        res.setContentType(mediaM.getReturnMimeType(req));
        // set up the serializer
        BaseMarkupSerializer ser = mediaM.getSerializer(mediaM.getMedia(req), res.getWriter());
        ser.asContentHandler();
        // get the framing stylesheet
        String xslURI = ResourceLoader.getResourceAsURLString(this.getClass(), set.getStylesheetURI(req));
        try {
            TransformerHandler th=XSLT.getTransformerHandler(xslURI);
            th.setResult(new SAXResult(ser));
            org.xml.sax.helpers.AttributesImpl atl = new org.xml.sax.helpers.AttributesImpl();
            atl.addAttribute("","name","name", "CDATA", channelName);
            // add other attributes: hasHelp, hasAbout, hasEdit
            th.startDocument();
            th.startElement("","channel","channel", atl);
            ChannelSAXStreamFilter custodian = new ChannelSAXStreamFilter(th);
            int out=cr.outputRendering(custodian);
            if(out==IChannelRenderer.RENDERING_TIMED_OUT) {
                throw new InternalTimeoutException("The channel has timed out");
            }
            th.endElement("","channel","channel");
            th.endDocument();
        } catch (InternalPortalException ipe) {
            throw ipe.getCause();
        }
    }

    private ChannelRuntimeData getRuntimeData(HttpServletRequest req) {
        // construct runtime data
        this.binfo=new BrowserInfo(req);
        String acceptLanguage = req.getHeader("Accept-Language");
        String requestLocalesString = req.getParameter("locale");
        this.lm=new LocaleManager(this.staticData.getPerson(), acceptLanguage);
        this.lm.setSessionLocales(LocaleManager.parseLocales(requestLocalesString));

        Hashtable targetParams = new Hashtable();
        UPFileSpec upfs=new UPFileSpec(req);
        String channelTarget = upfs.getTargetNodeId();

        if (log.isDebugEnabled())
            log.debug("StandaloneRenderer::render() : channelTarget=\""+channelTarget+"\".");
        Enumeration en = req.getParameterNames();
        if (en != null) {
            while (en.hasMoreElements()) {
                String pName= (String) en.nextElement();
                Object[] val= (Object[]) req.getParameterValues(pName);
                if (val == null) {
                    val = ((RequestParamWrapper)req).getObjectParameterValues(pName);
                }
                targetParams.put (pName, val);
            }
        }

        ChannelRuntimeData rd= new ChannelRuntimeData();
        rd.setBrowserInfo(binfo);
        rd.setLocales(lm.getLocales());
        rd.setHttpRequestMethod(req.getMethod());
        if(channelTarget!=null && chanID.equals(channelTarget)) {
            rd.setParameters(targetParams);
        }

        try {
            rd.setUPFile(new UPFileSpec(PortalSessionManager.INTERNAL_TAG_VALUE,UPFileSpec.RENDER_METHOD,"servletRoot",chanID,null));
        } catch (Exception e) {
            if (log.isDebugEnabled())
                log.debug("StandaloneRenderer::render() : unable to generate baseActionURL. "+e);
        }

        return rd;

    }
}
