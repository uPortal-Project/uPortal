/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security.provider;

import java.util.Enumeration;

import org.jasig.portal.security.IAdditionalDescriptor;
import org.jasig.portal.security.IOpaqueCredentials;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPrincipal;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.PortalSecurityException;

import junit.framework.TestCase;

/**
 * Testcase for RestrictedPerson.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class RestrictedPersonTest extends TestCase {

    private IPerson person;
    
    private RestrictedPerson restrictedPerson;
    
    protected void setUp() {
        IPerson fullPerson = new PersonImpl();
        fullPerson.setAttribute("att1", "val1");
        fullPerson.setAttribute("att2", "val2");
        
        fullPerson.setFullName("George Washington");
        
        fullPerson.setID(27);
        
        fullPerson.setSecurityContext(new DummySecurityContext());
        
        
        this.person = fullPerson;
        this.restrictedPerson = new RestrictedPerson(fullPerson);
    }
    
    protected void tearDown() {
        this.person = null;
        this.restrictedPerson = null;
    }
    
    /**
     * Test that getSecurityContext of RestrictedPerson
     * returns null.
     */
    public void testGetSecurityContext() {
        assertNull(this.restrictedPerson.getSecurityContext());
    }
    
    /**
     * Test that setSecurityContext does not change the security context.
     */
    public void testSetSecurityContext() {
        ISecurityContext baselineContext = this.person.getSecurityContext();
        assertNotNull(baselineContext);
        
        assertNull(this.restrictedPerson.getSecurityContext());
        
        this.restrictedPerson.setSecurityContext(new DummySecurityContext());
        assertNull(this.restrictedPerson.getSecurityContext());
        
        assertSame(baselineContext, this.person.getSecurityContext());
    }
    
    /**
     * Test that the getEntityIdentifier() method of RestrictedPerson
     * always returns null.
     */
    public void testGetEntityIdentifier() {
        assertNotNull(this.person.getEntityIdentifier());
        assertNull(this.restrictedPerson.getEntityIdentifier());
    }
    
    /**
     * Test that the setAttribute() method of RestrictedPerson has no effect.
     */
    public void testSetAttribute() {
        
        // test that new attributes do not write
        
        assertNull(this.person.getAttribute("notSet"));
        assertNull(this.restrictedPerson.getAttribute("notSet"));
        
        this.restrictedPerson.setAttribute("notSet", "stillNotSetWeHope");
        
        assertNull(this.person.getAttribute("notSet"));
        assertNull(this.restrictedPerson.getAttribute("notSet"));

        
        // test that existing attribute are not overwritten
        
        assertEquals("val1", this.person.getAttribute("att1"));
        assertEquals("val1", this.restrictedPerson.getAttribute("att1"));
        
        this.restrictedPerson.setAttribute("att1", "bogus");
        
        assertEquals("val1", this.person.getAttribute("att1"));
        assertEquals("val1", this.restrictedPerson.getAttribute("att1"));
        
    }

    /**
     * Test that the setFullName method of RestrictedPerson has no effect.
     */
    public void testSetFullname() {
        assertEquals("George Washington", this.restrictedPerson.getFullName());
        assertEquals("George Washington", this.person.getFullName());

        this.restrictedPerson.setFullName("Peter Furmonavicius");
        
        assertEquals("George Washington", this.restrictedPerson.getFullName());
        assertEquals("George Washington", this.person.getFullName());
    }
    
    /**
     * Test that the RestrictedPerson setID method has no effect.
     */
    public void testSetID() {
        assertEquals(27, this.person.getID());
        assertEquals(27, this.restrictedPerson.getID());
        
        this.restrictedPerson.setID(12);
        
        assertEquals(27, this.person.getID());
        assertEquals(27, this.restrictedPerson.getID());
    }
    
    /**
     * A dummy ISecurityContext implementation.
     * Useful as a non-null ISecurityContext, but otherwise no methods do
     * anything.
     */
    private class DummySecurityContext 
        implements ISecurityContext {

        /* (non-Javadoc)
         * @see org.jasig.portal.security.ISecurityContext#getAuthType()
         */
        public int getAuthType() {
            return 0;
        }

        /* (non-Javadoc)
         * @see org.jasig.portal.security.ISecurityContext#getPrincipalInstance()
         */
        public IPrincipal getPrincipalInstance() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.jasig.portal.security.ISecurityContext#getOpaqueCredentialsInstance()
         */
        public IOpaqueCredentials getOpaqueCredentialsInstance() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.jasig.portal.security.ISecurityContext#authenticate()
         */
        public void authenticate() throws PortalSecurityException {
            // do nothing -- dummy implementation
        }

        /* (non-Javadoc)
         * @see org.jasig.portal.security.ISecurityContext#getPrincipal()
         */
        public IPrincipal getPrincipal() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.jasig.portal.security.ISecurityContext#getOpaqueCredentials()
         */
        public IOpaqueCredentials getOpaqueCredentials() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.jasig.portal.security.ISecurityContext#getAdditionalDescriptor()
         */
        public IAdditionalDescriptor getAdditionalDescriptor() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.jasig.portal.security.ISecurityContext#isAuthenticated()
         */
        public boolean isAuthenticated() {
            return false;
        }

        /* (non-Javadoc)
         * @see org.jasig.portal.security.ISecurityContext#getSubContext(java.lang.String)
         */
        public ISecurityContext getSubContext(String ctx) throws PortalSecurityException {
            return null;
        }

        /* (non-Javadoc)
         * @see org.jasig.portal.security.ISecurityContext#getSubContexts()
         */
        public Enumeration getSubContexts() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.jasig.portal.security.ISecurityContext#getSubContextNames()
         */
        public Enumeration getSubContextNames() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.jasig.portal.security.ISecurityContext#addSubContext(java.lang.String, org.jasig.portal.security.ISecurityContext)
         */
        public void addSubContext(String name, ISecurityContext ctx) throws PortalSecurityException {
            // do nothing -- dummy implementation
        }
        
    }
    
}
