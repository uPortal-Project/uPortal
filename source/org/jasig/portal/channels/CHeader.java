package org.jasig.portal.channels;

import java.net.URL;
import java.util.Hashtable;
import java.util.HashMap;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.NotContextException;
// import org.jasig.portal.security.Permission;
// import org.jasig.portal.security.PermissionManager;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ICacheable;
import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.MediaManager;
import org.jasig.portal.PortalException;
import org.jasig.portal.GeneralRenderingException;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.XSLT;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.SmartCache;
import org.jasig.portal.utils.DocumentFactory;
import org.xml.sax.ContentHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * This channel provides content for a page header.  It is indended
 * to be included in a layout folder of type "header".  Most stylesheets
 * will render the content of such header channels consistently on every
 * page.
 * @author Peter Kharchenko
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @author Bernie Durfee, bdurfee@interactivebusiness.com
 * @version $Revision 1.1$
 */
public class CHeader extends BaseChannel
    implements ICacheable {
  // Cache the answers to canUserPublish() to speed things up
  private static SmartCache m_canUserPublishResponses = new SmartCache(60*10);
  private static final String sslLocation = "CHeader/CHeader.ssl";

  /**
   * Checks user permissions to see if the user is authorized to publish channels
   * @return true if user can publish
   */
  private boolean canUserPublish () {
    // Get the current user ID
    int userID = staticData.getPerson().getID();
    // Check the cache for the answer
    if (m_canUserPublishResponses.get("USER_ID." + userID) != null) {
      // Return the answer if it's in the cache
      if (((Boolean)m_canUserPublishResponses.get("USER_ID." + userID)).booleanValue()) {
        return  (true);
      }
      else {
        return  (false);
      }
    }
    // Get a reference to an IAuthorizationPrincipal (was PermissionManager for this channel).
    IAuthorizationPrincipal ap = staticData.getAuthorizationPrincipal();
    try {
	    // Let the authorization service decide:
	    boolean hasPermission = ap.canPublish();
        // Cache the result
        m_canUserPublishResponses.put("USER_ID." + userID, new Boolean(hasPermission));
        return  (hasPermission);

    } catch (Exception e) {
      LogService.instance().log(LogService.ERROR, e);
      // Deny the user publish access if anything went wrong
      return  (false);
    }
  }
  /**
   * put your documentation comment here
   * @return ChannelCacheKey
   */
  public ChannelCacheKey generateKey () {
    ChannelCacheKey k = new ChannelCacheKey();
    StringBuffer sbKey = new StringBuffer(1024);

    sbKey.append("org.jasig.portal.CHeader: ");

    if(staticData.getPerson().isGuest()) {
        // guest users are cached system-wide. 
        k.setKeyScope(ChannelCacheKey.SYSTEM_KEY_SCOPE);
        sbKey.append("userId:").append(staticData.getPerson().getID()).append(", ");
    } else {
        // otherwise cache is instance-specific
        k.setKeyScope(ChannelCacheKey.INSTANCE_KEY_SCOPE);
    }
    sbKey.append("authenticated:").append(staticData.getPerson().getSecurityContext().isAuthenticated()).append(", ");
    sbKey.append("baseActionURL:").append(runtimeData.getBaseActionURL());
    sbKey.append("stylesheetURI:");
    try {
      String sslUri = ResourceLoader.getResourceAsURLString(this.getClass(), sslLocation);
      sbKey.append(XSLT.getStylesheetURI(sslUri, runtimeData.getBrowserInfo()));
    } catch (Exception e) {
      sbKey.append("not defined");
    }
    k.setKey(sbKey.toString());
    k.setKeyValidity(new Long(System.currentTimeMillis()));
    return  k;
  }
  /**
   * Gets the current date/time with specified format
   * @param format the format string
   * @return a formatted date and time string
   */
  public static String getDate(String format) {
    try {
      // Format the current time.
      java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat(format);
      java.util.Date currentTime = new java.util.Date();
      return formatter.format(currentTime);
    }
    catch (Exception e) {
      LogService.instance().log(LogService.ERROR, e);
    }

    return "&nbsp;";
  }
  /**
   * Returns the DOM object associated with the user
   * NOTE: This should be made more effecient through caching
   * @return DOM object associated with the user
   */
  private Document getUserXML () {
    // Get the fullname of the current user
    String fullName = (String)staticData.getPerson().getFullName();
    // Get a new DOM instance
    Document doc = DocumentFactory.getNewDocument();
    // Create <header> element
    Element headerEl = doc.createElement("header");
    // Create <full-name> element under <header>
    Element fullNameEl = doc.createElement("full-name");
    fullNameEl.appendChild(doc.createTextNode(fullName));
    headerEl.appendChild(fullNameEl);
    // Create <timestamp-long> element under <header>
    Element timeStampLongEl = doc.createElement("timestamp-long");
    timeStampLongEl.appendChild(doc.createTextNode(getDate("EEEE, MMM d, yyyy 'at' hh:mm a")));
    headerEl.appendChild(timeStampLongEl);
    // Create <timestamp-short> element under <header>
    Element timeStampShortEl = doc.createElement("timestamp-short");
    timeStampShortEl.appendChild(doc.createTextNode(getDate("M.d.y h:mm a")));
    headerEl.appendChild(timeStampShortEl);
    // Don't render the publish, subscribe, user preferences links if it's the guest user
    if (!staticData.getPerson().getSecurityContext().isAuthenticated()) {
      Context globalIDContext = null;
      try {
        // Get the context that holds the global IDs for this user
        globalIDContext = (Context)staticData.getJNDIContext().lookup("/channel-ids");
      } catch (NotContextException nce) {
        LogService.instance().log(LogService.ERROR, "CHeader.getUserXML(): Could not find subcontext /channel-ids in JNDI");
      } catch (NamingException e) {
        LogService.instance().log(LogService.ERROR, e);
      }
      try {
        if (canUserPublish()) {
          // Create <chan-mgr-chanid> element under <header>
          Element chanMgrChanidEl = doc.createElement("chan-mgr-chanid");
          chanMgrChanidEl.appendChild(doc.createTextNode((String)globalIDContext.lookup("/portal/channelmanager/general")));
          headerEl.appendChild(chanMgrChanidEl);
        }
      } catch (NotContextException nce) {
        LogService.instance().log(LogService.ERROR, "CHeader.getUserXML(): Could not find channel ID for fname=/portal/channelmanager/general for UID="
            + staticData.getPerson().getID() + ". Be sure that the channel is in their layout.");
      } catch (NamingException e) {
        LogService.instance().log(LogService.ERROR, e);
      }
      try {
        // Create <preferences-chanid> element under <header>
        Element preferencesChanidEl = doc.createElement("preferences-chanid");
        preferencesChanidEl.appendChild(doc.createTextNode((String)globalIDContext.lookup("/portal/userpreferences/general")));
        headerEl.appendChild(preferencesChanidEl);
      } catch (NotContextException nce) {
        LogService.instance().log(LogService.ERROR, "CHeader.getUserXML(): Could not find channel ID for fname=/portal/userpreferences/general for UID="
            + staticData.getPerson().getID() + ". Be sure that the channel is in their layout.");
      } catch (NamingException e) {
        LogService.instance().log(LogService.ERROR, e);
      }
    }
    doc.appendChild(headerEl);
    return  (doc);
  }
  /**
   * put your documentation comment here
   * @param validity
   * @return true if cache is valid
   */
  public boolean isCacheValid (Object validity) {
    if (validity instanceof Long) {
      Long oldtime = (Long)validity;
      if (staticData.getPerson().isGuest()) {
        return  true;
      }
      if (System.currentTimeMillis() - oldtime.longValue() < 1*60*1000) {
        return  true;
      }
    }
    return  false;
  }
  /**
   * Render method.
   * @param out
   * @exception PortalException
   */
  public void renderXML (ContentHandler out) throws PortalException {
    // Perform the transformation
    XSLT xslt = new XSLT(this);
    xslt.setXML(getUserXML());
    xslt.setXSL(sslLocation, runtimeData.getBrowserInfo());
    xslt.setTarget(out);
    xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
    if (staticData.getPerson().getSecurityContext().isAuthenticated()) {
      xslt.setStylesheetParameter("authenticated", "true");
    }
    xslt.transform();
  }
}
