/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.node;

import java.io.PrintWriter;

import junit.framework.TestCase;

import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalException;
import org.jasig.portal.channels.BaseChannel;
import org.jasig.portal.channels.error.CError;
import org.jasig.portal.channels.portlet.IPortletAdaptor;

/**
 * JUnit testcase for UserLayoutChannelDescription.
 */
public class UserLayoutChannelDescriptionTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testIsPortlet() {
		// test that when channel class is not set
		// channel description does not think it's a portlet
		UserLayoutChannelDescription description = new UserLayoutChannelDescription();
		assertFalse(description.isPortlet());
		
		// test that when channel class is set to a non-portlet IChannel
		// channel description does not think its a portlet
		description.setClassName(CError.class.getName());
		assertFalse(description.isPortlet());
		
		// test that when channel class is set to a non-existing class
		// implementation does not think it's a portlet
		description.setClassName("not.a.real.package.DoesNotExist");
		assertFalse(description.isPortlet());
		
		// test that when channel class is set to CPortletAdapter
		// channel description thinks its a portlet
//		description.setClassName(CPortletAdapter.class.getName());
//		assertTrue(description.isPortlet());
		
		// test that when channel class is set to another IPortletAdaptor
		// channel description thinks its a portlet
		description.setClassName(AnotherPortletAdaptor.class.getName());
		assertTrue(description.isPortlet());
	}

	/**
	 * Test class used to test recognition of non-CPortletAdaptor 
	 * IPortletAdaptors.
	 */
	private class AnotherPortletAdaptor 
		extends BaseChannel 
		implements IPortletAdaptor {

        /* (non-Javadoc)
         * @see org.jasig.portal.IPrivileged#setPortalControlStructures(org.jasig.portal.PortalControlStructures)
         */
        public void setPortalControlStructures(PortalControlStructures pcs) throws PortalException {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see org.jasig.portal.channels.portlet.IPortletAdaptor#processAction()
         */
        public void processAction() throws PortalException {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see org.jasig.portal.ICacheable#generateKey()
         */
        public ChannelCacheKey generateKey() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.jasig.portal.ICacheable#isCacheValid(java.lang.Object)
         */
        public boolean isCacheValid(Object validity) {
            // TODO Auto-generated method stub
            return false;
        }

        /* (non-Javadoc)
         * @see org.jasig.portal.ICharacterChannel#renderCharacters(java.io.PrintWriter)
         */
        public void renderCharacters(PrintWriter pw) throws PortalException {
            // TODO Auto-generated method stub
            
        }
	}
	
	
}
