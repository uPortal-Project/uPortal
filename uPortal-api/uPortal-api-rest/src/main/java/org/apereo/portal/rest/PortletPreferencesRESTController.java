package org.apereo.portal.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiParam;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.portal.portlet.container.services.PortletPreferencesFactory;
import org.apereo.portal.portlet.dao.IPortletDefinitionDao;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletDefinitionId;
import org.apereo.portal.portlet.om.IPortletEntity;
import org.apereo.portal.portlet.om.IPortletPreference;
import org.apereo.portal.portlet.registry.IPortletEntityRegistry;
import org.apereo.portal.security.AuthorizationPrincipalHelper;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.security.PortalSecurityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * PortletPreferencesRESTController provides REST targets for preference operations for soffits and
 * webcomponents.
 */
@Setter
@Getter
@Slf4j
@RestController
public class PortletPreferencesRESTController {

    private ObjectMapper mapper = new ObjectMapper();

    private IPersonManager personManager;

    private final boolean DEFINITION_MODE = true;
    private final boolean ENTITY_MODE = false;

    private static final String METHOD_COMPOSITE = "composite";
    private static final String METHOD_SINGLE_ONLY = "singleonly";

    private IPortletDefinitionDao portletDao;

    private IPortletEntityRegistry portletEntityRegistry;

    private PortletPreferencesFactory portletPreferencesFactory;

    @Autowired
    @Qualifier("portletPreferencesFactory")
    public void setPortletPreferencesFactoryAPI(
            PortletPreferencesFactory portletPreferencesFactory) {
        this.portletPreferencesFactory = portletPreferencesFactory;
    }

