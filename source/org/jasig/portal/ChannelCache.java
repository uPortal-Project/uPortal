/**
 * $Author$
 * $Date$
 * $Id$
 * $Name$
 * $Revision$
 *
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
 */




package org.jasig.portal;

import java.lang.ref.*;
import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import java.net.*;
import java.lang.Byte;
import com.objectspace.xml.*;
import org.jasig.portal.layout.*;

/**
 * This class is responsible for managing a user's channels.
 * It is configurable to either only cache a small set of the
 * channels requested, or to cache them all.
 *
 * @author $Author$
 */
public class ChannelCache {
    // keeps a list of all the classes that are to be cached
    // this must be a synchronized cache, although, if you are restricting users
    // to one browser, it could be unsynchronized
    private Set classSet = Collections.synchronizedSet(new HashSet());

    // holds all the channels currently active
    private Hashtable channelCache = new Hashtable();

    // determines whether we bother to constraing the caching
    private boolean constrainedCaching = false;
    private boolean configured = false;   // determines if we need to configure static variables



    /* Constructor to do default initialization of the channel cache.
     * It gets its configuration from the SessionManager (option 
     * session.memory.constrainted_channel_caching) and then configures
     * itself apropriately.  If the configuration is messed up, it defaults
     * to a safe configuration of "no" (meaning it will cache all channels).
     * @author $Author$
     *
     */
    ChannelCache() {
	// get the configurations from the SessionManager
	String constCacheOption = SessionManager.getConfiguration("session.memory.constrained_channel_caching");
	
	if("yes".equals(constCacheOption)) {
	    Logger.log(Logger.DEBUG, "ChannelCache: constrained channel caching is now on");
	    constrainedCaching = true;
	    // load the channels and store them for later
	    loadChannelClasses();
	} else {
	    constrainedCaching = false;
	}
    }


    /**
     * Used to determine whether this cache is constraining the channels
     * or not.
     *
     * @author $Author$
     */
    public boolean isCacheConstrained() {
	return constrainedCaching;
    }

    /**
     * Used to set the constraint state.  Setting this to true will begin channel
     * caching, and setting it to false will stop channel caching.  This is also
     * a method of reloading the list of classes to cache, as this method
     * does a reload of the list of classes when it is called.
     *
     * NOTE:  The list of classes to cache MUST be set in session.properties
     * for this to work.  If it isn't, then it will simply log an error and
     * ignore this call.
     *
     * @author $Author$
     */
    public void setConstrainedCaching(boolean constrain) {
	constrainedCaching = constrain;
	
	// reload the class list
	if(constrainedCaching == true) {
	    loadChannelClasses();
	}
    }

    
    /**
     * Simple method to load the list of channel classes,
     * turn them into Class objects, and store them for later.
     * It gets this list from the SessionManager (option
     * session.memory.cached_channels) and parses it.  It expects
     * the format of this option to be a list of class names 
     * separated by commas (no spaces allowed).
     *
     * This is only called by the constructor if the constrained channel
     * caching options is set to "yes".
     *
     * @author $Author$
     * @see ChannelCache()
     * 
     */
    protected void loadChannelClasses() {
	// get the list of channels from the SessionManager
	String classSetString = SessionManager.getConfiguration("session.memory.cached_channels");

	// parse the list and create the Class objects
	StringTokenizer stok = new StringTokenizer(classSetString, ",");

	while(stok.hasMoreTokens()) {
	    // create the class and add it to our list
	    String currentClassName = stok.nextToken().trim();
	    try {
		Class currentClass = Class.forName(currentClassName);
		classSet.add(currentClass);
		Logger.log(Logger.DEBUG, "ChannelCache:  caching channels of class type " + currentClass);
	    } catch (ClassNotFoundException e) {
		Logger.log(Logger.ERROR, "ChannelCache:  Class specified in session.properties does not exist: " + currentClassName);

	    }
	}
    }



    /**
     * Get a unique identifier for the channel instance.
     *
     * @author $Author$
     * @param channel object from layout XML
     * @return a unique identifier for the channel instance
     */
    public String getChannelID (org.jasig.portal.layout.IChannel channel)
    {
	try  {
	    String sChannelInstanceID = channel.getInstanceIDAttribute ();
	    
	    if (sChannelInstanceID == null)
		throw new Exception ("Channel instance ID not found in layout xml.");
	    
	    return sChannelInstanceID;
	} catch (Exception e)  {
	    Logger.log (Logger.ERROR, e);
	}
	return null;
    }


