/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import java.lang.reflect.Constructor;

import org.jasig.portal.properties.PropertiesManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicLong;

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


    private static final Log LOG = LogFactory.getLog(ChannelRendererFactory.class);

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
        String keyBase, final AtomicLong activeThreads, final AtomicLong maxActiveThreads
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

            if (LOG.isDebugEnabled())
                LOG.debug("ChannelRendererFactory::newInstance(" + keyBase +
                        ") : about to construct channel renderer factory: " +
                        factoryClassName);

            // Get the string argument constructor for the class.
            Constructor ctor = Class.forName(
                factoryClassName
                ).getConstructor( new Class[]{ String.class, AtomicLong.class, AtomicLong.class } );

            // Reflectively construct the factory with the key base argument.
            factory = (IChannelRendererFactory)ctor.newInstance(
                new Object[]{ keyBase, activeThreads, maxActiveThreads}
                );

            if (LOG.isDebugEnabled())
                LOG.debug("ChannelRendererFactory::newInstance(" + keyBase +
                        ") : constructed channel renderer factory: " + factoryClassName);
        }
        catch( Exception x )
        {
            // Log the failure.
            LOG.error(
                "ChannelRendererFactory::newInstance(" + keyBase + ") : failed to construct factory: " + factoryClassName,
                x
                );
        }

        return factory;
    }
}
