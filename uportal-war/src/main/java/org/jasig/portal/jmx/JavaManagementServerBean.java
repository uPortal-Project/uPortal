/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.jmx;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * A bean to make starting and stopping a JMX server easier.
 * <br>
 * <br>
 * The only required property is {@link #setPortOne(int)}, if portTwo is not set is will be calculated
 * as portOne + 1. faileOnException is set to false buy default.
 * <br>
 * <br>
 * The bean heeds the following system properties:
 * <table>
 *   <tr>
 *     <th>Property</th>
 *     <th>Function</th>
 *   </tr>
 *   <tr>
 *     <td>com.sun.management.jmxremote</td>
 *     <td>Having this property set (value doesn't matter) enables starting of the JMX Server</td>
 *   </tr>
 *   <tr>
 *     <td>com.sun.management.jmxremote.ssl</td>
 *     <td>Enables SSL based connections</td>
 *   </tr>
 *   <tr>
 *     <td>com.sun.management.jmxremote.password.file</td>
 *     <td>The password file that contains usernames and passwords for connecting to the server</td>
 *   </tr>
 *   <tr>
 *     <td>com.sun.management.jmxremote.access.file</td>
 *     <td>The password file that contains ACLs for users connecting to the server</td>
 *   </tr>
 * </table>
 * <br>
 * <p>
 *  <b>IMPORTNANT NOTE</b> Using this bean starts an RMI server. If this bean is used in a JVM that is
 *  long running and has any sort of generational garbage collector configured the system properties 
 *  sun.rmi.dgc.client.gcInterval and sun.rmi.dgc.server.gcInterval need to be set to some high value.
 *  They configure how often the RMI server requests a full GC. To disable the RMI induced GCs set the
 *  properties to '0x7ffffffffffffffe' Only do this if you can rely on the generational GC to reguarly
 *  clean up RMI objects.
 * </p>
 * 
 * @author Eric Dalquist <a href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
 * @version $Revision$
 */
public class JavaManagementServerBean {
    //System properties
    private static final String JMX_ENABLED_PROPERTY = "com.sun.management.jmxremote";
    private static final String JMX_SSL_PROPERTY = "com.sun.management.jmxremote.ssl";
    private static final String JMX_PASSWORD_FILE_PROPERTY = "com.sun.management.jmxremote.password.file";
    private static final String JMX_ACCESS_FILE_PROPERTY = "com.sun.management.jmxremote.access.file";
    
    //JMX properties
    private static final String JMX_REMOTE_X_ACCESS_FILE = "jmx.remote.x.access.file";
    private static final String JMX_REMOTE_X_PASSWORD_FILE = "jmx.remote.x.password.file";
    

    protected final Log logger = LogFactory.getLog(this.getClass());

    private String host = null;
    private int portOne = -1;
    private int portTwo = -1;
    private boolean failOnException = false;

    private JMXConnectorServer jmxConnectorServer;
    
    public boolean isFailOnException() {
        return this.failOnException;
    }
    /**
     * If an exception should be thrown if setting up the JMX server fails
     */
    public void setFailOnException(boolean failOnException) {
        this.failOnException = failOnException;
    }

    public int getPortTwo() {
        return this.portTwo;
    }
    /**
     * Second port in the JMX connection string
     */
    public void setPortTwo(int portTwo) {
        this.portTwo = portTwo;
    }

    public int getPortOne() {
        return portOne;
    }
    /**
     * First port in the JMX connection string
     */
    @Required
    public void setPortOne(int portOne) {
        Validate.isTrue(portOne > 0, "portOne must be greater than 0");
        this.portOne = portOne;
    }
    
    public String getHost() {
        return host;
    }
    /**
     * The host to listen on
     */
    public void setHost(String host) {
        this.host = host;
    }
    
    
    /**
     * Starts the RMI server and JMX connector server
     */
    public void startServer() {
        if (!System.getProperties().containsKey(JMX_ENABLED_PROPERTY)) {
            this.logger.info("System Property '" + JMX_ENABLED_PROPERTY + "' is not set, skipping initialization.");
            return;
        }
        
        try {
            //Get the base rmi port
            final int portOne = this.getPortOne();
            
            //Get the second rmi port or calculate it
            final int portTwo = this.calculatePortTwo(portOne);
            
            //Create the RMI registry on the base port
            try {
                LocateRegistry.createRegistry(portOne);
                
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Started RMI Registry on port " + portOne);
                }
            }
            catch (RemoteException re) {
                throw new IllegalStateException("Could not create RMI Registry on port " + portOne, re);
            }
            
            //Generate the JMX Service URL
            final JMXServiceURL jmxServiceUrl = this.getServiceUrl(portOne, portTwo);
            
            //Map for the JMX environment configuration
            final Map<String, Object> jmxEnv = this.getJmxServerEnvironment();
            
            //Create the MBean Server
            final MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
            
            //Create the JMX Connector
            try {
                this.jmxConnectorServer = JMXConnectorServerFactory.newJMXConnectorServer(jmxServiceUrl, jmxEnv, mbeanServer);
                
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Created JMXConnectorServer for JMXServiceURL='" + jmxServiceUrl + "', jmxEnv='" + jmxEnv + "' MBeanServer='" + mbeanServer + "'");
                }
            }
            catch (IOException ioe) {
                throw new IllegalStateException("Failed to create a new JMXConnectorServer for JMXServiceURL='" + jmxServiceUrl + "', jmxEnv='" + jmxEnv + "' MBeanServer='" + mbeanServer + "'", ioe);
            }
    
            //Start the JMX Connector
            try {
                this.jmxConnectorServer.start();
                this.logger.info("Started JMXConnectorServer. Listening on '" + jmxServiceUrl + "'");
            }
            catch (IOException ioe) {
                throw new IllegalStateException("Failed to start the JMXConnectorServer", ioe);
            }
        }
        catch (RuntimeException re) {
            if (this.failOnException) {
                throw re;
            }

            this.logger.error("Failed to initialize the JMX Server", re);
        }
    }

    /**
     * Stops the JMX connector server and RMI server
     */
    public void stopServer() {
        if (this.jmxConnectorServer == null) {
            this.logger.info("No JMXConnectorServer to stop");
            return;
        }
        
        try {
            try {
                this.jmxConnectorServer.stop();
                this.logger.info("Stopped JMXConnectorServer");
            }
            catch (IOException ioe) {
                throw new IllegalStateException("Failed to stop the JMXConnectorServer", ioe);
            }
            
            this.jmxConnectorServer = null;
        }
        catch (RuntimeException re) {
            if (this.failOnException) {
                throw re;
            }

            this.logger.error("Failed to shutdown the JMX Server", re);
        }
    }

    /**
     * Get the second rmi port from the init parameters or calculate it
     * 
     * @param portOne Base port to calculate the second port from if needed.
     * @return The second port
     */
    protected int calculatePortTwo(final int portOne) {
        int portTwo = this.portTwo;
        
        if (portTwo <= 0) {
            portTwo = portOne + 1;
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Using " + portTwo + " for portTwo.");
        }
        
        return portTwo;
    }

    /**
     * Generates the JMXServiceURL for the two specified ports.
     * 
     * @return A JMXServiceURL for this host using the two specified ports.
     * @throws IllegalStateException If localhost cannot be resolved or if the JMXServiceURL is malformed.
     */
    protected JMXServiceURL getServiceUrl(final int portOne, int portTwo) {
        final String jmxHost;
        if (this.host == null) {
            final InetAddress inetHost;
            try {
                inetHost = InetAddress.getLocalHost();
            }
            catch (UnknownHostException uhe) {
                throw new IllegalStateException("Cannot resolve localhost InetAddress.", uhe);
            }
            
            jmxHost = inetHost.getHostName();
        }
        else {
            jmxHost = this.host;
        }
        
        final String jmxUrl = "service:jmx:rmi://" + jmxHost + ":" + portTwo + "/jndi/rmi://" + jmxHost + ":" + portOne + "/server";
        
        final JMXServiceURL jmxServiceUrl;
        try {
            jmxServiceUrl = new JMXServiceURL(jmxUrl);
        }
        catch (MalformedURLException mue) {
            throw new IllegalStateException("Failed to create JMXServiceURL for url String '" + jmxUrl + "'", mue);
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Generated JMXServiceURL='" + jmxServiceUrl + "' from String " + jmxUrl + "'.");
        }
        
        return jmxServiceUrl;
    }

    /**
     * Generates the environment Map for the JMX server based on system properties
     * @return A non-null Map of environment settings for the JMX server.
     */
    protected Map<String, Object> getJmxServerEnvironment() {
        final Map<String, Object> jmxEnv = new HashMap<String, Object>();
        
        //SSL Options
        final String enableSSL = System.getProperty(JMX_SSL_PROPERTY);
        if (Boolean.getBoolean(enableSSL)) {
            SslRMIClientSocketFactory csf = new SslRMIClientSocketFactory();
            jmxEnv.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, csf);
            
            SslRMIServerSocketFactory ssf = new SslRMIServerSocketFactory();
            jmxEnv.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, ssf);
        }

        //Password file options
        final String passwordFile = System.getProperty(JMX_PASSWORD_FILE_PROPERTY);
        if (passwordFile != null) {
            jmxEnv.put(JMX_REMOTE_X_PASSWORD_FILE, passwordFile);
        }

        //Access file options
        final String accessFile = System.getProperty(JMX_ACCESS_FILE_PROPERTY);
        if (accessFile != null) {
            jmxEnv.put(JMX_REMOTE_X_ACCESS_FILE, accessFile);
        }
        
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Configured JMX Server Environment = '" + jmxEnv + "'");
        }
        
        return jmxEnv;
    }
}
