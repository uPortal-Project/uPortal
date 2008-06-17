/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */

package org.jasig.portal.services;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

public class HttpClientManagerTest extends TestCase {
    private int port;
    private ServerSocket serverSocket;
    private Thread serverThread;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        serverSocket = new ServerSocket();
        serverThread = new Thread() {
            public void run() {
                try {
                    serverSocket.bind(null);
                    serverSocket.accept();
                    serverSocket.notifyAll();
                }
                catch (IOException e) {
                    // TODO Auto-generated catch block
                }
            }
        };
        serverThread.setDaemon(true);
        serverThread.start();
        
        synchronized (serverSocket) {
            serverSocket.wait(100);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        serverSocket.close();
    }

    public void testHttpClientReadTimeout() throws Exception {
        HttpClient client = HttpClientManager.getNewHTTPClient();
        final InetAddress localAddress = serverSocket.getInetAddress();
        final String hostAddress = localAddress.getHostAddress();
        final int localPort = serverSocket.getLocalPort();
        final String testUrl = "http://" + hostAddress + ":" + localPort;
        final GetMethod get = new GetMethod(testUrl);

        try {
            client.executeMethod(get);
        }
        catch (SocketTimeoutException ste) {
            //expected
        }
        catch (SocketException se) {
            //expected
        }
    }
}
