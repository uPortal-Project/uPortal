/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.ldap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
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
public class LdapServices
{  
    public static final String DEFAULT_LDAP_SERVER = "DEFAULT_LDAP_SERVER";
    
    private static final Log log = LogFactory.getLog(LdapServices.class);
    
    private static final String PROPERTIES_PATH = "/properties/";
    private static final String LDAP_PROPERTIES_FILE = PROPERTIES_PATH + "ldap.properties";
    private static final String LDAP_XML_FILE = PROPERTIES_PATH + "ldap.xml";
    private static final String LDAP_XML_CONNECTION_XPATH = "ldapConnections/connection";
    
    private static final Map ldapConnections = new Hashtable();
    private static ILdapServer defaultConn = null;
    private static boolean initialized = false;
    
    /**
     * Get the default {@link ILdapServer}. A one-time initialization
     * is performed when this method or {@link #getLdapServer(String name)}
     * is called. If a default connection is not found during initialization
     * an <code>IllegalStateException</code> will be thrown.
     * 
     * @return The default {@link ILdapServer}. 
     */
    public static ILdapServer getDefaultLdapServer() {
        initConnections();
        
        return defaultConn;
    }
  
    /**
     * Get a named {@link ILdapServer}. A one-time initialization
     * is performed when this method or {@link #getDefaultLdapServer()}
     * is called. If a default connection is not found during initialization
     * an <code>IllegalStateException</code> will be thrown.
     * 
     * @param name The name of the connection to return.
     * @return An {@link ILdapServer} with the specified name, <code>null</code> if there is no connection with the specified name.
     */
    public static ILdapServer getLdapServer(String name) {
        if (DEFAULT_LDAP_SERVER.equals(name)) {
            return getDefaultLdapServer();
        }
        
        initConnections();
        
        synchronized (ldapConnections) {
            return (ILdapServer)ldapConnections.get(name);
        }
    }
  
