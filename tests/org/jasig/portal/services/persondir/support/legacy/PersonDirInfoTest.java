/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support.legacy;

import org.jasig.portal.services.persondir.support.legacy.PersonDirInfo;
import org.jasig.portal.services.persondir.support.legacy.PersonDirXmlParserTest;

import junit.framework.TestCase;

/**
 * Testcases for PersonDirInfo.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class PersonDirInfoTest extends TestCase {

    /**
     * Test that properly identifies JDBC PersonDirInfo entries.
     */
    public void testJdbcRef() {
        PersonDirInfo pdi = new PersonDirInfo();
        pdi.setResRefName("rdbmservices_managed_datasource");
        assertTrue(pdi.isJdbc());
        assertFalse(pdi.isLdap());
    }
    
    /**
     * Test that JDBC configured within PersonDirInfo is properly identified
     * as JDBC.
     */
    public void testJdbcUrl() {
        PersonDirInfo pdi = new PersonDirInfo();
        pdi.setUrl("jdbc://someserver:45");
        assertTrue(pdi.isJdbc());
        assertFalse(pdi.isLdap());
    }

    /**
     * Test that properly identifies references to LdapServices configured
     * LDAP servers as LDAP servers.
     */
    public void testLdapRef() {
        PersonDirInfo pdi = new PersonDirInfo();
        pdi.setLdapRefName("ldapservices_managed_ldap");
        assertTrue(pdi.isLdap());
        assertFalse(pdi.isJdbc());
    }
    
    /**
     * Test that properly identifies LDAP urls as LDAP server entries.
     */
    public void testLdapUrl() {
        PersonDirInfo pdi = new PersonDirInfo();
        pdi.setUrl("ldap://someserver");
        assertTrue(pdi.isLdap());
        assertFalse(pdi.isJdbc());
    }
    
    /**
     * Test that the validate() method of an entirely new PDI
     * throws IllegalStateException.
     */
    public void testValidateBare() {
        PersonDirInfo pdi = new PersonDirInfo();
        assertNotNull(pdi.validate());
    }
    
    /**
     * Test that a PersonDirInfo which has no uid query set throws
     * IllegalStateException on validation attempt.
     */
    public void testValidateNoUid() {
        PersonDirInfo pdi = new PersonDirInfo();
        pdi.setLdapRefName("someserver");
        assertNotNull(pdi.validate());
    }
    
    /**
     * Test that validating valid PDIs throws no exception.
     */
    public void testValids() {
        assertNull(PersonDirXmlParserTest.getJdbcPersonDirInfo().validate());
        assertNull(PersonDirXmlParserTest.getJdbcRefPersonDirInfo().validate());
        assertNull(PersonDirXmlParserTest.getLdapPersonDirInfo().validate());
        assertNull(PersonDirXmlParserTest.getLdapRefPersonDirInfo().validate());
    }

    /**
     * Test that setting ldapRef after setting the url results in IllegalStateException.
     */
    public void testSetUrlAndLdapRef() {
        PersonDirInfo pdi = new PersonDirInfo();
        pdi.setUrl("jdbc://someserver:45");
        try {
            pdi.setLdapRefName("foo");
        } catch (IllegalStateException ise) {
            // good
            return;
        }
        fail("Should have thrown ISE when trying to set refname on a " +
                "PDI which already has a URL set.");
    }
    
}