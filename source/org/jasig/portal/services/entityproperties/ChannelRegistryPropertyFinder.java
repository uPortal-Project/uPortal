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


package  org.jasig.portal.services.entityproperties;

import  org.jasig.portal.*;
import  org.jasig.portal.groups.*;
import  org.w3c.dom.*;
import  org.jasig.portal.services.*;


/**
 * A finder implementation to provide channel properties derived from the
 * ChannelRegistryManager
 *
 * @author Alex Vigdor av317@columbia.edu
 * @version $Revision$
 */
public class ChannelRegistryPropertyFinder
        implements IEntityPropertyFinder {
    protected static final String[] names;
    protected static Class chan = null;
    static {
        /* this array should hold the desired attributes of a channel element
         as defined in channelRegistry.dtd
         */
        names = new String[5];
        names[0] = "name";
        names[1] = "title";
        names[2] = "description";
        names[3] = "fname";
        names[4] = "timeout";
        try {
            chan = Class.forName("org.jasig.portal.ChannelDefinition");
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, "ChannelRegistryPropertyFinder - static:");
            LogService.instance().log(LogService.ERROR, e);
        }
    }

    public ChannelRegistryPropertyFinder() {
    }

    public String[] getPropertyNames(IBasicEntity entity) {
        return  names;
    }

    public String getProperty(IBasicEntity entity, String name) {
        String r = null;
        if (entity.getType().equals(chan)) {
            try {
                Element c = ChannelRegistryManager.getChannel(entity.getKey());
                r = c.getAttribute(name);
            } catch (Exception e) {
                LogService.instance().log(LogService.ERROR, "ChannelRegistryPropertyFinder.getProperty() :");
                LogService.instance().log(LogService.ERROR, e);
            }
        }
        return  r;
    }
}



