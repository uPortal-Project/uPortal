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

package org.jasig.portal.services;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.WeakHashMap;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.jasig.portal.RDBMServices;
import org.jasig.portal.UserIdentityStoreFactory;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.PersonFactory;
import org.jasig.portal.security.provider.RestrictedPerson;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Extract eduPerson-like attributes from whatever LDAP directory or JDBC
 * database happens to be lying around in the IT infrastructure.
 * <p>
 * Multivalued attributes are supported as of uPortal 2.3.
 *
 * Parameterized by uPortal/properties/PersonDirs.xml.
 * Original author is Howard.Gilbert@yale.edu.
 * @author Howard Gilbert
 * @version $Revision$
 */
public class PersonDirectory {

  static Vector sources = null; // List of PersonDirInfo objects
  static Hashtable drivers = new Hashtable(); // Registered JDBC drivers
  public static HashSet propertynames = new HashSet();
  protected static Map persons = new WeakHashMap(1024);
  private static PersonDirectory instance;

  private PersonDirectory() {
    getParameters();
  }
    
  public static synchronized PersonDirectory instance() {
    if (instance == null) {
      instance = new PersonDirectory();
    }
    return instance;
  }

  public static Iterator getPropertyNamesIterator() {
    return propertynames.iterator();
  }

  /**
   * Parse XML file and create PersonDirInfo objects
   *
   * <p>Parameter file is uPortal/properties/PersonDirs.xml.
   * It contains a <PersonDirs> object with a list of
   * <PersonDirInfo> objects each defining an LDAP or JDBC source.
   *
   * <p>LDAP entry has format:
   * <pre>
   * <PersonDirInfo>
   * <url>ldap://yu.yale.edu:389/dc=itstp, dc=yale, dc=edu</url>
   * <logonid>cn=bogus,cn=Users,dc=itstp,dc=yale,dc=edu</logonid>
   * <logonpassword>foobar</logonpassword>
   * <uidquery>(cn={0})</uidquery>
   * <usercontext>cn=Users</usercontext>
   * <attributes>mail telephoneNumber</attributes>
   * </PersonDirInfo>
   * </pre>
   *
   * The logonid and logonpassword would be omitted for LDAP directories
   * that do not require a signon. The URL establishes an "initial context"
   * which acts like a root directory. The usercontext is a subdirectory
   * relative to this initial context in which to search for usernames.
   * In a lot of LDAP directories, the user context is "ou=People".
   * The username is the {0} parameter substituted into the query. The
   * query searches the subtree of objects under the usercontext, so if
   * the usercontext is the whole directory the query searches the whole
   * directory.
   *
   * <p>JDBC entry has format:
   * <pre>
   * <PersonDirInfo>
   * <url>jdbc:oracle:thin:@oracleserver.yale.edu:1521:XXXX</url>
   * <driver>oracle.jdbc.driver.OracleDriver</driver>
   * <logonid>bogus</logonid>
   * <logonpassword>foobar</logonpassword>
   * <uidquery>select * from portal_person_directory where netid=?</uidquery>
   * <attributes>last_name:sn first_name:givenName role</attributes>
   * </PersonDirInfo>
   * </pre>
   *
   * The SELECT can fetch any number of variables, but only the columns
   * named in the attributes list will be actually used. When an attribute
   * has two names, separated by a colon, the first name is used to find
   * the variable/column in the result of the query and the second is the
   * name used to store the value as an IPerson attribute. Generally we
   * recommend the use of official eduPerson names however bad they might
   * be (sn for "surname"). However, an institution may have local variables
   * that they want to include, as the Yale variable "role" that includes
   * distinctions like grad-student and non-ladder-faculty. This version of
   * this code doesn't have the ability to transform values, so if you wanted
   * to translate some bizzare collection of roles like Yale has to the
   * simpler list of eduPersonAffiliation (faculty, student, staff) then
   * join the query to a list-of-values table that does the translation.
   */
  private synchronized boolean getParameters() {
    if (sources!=null)
      return true;
    sources = new Vector();
    try  {

      // Build a DOM tree out of uPortal/properties/PersonDirs.xml
      Document doc = ResourceLoader.getResourceAsDocument(this.getClass(), "/properties/PersonDirs.xml");

      // Each directory source is a <PersonDirInfo> (and its contents)
      NodeList list = doc.getElementsByTagName("PersonDirInfo");
      for (int i=0;i<list.getLength();i++) { // foreach PersonDirInfo
        Element dirinfo = (Element) list.item(i);
        PersonDirInfo pdi = new PersonDirInfo(); // Java object holding parameters
        for (Node param = dirinfo.getFirstChild();
             param!=null; // foreach tag under the <PersonDirInfo>
             param=param.getNextSibling()) {
          if (!(param instanceof Element))
            continue; // whitespace (typically \n) between tags
          Element pele = (Element) param;
          String tagname = pele.getTagName();
          String value = XML.getElementText(pele);

          // each tagname corresponds to an object data field
          if (tagname.equals("url")) {
            pdi.url=value;
          } else if (tagname.equals("res-ref-name")) {
            pdi.ResRefName=value;
          } else if (tagname.equals("logonid")) {
            pdi.logonid=value;
          } else if (tagname.equals("driver")) {
            pdi.driver=value;
          } else if (tagname.equals("logonpassword")) {
            pdi.logonpassword=value;
          } else if (tagname.equals("uidquery")) {
            pdi.uidquery=value;
          } else if (tagname.equals("fullnamequery")) {
            pdi.fullnamequery=value;
          } else if (tagname.equals("usercontext")) {
            pdi.usercontext=value;
         } else if (tagname.equals("timeout")) {
            pdi.ldaptimelimit=Integer.parseInt(value);
          } else if (tagname.equals("attributes")) {
            NodeList anodes = pele.getElementsByTagName("attribute");
            int anodecount = anodes.getLength();
            if (anodecount!=0) {
              pdi.attributenames = new String[anodecount];
              pdi.attributealiases = new String[anodecount];
              for (int j =0; j<anodecount;j++) {
                Element anode = (Element) anodes.item(j);
                NodeList namenodes = anode.getElementsByTagName("name");
                String aname = "$$$";
                if (namenodes.getLength()!=0)
                  aname= XML.getElementText((Element)namenodes.item(0));
                pdi.attributenames[j]=aname;
                NodeList aliasnodes = anode.getElementsByTagName("alias");
                if (aliasnodes.getLength()==0) {
                  pdi.attributealiases[j]=aname;
                } else {
                  pdi.attributealiases[j]=XML.getElementText((Element)aliasnodes.item(0));
                }
              }
            } else {
              // The <attributes> tag contains a list of names
              // and optionally aliases each in the form
              // name[:alias]
              // The name is an LDAP property or database column name.
              // The alias, if it exists, is an eduPerson property that
              // corresponds to the previous LDAP or DBMS name.
              // If no alias is specified, the eduPerson name is also
              // the LDAP or DBMS column name.
              StringTokenizer st = new StringTokenizer(value);
              int n = st.countTokens();
              pdi.attributenames = new String[n];
              pdi.attributealiases = new String[n];
              for (int k=0;k<n;k++) {
                String tk = st.nextToken();
                int pos =tk.indexOf(':');
                if (pos>0) { // There is an alias
                  pdi.attributenames[k]=tk.substring(0,pos);
                  pdi.attributealiases[k]=tk.substring(pos+1);

                } else { // There is no alias
                  pdi.attributenames[k]=tk;
                  pdi.attributealiases[k]=tk;
                }
              }
            }
          } else {
            LogService.log(LogService.ERROR,"PersonDirectory::getParameters(): Unrecognized tag "+tagname+" in PersonDirs.xml");
          }
        }
        for (int ii=0;ii<pdi.attributealiases.length;ii++) {
          String aa = pdi.attributealiases[ii];
          propertynames.add(aa);
        }
        sources.addElement(pdi); // Add one LDAP or JDBC source to the list
      }
    }
    catch(Exception e)
    {
      sources = null;
      LogService.log(LogService.WARN,"PersonDirectory::getParameters(): properties/PersonDirs.xml is not available, directory searching disabled.");
      return false;
    }
    return true;
  }

