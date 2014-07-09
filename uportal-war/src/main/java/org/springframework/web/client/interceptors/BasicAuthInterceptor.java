package org.springframework.web.client.interceptors;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;


/**
 * @author Josh Helmer, jhelmer@unicon.net
 */
public class BasicAuthInterceptor implements ClientHttpRequestInterceptor {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private String id;
    private Environment environment;
    private String authHeader;


    @Required
    public void setId(String id) {
        this.id = id;
    }


    @Autowired
    public void setEnvironment(final Environment env) {
        this.environment = env;
    }


    @Override
    public ClientHttpResponse intercept(HttpRequest req, byte[] bytes, ClientHttpRequestExecution execution) throws IOException {
        Assert.notNull(environment);
        Assert.notNull(id);

        req.getHeaders().add(AUTHORIZATION_HEADER, getAuthHeader());

        return execution.execute(req, bytes);
    }


    private synchronized String getAuthHeader() {
        if (authHeader == null) {
            String authString = environment.getProperty("org.jasig.http.basic-auth." + id + ".authString");

            if (StringUtils.isBlank(authString)) {
                String username = environment.getProperty("org.jasig.http.basic-auth." + id + ".username");
                String password = environment.getProperty("org.jasig.http.basic-auth." + id + ".password");

                String auth = username + ":" + password;
                authString = new String(Base64.encodeBase64(auth.getBytes()));
            }

            authHeader = "Basic " + authString;
        }

        return authHeader;
    }
}
