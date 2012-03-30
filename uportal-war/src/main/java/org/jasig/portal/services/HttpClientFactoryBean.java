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