    /**
     * Get uPortal Preferences for portlet/soffit/webcomponent.
     *
     * <p>The path for this method is /preferences/fname. The "fname" is a string representing the
     * fname of the portlet/soffit/webcomponent to return the preferences for.
     *
     * <p>An optional request parameter of "method" can be included, with supported values of
     * "composite" or "singleonly".
     *
     * <p>"composite" returns the per-user preferences for the given portlet/soffit/webcomponent
     * including any non-overwritten definition and descriptor preferences for the given
     * portlet/soffit/webcomponent. Every preference for the soffit with the current user's
     * preferences set is returned, including extra preferences for soffits (for example, the url),
     * so please set any of these to read-only.
     *
     * <p>"singleonly" returns only the user set preferences -- it does not include any defaults set
     * in the definition.
     */
    @GetMapping("/preferences/{fname}")
    public ResponseEntity<String> getPreferences(
            @ApiParam(value = "The scope of the parameters to get") @RequestParam String method,
            @ApiParam(value = "The portlet fname to get preferences for")
                    @PathVariable(value = "fname")
                    String fname,
            HttpServletRequest request,
            HttpServletResponse response) {
        IPortletDefinition portletDefinition = portletDao.getPortletDefinitionByFname(fname);
        if (portletDefinition == null) {
            return new ResponseEntity<>("ERROR: Portlet not found", HttpStatus.NOT_FOUND);
        }
        try {
            IPortletEntity entity =
                    portletEntityRegistry.getOrCreateDefaultPortletEntity(
                            request, portletDefinition.getPortletDefinitionId());
            PortletPreferences entityPreferences =
                    portletPreferencesFactory.createAPIPortletPreferences(
                            request, entity, false, ENTITY_MODE);

            if (StringUtils.isBlank(method)
                    || StringUtils.equalsIgnoreCase(method, METHOD_COMPOSITE)) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(entityPreferences.getMap()));

            } else if (StringUtils.equalsIgnoreCase(method, METHOD_SINGLE_ONLY)) {
                PortletPreferences definitionPreferences =
                        portletPreferencesFactory.createAPIPortletPreferences(
                                request, entity, false, DEFINITION_MODE);
                Map<String, String[]> userPreferences =
                        getEntityOnly(entityPreferences.getMap(), definitionPreferences.getMap());

                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(userPreferences));
            } else {
                // unsupported
                return ResponseEntity.badRequest()
                        .body("Request method " + method + " not supported.");
            }
        } catch (
                IllegalArgumentException
                        e) { // TODO: find out what other exceptions could be thrown
            if (e.getMessage().contains("No portlet definition found for id '")) {
                String error = "ERROR: User not authorized to access portlet '" + fname + "'.";
                log.info(error);
                return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
            }
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            log.error("threw error trying to retrieve composite preferences", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get uPortal Entity information for portlet/soffit/webcomponent. This method provides a REST
     * interface for obtaining uPortal portlet/soffit/webcomponent metadata.
     *
     * <p>The path for this method is /preferences/entity/fname. The fname is a string representing
     * the fname of the portlet/soffit/webcomponent to find the entity information for.
     */
    // TODO: Drew wondered if this should live somewhere else. Maybe a portlet/soffit/webcomponent
    // metadata endpoint? /metadata/entity/{fname}?
    @RequestMapping(value = "/preferences/entity/{fname}", method = RequestMethod.GET)
    public ResponseEntity<String> getEntity(
            HttpServletRequest request,
            HttpServletResponse response,
            @ApiParam(value = "The portlet fname to get entity for") @PathVariable(value = "fname")
                    String fname) {
        try {
            IPortletDefinition portletDefinition = portletDao.getPortletDefinitionByFname(fname);
            if (portletDefinition == null) {
                return new ResponseEntity<>("ERROR: Portlet not found", HttpStatus.NOT_FOUND);
            }
            IPortletEntity entity =
                    portletEntityRegistry.getOrCreateDefaultPortletEntity(
                            request, portletDefinition.getPortletDefinitionId());
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder
                    .append("{\"" + portletDefinition.getFName() + "\": {")
                    .append(" \"layoutNodeID\": \"" + entity.getLayoutNodeId() + "\",")
                    .append(" \"userID\": \"" + entity.getUserId() + "\",")
                    .append(" \"entityID\": \"" + entity.getPortletEntityId() + "\",")
                    .append(" \"windowStates\": [");
            boolean hasComma = false;
            for (Map.Entry<Long, WindowState> entry : entity.getWindowStates().entrySet()) {
                stringBuilder
                        .append(" { \"stylesheetID\": \"")
                        .append(entry.getKey())
                        .append("\", \"windowState\": \"")
                        .append(entry.getValue().toString())
                        .append("\"},");
                hasComma = true;
            }
            if (hasComma) {
                stringBuilder.setLength(stringBuilder.length() - 1);
            }
            stringBuilder.append(" ] } }");
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(stringBuilder.toString());
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("No portlet definition found for id '")) {
                String error = "ERROR: User not authorized to access portlet '" + fname + "'.";
                log.info(error);
                return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
            }
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // get the entity preferences by comparing the entity with the defintion and
    // returning the entity ones that are different
    private Map<String, String[]> getEntityOnly(
            Map<String, String[]> entity, Map<String, String[]> definition) {
        Map<String, String[]> result = new HashMap<>();
        for (Map.Entry<String, String[]> entry : entity.entrySet()) {
            String key = entry.getKey();
            String[] value = entry.getValue();
            if (!definition.containsKey(key)) {
                // the entity has something the definition does not
                result.put(key, value);
            } else {
                // they both have it so compare the two
                String[] defvalues = definition.get(key);
                if (value == null) {
                    if (defvalues != null) {
                        result.put(key, value);
                    }
                } else {
                    if (!Arrays.equals(value, defvalues)) {
                        result.put(key, value);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Get uPortal definition preferences for portlet/soffit/webcomponent. This method provides a
     * REST interface for uPortal for obtaining the definition preferences for the
     * portlet/soffit/webcomponent
     *
     * <p>The path for this method is /preferences/definition/fname. The "fname" is a string
     * representing the fname of the portlet/soffit/webcomponent to return the definition
     * preferences.
     *
     * <p>An optional request parameter of "method" can be included, with supported values of
     * "composite" or "singleonly".
     *
     * <p>"composite" returns the default preferences for the given portlet/soffit/webcomponent
     * including any non-overwritten descriptor preferences for the given
     * portlet/soffit/webcomponent. This Will include extra preferences for soffits including the
     * url, so please set any of these to read-only when you define them.
     *
     * <p>"singleonly" returns only the user set preferences -- it does not include any defaults set
     * in the definition.
     *
     * <p>These are the default preferences for the given portlet/soffit/webcomponent including any
     * non-overwritten descriptor preferences for the given portlet/soffit/webcomponent. Will
     * include extra preferences for soffits including the url, so please set any of these to
     * read-only when you define them. You must be logged in as a portal administrator or have
     * configuration rights for the given portlet/soffit/webcomponent in uPortal to access these.
     * Will return a 403 or 404 otherwise.
     */
    @GetMapping("/preferences/definition/{fname}")
    public ResponseEntity<String> getDefinitionPreferences(
            @ApiParam(value = "The scope of the parameters to get") @RequestParam String method,
            @ApiParam(value = "The portlet fname to find preferences for")
                    @PathVariable(value = "fname")
                    String fname,
            HttpServletRequest request,
            HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        IPerson person;
        try {
            person = personManager.getPerson(request);
        } catch (PortalSecurityException e) {
            return new ResponseEntity<>(
                    "ERROR: no user. Are you logged in?", HttpStatus.UNAUTHORIZED);
        }

        if (person.isGuest()) {
            log.warn(
                    "Guest user tried to access getDefinitionPreferences. Possible hacking attempt.");
            return new ResponseEntity<>(
                    "ERROR: guest cannot use this action.", HttpStatus.UNAUTHORIZED);
        } else if (!person.getSecurityContext().isAuthenticated()) {
            log.warn(
                    "Person: {"
                            + person.getUserName()
                            + "} tried to access getDefinitionPreferences while not authenticated. Possible"
                            + " hacking attempt.");
            return new ResponseEntity<>(
                    "ERROR: must be logged in to use this action.", HttpStatus.UNAUTHORIZED);
        }

        IPortletDefinition portletDefinition = portletDao.getPortletDefinitionByFname(fname);
        if (portletDefinition == null) {
            return new ResponseEntity<>("ERROR: Portlet not found", HttpStatus.NOT_FOUND);
        }

        if (!canConfigure(person, portletDefinition.getPortletDefinitionId())) {
            log.warn(
                    "Person: {"
                            + person.getUserName()
                            + "} tried to access getDefinitionPreferences for portlet: {"
                            + fname
                            + "} without configuration permissions. Possible hacking attempt.");
            return new ResponseEntity<>(
                    "ERROR: user is not authorized to perform this action",
                    HttpStatus.UNAUTHORIZED);
        }

        try {
            if (method.isEmpty() || method.equalsIgnoreCase(METHOD_COMPOSITE)) {
                IPortletEntity entity =
                        portletEntityRegistry.getOrCreateDefaultPortletEntity(
                                request, portletDefinition.getPortletDefinitionId());

                PortletPreferences preferences =
                        portletPreferencesFactory.createAPIPortletPreferences(
                                request, entity, false, DEFINITION_MODE);

                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(preferences.getMap()));

            } else if (method.equalsIgnoreCase(METHOD_SINGLE_ONLY)) {
                List<IPortletPreference> preferences = portletDefinition.getPortletPreferences();

                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(prefMap(preferences)));

            } else {
                return ResponseEntity.badRequest()
                        .body("Request method " + method + " not supported.");
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean canConfigure(IPerson user, IPortletDefinitionId portletDefinitionId) {
        return AuthorizationPrincipalHelper.principalFromUser(user)
                .canConfigure(portletDefinitionId.toString());
    }

    private Map<String, String[]> prefMap(List<IPortletPreference> preferences) {
        Map<String, String[]> map = new HashMap<>();
        for (IPortletPreference p : preferences) {
            map.put(p.getName(), p.getValues());
        }
        return map;
    }

    /**
     * Set uPortal definition preferences for portlet/soffit/webcomponent. This method provides a
     * REST interface for uPortal for obtaining the definition preferences for the
     * portlet/soffit/webcomponent
     *
     * <p>The path for this method is /preferences/putdefinitionpreferences/fname. The fname is a
     * string representing the fname of the portlet/soffit/webcomponent to set definition
     * preferences for. These are the default preferences for the given portlet/soffit/webcomponent.
     * This will set the key:value(s) pairs provided into the defintion preferences overwriting any
     * current definition preferences with the same key, adding a new key:value(s) pair if the key
     * does not exits. You must be logged in as a portal administrator or have configuration rights
     * for the given portlet/soffit/webcomponent in uPortal to access these. Will return a 403 or
     * 404 otherwise.
     */
    @RequestMapping(value = "/preferences/definition/{fname}", method = RequestMethod.PUT)
    public ResponseEntity<String> putDefinitionPreferences(
            HttpServletRequest request,
            HttpServletResponse response,
            @ApiParam(value = "The portlet fname to store preferences for")
                    @PathVariable(value = "fname")
                    String fname,
            @ApiParam(
                            value =
                                    "the entity preferences to be stored in a single json object ex. "
                                            + "{\"key1\":\"value1\",\"key2\":\"value2\", ...}. "
                                            + "Values can be strings, booleans, numbers, null, or a jsonarray of values "
                                            + "(objects can be stored as a value as serialized strings). "
                                            + "This will overwrite any currently saved preferences for the portlet"
                                            + " with the same key as the pairs put in.")
                    @RequestBody
                    String body) {

        HttpSession session = request.getSession(false);
        if (session == null) {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }
        final IPerson person = personManager.getPerson(request);
        if (person.isGuest()) {
            return new ResponseEntity<>(
                    "ERROR: guests may not use this action.", HttpStatus.FORBIDDEN);
        } else if (!person.getSecurityContext().isAuthenticated()) {
            return new ResponseEntity<>(
                    "ERROR: must be logged in to use this action.", HttpStatus.UNAUTHORIZED);
        }

        try {
            JsonNode map = mapper.readTree(body);

            IPortletDefinition portletDefinition = portletDao.getPortletDefinitionByFname(fname);
            if (portletDefinition == null) {
                return new ResponseEntity<>("ERROR: Portlet not found", HttpStatus.NOT_FOUND);
            }
            IPortletEntity entity =
                    portletEntityRegistry.getOrCreateDefaultPortletEntity(
                            request, portletDefinition.getPortletDefinitionId());

            if (!canConfigure(person, entity.getPortletDefinitionId())) {
                return new ResponseEntity<>(
                        "ERROR: user is not authorized to perform this action",
                        HttpStatus.FORBIDDEN);
            }

            PortletPreferences preferences =
                    portletPreferencesFactory.createAPIPortletPreferences(
                            request, entity, false, DEFINITION_MODE);

            boolean stored = storePreferences(preferences, map);
            if (stored) {
                return ResponseEntity.ok(null);
            } else {
                return ResponseEntity.badRequest()
                        .body("ERROR: provided portlet preferences resolved to null");
            }
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("No portlet definition found for id '")) {
                String error = "ERROR: User not authorized to access portlet '" + fname + "'.";
                log.info(error);
                return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
            }

            log.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (IOException e) {

            log.error("threw error trying to parse preferences", e);
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {

            log.error("threw error trying to store preferences", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Set uPortal entity preferences for portlet/soffit/webcomponent. This method provides a REST
     * interface for uPortal for obtaining the entity preferences for the
     * portlet/soffit/webcomponent
     *
     * <p>The path for this method is /preferences/fname. The fname is a string representing the
     * fname of the portlet/soffit/webcomponent to set entity preferences for. These are the user
     * preferences for the given portlet/soffit/webcomponent. This will set the key:value(s) pairs
     * provided into the entity preferences overwriting any current entity preferences with the same
     * key, adding a new key:value(s) pair if the key does not exits.
     */
    @RequestMapping(value = "/preferences/{fname}", method = RequestMethod.PUT)
    public ResponseEntity<String> putEntityPreferences(
            HttpServletRequest request,
            HttpServletResponse response,
            @ApiParam(value = "The portlet fname to store preferences for")
                    @PathVariable(value = "fname")
                    String fname,
            @ApiParam(
                            value =
                                    "the entity preferences to be stored in a single json object ex. "
                                            + "{\"key1\":\"value1\",\"key2\":\"value2\", ...}. "
                                            + "Values can be strings, booleans, numbers, null, or a jsonarray of values "
                                            + "(objects can be stored as a value as serialized strings). "
                                            + "This will overwrite any currently saved preferences for the portlet"
                                            + " with the same key as the key passed in.")
                    @RequestBody
                    String body) {
        try {
            JsonNode map = mapper.readTree(body);

            IPortletDefinition portletDefinition = portletDao.getPortletDefinitionByFname(fname);
            if (portletDefinition == null) {
                return new ResponseEntity<>("ERROR: Portlet not found", HttpStatus.NOT_FOUND);
            }
            IPortletEntity entity =
                    portletEntityRegistry.getOrCreateDefaultPortletEntity(
                            request, portletDefinition.getPortletDefinitionId());

            PortletPreferences preferences =
                    portletPreferencesFactory.createAPIPortletPreferences(
                            request, entity, false, ENTITY_MODE);

            boolean stored = storePreferences(preferences, map);
            if (stored) {
                return ResponseEntity.ok().body(null);
            } else {
                return ResponseEntity.badRequest()
                        .body("ERROR: provided portlet preferences resolved to null");
            }
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("No portlet definition found for id '")) {
                String error = "ERROR: User not authorized to access portlet '" + fname + "'.";
                log.info(error);
                return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
            }

            log.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (IOException e) {
            log.error("threw error trying to parse user preferences", e);
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (PreferenceException e) {
            log.info(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ReadOnlyException e) {
            log.info(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {

            log.error("threw error trying to store preferences", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // process the incoming map to ensure validity, then store
    private boolean storePreferences(PortletPreferences preferences, JsonNode map)
            throws Exception {
        if (preferences == null) {
            return false;
        }

        try {
            Iterator<Entry<String, JsonNode>> nodes = map.fields();
            Map<String, String[]> tempMap = new HashMap<>();
            while (nodes.hasNext()) {
                Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) nodes.next();
                String key = entry.getKey();
                JsonNode node = entry.getValue();
                if (node.isValueNode()) {
                    String[] temp = {node.asText()};
                    tempMap.put(key, temp);
                } else if (node.isArray()) {
                    List<String> array = new ArrayList<String>();
                    for (JsonNode innerNode : node) {
                        if (innerNode.isValueNode()) {
                            array.add(innerNode.asText());
                        } else {
                            throw new PreferenceException(
                                    "ERROR: preference arrays must only contain strings, numbers, booleans, or null");
                        }
                    }
                    tempMap.put(key, array.toArray(new String[array.size()]));
                } else {
                    throw new PreferenceException(
                            "ERROR: preferences must be strings, numbers, booleans, null, or arrays of strings,"
                                    + " numbers, booleans, or nulls");
                }
            }
            if (tempMap.size() == 0) {
                throw new PreferenceException(
                        "ERROR: invalid json. json must be in key:value pairs.");
            }
            for (Map.Entry<String, String[]> entry : tempMap.entrySet()) {
                preferences.setValues(entry.getKey(), entry.getValue());
            }
            preferences.store();
            return true;

        } catch (Exception e) {
            throw e;
        }
    }

    /** Thrown when supplied preferences were unable to be parsed. */
    private class PreferenceException extends Exception {
        public PreferenceException(String message) {
            super(message);
        }
    }
}
