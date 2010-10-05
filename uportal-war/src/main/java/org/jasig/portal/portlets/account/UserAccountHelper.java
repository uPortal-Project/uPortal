package org.jasig.portal.portlets.account;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jasig.portal.portlets.StringListAttribute;
import org.jasig.services.persondir.IPersonAttributes;

public class UserAccountHelper {
    
    public PersonForm getForm(IPersonAttributes person) {
        PersonForm form = new PersonForm();
        form.setUsername(person.getName());
        
        Map<String, StringListAttribute> attributes = new HashMap<String, StringListAttribute>();
        for (Map.Entry<String, List<Object>> attribute : person.getAttributes().entrySet()) {
            List<String> values = new ArrayList<String>();
            for (Object value : attribute.getValue()) {
                values.add((String) value);
            }
            
            attributes.put(attribute.getKey(), new StringListAttribute(values));
        }
        
        return form;
    }

}
