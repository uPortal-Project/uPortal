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

package org.jasig.portal;

import java.lang.reflect.Constructor;

import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.services.LogService;

/**
 * <p>The <code>ChannelRendererFactory</code> creates
 * <code>IChannelRendererFactory</code> objects which are used to construct
 * <code>IChannelRenderer</code> objects with implementation specific
 * parameters.</p>
 *
 * <p>This factory design is motivated by the need for different kinds of
 * <code>IChannelRenderer</code> implementations including single-threaded
 * serial channel renderers, multi-threaded parallel channel renderers, and
 * thread-pool serial/parallel channel renderers.</p>
 *
 * @author <a href="mailto:jnielsen@sct.com">Jan Nielsen</a>
 *
 * @version $Revision$
 **/
public final class ChannelRendererFactory
{
    /** <p> Class version identifier.</p> */
    public final static String RCS_ID = "@(#) $Header$";

    /**
     * <p>Creates a new instance of a channel renderer factory object. This
     * factory looks for the property <code>keyBase + ".factoryClassName"</code>
     * in the configuration system and then reflectively constructs the
     * factory class with a single string argument constructor, passing in
     * the <code>keyBase</code> as the argument.</p>
     *
     * @param keyBase configuration base key
     *
     * @return new instance of a channel renderer for the specified channel,
     * or <code>null</code>
     **/
    public static final IChannelRendererFactory newInstance(
        String keyBase
        )
    {
        IChannelRendererFactory factory = null;
        String factoryClassName = null;

        try
        {
            // Retrieve the factory class implementation from configuration.
            factoryClassName = PropertiesManager.getProperty(
                keyBase + ".ChannelRendererFactory.className"
                );

            LogService.log(
                LogService.DEBUG,
                "ChannelRendererFactory::newInstance(" + keyBase + ") : about to construct channel renderer factory: " + factoryClassName
                );

            // Get the string argument constructor for the class.
            Constructor ctor = Class.forName(
                factoryClassName
                ).getConstructor( new Class[]{ String.class } );

            // Reflectively construct the factory with the key base argument.
            factory = (IChannelRendererFactory)ctor.newInstance(
                new Object[]{ keyBase }
                );

            // Log the success.
            LogService.log(
                LogService.DEBUG,
                "ChannelRendererFactory::newInstance(" + keyBase + ") : constructed channel renderer factory: " + factoryClassName
                );
        }
        catch( Exception x )
        {
            // Log the failure.
            LogService.log(
                LogService.ERROR,
                "ChannelRendererFactory::newInstance(" + keyBase + ") : failed to construct factory: " + factoryClassName,
                x
                );
        }

        return factory;
    }
}
