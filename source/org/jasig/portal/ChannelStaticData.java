/**
 * Copyright (c) 2000 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal;

import java.util.Map;

/**
 * Used to store channel configuration items and parameters.
 * @author Ken Weiner, Peter Kharchenko
 * @version $Revision$
 * @author Peter Kharchenko
 */

public class ChannelStaticData extends java.util.Hashtable
{
    private String sChannelID = null;

    // Parameters are strings !
    /**
     * Set information contained in a channel <param> element
     * @param key param name
     * @param value param value
     */
    public synchronized String setParameter (String key, String value) {return (String) super.put (key, value);}
    /**
     * Get information contained in a particular <param> element
     * @param key param name
     * @return param value
     */
    public synchronized String getParameter (String key) {return (String) super.get (key);}


    // if you need to pass Objects, use this
    /**
     * Similar to the {@link #setParameter(String,String)}, but can be used to pass things other then strings.
     */
    public synchronized Object put (Object key, Object value) {return super.put (key, value);}
    /**
     * Similar to the {@link #getParameter(String)}, but can be used to pass things other then strings.
     */
    public synchronized Object get (Object key) {return super.get (key);}


    /**
     * Copy parameter list from a Map
     * @param params a map of params
     */
    public void setParameters(Map params) {
	// copy a Map
	this.putAll(params);
    };


    /**
     * Sets the channel ID
     * @param sChannelID the unique channelID
     */
    public void setChannelID (String sChID) {this.sChannelID = sChID;}

    /**
     * Gets the channel ID
     * @return the channel's ID
     */
    public String getChannelID () {return sChannelID;}
}