  /**
   * Run down the list of LDAP or JDBC sources and extract info from each
   */
  public Hashtable getUserDirectoryInformation (String username){
    Hashtable attribs = new Hashtable();
    for (int i=0;i<sources.size();i++) {
      PersonDirInfo pdi = (PersonDirInfo) sources.elementAt(i);
      if (pdi.disabled)
        continue;
      // new format of personDirs.xml is type attribute to distinguish
      // DataSource from ldap source
      if (pdi.ResRefName!=null && pdi.ResRefName.length()>0)
        processJdbcDir(username, pdi,attribs);
      else if (pdi.url.startsWith("ldap"))
        processLdapDir(username, pdi,attribs);
      else if (pdi.url.startsWith("jdbc:"))
        processJdbcDir(username, pdi,attribs);
    }
    return attribs;
  }

  public void getUserDirectoryInformation(String uid,  IPerson m_Person) {
    Hashtable attribs = this.getUserDirectoryInformation(uid);
    Enumeration en = attribs.keys();
      while (en.hasMoreElements()) {
        String key = (String)en.nextElement();
        Object value = attribs.get(key);
        m_Person.setAttribute(key,value);
      }
      persons.put(uid, m_Person);
  }

  /**
   * Extract named attributes from a single LDAP directory
   *
   * <p>Connect to the LDAP server indicated by the URL.
   * An optional userid and password will be used if the LDAP server
   * requires logon (AD does, most directories don't). The userid given
   * here would establish access privileges to directory fields for the
   * uPortal. Connection establishes an initial context (like a current
   * directory in a file system) that is usually the root of the directory.
   * Howwever, while a file system root is simply "/", a directory root is
   * the global name of the directory, like "dc=yu,dc=yale,dc=edu" or
   * "o=Yale University",c=US". Then search a subcontext where the people
   * are (cn=Users in AD, ou=People sometimes, its a local convention).
   *
   */
  void processLdapDir(String username, PersonDirInfo pdi, Hashtable attribs) {

    //JNDI boilerplate to connect to an initial context
    Hashtable jndienv = new Hashtable();
    DirContext context = null;
    jndienv.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
    jndienv.put(Context.SECURITY_AUTHENTICATION,"simple");
    if (pdi.url.startsWith("ldaps")) { // Handle SSL connections
      String newurl=pdi.url.substring(0,4) + pdi.url.substring(5);
      jndienv.put(Context.SECURITY_PROTOCOL,"ssl");
      jndienv.put(Context.PROVIDER_URL,newurl);
    }
    else {
      jndienv.put(Context.PROVIDER_URL,pdi.url);
    }
    if (pdi.logonid!=null)
      jndienv.put(Context.SECURITY_PRINCIPAL,pdi.logonid);
    if (pdi.logonpassword!=null)
      jndienv.put(Context.SECURITY_CREDENTIALS,pdi.logonpassword);
    try {
      context = new InitialDirContext(jndienv);
    } catch (NamingException nex) {
      return;
    }

    // Search for the userid in the usercontext subtree of the directory
    // Use the uidquery substituting username for {0}
    NamingEnumeration userlist = null;
    SearchControls sc = new SearchControls();
    sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
    sc.setTimeLimit(pdi.ldaptimelimit);
    Object [] args = new Object[] {username};
    try {
      userlist = context.search(pdi.usercontext,pdi.uidquery,args,sc);
    } catch (NamingException nex) {
      return;
    }

    // If one object matched, extract properties from the attribute list
    try {
      if (userlist.hasMoreElements()) {
        SearchResult result = (SearchResult) userlist.next();
        Attributes ldapattribs = result.getAttributes();
        for (int i=0;i<pdi.attributenames.length;i++) {
          Attribute tattrib = null;
          if (pdi.attributenames[i] != null)
            tattrib = ldapattribs.get(pdi.attributenames[i]);
          if (tattrib!=null) {
            // determine if this attribute is a String or a binary (byte array)
            if (tattrib.size() == 1) {
                Object att = tattrib.get();
                if (att instanceof byte[]) {
                    attribs.put(pdi.attributealiases[i],(Object)att);
                } else {
                    String value = att.toString();
                    attribs.put(pdi.attributealiases[i],value);
                }
            } else {
                // multivalued
                Vector values = new Vector();
                for (NamingEnumeration ne = tattrib.getAll(); ne.hasMoreElements(); ) {
                    Object value = ne.nextElement();
                    if (value instanceof byte[]) {
                        values.add(value);
                    } else {
                        values.add(value.toString());
                    }
                }
                attribs.put(pdi.attributealiases[i], values);
            }
          }
        }
      }
    } catch (NamingException nex) {
      ;
    }

    try {userlist.close();} catch (Exception e) {;}
    try {context.close();} catch (Exception e) {;}

  }

