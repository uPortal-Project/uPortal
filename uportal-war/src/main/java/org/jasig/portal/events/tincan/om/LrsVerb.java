package org.jasig.portal.events.tincan.om;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.jasig.portal.events.tincan.json.LrsVerbSerializer;

/**
 * A list of verbs from http://www.adlnet.gov/expapi/verbs/
 */
@JsonSerialize(using=LrsVerbSerializer.class)
public enum LrsVerb {
    ANSWERED,
    ASKED,
    ATTEMPTED,
    ATTENDED,
    COMMENTED,
    COMPLETED,
    EXITED,
    EXPERIENCED,
    FAILED,
    IMPORTED,
    INITIALIZED,
    INTERACTED,
    LAUNCHED,
    MASTERED,
    PASSED,
    PREFERRED,
    PROGRESSED,
    REGISTERED,
    RESPONDED,
    RESUMED,
    SCORED,
    SHARED,
    SUSPENDED,
    TERMINATED,
    VOIDED;
    
    public final static String BASE_URI = "http://adlnet.gov/expapi/verbs/";
    private final String uri;
    
    private LrsVerb() {
        this.uri = BASE_URI + this.name().toLowerCase();
    }

    /**
     * @return The URI for this verb
     */
    public String getUri() {
        return uri;
    }
}
