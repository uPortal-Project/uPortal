/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.url;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Special type of map for dealing with request parameters. Extends a {@link LinkedHashMap} and adds
 * custom {@link #toString()}, {@link #hashCode()}, and {@link #equals(Object)} methods that deal
 * with having values that are String[] correctly.
 *
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
        super(m.size());

        for (final Map.Entry<? extends String, ? extends String[]> parameterEntry : m.entrySet()) {
            final String name = parameterEntry.getKey();
            final String[] values = parameterEntry.getValue();

            if (values == null) {
                this.put(name, new String[0]);
            } else {
                final String[] newValues = Arrays.copyOf(values, values.length);
                this.put(name, newValues);
            }
        }
    }

    public Map<String, List<String>> toListMap() {
        return convertArrayMap(this);
    }

    public static Map<String, List<String>> convertArrayMap(Map<String, String[]> parameterMap) {
        final Map<String, List<String>> newMap = new LinkedHashMap<String, List<String>>();

        for (final Map.Entry<String, String[]> parameterEntry : parameterMap.entrySet()) {
            final String[] values = parameterEntry.getValue();
            if (values == null) {
                newMap.put(parameterEntry.getKey(), new ArrayList<String>());
            } else {
                newMap.put(parameterEntry.getKey(), Arrays.asList(values));
            }
        }

        return newMap;
    }

    public static ParameterMap convertListMap(Map<String, List<String>> parameterMap) {
        final ParameterMap newMap = new ParameterMap();

        for (final Map.Entry<String, List<String>> parameterEntry : parameterMap.entrySet()) {
            final List<String> values = parameterEntry.getValue();
            if (values == null) {
                newMap.put(parameterEntry.getKey(), new String[0]);
            } else {
                newMap.put(parameterEntry.getKey(), values.toArray(new String[values.size()]));
            }
        }

        return newMap;
    }

    public static Map<String, List<String>> immutableCopyOfListMap(
            Map<String, List<String>> parameterMap) {
        final Builder<String, List<String>> builder = ImmutableMap.builder();

        for (final Map.Entry<String, List<String>> parameterEntry : parameterMap.entrySet()) {
            final List<String> values = parameterEntry.getValue();
            if (values == null || values.isEmpty()) {
                builder.put(parameterEntry.getKey(), Collections.<String>emptyList());
            } else {
                builder.put(parameterEntry.getKey(), ImmutableList.copyOf(values));
            }
        }

        return builder.build();
    }

    public static Map<String, List<String>> immutableCopyOfArrayMap(
            Map<String, String[]> parameterMap) {
        final Builder<String, List<String>> builder = ImmutableMap.builder();

        for (final Map.Entry<String, String[]> parameterEntry : parameterMap.entrySet()) {
            final String[] values = parameterEntry.getValue();
            if (values == null || values.length == 0) {
                builder.put(parameterEntry.getKey(), Collections.<String>emptyList());
            } else {
                builder.put(parameterEntry.getKey(), ImmutableList.copyOf(values));
            }
        }

        return builder.build();
    }

    public static void putAllList(Map<String, List<String>> dest, Map<String, String[]> src) {
        for (final Map.Entry<String, String[]> parameterEntry : src.entrySet()) {
            final String[] values = parameterEntry.getValue();
            if (values == null) {
                dest.put(parameterEntry.getKey(), new ArrayList<String>());
            } else {
                dest.put(parameterEntry.getKey(), Arrays.asList(values));
            }
        }
    }

    public static void putAllArray(Map<String, String[]> dest, Map<String, List<String>> src) {
        for (final Map.Entry<String, List<String>> parameterEntry : src.entrySet()) {
            final List<String> values = parameterEntry.getValue();
            if (values == null) {
                dest.put(parameterEntry.getKey(), new String[0]);
            } else {
                dest.put(parameterEntry.getKey(), values.toArray(new String[values.size()]));
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;

        if (!(o instanceof Map<?, ?>)) return false;
        Map<?, ?> m = (Map<?, ?>) o;
        if (m.size() != size()) return false;

        try {
            Iterator<Entry<String, String[]>> i = entrySet().iterator();
            while (i.hasNext()) {
                Entry<String, String[]> e = i.next();
                String key = e.getKey();
                String[] value = e.getValue();
                if (value == null) {
                    if (!(m.get(key) == null && m.containsKey(key))) return false;
                } else {
                    final Object oValue = m.get(key);
                    if (value != oValue) {
                        if (oValue == null || !(oValue instanceof String[])) return false;

                        if (!Arrays.equals(value, (String[]) oValue)) return false;
                    }
                }
            }
        } catch (ClassCastException unused) {
            return false;
        } catch (NullPointerException unused) {
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
            h +=
                    ((key == null ? 0 : key.hashCode())
                            ^ (value == null ? 0 : Arrays.hashCode(value)));
        }
        return h;
    }

    @Override
    public String toString() {
        Iterator<Entry<String, String[]>> i = entrySet().iterator();
        if (!i.hasNext()) return "{}";

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (; ; ) {
            Entry<String, String[]> e = i.next();
            String key = e.getKey();
            String[] value = e.getValue();
            sb.append(key);
            sb.append('=');
            sb.append(Arrays.toString(value));
            if (!i.hasNext()) return sb.append('}').toString();
            sb.append(", ");
        }
    }
}
