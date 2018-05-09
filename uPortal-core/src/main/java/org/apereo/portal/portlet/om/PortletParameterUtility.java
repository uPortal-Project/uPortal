package org.apereo.portal.portlet.om;

import java.util.HashMap;
import java.util.Map;

/**
 * Utilities for working with portlet parameters.
 *
 * @since uPortal 5.2
 */
public class PortletParameterUtility {


    /**
     * Converts from Map String->IPortletDefinitionParameter (as offered by IPortletDefinition)
     * to Map String->String (the view on these parameters that might be simplest for some uses,
     * e.g. generating JSON.
     *
     * @param parametersMap potentially null Map from String name of parameter to
     *     IPortletDefinitionParameter
     * @return potentially null Map from String param name to String param value
     * @since uPortal 5.2
     */
    public static Map<String, String> parameterMapToStringStringMap(
            Map<String, IPortletDefinitionParameter> parametersMap) {

        if (null == parametersMap) {
            return null;
        }

        Map<String, String> stringMap = new HashMap<>(parametersMap.size());

        for (String key : parametersMap.keySet()) {
            stringMap.put(key, parametersMap.get(key).getValue());
        }

        return stringMap;
    }


}
