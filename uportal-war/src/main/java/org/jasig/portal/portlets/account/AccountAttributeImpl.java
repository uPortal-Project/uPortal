package org.jasig.portal.portlets.account;

import java.util.List;
import java.util.Map;

public class AccountAttributeImpl implements IAccountAttribute {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String type;
    private Object value;
    private Map<String, Boolean> hiddenViews;
    private Boolean searchable;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setHiddenViews(Map<String, Boolean> hiddenViews) {
        this.hiddenViews = hiddenViews;
    }

    public void setSearchable(Boolean searchable) {
        this.searchable = searchable;
    }
    
    public Boolean getSearchable() {
        return searchable;
    }

    @Override
    public Boolean hideOnView(String viewName) {
        return hiddenViews.get(viewName);
    }

    @Override
    public Boolean isSearchable() {
        return searchable;
    }
}
