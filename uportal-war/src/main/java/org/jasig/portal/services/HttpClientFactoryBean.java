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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AbstractFactoryBean;

public class HttpClientFactoryBean extends AbstractFactoryBean<HttpClient> {
    private HttpConnectionManager httpConnectionManager;
    private String proxyHost;
    private int proxyPort = 8080;

    @Value("${org.jasig.portal.services.HttpClientManager.proxyHost:}")
    public void setProxyHost(String proxyHost) {
        this.proxyHost = StringUtils.trimToNull(proxyHost);
    }

    @Value("${org.jasig.portal.services.HttpClientManager.proxyPort:8080}")
    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public void setHttpConnectionManager(HttpConnectionManager httpConnectionManager) {
        this.httpConnectionManager = httpConnectionManager;
    }

    @Override
    public Class<?> getObjectType() {
        return HttpClient.class;
    }

    @Override
    protected HttpClient createInstance() throws Exception {
        final HttpClient httpClient;
        if (this.httpConnectionManager != null) {
            httpClient = new HttpClient(this.httpConnectionManager);
        }
        else {
            httpClient = new HttpClient();
        }
        
        if (this.proxyHost != null) {
            httpClient.getHostConfiguration().setProxy(this.proxyHost, this.proxyPort);
        }
        
        return httpClient;
    }
}
