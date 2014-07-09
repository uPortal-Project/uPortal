package org.springframework.web.client.interceptors;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthServiceProvider;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
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
public class TwoLeggedOAuthInterceptor implements ClientHttpRequestInterceptor {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private String id;
    private Environment env;
    private RealmOAuthConsumer consumer;


    @Required
    public void setId(String id) {
        this.id = id;
    }


    @Autowired
    public void setEnvironment(final Environment env) {
        this.env = env;
    }


    @Override
    public ClientHttpResponse intercept(HttpRequest req, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        Assert.notNull(env);
        Assert.notNull(id);

        try {
            String authString = getOAuthAuthString(req);
            req.getHeaders().add(AUTHORIZATION_HEADER, authString);
        } catch (Exception e) {
            throw new IOException("Error building OAuth header", e);
        }

        return execution.execute(req, body);
    }


    private String getOAuthAuthString(HttpRequest req) throws OAuthException, IOException, URISyntaxException {
        RealmOAuthConsumer consumer = getConsumer();
        OAuthAccessor accessor = new OAuthAccessor(consumer);

        String method = req.getMethod().name();
        URI uri = req.getURI();
        URIBuilder builder = new URIBuilder(req.getURI());
        List<NameValuePair> GETParams = builder.getQueryParams();

        OAuthMessage msg = accessor.newRequestMessage(method, uri.toString(), null);

        for (NameValuePair param : GETParams) {
            msg.addParameter(param.getName(), param.getValue());
        }

        msg.addRequiredParameters(accessor);

        return msg.getAuthorizationHeader(consumer.getRealm());
    }


    private synchronized RealmOAuthConsumer getConsumer() {
        // could just inject these, but I kinda prefer pushing this out
        // to the properties file...
        if (consumer == null) {
            OAuthServiceProvider serviceProvider = new OAuthServiceProvider("", "", "");
            String realm = env.getProperty("org.jasig.http.oauth." + id + ".realm");
            String consumerKey = env.getProperty("org.jasig.http.oauth." + id + ".consumerKey");
            String secretKey = env.getProperty("org.jasig.http.oauth." + id + ".secretKey");

            consumer = new RealmOAuthConsumer(consumerKey, secretKey, realm, serviceProvider);
        }

        return consumer;
    }


    private static class RealmOAuthConsumer extends OAuthConsumer {
        private String realm;


        public RealmOAuthConsumer(String consumerKey, String consumerSecret, String realm, OAuthServiceProvider serviceProvider) {
            super(null, consumerKey, consumerSecret, serviceProvider);
            realm = realm;
        }


        public String getRealm() {
            return realm;
        }
    }
}
