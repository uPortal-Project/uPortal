/* Copyright 2001, 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.ldap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathAPI;
import org.jasig.portal.ldap.ILdapServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.ResourceLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Provides LDAP access in a way similar to a relational DBMS. This class
 * was modified for the 2.4 release to function more like {@link org.jasig.portal.RDBMServices}.
 * The class should be used via the static {@link #getDefaultLdapServer()} and
 * {@link #getLdapServer(String name)} methods.
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$
 */
public final class LdapServices {  
    
    private static final Log log = LogFactory.getLog(LdapServices.class);
    
    /**
     * The special name which can be used by clients to request the ILdapServer
     * representing that configured in the .properties file rather (as opposed to
     * the named servers configured in the XML configuration file.)
     */
    public static final String DEFAULT_LDAP_SERVER = "DEFAULT_LDAP_SERVER";
    
    private static final String PROPERTIES_PATH = "/properties/";
    private static final String LDAP_PROPERTIES_FILE = 
        PROPERTIES_PATH + "ldap.properties";
    private static final String LDAP_XML_FILE = PROPERTIES_PATH + "ldap.xml";
    private static final String LDAP_XML_CONNECTION_XPATH = 
        "ldapConnections/connection";
    
    /**
     * Map from server names to ILdapServer objects.
     */
    private static final Map ldapConnections = new Hashtable();
    private static ILdapServer defaultConn = null;
    
    /**
     * Load the ILdapServers in response to our configuration.
     */
    static {
        loadLdapProperties();
        
        loadLdapXml();
        
        //Make sure a default connection was created.
        if (defaultConn == null) {
            RuntimeException re = 
                new IllegalStateException("No default connection was created during initialization.");
            log.error(re.getMessage(), re);
            throw re;
        }
    }
  
    /**
     * Load LDAP servers from the XML configuration file.
     */
    private static void loadLdapXml() {
        //Read extra connections from ldap.xml
        Document config = null;
        try {
            config = ResourceLoader.getResourceAsDocument(LdapServices.class, 
                    LDAP_XML_FILE);
        } 
        catch (Exception e) {
            log.error( "Could not create Document from " + LDAP_XML_FILE, e);
        }
        
        if (config != null){
            config.normalize();

            try {
                NodeList connElements = 
                    XPathAPI.selectNodeList(config, LDAP_XML_CONNECTION_XPATH);
                
                //Loop through each <connection> element
                for (int connIndex = 0; connIndex < connElements.getLength(); connIndex++) {
                    Node connElement = connElements.item(connIndex);

                    try {
                        if (connElement instanceof Element) {
                            //See if this connection is flagged as default
                            NamedNodeMap connAtts = connElement.getAttributes();
                            Node defaultFlagAtt = connAtts.getNamedItem("default");
                            
                            boolean isDefaultConn;
                            if (defaultFlagAtt != null)
                                isDefaultConn = (new Boolean(defaultFlagAtt.getNodeValue())).booleanValue();
                            else
                                isDefaultConn = false;
                            
                            String name = null;
                            String host = null;
                            String port = null;
                            String baseDN = null;
                            String managerDN = null;
                            String managerPW = null;
                            String uidAttribute = null;
                            boolean useSsl = false;
                            String factory = null;

                            //Loop through all the child nodes of the connection
                            NodeList connParams = connElement.getChildNodes();
                            for (int connParamIndex = 0; connParamIndex < connParams.getLength(); connParamIndex++) {
                                Node connParam = connParams.item(connParamIndex);
                                
                                if (connParam instanceof Element) {
                                    String tagName = ((Element)connParam).getTagName();
                                    String tagValue = null;

                                    if (connParam.getFirstChild() instanceof Text) {
                                        tagValue = ((Text)connParam.getFirstChild()).getData();
                                    }

                                    if (tagName.equals("name")) {
                                        name = tagValue;
                                    }
                                    else if (tagName.equals("host")) {
                                        host = tagValue;
                                    }
                                    else if (tagName.equals("port")) {
                                        port = tagValue;
                                    }
                                    else if (tagName.equals("baseDN")) {
                                        baseDN = tagValue;
                                    }
                                    else if (tagName.equals("managerDN")) {
                                        managerDN = tagValue;
                                    }
                                    else if (tagName.equals("managerPW")) {
                                        managerPW = tagValue;
                                    }
                                    else if (tagName.equals("uidAttribute")) {
                                        uidAttribute = tagValue;
                                    }
                                    else if (tagName.equals("protocol")) {
                                        useSsl = "ssl".equals(tagValue);
                                    }
                                    else if (tagName.equals("factory")) {
                                        factory = tagValue;
                                    }                                        
                                }
                            }

                            //Create a new ILdapServer
                            if (name != null) {
                                try {
                                    ILdapServer newConn = new LdapServerImpl(
                                            name, host, port, baseDN, uidAttribute, managerDN, managerPW, useSsl, factory);
                                    ldapConnections.put(name, newConn);
                                    
                                    if (isDefaultConn) {
                                        defaultConn = newConn;
                                        if (log.isInfoEnabled())
                                            log.info("Replaced '" + LDAP_PROPERTIES_FILE + 
                                                    "' connection with default connection '" + 
                                                    name + "' from '" + LDAP_XML_FILE + "'");
                                    }
                                }
                                catch (IllegalArgumentException iae) {
                                    if (log.isInfoEnabled())
                                        log.info("Invalid data for server " + name + 
                                                " in " + LDAP_XML_FILE, iae);
                                }                                    
                            }
                            else {
                                log.error( "Error creating ILdapServer, no name specified.");
                            }
                        }
                    }
                    catch (Exception e) {
                        log.error( "Error creating ILdapServer from node: " + connElement.getNodeName(), e);
                    }
                }
            }
            catch (TransformerException te) {
                log.error( "Error applying XPath query (" + LDAP_XML_CONNECTION_XPATH + ") on " + LDAP_XML_FILE, te);
            }
        }
        else {
            log.error( "No document was loaded from " + LDAP_XML_FILE);                
        }
    }
    
