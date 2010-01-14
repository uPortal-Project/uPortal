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

package org.jasig.portal.services;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.properties.PropertiesManager;

/**
 * Centralized access to the HttpClient connection manager
 * @author George Lindholm
 * @since uPortal 2.5
 * @version $Revision$
 */
public class HttpClientManager {
    private static final Log log = LogFactory.getLog(HttpClientManager.class);

    private static final int POOL_SIZE = PropertiesManager.getPropertyAsInt("org.jasig.portal.services.HttpClientManager.poolSize", 20);
    private static final int HOST_CONNECTIONS = PropertiesManager.getPropertyAsInt("org.jasig.portal.services.HttpClientManager.hostConnections", 2);
    private static final int DEFAULT_CONNECTION_TIMEOUT = PropertiesManager.getPropertyAsInt("org.jasig.portal.services.HttpClientManager.connectionTimeout",5000); // five seconds
    private static final int DEFAULT_READ_TIMEOUT = PropertiesManager.getPropertyAsInt("org.jasig.portal.services.HttpClientManager.readTimeout", 2000); // five seconds

	private static final MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
	static {
		final HttpConnectionManagerParams pars = connectionManager.getParams();
		pars.setConnectionTimeout(DEFAULT_CONNECTION_TIMEOUT);
		pars.setSoTimeout(DEFAULT_READ_TIMEOUT);
		pars.setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
		pars.setMaxTotalConnections(POOL_SIZE);
		pars.setDefaultMaxConnectionsPerHost(HOST_CONNECTIONS);
	}

	public static HttpClient getNewHTTPClient() {
		return new HttpClient(connectionManager);
	}

	public static int getActiveConnections() {
		return connectionManager.getConnectionsInPool();
	}

	public static int getMaxConnections() {
		return POOL_SIZE;
	}
}