  /**
   * Extract data from a JDBC database
   */
  void processJdbcDir(String username, PersonDirInfo pdi, Hashtable attribs) {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {

      // Get a connection with from container
      if (pdi.ResRefName!=null && pdi.ResRefName.length()>0) {
          RDBMServices rdbmServices = new RDBMServices();
          conn = RDBMServices.getConnection(pdi.ResRefName);
          LogService.log(LogService.DEBUG,"PersonDirectory::processJdbcDir(): Looking in "+pdi.ResRefName+
            " for person attributes of "+username);
        }

      // if no resource reference found use URL to get jdbc connection
      if (conn == null) {
        // Register the driver class if it has not already been registered
        // Not agressively synchronized, duplicate registrations are OK.
        if (pdi.driver!=null && pdi.driver.length()>0) {
          if (drivers.get(pdi.driver)==null) {
            try {
              Driver driver = (Driver) Class.forName(pdi.driver).newInstance();
              DriverManager.registerDriver(driver);
              drivers.put(pdi.driver,driver);
            } catch (Exception driverproblem) {
              pdi.disabled=true;
              pdi.logged=true;
              LogService.log(LogService.ERROR,"PersonDirectory::processJdbcDir(): Cannot register driver class "+pdi.driver);
              return;
            }
          }
        }
      conn = DriverManager.getConnection(pdi.url,pdi.logonid,pdi.logonpassword);
      LogService.log(LogService.DEBUG,"PersonDirectory::processJdbcDir(): Looking in "+pdi.url+
        " for person attributes of "+username);
      }

      // Execute query substituting Username for parameter
      stmt = conn.prepareStatement(pdi.uidquery);
      
      stmt.setString(1,username);
      rs = stmt.executeQuery();
      
      // Place data from result set into a list of maps
      // representing the name/value pairs from each row
      List results = new ArrayList();
      while (rs.next()) {
          Map resultRow = new HashMap(pdi.attributenames.length);
          for (int i = 0; i < pdi.attributenames.length; i++) {
              String attName = pdi.attributenames[i];
              if (attName != null && attName.length() > 0) {
                  String attValue = rs.getString(attName);
                  if (attValue != null) {
                      resultRow.put(attName, attValue);
                  }
              }
          }
          results.add(resultRow);
      }
      
      // For each attribute name, gather the non-null values
      // (one from each result row) and determine whether the 
      // result is single or multi-valued.
      for (int i = 0; i < pdi.attributenames.length; i++) {
          String attAlias = pdi.attributealiases[i];
          String attName = pdi.attributenames[i];
          Set attValues = new HashSet();
          for (Iterator iter = results.iterator(); iter.hasNext();) {
              Map resultRow = (Map)iter.next();
              String attValue = (String)resultRow.get(attName);
              attValues.add(attValue);
          }
          if (attValues.size() == 1) {
              // Single-valued result
              String attValue = (String)attValues.iterator().next();
              attribs.put(attAlias, attValue);
          } else if (attValues.size() > 1) {
              // Multi-valued result
              attribs.put(attAlias, new ArrayList(attValues));
          }
      }
      
    } catch (Exception e) {
      // If database down or can't logon, ignore this data source
      // It is not clear that we want to disable the source, since the
      // database may be temporarily down.
      LogService.log(LogService.ERROR,"PersonDirectory::processJdbcDir(): Error "+e);
    } finally {
        if (rs!=null) try {rs.close();} catch (Exception e) {}
        if (stmt!=null) try {stmt.close();} catch (Exception e) {}
        if (conn!=null) try {conn.close();} catch (Exception e) {}
    }
  }