    /**
     * Load LDAP server from ldap.properties.
     */
    private static void loadLdapProperties() {

        //This try/catch/finaly block reads the default LDAP connection
        //from a properties file.
        InputStream ins = null;
        try {
            //Read properties file
            ins = LdapServices.class.getResourceAsStream(LDAP_PROPERTIES_FILE);

            //If the properties file was found
            if (ins != null) {
                Properties ldapProps = new Properties();
                ldapProps.load(ins);

                try {
                    //Create the default connection object
                    defaultConn = new LdapServerImpl(
                            "ldap.properties configured connection", 
                            ldapProps.getProperty("ldap.host"), 
                            ldapProps.getProperty("ldap.port"), 
                            ldapProps.getProperty("ldap.baseDN"), 
                            ldapProps.getProperty("ldap.uidAttribute"),
                            ldapProps.getProperty("ldap.managerDN"), 
                            ldapProps.getProperty("ldap.managerPW"), 
                            "ssl".equals(ldapProps.getProperty("ldap.protocol")),
                            ldapProps.getProperty("ldap.factory"));
                    
                } catch (IllegalArgumentException iae) {
                    if (log.isInfoEnabled())
                        log.info("Invalid data in "
                                                + LDAP_PROPERTIES_FILE, iae);
                }
            } else {
                if (log.isInfoEnabled())
                    log.info(LDAP_PROPERTIES_FILE
                            + " was not found, all ldap "
                            + "connections will be loaded from "
                            + LDAP_XML_FILE);
            }
        } catch (Exception e) {
            log.error("LdapServices::initConnections(): Error while loading "
                                    + "default ldap connection from "
                                    + LDAP_PROPERTIES_FILE, e);
        } finally {
            try {
                if (ins != null)
                    ins.close();
            } catch (IOException ioe) {
                log.error("Unable to close "
                        + LDAP_PROPERTIES_FILE + " InputStream ", ioe);
            }
        }

    }
    
    /**
     * Get the default {@link ILdapServer}.
     * @return The default {@link ILdapServer}. 
     */
    public static ILdapServer getDefaultLdapServer() {
        return defaultConn;
    }
    
    /**
     * Get a named {@link ILdapServer}.
     * Using the special name 'DEFAULT_LDAP_SERVER' causes this method to
     * return the default Ldap server.
     * @param name The name of the ILdapServer to return.
     * @return An {@link ILdapServer} with the specified name, 
     * <code>null</code> if there is no connection with the specified name.
     */
    public static ILdapServer getLdapServer(String name) {
        /**
         * If the request is for the special name indicating the default Ldap server
         * return that rather than looking in the map of named servers.
         */
        if (LdapServices.DEFAULT_LDAP_SERVER.equals(name))
            return getDefaultLdapServer();
        
        return (ILdapServer) ldapConnections.get(name);
    }
  
    /**
     * Get an iterator over the named {@link ILdapServer} instances.
     * If a server is configured in ldap.properties it will not be available via this
     * method; only servers configured in ldap.xml are available via this method.
     * @return an Iterator over named {@link ILdapServer}s.
     */
    public static Iterator getNamedLdapServers() {
        return ldapConnections.values().iterator();
    }
    
    /**
     * This class only provides static methods.
     */
    private LdapServices() {
        // private constructor prevents instantiation of this 
        // static service-providing class
    }  
}