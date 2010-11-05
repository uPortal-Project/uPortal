package org.jasig.portal.persondir;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LocalAccountQuery provides the persondirectory query object for local accounts. 
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class LocalAccountQuery {

    private String username;
    
    final private Map<String, List<String>> attributes = new HashMap<String, List<String>>();

    public String getName() {
        return username;
    }

    public void setUserName(String name) {
        this.username = name;
    }

    public Map<String, List<String>> getAttributes() {
        return attributes;
    }

    public void setAttribute(String name, List<String> values) {
        this.attributes.put(name, values);
    }
    
}
