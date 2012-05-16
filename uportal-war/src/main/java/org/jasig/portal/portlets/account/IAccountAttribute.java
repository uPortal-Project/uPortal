package org.jasig.portal.portlets.account;

import java.io.Serializable;

public interface IAccountAttribute extends Serializable{
    
    /* returns the attribute name as seen in database */
    String getName();
    
    void setName(String name);
    
    /* returns the attribute type, should be usable in jsp */
    String getType();
    
    void setType(String type);
    
    /* returns the attribute value and should be able to be cast via generic constructor of type */
    Object getValue();
    
    /* this could be String, but I figure use 'toString' or similar to set it */
    void setValue(Object o);
    
    /* pass in the view name to see if this should be displayed on that page or not.  Prevents searching users by private variables */
    Boolean isSearchable();
    
    /* pass in the name of a view or parameter to hide the variable there*/
    Boolean hideOnView(String viewName);
}
