/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

// formatted with JxBeauty (c) johann.langhofer@nextra.at

package  org.jasig.portal.channels.permissionsmanager;

import java.util.HashMap;

import org.jasig.portal.ChannelRegistryManager;
import org.jasig.portal.IPermissible;
import org.jasig.portal.groups.IEntityNameFinder;
import org.jasig.portal.services.EntityNameFinderService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * An IPermissible implementation that provides management of uPortal 2.0
 * channel subscribtion permissions
 *
 * @author Alex Vigdor
 * @version $Revision$
 */
public class PermissibleSubscribeChannels
        implements IPermissible {
    private static final Log log = LogFactory.getLog(PermissibleSubscribeChannels.class);
    protected HashMap activities = new HashMap();
    protected HashMap targets = new HashMap();

    /**
     * put your documentation comment here
     */
    public PermissibleSubscribeChannels () {
        try {
            activities.put("SUBSCRIBE", "Subscribe to Channel");
            Document chanregistry = ChannelRegistryManager.getChannelRegistry();
            NodeList chans = chanregistry.getElementsByTagName("channel");
            IEntityNameFinder chanf = EntityNameFinderService.instance().getNameFinder(Class.forName("org.jasig.portal.ChannelDefinition"));
            for (int i = 0; i < chans.getLength(); i++) {
                Element chan = (Element)chans.item(i);
                String chanID = chan.getAttribute("ID");
                if (chanID.indexOf("chan") == 0) {
                    chanID = chanID.substring(4);
                }
                targets.put("CHAN_ID." + chanID, chanf.getName(chanID));
            }
        } catch (Exception e) {
            log.error(e, e);
        }
    }

    /**
     * put your documentation comment here
     * @return an array of <code>String</code> activity tokens
     */
    public String[] getActivityTokens () {
        return  (String[])activities.keySet().toArray(new String[0]);
    }

    /**
     * put your documentation comment here
     * @param token
     * @return the <code>String</code> activity name
     */
    public String getActivityName (String token) {
        return  (String)activities.get(token);
    }

    /**
     * put your documentation comment here
     * @return an array of <code>String</code> target tokens
     */
    public String[] getTargetTokens () {
        return  (String[])targets.keySet().toArray(new String[0]);
    }

    /**
     * put your documentation comment here
     * @param token
     * @return a <code>String</code> target name
     */
    public String getTargetName (String token) {
        return  (String)targets.get(token);
    }

    /**
     * put your documentation comment here
     * @return the <code>String</code> owner token
     */
    public String getOwnerToken () {
        return  "UP_FRAMEWORK";
    }

    /**
     * put your documentation comment here
     * @return the <code>String</code> owner name
     */
    public String getOwnerName () {
        return  "uPortal Channel Subscription";
    }
}



