/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support.legacy;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.jasig.portal.ResourceMissingException;
import org.jasig.portal.utils.ResourceLoader;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

/**
 * Testcase for PersonDirXmlParser.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class PersonDirXmlParserTest extends TestCase {
    
    /**
     * Test parsing a PersonDirInfo XML file that defines a JDBC 
     * attribute source.
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws ResourceMissingException
     */
    public void testJdbc() throws ResourceMissingException, IOException, ParserConfigurationException, SAXException {
        Document doc = 
            ResourceLoader.getResourceAsDocument(this.getClass(), "jdbcPersonDir.xml");
        List personDirInfos = PersonDirXmlParser.getPersonDirInfos(doc);
        assertEquals(1, personDirInfos.size());
        PersonDirInfo pdi = (PersonDirInfo) personDirInfos.get(0);
        
        assertTrue(pdi.isJdbc());
        assertFalse(pdi.isLdap());
        
        assertEquals(getJdbcPersonDirInfo(), pdi);
        
    }
    
    /**
     * Test parsing a PersonDirInfo that defines a JDBC source that references a
     * DataSource configured in RDBMServices.
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws ResourceMissingException
     */
    public void testJdbcRef() throws ResourceMissingException, IOException, ParserConfigurationException, SAXException {
        Document doc = 
            ResourceLoader.getResourceAsDocument(this.getClass(), "jdbcRefPersonDir.xml");
        List personDirInfos = PersonDirXmlParser.getPersonDirInfos(doc);
        assertEquals(1, personDirInfos.size());
        PersonDirInfo pdi = (PersonDirInfo) personDirInfos.get(0);
        
        assertTrue(pdi.isJdbc());
        assertFalse(pdi.isLdap());
        
        assertEquals(getJdbcRefPersonDirInfo(), pdi);
    }
    
    public void testLdap() throws ResourceMissingException, IOException, ParserConfigurationException, SAXException {
        Document doc = 
            ResourceLoader.getResourceAsDocument(this.getClass(), "ldapPersonDir.xml");
        List personDirInfos = PersonDirXmlParser.getPersonDirInfos(doc);
        assertEquals(1, personDirInfos.size());
        PersonDirInfo pdi = (PersonDirInfo) personDirInfos.get(0);
        
        assertFalse(pdi.isJdbc());
        assertTrue(pdi.isLdap());
        
        assertEquals(getLdapPersonDirInfo(), pdi);
        
    }
    
    /**
     * Test parsing a PersonDirs.xml that defines an LDAP attribute source that
     * references LdapServices to identify the actual LDAP server.
     * @throws ResourceMissingException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public void testLdapRef() throws ResourceMissingException, IOException, ParserConfigurationException, SAXException {
        Document doc = 
            ResourceLoader.getResourceAsDocument(this.getClass(), "ldapRefPersonDir.xml");
        List personDirInfos = PersonDirXmlParser.getPersonDirInfos(doc);
        assertEquals(1, personDirInfos.size());
        PersonDirInfo pdi = (PersonDirInfo) personDirInfos.get(0);
        
        assertFalse(pdi.isJdbc());
        assertTrue(pdi.isLdap());
        
        assertEquals(getLdapRefPersonDirInfo(), pdi);
    }
    
    /**
     * Test proper parsing of a file declaring several PersonDirInfo person
     * attribute sources.
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws ResourceMissingException
     */
    public void testSeveralSources() throws ResourceMissingException, IOException, ParserConfigurationException, SAXException {
        Document doc = 
            ResourceLoader.getResourceAsDocument(this.getClass(), "severalPersonDir.xml");
        List personDirInfos = PersonDirXmlParser.getPersonDirInfos(doc);
        
        assertEquals(4, personDirInfos.size());
        
        assertEquals(getJdbcPersonDirInfo(), personDirInfos.get(0));
        assertEquals(getJdbcRefPersonDirInfo(), personDirInfos.get(1));
        assertEquals(getLdapPersonDirInfo(), personDirInfos.get(2));
        assertEquals(getLdapRefPersonDirInfo(), personDirInfos.get(3));
        
    }
    
    /**
     * Test that invoking getPersonDirInfos() on a null document
     * fails with an IllegalArgumentException.
     */
    public void testNullArg() {
        try {
            PersonDirXmlParser.getPersonDirInfos(null);
        } catch (IllegalArgumentException iae) {
            // good
            return;
        }
        fail("Should have throws IAE on null argument.");
    }

    /**
     * Test that invoking PersonDirXmlParser on a file containing
     * zero PersonDirInfo declarations results in the empty list.
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws ResourceMissingException
     */
    public void testZeroPDIs() throws ResourceMissingException, IOException, ParserConfigurationException, SAXException {
        Document doc = 
            ResourceLoader.getResourceAsDocument(this.getClass(), "zeroPersonDir.xml");
        List personDirInfos = PersonDirXmlParser.getPersonDirInfos(doc);
        assertTrue(personDirInfos.isEmpty());
    }
    
    /**
     * Test that invoking PersonDirXmlParser on a file having
     * nothing to do with PersonDirInfo results in empty list of PDIs.
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws ResourceMissingException
     */
    public void testBadXml() throws ResourceMissingException, IOException, ParserConfigurationException, SAXException {
    	 Document doc = 
            ResourceLoader.getResourceAsDocument(this.getClass(), "bogus.xml");
        List personDirInfos = PersonDirXmlParser.getPersonDirInfos(doc);
        assertTrue(personDirInfos.isEmpty());

    }
    
    /**
     * Get a PersonDirInfo representing a query against a directly-configured
     * JDBC attribute source.
     * @return Returns the jdbcPersonDirInfo.
     */
    static PersonDirInfo getJdbcPersonDirInfo() {
        PersonDirInfo jdbcPersonDirInfo = new PersonDirInfo();
        jdbcPersonDirInfo = new PersonDirInfo();
        jdbcPersonDirInfo.setDriver("org.hsql.jdbcDriver");
        jdbcPersonDirInfo.setUrl("jdbc:HypersonicSQL:hsql://localhost:8887");
        jdbcPersonDirInfo.setLogonid("username");
        jdbcPersonDirInfo.setLogonpassword("password");
        jdbcPersonDirInfo.setUidquery("select * from schema.table where netid=?");
        
        // these are the names of attributes as they will be accessed and consumed
        // by clients of PersonDirectory
        String [] jdbcAttributeAliases = {
                "displayName",
                "sn",
                "uPortalAffiliation",
                "givenName",
                "email",
                "alias",
                "personType",
                "YaleCollege",
                "ClassYear",
                "StudentStatus",
                "uPortalTemplateUserName",
                "netid",
                "eduPersonPrincipalName",
                "WorkPhone",
        };
        jdbcPersonDirInfo.setAttributealiases(jdbcAttributeAliases);
        
        // in the case of JDBC, these are database column names
        // but PDI regards them as a generic list of names of attributes in the
        // attribute source
        String[] jdbcAttributeNames = {
                "first_last",
                "last_name",
                "role",
                "first_name",
                "email_alias",
                "alias",
                "person_type",
                "college",
                "class_year",
                "student_status",
                "template",
                "netid",
                "netid",
                "work_phone"                
        };
        
        jdbcPersonDirInfo.setAttributenames(jdbcAttributeNames);
        
        return jdbcPersonDirInfo;
    }
   
    /**
     * Obtain an example PersonDirInfo which represents using a query against
     * an RDBMServices-configured JDBC DataSource to obtain person
     * attribute information.
     * @return Returns a suitable example PersonDirInfo.
     */
    static PersonDirInfo getJdbcRefPersonDirInfo() {
        PersonDirInfo jdbcRefPersonDirInfo = new PersonDirInfo();
        
        jdbcRefPersonDirInfo.setResRefName("PersonDb");
        
        jdbcRefPersonDirInfo.setUidquery("select * from schema.table where netid=?");
        
        // in the case of JDBC, these are database column names
        // but PDI regards them as a generic list of names of attributes in the
        // attribute source
        String[] jdbcRefAttributeNames = {
                "first_last",
                "last_name",
                "role",
                "first_name",
                "email_alias",
                "alias",
                "person_type",
                "college",
                "class_year",
                "student_status",
                "template",
                "netid",
                "netid",
                "work_phone"                
        };
        
        jdbcRefPersonDirInfo.setAttributenames(jdbcRefAttributeNames);
        
        // these are the names of attributes as they will be accessed and consumed
        // by clients of PersonDirectory
        
        String [] jdbcRefAttributeAliases = {
                "displayName",
                "sn",
                "uPortalAffiliation",
                "givenName",
                "email",
                "alias",
                "personType",
                "YaleCollege",
                "ClassYear",
                "StudentStatus",
                "uPortalTemplateUserName",
                "netid",
                "eduPersonPrincipalName",
                "WorkPhone",
        };
        
        jdbcRefPersonDirInfo.setAttributealiases(jdbcRefAttributeAliases);
        
        return jdbcRefPersonDirInfo;
    }

    /**
     * Get an exple PersonDirInfo object representing an LDAP person attribute
     * source which is configured directly in the PersonDirInfo (as opposed to
     * by reference to an LdapServices LDAP connection).
     * @return Returns a suitable example PersonDirInfo
     */
    static PersonDirInfo getLdapPersonDirInfo() {
        PersonDirInfo ldapPersonDirInfo = new PersonDirInfo();
        
        ldapPersonDirInfo.setUrl("ldaps://mrfrumble.its.yale.edu:389/o=yale.edu");
        ldapPersonDirInfo.setLogonid("someUser");
        ldapPersonDirInfo.setLogonpassword("somePassword");
        ldapPersonDirInfo.setUidquery("(uid={0})");
        
        // these are the names of attributes as they will be accessed and consumed
        // by clients of PersonDirectory
        ldapPersonDirInfo.setAttributealiases(new String[] { "emailfromldap" });
        
        // in the case of LDAP, these are attribute names in the LDAP store
        // but PDI regards them as a generic list of names of attributes in the
        // attribute source
        ldapPersonDirInfo.setAttributenames(new String[] { "mail"});
        
        ldapPersonDirInfo.setUsercontext("");
        return ldapPersonDirInfo;
    }
  
    /**
     * Get an example PersonDirInfo representing using an LdapServices-configured
     * LDAP server to obtain person attributes.
     * @return Returns a suitable example PersonDirInfo.
     */
    static PersonDirInfo getLdapRefPersonDirInfo() {
        
        PersonDirInfo ldapRefPersonDirInfo = new PersonDirInfo();
        
        ldapRefPersonDirInfo.setLdapRefName("DEFAULT_LDAP_SERVER");
        
        ldapRefPersonDirInfo.setUidquery("(cn={0})");
        
        // in the case of LDAP, these are names of attributes in the LDAP store
        // but PDI regards them as a generic list of names of attributes in the
        // attribute source
        String[] ldapRefAttributeNames = {
                "eduPersonPrimaryAffiliation",
                "eduPersonAffiliation",
                "eduPersonNickname",
                "eduPersonOrgDN",
                "eduPersonOrgUnitDN",
                "eduPersonPrimaryAffiliation",
                "eduPersonPrincipalName",
                "c",
                "cn",
                "description",
                "displayName",
                "facsimileTelephoneNumber",
                "givenName"         
        };
        
        ldapRefPersonDirInfo.setAttributenames(ldapRefAttributeNames);
        
        // these are the names of attributes as they will be accessed and consumed
        // by clients of PersonDirectory
        
        String [] ldapRefAttributeAliases = {
               "uPortalTemplateUserName",
               "eduPersonAffiliation",
               "eduPersonNickname",
               "eduPersonOrgDN",
               "eduPersonOrgUnitDN",
               "eduPersonPrimaryAffiliation",
               "eduPersonPrincipalName",
               "c",
               "cn",
               "description",
               "displayName",
               "facsimileTelephoneNumber",
               "givenName"
        };
        
        ldapRefPersonDirInfo.setAttributealiases(ldapRefAttributeAliases);
        
        ldapRefPersonDirInfo.setUsercontext("cn=Users");
        
        return ldapRefPersonDirInfo;
    }
   
}