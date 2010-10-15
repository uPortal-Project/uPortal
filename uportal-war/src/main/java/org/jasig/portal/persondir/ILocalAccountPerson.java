package org.jasig.portal.persondir;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jasig.services.persondir.IPersonAttributes;

/**
 * A person account stored in the local uPortal database
 * 
 * @version $Revision$
 */
public interface ILocalAccountPerson extends IPersonAttributes {

    public long getId();
    
    public String getName();

    public void setName(String name);

    public String getPassword();

    public void setPassword(String password);

    public Date getLastPasswordChange();

    public void setLastPasswordChange(Date lastPasswordChange);

    public Object getAttributeValue(String name);

    public List<Object> getAttributeValues(String name);

    public Map<String, List<Object>> getAttributes();

    public void setAttribute(String name, List<String> values);

    public void setAttributes(Map<String, List<String>> values);

}