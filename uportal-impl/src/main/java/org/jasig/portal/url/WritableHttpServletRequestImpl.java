/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.url;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;

/**
 * WritableHttpServletRequestImpl description.
 *
 * @author Peter Kharchenko: pkharchenko at unicon.net
 * @version $Revision: 11911 $
 */
public class WritableHttpServletRequestImpl extends AbstractHttpServletRequestWrapper implements IWritableHttpServletRequest {
    private final Map<String, String[]> parameterMap = new HashMap<String, String[]>();

    /**
     * Construct a writable this.request wrapping a real this.request
     * @param this.request Request to wrap, can not be null.
     */
    @SuppressWarnings("unchecked")
    public WritableHttpServletRequestImpl(HttpServletRequest request) {
        super(request);

        // place all parameters into the map, saves run-time merging
        this.parameterMap.putAll(request.getParameterMap());
    }

    public boolean addParameterValue(String name, String value) {
        Validate.notNull(name, "name can not be null");
        Validate.notNull(value, "value can not be null");
        
        final String[] currentValues = this.parameterMap.get(name);
        final String[] newValues;
        
        if (currentValues == null) {
            newValues = new String[] { value };
        }
        else {
            newValues = (String[])ArrayUtils.add(currentValues,  value);
        }
        
        this.parameterMap.put(name, newValues);
        return currentValues != null;
    }

    public boolean setParameterValue(String name, String value) {
        Validate.notNull(name, "name can not be null");
        Validate.notNull(value, "value can not be null");
        
        return this.parameterMap.put(name, new String[] { value }) != null;
    }

    public boolean setParameterValues(String name, String[] values) {
        Validate.notNull(name, "name can not be null");
        Validate.notNull(values, "values can not be null");
        Validate.noNullElements(values, "values can not contain null elements");
        
        return this.parameterMap.put(name, values) != null;
    }

    public boolean deleteParameter(String name) {
        Validate.notNull(name, "name can not be null");
        
        return this.parameterMap.remove(name) != null;
    }

    public boolean deleteParameterValue(String name, String value) {
        Validate.notNull(name, "name can not be null");
        Validate.notNull(value, "value can not be null");
        
        final String[] currentValues = this.parameterMap.get(name);
        if (currentValues == null) {
            return false;
        }

        final List<String> newValues = new ArrayList<String>(currentValues.length);
        for (final String currentValue : currentValues) {
            if (value != currentValue && !value.equals(currentValue)) {
                newValues.add(currentValue);
            }
        }
        
        this.parameterMap.put(name, newValues.toArray(new String[newValues.size()]));

        return currentValues.length != newValues.size();
    }

    @Override
    public String getParameter(String name) {
        Validate.notNull(name, "name can not be null");
        
        final String[] pValues = this.parameterMap.get(name);
        
        if (pValues != null && pValues.length > 0) {
            return pValues[0];
        }

        return null;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return this.parameterMap;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(this.parameterMap.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        return this.parameterMap.get(name);
    }
}