    private static void initConnections() {
        //synchronize on the connections map, ensures only one thread
        //will be initializing the connections and nothing will be
        //trying to read from the map while it is being initialized.
        synchronized (ldapConnections) {
            
            //If already initialized just return
            if (initialized)
                return;
            
            //This try/catch/finaly block reads the default LDAP connection
            //from a properties file.
            InputStream ins = null;
            try
            {
                //Read properties file
                ins = LdapServices.class.getResourceAsStream(LDAP_PROPERTIES_FILE);
                
                //If the properties file was found
                if (ins != null) {
                    Properties ldapProps = new Properties();
                    ldapProps.load(ins);
                  
                    try {
                        //Create the default connection object
                        defaultConn = new LdapConnectionImpl(
                            "ldap.properties configured connection",
                            ldapProps.getProperty("ldap.host"),
                            ldapProps.getProperty("ldap.port"),
                            ldapProps.getProperty("ldap.baseDN"),
                            ldapProps.getProperty("ldap.uidAttribute"),
                            ldapProps.getProperty("ldap.managerDN"),
                            ldapProps.getProperty("ldap.managerPW"),
                            ldapProps.getProperty("ldap.protocol"),
                            ldapProps.getProperty("ldap.factory"));
                    }
                    catch (IllegalArgumentException iae) {
                        log.info( "Invalid data in " + LDAP_PROPERTIES_FILE, iae);
                    }
                }
                else {
                    log.info( "LdapServices::initConnections(): " + LDAP_PROPERTIES_FILE + " was not found, all ldap connections will be loaded from " + LDAP_XML_FILE);
                }
            }
            catch(Exception e)
            {
                log.error( "LdapServices::initConnections(): Error while loading default ldap connection from " + LDAP_PROPERTIES_FILE, e);
            }
            finally
            {
                try
                {
                    if(ins != null)
                        ins.close();
                }
                catch(IOException ioe)
                {
                    log.error( "LdapServices::initConnections(): Unable to close " + LDAP_PROPERTIES_FILE + " InputStream " + ioe);
                }
            }
            
            
            
            
            //Read extra connections from ldap.xml
            Document config = null;
            try {
                config = ResourceLoader.getResourceAsDocument(LdapServices.class, LDAP_XML_FILE);
            } 
            catch (Exception e) {
                log.error( "LdapServices::initConnections(): Could not create Document from " + LDAP_XML_FILE, e);
            }
            
            if (config != null){
                config.normalize();

                try {
                    NodeList connElements = XPathAPI.selectNodeList(config, LDAP_XML_CONNECTION_XPATH);
                    
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
                                String protocol = null;
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
                                            protocol = tagValue;
                                        }
                                        else if (tagName.equals("factory")) {
                                            factory = tagValue;
                                        }                                        
                                    }
                                }

                                //Create a new ILdapServer
                                if (name != null) {
                                    try {
                                        ILdapServer newConn = new LdapConnectionImpl(name, host, port, baseDN, uidAttribute, managerDN, managerPW, protocol, factory);
                                        ldapConnections.put(name, newConn);
                                        
                                        if (isDefaultConn) {
                                            defaultConn = newConn;
                                            log.info("LdapServices::initConnections(): Replaced '" + LDAP_PROPERTIES_FILE + "' connection with default connection '" + name + "' from '" + LDAP_XML_FILE + "'");
                                        }
                                    }
                                    catch (IllegalArgumentException iae) {
                                        log.info( "Invalid data for server " + name + " in " + LDAP_XML_FILE, iae);
                                    }                                    
                                }
                                else {
                                    log.error( "LdapServices::initConnections(): Error creating ILdapServer, no name specified.");
                                }
                            }
                        }
                        catch (Exception e) {
                            log.error( "LdapServices::initConnections(): Error creating ILdapServer from node: " + connElement.getNodeName(), e);
                        }
                    }
                }
                catch (TransformerException te) {
                    log.error( "LdapServices::initConnections(): Error applying XPath query (" + LDAP_XML_CONNECTION_XPATH + ") on " + LDAP_XML_FILE, te);
                }
            }
            else {
                log.error( "LdapServices::initConnections(): No document was loaded from " + LDAP_XML_FILE);                
            }
            
            
            
            //Make sure a default connection was created.
            if (defaultConn == null) {
                RuntimeException re = new IllegalStateException("No default connection was created during initialization.");
                log.error( "LdapServices::initConnections():", re);
                throw re;
            }            
            
            initialized = true;
        }
    }
    
    /**
     * This class only provides static methods.
     */
    private LdapServices() {
    }
    

    /**
     * Internal implementation of the {@link ILdapServer} interface.
     * 
     * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
     */
    private static class LdapConnectionImpl implements ILdapServer {
        private final String ldapName;
        private final String ldapHost;
        private final String ldapPort;
        private final String ldapBaseDn;
        private final String ldapUidAttribute;
        private final String ldapManagerDn;
        private final String ldapManagerPw;
        private final String ldapManagerProtocol;
        private final String ldapInitCtxFactory;
        
        public LdapConnectionImpl(
            String name, String host, String port, String baseDn,
            String uidAttribute, String managerDn, String managerPw,
            String managerProtocol, String initialContextFactory) {
            
            if (name == null)
                throw new IllegalArgumentException("name cannot be null.");
            if (host == null)
                throw new IllegalArgumentException("host cannot be null.");
            
            this.ldapName = name;
            this.ldapHost = checkNull(host, "");
            this.ldapPort = checkNull(port, "389");
            this.ldapBaseDn = checkNull(baseDn, "");
            this.ldapUidAttribute = checkNull(uidAttribute, "");
            this.ldapManagerDn = checkNull(managerDn, "");
            this.ldapManagerPw = checkNull(managerPw, "");
            this.ldapManagerProtocol = checkNull(managerProtocol, "");
            this.ldapInitCtxFactory = checkNull(initialContextFactory, "com.sun.jndi.ldap.LdapCtxFactory");
            
            log.debug("LdapServices: Creating LDAP Connection: " + this.ldapName);
            log.debug("\thost = " + this.ldapHost);
            log.debug("\tport = " + this.ldapPort);
            log.debug("\tbaseDN = " + this.ldapBaseDn);
            log.debug("\tuidAttribute = " + this.ldapUidAttribute);
            log.debug("\tmanagerDN = " + this.ldapManagerDn);
            log.debug("\tmanagerPW = " + this.ldapManagerPw);
            log.debug("\tprotocol = " + this.ldapManagerProtocol);            
        }
        
        
        private String checkNull(String chkStr, String defStr) {
            if (chkStr == null)
                return defStr;
            else
                return chkStr;
        }
        
        
        public DirContext getConnection() {
            DirContext conn = null;

            try {
                Hashtable env = new Hashtable(5, 0.75f);
                env.put(Context.INITIAL_CONTEXT_FACTORY, this.ldapInitCtxFactory);
                StringBuffer urlBuffer = new StringBuffer("ldap://");
                urlBuffer.append(ldapHost).append(":").append(ldapPort);
     
                env.put(Context.PROVIDER_URL, urlBuffer.toString());
                env.put(Context.SECURITY_AUTHENTICATION, "simple");
                env.put(Context.SECURITY_PRINCIPAL,      ldapManagerDn);
                env.put(Context.SECURITY_CREDENTIALS,    ldapManagerPw);
                
                if(ldapManagerProtocol.equals("ssl"))
                    env.put(Context.SECURITY_PROTOCOL,"ssl");
                
                conn = new InitialDirContext(env);
            }
            catch ( Exception e ) {
                log.error( "LdapServices::LdapConnectionImpl::getConnection(): Error creating the LDAP Connection.", e);
            }
     
            return conn;
        }

        public String getBaseDN() {
            return ldapBaseDn;
        }

        public String getUidAttribute() {
            return ldapUidAttribute;
        }

        public void releaseConnection (DirContext conn) {
            if (conn == null)
                return;
            
            try {
                conn.close();
            }
            catch (Exception e) {
                log.debug("LdapServices::LdapConnectionImpl::getConnection(): Error closing the LDAP Connection.", e);
            }
        }       
    }
}