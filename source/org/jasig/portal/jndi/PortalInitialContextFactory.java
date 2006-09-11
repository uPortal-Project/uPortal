/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
 *  See license distributed with this file and
 *  available online at http://www.uportal.org/license.html
 */


package org.jasig.portal.jndi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import tyrex.naming.MemoryContext;

/**
 * PortalInitialContextFactory
 * @author Bernie Durfee, bdurfee@interactivebusiness.com
 * @version $Revision$
 */
public class PortalInitialContextFactory implements InitialContextFactory
{
	private static MemoryContext m_context;
	
	public Context getInitialContext(Hashtable environment)
	{
		// Don't give the context if no environment was passed in
		if(environment == null)
		{
			return(null);
		}
		
		try
		{
			initContext(environment);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return(null);
		}
		
		// The environment hashtable can be scanned here for authorization information
		
		// Maybe...
		//  Context.SECURITY_PRINCIPAL should be the class name?
		//  Context.SECURITY_CREDENTIALS should be the passed in ticket?
		
		return(m_context);
	}
	
	private static synchronized void initContext(Hashtable environment) throws NamingException {
		// Create the new context with the environment only once
		if(m_context == null)
		{
			m_context = new MemoryContext(environment);
		}
	}
}
