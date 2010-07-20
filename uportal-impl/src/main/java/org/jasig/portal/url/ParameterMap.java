/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.url;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Special type of map for dealing with request parameters. Extends a {@link LinkedHashMap} and adds custom {@link #toString()},
 * {@link #hashCode()}, and {@link #equals(Object)} methods that deal with having values that are String[] correctly.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ParameterMap extends LinkedHashMap<String, String[]> {
    private static final long serialVersionUID = 1L;

    public ParameterMap() {
        super();
    }

    public ParameterMap(int initialCapacity, float loadFactor, boolean accessOrder) {
        super(initialCapacity, loadFactor, accessOrder);
    }

    public ParameterMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public ParameterMap(int initialCapacity) {
        super(initialCapacity);
    }

    public ParameterMap(Map<? extends String, ? extends String[]> m) {
        super(m);
    }
    
    public Map<String, List<String>> toListMap() {
        return convertArrayMap(this);
    }
    
    public static Map<String, List<String>> convertArrayMap(Map<String, String[]> parameterMap) {
        final Map<String, List<String>> newMap = new LinkedHashMap<String, List<String>>();
        
        for (final Map.Entry<String, String[]> parameterEntry : parameterMap.entrySet()) {
            newMap.put(parameterEntry.getKey(), Arrays.asList(parameterEntry.getValue()));
        }
        
        return newMap;
    }
    
    public static ParameterMap convertListMap(Map<String, List<String>> parameterMap) {
        final ParameterMap newMap = new ParameterMap();
        
        for (final Map.Entry<String, List<String>> parameterEntry : parameterMap.entrySet()) {
            final List<String> values = parameterEntry.getValue();
            if (values == null) {
                newMap.put(parameterEntry.getKey(), null); 
            }
            else {
                newMap.put(parameterEntry.getKey(), values.toArray(new String[values.size()]));   
            }
        }
        
        return newMap;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof Map<?, ?>))
            return false;
        Map<?, ?> m = (Map<?, ?>) o;
        if (m.size() != size())
            return false;

        try {
            Iterator<Entry<String, String[]>> i = entrySet().iterator();
            while (i.hasNext()) {
                Entry<String, String[]> e = i.next();
                String key = e.getKey();
                String[] value = e.getValue();
                if (value == null) {
                    if (!(m.get(key) == null && m.containsKey(key)))
                        return false;
                }
                else {
                    final Object oValue = m.get(key);
                    if (value != oValue) {
                        if (oValue == null || !(oValue instanceof String[]))
                            return false;
                        
                        if (!Arrays.equals(value, (String[])oValue))
                            return false;
                    }
                }
            }
        }
        catch (ClassCastException unused) {
            return false;
        }
        catch (NullPointerException unused) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int h = 0;
        Iterator<Entry<String, String[]>> i = entrySet().iterator();
        while (i.hasNext()) {
            final Entry<String, String[]> e = i.next();
            final String key = e.getKey();
            final String[] value = e.getValue();
            h += ((key==null   ? 0 : key.hashCode()) ^
                  (value==null ? 0 : Arrays.hashCode(value)));
        }
        return h;
    }

    @Override
    public String toString() {
        Iterator<Entry<String, String[]>> i = entrySet().iterator();
        if (!i.hasNext())
            return "{}";

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (;;) {
            Entry<String, String[]> e = i.next();
            String key = e.getKey();
            String[] value = e.getValue();
            sb.append(key);
            sb.append('=');
            sb.append(Arrays.toString(value));
            if (!i.hasNext())
                return sb.append('}').toString();
            sb.append(", ");
        }
    }
}
