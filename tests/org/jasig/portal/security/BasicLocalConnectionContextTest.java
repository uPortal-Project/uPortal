/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;

import junit.framework.TestCase;

/**
 * Unit test for BasicLocalConnectionContext that tests that the username and 
 * password are passed in correctly with the static data and that the headers
 * get set on the HttpConnection object. 
 * 
 * @author Brad Johnson, brad.johnson@ttu.edu
 */
public class BasicLocalConnectionContextTest extends TestCase{
    
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "RL4F066tM36fMGhsSCj1a7";

    // If you change the above user/pass you will need to recalculate the following by running
    // the following. I wanted to hard code the encoded value here so this test doesn't pass
    // if the encoder is broken somehow:

    //String encoded_user_pass = (new sun.misc.BASE64Encoder()).encode((USERNAME+":"+PASSWORD).getBytes());
    
    private static final String ENCODED_USER_PASS = "dGVzdHVzZXI6Ukw0RjA2NnRNMzZmTUdoc1NDajFhNw==";
    
    /**
     * Mock object for creating a HttpURLConnection object.
     *  
     * @author jjohnson
     *
     */
    class MockHttpConn extends HttpURLConnection{

       Map m = new HashMap();
       
       public MockHttpConn() throws MalformedURLException{
           super (new URL("http://fakehost"));
       }

       public String getRequestProperty(String key) {
           return (String)m.get(key);
       }
       public void setRequestProperty(String key, String value) {
           m.put(key,value);
       }

       public void disconnect() {
       }

       public boolean usingProxy() {
            return false;
       }

       public void connect() throws IOException {
       }
    }
   
    /**
     * Tests the basic auth by setting up the object with a mock
     * HttpURLConnection and checks that sendLocalData() sets 
     * the headers correctly. 
     * 
     * @throws IOException
     */
    public void testBasicAuth() throws IOException{
        
        BasicLocalConnectionContext context = new BasicLocalConnectionContext();
        
        // create static data
        ChannelStaticData sd = new ChannelStaticData();
        sd.setParameter("remote.username",USERNAME);
        sd.setParameter("remote.password",PASSWORD);
        
        // init context with static data
        context.init(sd);

        // create Http Connection
        HttpURLConnection conn =  new MockHttpConn();
        
        // create runtime data
        ChannelRuntimeData rd = new ChannelRuntimeData();
        
        // call method to test
        context.sendLocalData(conn,rd);

        // verify that request property was set right
        String authHeader = conn.getRequestProperty("Authorization");
        assertEquals("Basic "+ENCODED_USER_PASS,authHeader);
    }
}
