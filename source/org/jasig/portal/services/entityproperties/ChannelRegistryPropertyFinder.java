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

import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.ChannelRegistryStoreFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.services.LogService;


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
        names[0] = "Name";
        names[1] = "Title";
        names[2] = "Description";
        names[3] = "Functional Name";
        names[4] = "Timeout";
        try {
            chan = Class.forName("org.jasig.portal.ChannelDefinition");
        } catch (Exception e) {
            LogService.log(LogService.ERROR, "ChannelRegistryPropertyFinder - static:");
            LogService.log(LogService.ERROR, e);
        }
    }

    public ChannelRegistryPropertyFinder() {
    }

    public String[] getPropertyNames(EntityIdentifier entityID) {
        return  names;
    }

    public String getProperty(EntityIdentifier entityID, String name) {
        String r = null;
        if (entityID.getType().equals(chan)) {
            try {
                //Element c = ChannelRegistryManager.getChannel("chan"+entityID.getKey());
                //r = c.getAttribute(name);
                ChannelDefinition cd = ChannelRegistryStoreFactory.getChannelRegistryStoreImpl().getChannelDefinition(Integer.parseInt(entityID.getKey()));
                if (name.equals("Name")){
                  r = cd.getName(); 
                }
                if (name.equals("Title")){
                  r = cd.getTitle(); 
                }
                if (name.equals("Description")){
                  r = cd.getDescription();
                }
                if (name.equals("Functional Name")){
                  r = cd.getFName(); 
                }
                if (name.equals("Timeout")){
                  r = String.valueOf(cd.getTimeout()); 
                }
            } catch (Exception e) {
                LogService.log(LogService.ERROR, "ChannelRegistryPropertyFinder.getProperty("+entityID.getKey()+"-"+entityID.getType().getName()+","+name+") :");
                LogService.log(LogService.ERROR, e);
            }
        }
        return  r;
    }
}



