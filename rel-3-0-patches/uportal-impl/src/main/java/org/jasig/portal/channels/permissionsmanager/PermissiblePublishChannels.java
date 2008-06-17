/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.channels.permissionsmanager;

import java.util.HashMap;

import org.jasig.portal.IPermissible;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * An IPermissible implementation that provides management of uPortal 2.0
 * channel publishing permissions
 *
 * @author Alex Vigdor
 * @version $Revision$
 */
public class PermissiblePublishChannels
        implements IPermissible {
    private static final Log log = LogFactory.getLog(PermissiblePublishChannels.class);
    protected HashMap activities = new HashMap();
    protected HashMap targets = new HashMap();

    /**
     * put your documentation comment here
     */
    public PermissiblePublishChannels () {
        try {
            activities.put("PUBLISH", "Publish a Channel");
            targets.put("CHAN_ID.*", "This uPortal installation");
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
     * @return a <code>String</code> activity name
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
     * @return a <code>String</code> owner token
     */
    public String getOwnerToken () {
        return  "UP_FRAMEWORK";
    }

    /**
     * put your documentation comment here
     * @return a <code>String</code> owner name
     */
    public String getOwnerName () {
        return  "uPortal Channel Publication";
    }
}



