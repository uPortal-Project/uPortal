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
package org.apereo.portal.soffit.renderer;

import static org.junit.Assert.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.portal.soffit.model.v1_0.Bearer;
import org.apereo.portal.soffit.model.v1_0.Definition;
import org.apereo.portal.soffit.model.v1_0.PortalRequest;
import org.apereo.portal.soffit.model.v1_0.Preferences;
import org.junit.Test;
import org.mockito.Mockito;

public class ModelAttributeServiceTest extends ModelAttributeService {

    private final Object returnValue = new Object();

    @Test
    public void testGetModelAttributeFromMethod() {

        final ModelAttributeService modelAttributeService = new ModelAttributeService();

        final Class[] parameterClasses =
                new Class[] {HttpServletRequest.class, PortalRequest.class, Bearer.class};
        final Method method;
        try {
            method = getClass().getMethod("soffitModelAttributeMethod", parameterClasses);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        // Object Model
        final HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        final HttpServletResponse res = Mockito.mock(HttpServletResponse.class);
        final PortalRequest portalRequest = Mockito.mock(PortalRequest.class);
        final Bearer bearer = Mockito.mock(Bearer.class);
        final Preferences preferences = Mockito.mock(Preferences.class);
        final Definition definition = Mockito.mock(Definition.class);

        final Object modelAttribute =
                modelAttributeService.getModelAttributeFromMethod(
                        this, method, req, res, portalRequest, bearer, preferences, definition);

        assertEquals("Incorrect modelAttribute", modelAttribute, returnValue);

        final Method brokenMethod;
        try {
            brokenMethod =
                    getClass().getMethod("brokenSoffitModelAttributeMethod", parameterClasses);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        try {
            modelAttributeService.getModelAttributeFromMethod(
                    this, brokenMethod, req, res, portalRequest, bearer, preferences, definition);
            fail("Expected IllegalStateException for void-declaring method");
        } catch (IllegalStateException e) {
            // Expected;  fall through...
        }
    }

    @Test
    public void testPrepareMethodParameters() {

        final ModelAttributeService modelAttributeService = new ModelAttributeService();

        final Class[] parameterClasses =
                new Class[] {HttpServletRequest.class, PortalRequest.class, Bearer.class};
        final Method method;
        try {
            method = getClass().getMethod("soffitModelAttributeMethod", parameterClasses);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        // Object Model
        final HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        final HttpServletResponse res = Mockito.mock(HttpServletResponse.class);
        final PortalRequest portalRequest = Mockito.mock(PortalRequest.class);
        final Bearer bearer = Mockito.mock(Bearer.class);
        final Preferences preferences = Mockito.mock(Preferences.class);
        final Definition definition = Mockito.mock(Definition.class);

        final Object[] parameters =
                modelAttributeService.prepareMethodParameters(
                        method, req, res, portalRequest, bearer, preferences, definition);

        assertEquals(
                "parameterClasses and parameters arrays must be the same length",
                parameterClasses.length,
                parameters.length);

        for (int i = 0; i < parameters.length; i++) {
            assertTrue("Mismatched parameter type", parameterClasses[i].isInstance(parameters[i]));
        }
    }

    @Test
    public void testAttributeAppliesToView() {

        final ModelAttributeService modelAttributeService = new ModelAttributeService();

        final SoffitModelAttribute unspecific =
                new BaseSoffitModelAttribute() {
                    @Override
                    public String value() {
                        return "unspecific";
                    }

                    @Override
                    public String viewRegex() {
                        return ".*";
                    }
                };

        final SoffitModelAttribute specific =
                new BaseSoffitModelAttribute() {
                    @Override
                    public String value() {
                        return "specific";
                    }

                    @Override
                    public String viewRegex() {
                        return "specific";
                    }
                };

        final String[] unspecificViewNames = new String[] {"apereo", "uPortal", "open source"};
        for (String name : unspecificViewNames) {
            // Each of these should match 'unspecific' but not 'specific'
            assertTrue(
                    "View name '" + name + "' should apply to model attribute: " + unspecific,
                    modelAttributeService.attributeAppliesToView(unspecific, name));
            assertFalse(
                    "View name '" + name + "' should NOT apply to model attribute: " + specific,
                    modelAttributeService.attributeAppliesToView(specific, name));
        }

        // But the name 'specific' should match...
        final String specificName = "specific";
        assertTrue(
                "View name '" + specificName + "' should apply to model attribute: " + specific,
                modelAttributeService.attributeAppliesToView(unspecific, specificName));
    }

    /** For use in some of the test cases via reflection. */
    public Object soffitModelAttributeMethod(
            HttpServletRequest req, PortalRequest portalRequest, Bearer bearer) {
        return returnValue;
    }

    /** For use in some of the test cases via reflection. */
    public void brokenSoffitModelAttributeMethod(
            HttpServletRequest req, PortalRequest portalRequest, Bearer bearer) {
        // do nothing...
    }

    private abstract static class BaseSoffitModelAttribute implements SoffitModelAttribute {
        @Override
        public boolean equals(Object obj) {
            return false;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).toString();
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return getClass();
        }
    };
}
