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
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AbstractFactoryBean;

public class MultiThreadedHttpConnectionManagerFactoryBean extends AbstractFactoryBean<MultiThreadedHttpConnectionManager> {
    private int maxTotalConnections = 200;
    private int defaultMaxConnectionsPerHost = 20;
    private int connectionTimeout = 5000; 
    private int soTimeout = 2000;
    
    @Value("${org.jasig.portal.services.HttpClientManager.poolSize:200}")
    public void setMaxTotalConnections(int maxTotalConnections) {
        this.maxTotalConnections = maxTotalConnections;
    }

    @Value("${org.jasig.portal.services.HttpClientManager.hostConnections:20}")
    public void setDefaultMaxConnectionsPerHost(int defaultMaxConnectionsPerHost) {
        this.defaultMaxConnectionsPerHost = defaultMaxConnectionsPerHost;
    }

    @Value("${org.jasig.portal.services.HttpClientManager.connectionTimeout:5000}")
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    @Value("${org.jasig.portal.services.HttpClientManager.readTimeout:2000}")
    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    @Override
    public Class<?> getObjectType() {
        return MultiThreadedHttpConnectionManager.class;
    }

    @Override
    protected MultiThreadedHttpConnectionManager createInstance() throws Exception {
        final MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager();
        
        final HttpConnectionManagerParams pars = multiThreadedHttpConnectionManager.getParams();
        pars.setConnectionTimeout(this.connectionTimeout);
        pars.setSoTimeout(this.soTimeout);
        pars.setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
        pars.setMaxTotalConnections(this.maxTotalConnections);
        pars.setDefaultMaxConnectionsPerHost(this.defaultMaxConnectionsPerHost);
        
        return multiThreadedHttpConnectionManager;
    }

    @Override
    protected void destroyInstance(MultiThreadedHttpConnectionManager instance) throws Exception {
        //TODO wrap in watcher to timeout the shutdown
        instance.shutdown();
    }
}