  /**
   * Returns a reference to a restricted IPerson represented by the supplied user ID.
   * The restricted IPerson allows access to person attributes, but not the security context.
   * @param uid the user ID
   * @return the corresponding person, restricted so that its security context is inaccessible
   */
  public static RestrictedPerson getRestrictedPerson(String uid) {
    IPerson person = (IPerson)persons.get(uid);
    if (person == null) {
      person = PersonFactory.createPerson();
      person.setAttribute(IPerson.USERNAME, uid);
      try {
        person.setID(UserIdentityStoreFactory.getUserIdentityStoreImpl().getPortalUID(person));
      } catch (Exception e) {
        LogService.log(LogService.ERROR, e);
      }
      instance().getUserDirectoryInformation(uid, person);
    }
    return new RestrictedPerson(person);
  }

  private class PersonDirInfo {
    String url; // protocol, server, and initial connection parameters
    String ResRefName; // Resource Reference name for a J2EE style DataSource
    String driver; // JDBC java class to register
    String logonid; // database userid or LDAP user DN (if needed)
    String logonpassword; // password
    String usercontext; // where are users? "OU=people" or "CN=Users"
    String uidquery; // SELECT or JNDI query for userid
    String fullnamequery; // SELECT or JNDI query using fullname
    int ldaptimelimit = 0; // timeout for LDAP in milliseconds. 0 means wait forever
    String[] attributenames;
    String[] attributealiases;
    boolean disabled = false;
    boolean logged = false;
  }
    
}
