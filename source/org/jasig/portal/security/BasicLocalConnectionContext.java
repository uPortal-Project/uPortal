/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/


package org.jasig.portal.security;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import sun.misc.BASE64Encoder;
import java.text.MessageFormat;
import java.net.HttpURLConnection;

/**
 * <p>
 * BasicLocalConnectionContext implements HTTP Basic Authentication as a 
 * LocalConnectionContext. Connections are provided as Objects:
 * they may be URL, LDAP, database connections, etc.
 * </p>
 * 
 * <p>
 * In order to use this class you should define two publish time parameters for
 * your channel: <code>remote.username</code> and <code>remote.password</code>.
 * The username and password defaut to "tomcat" if you don't set them. 
 * </p>
 *
 *  @author Stephen Barrett, smb1@cornell.edu
 * 
 */

public class BasicLocalConnectionContext extends LocalConnectionContext {
    protected static final String CHANPARAMUSERNAME = "remote.username";

    protected static final String CHANPARAMPASSWORD = "remote.password";

    protected static final String AUTHORIZATIONHDR = "Authorization";

    protected static final String AUTHORIZATIONTYPE = "Basic ";

    protected static final String USERNAMEANDPWDMASK = "{0}:{1}";

    // default to tomcat examples default
    private String usernameandpassword = "tomcat:tomcat";

    /**
     * Constructs the username/password combination from the parameters
     * set at publish time.
     * 
     * @param sd
     *            The calling channel's ChannelStaticData.
     */

    public void init(ChannelStaticData sd) {
        staticData = sd;

        /*
         * Construct the username/password combination.
         */
        if (sd.getParameter(CHANPARAMUSERNAME) != null)
            usernameandpassword = MessageFormat.format(USERNAMEANDPWDMASK,
                    new String[] { sd.getParameter(CHANPARAMUSERNAME),
                            sd.getParameter(CHANPARAMPASSWORD) });

    }

    /**
     * Sets the headers so that the connection will authenticate using 
     * HTTP Basic Authentication using the username and password passed
     * set at publish time.
     * 
     * @param connection
     *            Must be an instance of HttpURLConnection
     * @param rd
     *            The calling channel's ChannelRuntimeData.
     */
    public void sendLocalData(Object connection, ChannelRuntimeData rd) {
        HttpURLConnection modified_connection = (HttpURLConnection) connection;

        // encode and set the authentication credentials
        modified_connection.setRequestProperty(AUTHORIZATIONHDR,
                AUTHORIZATIONTYPE
                        + (new BASE64Encoder()).encode(usernameandpassword
                                .getBytes()));

        // all done. This will be sent with the request now.
    }
}