/*
 * Copyright © 2004 The JA-SIG Collaborative.  All rights reserved.
 * See notice at end of file.
 */
package org.jasig.portal.properties;


import junit.framework.TestCase;

/**
 * Test case for PropertiesManager.
 * Exercises property accessor methods against a test properties file.
 * @author andrew.petro@yale.edu
 */
public class PropertiesManagerTest extends TestCase {

    /**
     * The test properties file is in the properties package of the source tree
     * containing the PropertiesManager itself.
     */
    private static final String TEST_FILE = "./test.properties";
    
    /**
     * Save a copy of the value of the system property so that we can reset it when we clean up.
     */
    private String systemPropertyValue;
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        this.systemPropertyValue = System.getProperty(PropertiesManager.PORTAL_PROPERTIES_FILE_SYSTEM_VARIABLE);
        System.setProperty(PropertiesManager.PORTAL_PROPERTIES_FILE_SYSTEM_VARIABLE, TEST_FILE);
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        if (this.systemPropertyValue != null)
        System.setProperty(PropertiesManager.PORTAL_PROPERTIES_FILE_SYSTEM_VARIABLE, this.systemPropertyValue);
    }

    /**
     * Test the getProperty method.
     * Tests ability to retrieve a property from sample properties file.
     * Tests for proper exception throw when property not found.
     */
    public void testGetPropertyString() {
        assertEquals("splat", PropertiesManager.getProperty("simpleProperty"));
        try {
            PropertiesManager.getProperty("missingProperty");
        } catch (MissingPropertyException mpe){
            // correct
            return;
        }
        fail("Should have thrown an MissingPropertyException because property was missing.");
    }

    /**
     * This test demonstrates that getPropertyUntrimmed does *not* retain leading whitespace
     * on property values.
     */
    public void testGetPropertyUntrimmedLeadingWhitespace() {
        assertEquals("twoSpacesBefore", PropertiesManager.getPropertyUntrimmed("leadingWhitespace"));
    }

    /**
     * Test proper retention of trailing whitespace.
     */
    public void testGetPropertyUntrimmedTrailingWhitespace() {
        assertEquals("oneSpaceAfter ", PropertiesManager.getPropertyUntrimmed("trailingWhitespace"));
    }
    
    /**
     * Test exception throw when property missing.
     */
    public void testGetPropertyUntrimmedMissingProperty() {
        try {
            PropertiesManager.getPropertyUntrimmed("missingProperty");
        } catch (MissingPropertyException mpe){
            // correct
            return;
        }
        fail("Should have thrown an MissingPropertyException because a property was missing.");
    }
    
    /**
     * Test getPropertyAsBoolean().
     * Demonstrates behavior of defaulting to false when property value doesn't "look like" true.
     */
    public void testGetPropertyAsBoolean() {
        assertTrue(PropertiesManager.getPropertyAsBoolean("testBooleanTrue"));
        assertFalse(PropertiesManager.getPropertyAsBoolean("testBooleanFalse"));
        
        // weird (e.g., "wombat") property values evaluate as false
        assertFalse(PropertiesManager.getPropertyAsBoolean("testBadBoolean"));
    }
    
    /**
     * Test getting a missing property as a boolean: throws proper exception.
     */
    public void testGetPropertyAsBooleanMissingProperty() {
        try {
            PropertiesManager.getPropertyAsBoolean("missingProperty");
        } catch (MissingPropertyException mpe) {
            // correct
            return;
        }
        fail("Should have thrown MissingPropertyException because property was missing.");
    }
    
    /**
     * Test getPropertyAsByte().
     */
    public void testGetPropertyAsByte() {
        byte result = PropertiesManager.getPropertyAsByte("testByte");
        assertEquals(3, result);
    }

    /**
     * Test that getPropertyAsByte() throws proper runtime exception when property is missing.
     */
    public void testGetPropertyAsByteMissingProperty() {
        try {
            PropertiesManager.getPropertyAsByte("missingProperty");
        } catch (MissingPropertyException mpe) {
            // correct
            return;
        }
        fail("Should have thrown MissingPropertyException because property was missing.");
    }
    
    /**
     * Test that getPropertyAsByte() throws proper runtime exception when the property
     * value cannot be parsed as a byte.
     */
    public void testGetPropertyAsByteBadValue() {
        try {
            PropertiesManager.getPropertyAsByte("wombatProperty");
        } catch (BadPropertyException pbe){
            // correct
            return;
        }
        fail("Should have thrown BadPropertyException because the property value 'wombat' cannot be parsed as a byte.");
    }
    
    /**
     * Test getPropertyAsShort()
     */
    public void testGetPropertyAsShort() {
        short returned = PropertiesManager.getPropertyAsShort("testShort");
        assertEquals(5, returned);
    }

    /**
     * Test proper exception throw from getPropertyAsShort() when property is missing.
     */
    public void testGetPropertyAsShortMissingProperty() {
        try {
            PropertiesManager.getPropertyAsShort("missingProperty");
        } catch (MissingPropertyException mpe) {
            // correct
            return;
        }
        fail("Should have thrown MissingPropertyException because property was missing.");
    }
    
    /**
     * Test that getPropertyAsShort() throws proper runtime exception when the property
     * value cannot be parsed as a short.
     */
    public void testGetPropertyAsShortBadValue() {
        try {
            PropertiesManager.getPropertyAsShort("wombatProperty");
        } catch (BadPropertyException pbe){
            // correct
            return;
        }
        fail("Should have thrown BadPropertyException because the property value 'wombat' cannot be parsed as a short.");
    }
    
    /**
     * Test getPropertyAsInt()
     */
    public void testGetPropertyAsInt() {
        int returned = PropertiesManager.getPropertyAsInt("testInt");
        assertEquals(10, returned);
    }
    
    /**
     * Test getPropertyAsInt() handling of missing property.
     * Verifies that throws UndeclaredPortalException.
     */
    public void testGetPropertyAsIntMissingProperty() {
        try {
            PropertiesManager.getPropertyAsInt("missingProperty");
        } catch (MissingPropertyException mpe) {
            // correct
            return;
        }
        fail("Should have thrown MissingPropertyException because property was missing.");
    }

    /**
     * Test that getPropertyAsInt() throws proper runtime exception when the property
     * value cannot be parsed as an int.
     */
    public void testGetPropertyAsIntBadValue() {
        try {
            PropertiesManager.getPropertyAsInt("wombatProperty");
        } catch (BadPropertyException pbe){
            // correct
            return;
        }
        fail("Should have thrown BadPropertyException because the property value 'wombat' cannot be parsed as an int.");
    }
    
    /**
     * Test getPropertyAsLong()
     */
    public void testGetPropertyAsLong() {
        long result = PropertiesManager.getPropertyAsLong("testLong");
        assertEquals(45, result);
    }
    
    /**
     * Test proper error handing for getPropertyAsLong() for missing property.
     * In particular, test that throws UndeclaredPortalException when property missing.
     */
    public void testGetPropertyAsLongMissingProperty() {
        try {
            PropertiesManager.getPropertyAsLong("missingProperty");
        } catch (MissingPropertyException mpe) {
            // correct
            return;
        }
        fail("Should have thrown MissingPropertyException because property was missing."); 
    }

    /**
     * Test that getPropertyAsLong() throws proper runtime exception when the property
     * value cannot be parsed as a byte.
     */
    public void testGetPropertyAsLongBadValue() {
        try {
            PropertiesManager.getPropertyAsLong("wombatProperty");
        } catch (BadPropertyException pbe){
            // correct
            return;
        }
        fail("Should have thrown BadPropertyException because the property value 'wombat' cannot be parsed as a long.");
    }
    
    /**
     * Test getPropertyAsFloat()
     */
    public void testGetPropertyAsFloat() {
        float result = PropertiesManager.getPropertyAsFloat("testFloat");
        assertEquals(2.718f, result, 0.01);
    }

    /**
     * Test getPropertyAsFloat() for proper handling of missing property.
     * In particular, tests that UndeclaredPortalException thrown in this case.
     */
    public void testGetPropertyAsFloatMissingProperty() {
        try {
            PropertiesManager.getPropertyAsFloat("missingProperty");
        } catch (MissingPropertyException mpe) {
            // correct
            return;
        }
        fail("Should have thrown MissingPropertyException because property was missing.");
    }
    
    /**
     * Test that getPropertyAsFloat() throws proper runtime exception when the property
     * value cannot be parsed as a float.
     */
    public void testGetPropertyAsFloatBadValue() {
        try {
            PropertiesManager.getPropertyAsFloat("wombatProperty");
        } catch (BadPropertyException pbe){
            // correct
            return;
        }
        fail("Should have thrown BadPropertyException because the property value 'wombat' cannot be parsed as a float.");
    }
    
    /**
     * Test getPropertyAsDouble()
     */
    public void testGetPropertyAsDouble() {
        double result = PropertiesManager.getPropertyAsDouble("testDouble");
        assertEquals(3.1415, result, 0.01);
    }
    
    /**
     * Test getPropertyAsDouble() for proper handling of missing property.
     * In particular, tests that throws UndeclaredPortalException when property missing.
     */
    public void testGetPropertyAsDoubleMissingProperty() {
        try {
            PropertiesManager.getPropertyAsDouble("missingProperty");
        } catch (MissingPropertyException upe) {
            // correct
            return;
        }
        fail("Should have thrown MissingPropertyException because property was missing.");
    }

    /**
     * Test that getPropertyAsDouble() throws proper runtime exception when the property
     * value cannot be parsed as a byte.
     */
    public void testGetPropertyAsDoubleBadValue() {
        try {
            PropertiesManager.getPropertyAsDouble("wombatProperty");
        } catch (BadPropertyException pbe){
            // correct
            return;
        }
        fail("Should have thrown BadPropertyException because the property value 'wombat' cannot be parsed as a double.");
    }
    
    /**
     * Test getPropertyAsString with default value where property is present.
     */
    public void testGetPropertyWithDefault() {
        String result = PropertiesManager.getProperty("simpleProperty", "defaultValue");
        assertEquals("splat", result);
    }
    
    /**
     * Test getPropertyAsString with default value where property is missing.
     */
    public void testGetPropertyWithDefaultPropertyMissing() {
        String result = PropertiesManager.getProperty("missingProperty", "defaultValue");
        assertEquals("defaultValue", result);
        // test that we don't trim default values:
        result = PropertiesManager.getProperty("anotherMissingProperty", "defaultWithThreeTrailingSpaces   ");
        assertEquals("defaultWithThreeTrailingSpaces   ", result);
    }

    /**
     * Test getPropertyUntrimmed() with default value where property is present.
     */
    public void testGetPropertyUntrimmedWithDefault() {
        String result = PropertiesManager.getPropertyUntrimmed("trailingWhitespace", "defaultValue");
        assertEquals("oneSpaceAfter ", result);
    }

    /**
     * Test getPropertyUntrimmed() with default value where property is missing.
     */
    public void testGetPropertyUntrimmedWithDefaultPropertyMissing(){
        String result = PropertiesManager.getPropertyUntrimmed("missingProperty", "defaultValue");
        assertEquals("defaultValue", result);
    }
    
    /**
     * Test getPropertyAsBoolean with default value where property is present.
     */
    public void testGetPropertyAsBooleanWithDefault() {
        assertTrue(PropertiesManager.getPropertyAsBoolean("testBooleanTrue", false));
        assertFalse(PropertiesManager.getPropertyAsBoolean("testBooleanFalse", true));
        
        // demonstrates behavior when property is present but weird value - returns false.
        assertFalse(PropertiesManager.getPropertyAsBoolean("testBadBoolean", true));
    }
    
    /**
     * Test getPropertyAsBoolean with default value where property is absent.
     */
    public void testGetPropertyAsBooleanWithDefaultPropertyMissing() {
        assertTrue(PropertiesManager.getPropertyAsBoolean("missingProperty", true));
        assertFalse(PropertiesManager.getPropertyAsBoolean("missingProperty", false));
    }

    /**
     * Test getPropertyAsByte(String, byte) - default specified, property is present.
     */
    public void testGetPropertyAsByteWithDefault() {
        byte returned = PropertiesManager.getPropertyAsByte("testByte", (byte) 12);
        assertEquals(3, returned);
    }

    /**
     * Test getPropertyAsByte with default value where property is missing.
     */
    public void testGetPropertyAsByteWithDefaultPropertyMissing() {
        byte result = PropertiesManager.getPropertyAsByte("missingPropety", (byte) 12);
        assertEquals((byte) 12, result);
    }
    
    /**
     * Test getPropertyAsByte() with default value where property cannot be
     * parsed as a byte.
     */
    public void testGetPropertyAsByteWithDefaultPropertyBad(){
        byte result = PropertiesManager.getPropertyAsByte("wombatProperty", (byte) 12);
        assertEquals((byte) 12, result);
    }
    
    /**
     * Test getPropertyAsShort(String, short) - default specified, property present.
     */
    public void testGetPropertyAsShortWithDefault() {
        short result = PropertiesManager.getPropertyAsShort("testShort", (short) 12);
        assertEquals(5, result);
    }

    /**
     * Test getPropertyAsShort(String, short) - default specified, property absent.
     */
    public void testGetPropertyAsShortWithDefaultPropertyMissing() {
        short result = PropertiesManager.getPropertyAsShort("missingProperty", (short) 12);
        assertEquals(12, result);
    }
    
    /**
     * Test getPropertyAsShort(String, short) - default specified, property bad.
     */
    public void testGetPropertyAsShortWithDefaultPropertyBad() {
        short result = PropertiesManager.getPropertyAsShort("wombatProperty", (short) 12);
        assertEquals(12, result);
    }
    
    /**
     * Test getPropertyAsInt(String, int) - default specified, property present.
     */
    public void testGetPropertyAsIntWithDefault() {
        int result = PropertiesManager.getPropertyAsInt("testInt", 12);
        assertEquals(10, result);
    }

    /**
     * Test getPropertyAsInt(String, int) - default specified, property absent.
     */
    public void testGetPropertyAsIntWithDefaultPropertyMissing() {
        int result = PropertiesManager.getPropertyAsInt("missingProperty", 12);
        assertEquals(12, result);
    }
    
    /**
     * Test getPropertyAsInt(String, int) - default specified, property bad.
     */
    public void testGetPropertyAsIntWithDefaultPropertyBad() {
        int result = PropertiesManager.getPropertyAsInt("wombatProperty", 12);
        assertEquals(12, result);
    }
    
    /**
     * Test getPropertyAsLong(String, long) - default specified, property present.
     */
    public void testGetPropertyAsLongWithDefault() {
        long result = PropertiesManager.getPropertyAsLong("testLong", 42);
        assertEquals(45, result);
    }

    /**
     * Test getPropertyAsLong(String, long) - default specified, property absent.
     */
    public void testGetPropertyAsLongWithDefaultPropertyMissing() {
        long result = PropertiesManager.getPropertyAsLong("missingProperty", 42);
        assertEquals(42, result);
    }
    
    /**
     * Test getPropertyAsLong(String, long) - default specified, property bad.
     */
    public void testGetPropertyAsLongWithDefaultPropertyBad() {
        long result = PropertiesManager.getPropertyAsLong("wombatProperty", 42);
        assertEquals(42, result);
    }
    
    /**
     * Test getPropertyAsFloat(String, float) - default specified, property present.
     */
    public void testGetPropertyAsFloatWithDefault() {
        float result = PropertiesManager.getPropertyAsFloat("testFloat", (float) 4.2);
        assertEquals(result, 2.718, 0.01);
    }
    
    /**
     * Test getPropertyAsFloat(String, float) - default specified, property absent.
     */
    public void testGetPropertyAsFloatWithDefaultPropertyMissing() {
        float result = PropertiesManager.getPropertyAsFloat("missingProperty", (float) 4.2);
        assertEquals(result, 4.2, 0.01);
    }
    
    /**
     * Test getPropertyAsFloat(String, float) - default specified, property absent.
     */
    public void testGetPropertyAsFloatWithDefaultPropertyBad() {
        float result = PropertiesManager.getPropertyAsFloat("wombatProperty", (float) 4.2);
        assertEquals(result, 4.2, 0.01);
    }

    /**
     * Test getPropertyAsDouble(String, double) - default specified, property present.
     */
    public void testGetPropertyAsDoubleWithDefault() {
        double result = PropertiesManager.getPropertyAsDouble("testDouble", 2.22);
        assertEquals(3.1415, result, 0.01);
    }

    /**
     * Test getPropertyAsDouble(String, double) - default specified, property absent.
     */
    public void testGetPropertyAsDoubleWithDefaultPropertyMissing() {
        double result = PropertiesManager.getPropertyAsDouble("missingProperty", 2.22);
        assertEquals(2.22, result, 0.01);
    }
    
    /**
     * Test getPropertyAsDouble(String, double) - default specified, property bad.
     */
    public void testGetPropertyAsDoubleWithDefaultPropertyBad() {
        double result = PropertiesManager.getPropertyAsDouble("wombatProperty", 2.22);
        assertEquals(2.22, result, 0.01);
    }
    
    /**
     * Test that the getMissingProperties() method reports missing properties.
     */
    public void testGetMissingProperties() {
        int prevMissingCount = PropertiesManager.getMissingProperties().size();
        assertEquals("defaultValue", PropertiesManager.getProperty("emphaticallyMissing", "defaultValue"));
        assertTrue(PropertiesManager.getMissingProperties().contains("emphaticallyMissing"));
        assertEquals(prevMissingCount + 1, PropertiesManager.getMissingProperties().size());
    }

}

/*
 * Copyright © 2004 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */