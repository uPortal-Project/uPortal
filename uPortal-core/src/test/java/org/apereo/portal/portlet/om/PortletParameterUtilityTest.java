package org.apereo.portal.portlet.om;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class PortletParameterUtilityTest {

    @Test
    public void nullMapConvertsToNullMap() {
        assertNull(PortletParameterUtility.parameterMapToStringStringMap(null));
    }

    @Test
    public void convertsParameterMapToStringMap() {
        Map<String, IPortletDefinitionParameter> input = new HashMap<>(2);

        IPortletDefinitionParameter iconParam =
            new PortletDefinitionParameter("icon", "dashboard");
        IPortletDefinitionParameter altMaxUrlParam =
                new PortletDefinitionParameter(
                        "alternativeMaximizedLink", "https://public.my.wisc.edu");

        input.put("icon", iconParam);
        input.put("alternativeMaximizedLink", altMaxUrlParam);

        Map<String, String> expected = new HashMap<>(2);

        expected.put("icon", "dashboard");
        expected.put("alternativeMaximizedLink", "https://public.my.wisc.edu");

        assertEquals(expected, PortletParameterUtility.parameterMapToStringStringMap(input));
    }

    /**
     * JavaBean implementation of IPortletDefinitionParameter for use in testing.
     */
    class PortletDefinitionParameter implements IPortletDefinitionParameter {

        private String name;
        private String value;

        public PortletDefinitionParameter(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public void setValue(String value) {

        }

        @Override
        public void setDescription(String descr) {

        }
    }

}
