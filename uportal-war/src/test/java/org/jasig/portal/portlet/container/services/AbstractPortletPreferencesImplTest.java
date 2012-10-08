/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.portlet.container.services;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;

import org.apache.commons.collections.EnumerationUtils;
import org.jasig.portal.portlet.dao.jpa.PortletPreferenceImpl;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.jasig.portal.url.ParameterMap;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class AbstractPortletPreferencesImplTest {
    private Map<String, IPortletPreference> targetPrefs = new LinkedHashMap<String, IPortletPreference>();
    private Map<String, IPortletPreference> basePrefs = new LinkedHashMap<String, IPortletPreference>();
    private Map<String, IPortletPreference> storedPrefs = Collections.emptyMap();
    private boolean modified = true;
    private AbstractPortletPreferencesImpl<Object> portletPreferences;
    
    @Before
    public void setup() {
        targetPrefs = new LinkedHashMap<String, IPortletPreference>();
        basePrefs = new LinkedHashMap<String, IPortletPreference>();
        storedPrefs = Collections.emptyMap();
        modified = true;
                
        portletPreferences = new AbstractPortletPreferencesImpl<Object>(false) {
            @Override
            protected Object getLogDescription() {
                return "TEST";
            }

            @Override
            protected void loadTargetPortletPreferences(Object initContext, Map<String, IPortletPreference> targetPortletPreferences) {
                targetPortletPreferences.putAll(targetPrefs);
            }

            @Override
            protected void loadBasePortletPreferences(Object initContext, Map<String, IPortletPreference> basePortletPreferences) {
                basePortletPreferences.putAll(basePrefs);
            }

            @Override
            protected boolean storeInternal() throws IOException, ValidatorException {
                final Map<String, IPortletPreference> targetPortletPreferences = this.getTargetPortletPreferences();
                storedPrefs = ImmutableMap.copyOf(targetPortletPreferences); 
                return modified;
            }
        };
    }
    
    protected void addPref(Map<String, IPortletPreference> prefs, String name, boolean readOnly, String[] values) {
        final PortletPreferenceImpl preference = new PortletPreferenceImpl(name, readOnly);
        preference.setValues(values);
        prefs.put(name, preference);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testIsReadOnlyNullKey() throws ReadOnlyException, ValidatorException, IOException {
        portletPreferences.isReadOnly((String)null);
    }
    
    @Test
    public void testIsReadOnly() throws ReadOnlyException, ValidatorException, IOException {
        addPref(basePrefs, "key1", true, new String[] { "default" });
        addPref(basePrefs, "key2", false, new String[] { "default" });
        
        boolean readOnly = portletPreferences.isReadOnly("key1");
        assertTrue(readOnly);
        
        readOnly = portletPreferences.isReadOnly("key2");
        assertFalse(readOnly);
        
        readOnly = portletPreferences.isReadOnly("key3");
        assertFalse(readOnly);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullGetValueKey() throws ReadOnlyException {
        portletPreferences.getValue(null, null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullGetValuesKey() throws ReadOnlyException {
        portletPreferences.getValues(null, null);
    }
    
    @Test
    public void testGetValue() throws ReadOnlyException, ValidatorException, IOException {
        addPref(basePrefs, "key1", false, null);
        addPref(basePrefs, "key2", false, new String[] { });
        addPref(basePrefs, "key3", false, new String[] { null });
        addPref(basePrefs, "key4", false, new String[] { "value1", "value2"});
        
        String value = portletPreferences.getValue("key0", "FOOBAR");
        assertEquals("FOOBAR", value);
        
        // Next 3 asserts check whether null values are treated like non-existent values as specified in
        // PortletPreferences#getValue(String, String)
        value = portletPreferences.getValue("key1", "FOOBAR");
        assertEquals("FOOBAR", value);
        
        value = portletPreferences.getValue("key2", "FOOBAR");
        assertEquals("FOOBAR", value);
        
        value = portletPreferences.getValue("key3", "FOOBAR");
        assertNull(value);
        
        value = portletPreferences.getValue("key4", "FOOBAR");
        assertEquals("value1", value);
        
        value = portletPreferences.getValue("key1", null);
        assertNull(value);
        
        value = portletPreferences.getValue("key2", null);
        assertNull(value);
        
        
        value = portletPreferences.getValue("key3", null);
        assertNull(value);
    }
    
    @Test
    public void testGetValues() throws ReadOnlyException, ValidatorException, IOException {
        addPref(basePrefs, "key1", false, null);
        addPref(basePrefs, "key2", false, new String[] { });
        addPref(basePrefs, "key3", false, new String[] { null });
        addPref(basePrefs, "key4", false, new String[] { "value1", "value2"});
        
        String[] values = portletPreferences.getValues("key0", new String[] { "FOOBAR" });
        assertArrayEquals(new String[] { "FOOBAR" }, values);
        
        values = portletPreferences.getValues("key1", new String[] { "FOOBAR" });
        assertNull(values);
        
        values = portletPreferences.getValues("key2", new String[] { "FOOBAR" });
        assertArrayEquals(new String[] { }, values);
        
        values = portletPreferences.getValues("key3", new String[] { "FOOBAR" });
        assertArrayEquals(new String[] { null }, values);
        
        values = portletPreferences.getValues("key4", new String[] { "FOOBAR" });
        assertArrayEquals(new String[] { "value1", "value2"}, values);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullSetValueKey() throws ReadOnlyException {
        portletPreferences.setValue(null, null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullSetValuesKey() throws ReadOnlyException {
        portletPreferences.setValues(null, null);
    }
    
    @Test
    public void testNullSetValueValue() throws ReadOnlyException, ValidatorException, IOException {
        portletPreferences.setValue("key", null);
        portletPreferences.store();
        
        assertEquals(1, this.storedPrefs.size());
        
        final IPortletPreference pref = this.storedPrefs.get("key");
        assertNotNull(pref);
        assertEquals("key", pref.getName());
        assertArrayEquals(new String[] { null }, pref.getValues());
        assertFalse(pref.isReadOnly());
    }
    
    @Test
    public void testNullSetValueValues() throws ReadOnlyException, ValidatorException, IOException {
        portletPreferences.setValues("key", null);
        portletPreferences.store();
        
        assertEquals(1, this.storedPrefs.size());
        
        final IPortletPreference pref = this.storedPrefs.get("key");
        assertNotNull(pref);
        assertEquals("key", pref.getName());
        assertNull(pref.getValues());
        assertFalse(pref.isReadOnly());
    }
    
    @Test
    public void testNullEntryInValues() throws ReadOnlyException, ValidatorException, IOException {
        portletPreferences.setValues("key", new String[] { null });
        portletPreferences.store();
        
        assertEquals(1, this.storedPrefs.size());
        
        final IPortletPreference pref = this.storedPrefs.get("key");
        assertNotNull(pref);
        assertEquals("key", pref.getName());
        assertArrayEquals(new String[] { null }, pref.getValues());
        assertFalse(pref.isReadOnly());
    }

    
    @Test(expected=ReadOnlyException.class)
    public void testSetReadOnlyValue() throws ReadOnlyException, ValidatorException, IOException {
        addPref(basePrefs, "key", true, new String[] { "default" });

        //Set a modified value
        portletPreferences.setValue("key", "modified" );
    }

    
    @Test(expected=ReadOnlyException.class)
    public void testSetReadOnlyValues() throws ReadOnlyException, ValidatorException, IOException {
        addPref(basePrefs, "key", true, new String[] { "default" });

        //Set a modified value
        portletPreferences.setValues("key", new String[] { "modified" });
    }

    
    @Test
    public void testSetMatchesBase() throws ReadOnlyException, ValidatorException, IOException {
        addPref(basePrefs, "key", false, new String[] { "default" });

        //Set a modified value
        portletPreferences.setValues("key", new String[] { "modified" });
        
        //Initial store, check that correct stored map is created
        portletPreferences.store();
        
        //Actually "store" the stored prefs
        this.targetPrefs = new LinkedHashMap<String, IPortletPreference>(this.storedPrefs);
        
        assertEquals(1, this.storedPrefs.size());
        
        IPortletPreference pref = this.storedPrefs.get("key");
        assertNotNull(pref);
        assertEquals("key", pref.getName());
        assertArrayEquals(new String[] { "modified" }, pref.getValues());
        assertFalse(pref.isReadOnly());
        
        
        //Set the default value
        portletPreferences.setValues("key", new String[] { "default" });
        
        //Store again, should have nothing stored after this
        portletPreferences.store();
        
        assertEquals(0, this.storedPrefs.size());
    }

    
    @Test
    public void testSetUpdateExisting() throws ReadOnlyException, ValidatorException, IOException {
        addPref(basePrefs, "key", false, new String[] { "default" });

        //Set a modified value
        portletPreferences.setValues("key", new String[] { "modified" });
        
        //Initial store, check that correct stored map is created
        portletPreferences.store();
        
        //Actually "store" the stored prefs
        this.targetPrefs = new LinkedHashMap<String, IPortletPreference>(this.storedPrefs);
        
        assertEquals(1, this.storedPrefs.size());
        
        IPortletPreference pref = this.storedPrefs.get("key");
        assertNotNull(pref);
        assertEquals("key", pref.getName());
        assertArrayEquals(new String[] { "modified" }, pref.getValues());
        assertFalse(pref.isReadOnly());
        

        //Set a modified value
        portletPreferences.setValues("key", null);
        
        //Initial store, check that correct stored map is created
        portletPreferences.store();
        
        //Actually "store" the stored prefs
        this.targetPrefs = new LinkedHashMap<String, IPortletPreference>(this.storedPrefs);
        
        assertEquals(1, this.storedPrefs.size());
        
        pref = this.storedPrefs.get("key");
        assertNotNull(pref);
        assertEquals("key", pref.getName());
        assertArrayEquals(null, pref.getValues());
        assertFalse(pref.isReadOnly());
    }
    
    @Test
    public void testGetNames() throws ReadOnlyException, ValidatorException, IOException {
        addPref(basePrefs, "key", false, new String[] { "default" });
        addPref(basePrefs, "key1", false, new String[] { "default" });
        
        Enumeration<String> names = portletPreferences.getNames();
        assertEquals(ImmutableSet.of("key", "key1"), new HashSet<String>(EnumerationUtils.toList(names)));

        //Set a modified value
        portletPreferences.setValues("key", new String[] { "modified" });
        portletPreferences.setValues("key3", new String[] { "modified" });
        
        names = portletPreferences.getNames();
        assertEquals(ImmutableSet.of("key", "key1", "key3"), new HashSet<String>(EnumerationUtils.toList(names)));
        
        //Initial store, check that correct stored map is created
        portletPreferences.store();
        
        //Actually "store" the stored prefs
        this.targetPrefs = new LinkedHashMap<String, IPortletPreference>(this.storedPrefs);
        
        names = portletPreferences.getNames();
        assertEquals(ImmutableSet.of("key", "key1", "key3"), new HashSet<String>(EnumerationUtils.toList(names)));
        
        
        portletPreferences.reset("key3");
        
        names = portletPreferences.getNames();
        assertEquals(ImmutableSet.of("key", "key1"), new HashSet<String>(EnumerationUtils.toList(names)));
        
        //Initial store, check that correct stored map is created
        portletPreferences.store();
        
        //Actually "store" the stored prefs
        this.targetPrefs = new LinkedHashMap<String, IPortletPreference>(this.storedPrefs);
        
        names = portletPreferences.getNames();
        assertEquals(ImmutableSet.of("key", "key1"), new HashSet<String>(EnumerationUtils.toList(names)));
    }
    
    @Test
    public void testGetMap() throws ReadOnlyException, ValidatorException, IOException {
        addPref(basePrefs, "key", false, new String[] { "default" });
        addPref(basePrefs, "key1", false, new String[] { "default" });
        
        Map<String, String[]> map = portletPreferences.getMap();
        assertEquals(ImmutableMap.of("key", ImmutableList.of("default"), "key1", ImmutableList.of("default")), ParameterMap.convertArrayMap(map));

        //Set a modified value
        portletPreferences.setValues("key", new String[] { "modified" });
        portletPreferences.setValues("key3", new String[] { "modified" });
        
        map = portletPreferences.getMap();
        assertEquals(ImmutableMap.of("key", ImmutableList.of("modified"), "key1", ImmutableList.of("default"), "key3", ImmutableList.of("modified")), ParameterMap.convertArrayMap(map));
        
        //Initial store, check that correct stored map is created
        portletPreferences.store();
        
        //Actually "store" the stored prefs
        this.targetPrefs = new LinkedHashMap<String, IPortletPreference>(this.storedPrefs);
        
        map = portletPreferences.getMap();
        assertEquals(ImmutableMap.of("key", ImmutableList.of("modified"), "key1", ImmutableList.of("default"), "key3", ImmutableList.of("modified")), ParameterMap.convertArrayMap(map));
        
        
        portletPreferences.reset("key3");
        
        map = portletPreferences.getMap();
        assertEquals(ImmutableMap.of("key", ImmutableList.of("modified"), "key1", ImmutableList.of("default")), ParameterMap.convertArrayMap(map));
        
        //Initial store, check that correct stored map is created
        portletPreferences.store();
        
        //Actually "store" the stored prefs
        this.targetPrefs = new LinkedHashMap<String, IPortletPreference>(this.storedPrefs);
        
        map = portletPreferences.getMap();
        assertEquals(ImmutableMap.of("key", ImmutableList.of("modified"), "key1", ImmutableList.of("default")), ParameterMap.convertArrayMap(map));
    }
    

    
    @Test(expected=ReadOnlyException.class)
    public void testResetReadOnly() throws ReadOnlyException, ValidatorException, IOException {
        addPref(basePrefs, "key", true, new String[] { "default" });
        
        portletPreferences.reset("key");
    }
    
    @Test
    public void testResetToBase() throws ReadOnlyException, ValidatorException, IOException {
        addPref(basePrefs, "key", false, new String[] { "default" });

        //Set a modified value
        portletPreferences.setValues("key", new String[] { "modified" });
        
        //Initial store, check that correct stored map is created
        portletPreferences.store();
        
        //Actually "store" the stored prefs
        this.targetPrefs = new LinkedHashMap<String, IPortletPreference>(this.storedPrefs);
        
        assertEquals(1, this.storedPrefs.size());
        
        IPortletPreference pref = this.storedPrefs.get("key");
        assertNotNull(pref);
        assertEquals("key", pref.getName());
        assertArrayEquals(new String[] { "modified" }, pref.getValues());
        assertFalse(pref.isReadOnly());
        
        
        //Get the current value
        String[] values = portletPreferences.getValues("key", null);
        assertArrayEquals(new String[] { "modified" }, values);
        
        //Reset it
        portletPreferences.reset("key");
        
        //Get the default value
        values = portletPreferences.getValues("key", null);
        assertArrayEquals(new String[] { "default" }, values);
        
        
        //Do another store to verify nothing gets stored
        portletPreferences.store();
        
        //Actually "store" the stored prefs
        this.targetPrefs = new LinkedHashMap<String, IPortletPreference>(this.storedPrefs);
        
        assertEquals(0, this.storedPrefs.size());
    }
}
