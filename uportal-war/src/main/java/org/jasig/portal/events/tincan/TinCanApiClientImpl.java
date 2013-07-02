package org.jasig.portal.events.tincan;

import org.jasig.portal.events.tincan.om.LrsStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus.Series;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;

@Component
public class TinCanApiClientImpl implements TinCanEventSender {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private RestOperations restOperations;
    
    private String url;

    @Autowired
    public void setRestOperations(RestOperations restOperations) {
        this.restOperations = restOperations;
    }

    @Override
    public void sendEvent(LrsStatement statement) {
        
        //TODO OAUTH or BasicAuth, need header-setting rest client
        
        final ResponseEntity<Object> result = this.restOperations.postForEntity(url, statement, Object.class);
        
        if (result.getStatusCode().series() == Series.SUCCESSFUL) {
            logger.debug("LRS provider successfully sent to {}, statement: {}", url, statement);
        } else {
            logger.warn("LRS provider failed to send to {}, statement: {}\n\tResponse:", url, statement, result);
        }
    }
}
