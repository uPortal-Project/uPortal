package org.jasig.portal.layout.alm;

import java.util.Iterator;
import java.util.Map;

import javax.sql.DataSource;

public class ALFragmentNodeAttributeService {
    
    ALNodeAttributeDao dao;
    
    public ALFragmentNodeAttributeService(DataSource ds) {
        this.dao = new ALNodeAttributeDao(ds);
    }
    
    /**
     * Returns a Map from String attribute names to String attribute values.
     * @param fragmentId
     * @param nodeId
     * @return
     */
    public Map attributesForFragmentNode(int fragmentId, int nodeId) {
        return this.dao.attributesForFragmentNode(fragmentId, nodeId);
    }

    public void storeFragmentNodeAttribute(int fragmentId, int nodeId, String attributeName, String attributeValue) {
        
        // TODO: make this sane
        
        // does the attribute already exist?

        Map attributesForNode = attributesForFragmentNode(fragmentId, nodeId);
        
        // if so, update it
        if (attributesForNode.containsKey(attributeName)) {
            dao.setFragmentNodeAttribute(fragmentId, nodeId, attributeName, attributeValue);
        } else {
            // if not, insert it
            dao.insertFragmentNodeAttribute(fragmentId, nodeId, attributeName, attributeValue);   
        }
        
    }
    
    public void storeFragmentNodeAttributes(int fragmentId, int nodeId, Map attributeNamesToValues) {
        
        for (Iterator iter = attributeNamesToValues.keySet().iterator(); iter.hasNext();) {
            String attributeName = (String) iter.next();
            String attributeValue = (String) attributeNamesToValues.get(attributeName);
            
            storeFragmentNodeAttribute(fragmentId, nodeId, attributeName, attributeValue);
        }
        
    }

    
}
