/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.MappingSqlQuery;

/**
 * PersonAttributeDao implementation that maps from column names in the
 * single-row result of a SQL query against a DataSource to attribute names. You
 * must set a Map from column names to attribute names and only column names
 * appearing as keys in that map will be used.
 * 
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class JdbcPersonAttributeDaoImpl extends MappingSqlQuery implements
        PersonAttributeDao {

    /**
     * A Map from String column names to Sets of Strings 
     * representing the names of the uPortal attributes which should be set to the
     * value of the specified column.
     */
    private Map columnsToAttributes = new HashMap();
    
    /** 
     * The Set of uPortal attribute names this instance will map. 
     * This lets us avoid creating the Set every time getAttributeNames is called. 
     */
    private Set attrNames;
    

    /**
     * Instantiate the query, providing a DataSource against which the query
     * will run and the SQL representing the query, which should take exactly
     * one parameter: the unique ID of the user.
     * 
     * @param ds
     * @param sql
     */
    public JdbcPersonAttributeDaoImpl(DataSource ds, String sql) {
        super(ds, sql);
        declareParameter(new SqlParameter("uid", Types.VARCHAR));
        compile();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jasig.portal.services.persondir.support.PersonAttributeDao#attributesForUser(java.lang.String)
     */
    public Map attributesForUser(String uid) {
        if (uid == null)
            throw new IllegalArgumentException("Illegal to ask for attributes for the null user.");
        Object uniqueResult = DataAccessUtils.uniqueResult(this.execute(uid));
        if (uniqueResult == null)
            return new HashMap();
        return (Map) uniqueResult;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jasig.portal.services.persondir.support.PersonAttributeDao#getAttributeNames()
     */
    public Set getAttributeNames() {
        return Collections.unmodifiableSet(this.attrNames);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.jdbc.object.MappingSqlQuery#mapRow(java.sql.ResultSet,
     *         int)
     */
    protected Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        Map attributeNamesToValues = new HashMap();

        for (Iterator cols = this.columnsToAttributes.keySet().iterator(); cols
                .hasNext();) {
            String columnName = (String) cols.next();

            if (columnName != null && !"".equals(columnName)) {
                String attributeValue = null;
                try {
                    attributeValue = rs.getString(columnName);
                } catch (SQLException sqle) {
                    super.logger.error("Was unable to read attribute for column ["
                            + columnName + "]");
                    throw sqle;
                }

                // exclude null attribute values
                if (attributeValue != null) {
                    /*
                     * If the attribute has been mapped, add it under the mapped
                     * name(s). If it was not mapped, add it under the column name.
                     */
                    
                    Set attributeNames = (Set) this.columnsToAttributes
                            .get(columnName);
                    if (attributeNames == null)
                        attributeNames = Collections.singleton(columnName);
                    
                    for (Iterator iter = attributeNames.iterator(); iter.hasNext();){
                        String attributeName = (String) iter.next();
                        attributeNamesToValues.put(attributeName, attributeValue);
                    }

                }
            }

        }
        return attributeNamesToValues;
    }

    /**
     * Get the Map from non-null String column names to Sets of non-null Strings
     * representing the names of the uPortal attributes to be initialized from
     * the specified column.
     * @return Returns the columnsToAttributes mapping.
     */
    public Map getColumnsToAttributes() {
        return this.columnsToAttributes;
    }

    /**
     * Set the Map from column names to Sets of Strings that are the names
     * of the one or more uPortal attributes. Column names that do not appear
     * as keys in this map will be mapped to attribute names of the same value as
     * the column name. As a convenience, the Set received as an argument by
     * this method may map to Strings representing the name of the single
     * uPortal attribute to be set from the column name rather than to Sets containing
     * that one String.  This method translates to the Map expected by internal
     * consumers of this property.  
     * 
     * @param columnsToAttributesArg Map from non-null String column names 
     * to non-null String uPortal attribute names or Lists of at least one such String.
     * @throws IllegalArgumentException if the Map has non-Strings as keys or
     * if it has entry values that are neither Strings nor Sets of one or more Strings.
     */
    public void setColumnsToAttributes(Map columnsToAttributesArg) {
        /*
         * This implementation validates and makes a defensive copy of the
         * columnsToAttributes map argument.
         */
        Map internalColumnsToAttributes = new HashMap();
        if (columnsToAttributesArg == null)
            throw new IllegalArgumentException("Cannot set the mapping from " +
                    "column names to attributes to be null.");
        for (Iterator iter = columnsToAttributesArg.keySet().iterator(); iter.hasNext();){
            
            // validate the key
            Object key = iter.next();
            if (key == null)
                throw new IllegalArgumentException("The map from column names to" +
                        " uPortal attributes must not have any null keys.");
            if (! (key instanceof String))
                throw new IllegalArgumentException("The map from column names to" +
                        " uPortal attributes must not have any non-String keys.  The key ["
                        + key + " is of type [" + key.getClass().getName() + "] which is not a String.");
            
            // validate the value
            Object value = columnsToAttributesArg.get(key);
            if (value == null)
                throw new IllegalArgumentException("Null must not appear as the" +
                        "value of any key-value pair in the columnsToAttributes map.  " +
                        "However, null was the value for the key [" + key + "]");
            if (value instanceof String) {
                // translate to a Set containing the String
                value = Collections.singleton(value);
            } else if (value instanceof Set) {
                Set valueAsSet = (Set) value;
                if (valueAsSet.isEmpty()) {
                    throw new IllegalArgumentException("columnsToAttributes mapping illegal: " +
                            "Key [" + key + "] maps to empty set." +
                            " Keys must map to either a non-null String or a " +
                            "non-empty Set of non-null Strings.");
                }
            } else {
                throw new IllegalArgumentException("columnsToAttributes mapping illegal: " +
                        "Key [" + key + "] maps to an object that is neither a String nor  Set:" +
                                " it is of type [" + value.getClass().getName() + 
                                "], with String representation " + value);
            }
            
            internalColumnsToAttributes.put(key, value);
        }
        
        this.columnsToAttributes = internalColumnsToAttributes;
        this.updateAttrNameSet();
    }

    /**
     * Create and store a Set of the uPortal attribute names we will map.
     * This allows us to avoid doing it for every getAttributeNames call.
     */
    private void updateAttrNameSet() {
        Set attributeNames = new HashSet();
        
        for (final Iterator attrNameSets = this.columnsToAttributes.values().iterator(); attrNameSets.hasNext(); ) {
            final Set attrNameSet = (Set)attrNameSets.next();
            
            attributeNames.addAll(attrNameSet);
        }
        this.attrNames = attributeNames;
    }
}