package org.jasig.portal.portlets.search.gsa;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GsaSearchService {

    private RestTemplate restTemplate;
    
    @Autowired(required = true)
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    private String urlTemplate = "{baseUrl}?q={query}&site={site}&entqr=0&ud=1&sort=date%3AD%3AL%3Ad1&output=xml_no_dtd&oe=UTF-8&ie=UTF-8&proxyreload=1&entsp=0";
    
    public void setUrlTemplate(String urlTemplate) {
        this.urlTemplate = urlTemplate;
    }
    
    public GsaResults search(String query, String baseUrl, String site) {
        
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("query", query);
        vars.put("baseUrl", baseUrl);
        vars.put("site", site);
        
        GsaResults result = restTemplate.getForObject(urlTemplate, GsaResults.class, vars);
        return result;
    }
    
}