    /**
     * Gets a channel from the cache based on a IChannel instance passed to
     * it.  This method will create the channel if it doesn't exist in the
     * cache already.
     *
     * @author $Author$
     */
    public IChannel getChannel(org.jasig.portal.layout.IChannel channel) {
	org.jasig.portal.IChannel ch = null;

	try {
		String sClass = channel.getAttribute ("class");
		
		String sKey = getChannelID (channel);
		ch = getChannel(sKey);
		
		if (ch == null) {
		    ch = instantiateChannel(channel, sKey, sClass);
		    cacheChannel(sKey, ch);
		}
		
	} catch (Exception e)   {
	    Logger.log (Logger.ERROR, e);

	    return null;
	}

	return ch;
    }


    /**
     * Creates and instance of a channel and configures it 
     * based on the configuration from the XML document.
     *
     * @author $Author$
     */
    protected org.jasig.portal.IChannel instantiateChannel(org.jasig.portal.layout.IChannel channel, String sKey, String sClass) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
	// Load this channel's parameters into the channel config object
	ChannelConfig chConfig = new ChannelConfig ();
	org.jasig.portal.layout.IParameter[] parameters = channel.getParameters ();

	org.jasig.portal.IChannel ch = null;

	if (parameters != null)
	    {
		for (int iParam = 0; iParam < parameters.length; iParam++)
		    {
			String sParamName = parameters[iParam].getAttribute ("name");
			String sParamValue = parameters[iParam].getAttribute ("value");
			chConfig.setParameter (sParamName, sParamValue);
		    }
		
		chConfig.setChannelID (sKey);
	    }
	
	// Get new instance of channel
	Object channelObject = Class.forName (sClass).newInstance ();
	
	// If necessary, wrap an IXMLChannel to be compatible with 1.0's IChannel
	if (channelObject instanceof org.jasig.portal.IChannel)   {
		ch = (org.jasig.portal.IChannel) channelObject;
	} else if(channelObject instanceof org.jasig.portal.IXMLChannel) {
	    ch = new XMLChannelWrapper ((org.jasig.portal.IXMLChannel) channelObject);
	}
	
	// Send the channel its parameters
	ch.init (chConfig);

	return ch;
    }




    /**
     * Gets a channel from the cache based on the channel ID.
     * It will create the channel if it doesn't exist already.
     * TODO:  Find out if other users of getChannel expect it to
     * NOT create the channel.
     *
     * @author $Author$
     */
    public org.jasig.portal.IChannel getChannel(String sChannelID) {
	org.jasig.portal.IChannel ch = null;
	
	try
	    {
		// Uncomment this line to enable channel instance caching
		//  Caching channels may cause memory problems
		ch = (org.jasig.portal.IChannel) channelCache.get (sChannelID);
		
		return ch;
	    }
	catch (Exception e)
	    {
		Logger.log (Logger.ERROR, e);
	    }
	return null;
    }
    


    /**
     * Removes a channel from the cache, and from the user's list.
     * If it is requested again later, it will of course be reloaded.
     * 
     * @author $Author$
     */
    public void removeChannel(String sChannelID) {
	channelCache.remove(sChannelID);
    }
    

    /**
     * Puts the channel in the cache (if necessary).  This method
     * will operate differently depending on* how channel caching 
     * is configured.  If caching is constrained, then it will only
     * cache those channels who's class type (instanceof) matches
     * one in the classList variable.
     *
     * If caching is not constrained, then the channel will be
     * cached automatically.
     *
     * This will replace any channel that is currently in the cache,
     * so only call this when you really mean to either add a new
     * channel to the cache, or you want to replace one that is already
     * in the cache.
     *
     * @author $Author$
     */
    protected void cacheChannel(String sChannelID, IChannel channel) {

	if(constrainedCaching) {

	    // we have to do two different kinds of processing, depending on whether
	    // we are dealing with an XMLChannelWrapper or not

	    if(channel instanceof XMLChannelWrapper) {
		XMLChannelWrapper wrapper = (XMLChannelWrapper)channel;
		IXMLChannel xmlchannel = wrapper.getXMLChannel();

		
		if(classSet.contains(xmlchannel.getClass())) {
		    channelCache.put(sChannelID, channel);
		}

	    } else {
		if(classSet.contains(channel.getClass())) {
		    channelCache.put(sChannelID, channel);
		}
	    }
	} else {
	    // we are caching everything, so just cache it
	    channelCache.put(sChannelID, channel);
	}
    }
    

}





