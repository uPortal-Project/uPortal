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
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
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



