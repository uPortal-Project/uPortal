package org.jasig.portal.portlets.account;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.map.LazyMap;
import org.jasig.portal.portlets.StringListAttribute;
import org.jasig.portal.portlets.StringListAttributeFactory;

public class PersonForm implements Serializable {

    private long id = -1;
    private String username;
    private String password;
    private String confirmPassword;

    @SuppressWarnings("unchecked")
    private Map<String, StringListAttribute> attributes = LazyMap.decorate(
            new HashMap<String, StringListAttribute>(),
            new StringListAttributeFactory());

    public PersonForm() { }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Map<String, StringListAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, StringListAttribute> attributes) {
        this.attributes = attributes;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

}
