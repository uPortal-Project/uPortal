package org.jasig.portal.portlets;

import java.io.IOException;

import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;

import net.sf.json.JSONObject;

import org.codehaus.jackson.JsonNode;

/**
 * DAO That stores/retrieves objects as JSON in portlet preferences
 * 
 * @author Eric Dalquist
 */
public interface PortletPreferencesJsonDao {

    /**
     * Stores a Java object as JSON in a portlet preference
     * 
     * @param prefs Preferences to persist to
     * @param key Preference key to store in
     * @param data Object to store as JSON
     */
    void storeJson(PortletPreferences prefs, String key, Object data)
            throws IOException, ReadOnlyException, ValidatorException;

    /**
     * Read the specified portlet preference and parse it as a JSON string into the specified type.
     * If the preference is null returns null. 
     * 
     * @param prefs Preferences to read from
     * @param key Preference key to read from
     * @param type The class type parse the JSON into
     */
    <E> E getJson(PortletPreferences prefs, String key, Class<E> type)
            throws IOException;

    /**
     * Read the specified portlet preference and parse it as a JSON string into a {@link JsonNode}
     * If the preference is null returns a {@link JSONObject}.
     * 
     * @param prefs Preferences to read from
     * @param key Preference key to read from
     */
    JsonNode getJsonNode(PortletPreferences prefs, String key)
            throws IOException;

}