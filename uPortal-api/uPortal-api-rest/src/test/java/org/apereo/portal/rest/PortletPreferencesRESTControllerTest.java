package org.apereo.portal.rest;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;
import javax.portlet.WindowState;
import org.apereo.portal.portlet.container.services.PortletPreferencesFactory;
import org.apereo.portal.portlet.dao.IPortletDefinitionDao;
import org.apereo.portal.portlet.dao.jpa.PortletDefinitionImpl;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletDefinitionId;
import org.apereo.portal.portlet.om.IPortletEntity;
import org.apereo.portal.portlet.om.IPortletEntityId;
import org.apereo.portal.portlet.registry.IPortletEntityRegistry;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

public class PortletPreferencesRESTControllerTest {

    @InjectMocks private PortletPreferencesRESTController portletPreferencesRESTController;

    @Mock private IPersonManager personManagerMock;

    @Mock private IPortletDefinitionDao portletDaoMock;

    @Mock private IPortletEntityRegistry portletEntityRegistryMock;

    @Mock private PortletPreferencesFactory portletPreferencesFactoryMock;

    private MockHttpServletRequest requestMock;

    private MockHttpServletResponse responseMock;

    private static final String NON_EXISTANT_NAME = "non-existent-portlet";
    private static final String NOT_AUTHORIZED_NAME = "not-authorized-portlet";
    private static final String VALID_NAME = "bob-portlet";
    private static final String COMPOSITE_METHOD = "composite";
    private static final String SINGLE_ONLY_METHOD = "singleonly";
    private static final String MISSING_METHOD = "";
    private static final String UNKNOWN_METHOD = "unknown";

    private IPortletDefinition validPortletDefinitionMock = mock(PortletDefinitionImpl.class);
    private IPortletDefinitionId validPortletDefIDMock = mock(IPortletDefinitionId.class);
    private IPortletEntity validEntityMock = mock(IPortletEntity.class);
    private ObjectMapper mapper = new ObjectMapper();
    private IPerson validPersonMock = mock(IPerson.class, RETURNS_DEEP_STUBS);

    @Before
    public void setUp() throws Exception {

        portletPreferencesRESTController = new PortletPreferencesRESTController();
        responseMock = new MockHttpServletResponse();
        requestMock = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("session", "true");
        requestMock.setSession(session);
        MockitoAnnotations.openMocks(this);

        // non-existant mocks
        Mockito.when(portletDaoMock.getPortletDefinitionByFname(NON_EXISTANT_NAME))
                .thenReturn(null);

        // not-authorized to subscribe mocks
        IPortletDefinitionId not_authorized_portletDefIDMock = mock(IPortletDefinitionId.class);
        IPortletDefinition not_authorized_portletDefinitionMock = mock(PortletDefinitionImpl.class);
        Mockito.when(portletDaoMock.getPortletDefinitionByFname(NOT_AUTHORIZED_NAME))
                .thenReturn(not_authorized_portletDefinitionMock);
        Mockito.when(not_authorized_portletDefinitionMock.getPortletDefinitionId())
                .thenReturn(not_authorized_portletDefIDMock);
        Mockito.when(
                        portletEntityRegistryMock.getOrCreateDefaultPortletEntity(
                                requestMock, not_authorized_portletDefIDMock))
                .thenThrow(
                        new IllegalArgumentException("No portlet definition found for id '200'."));

        // authorized to subscribe common methods
        Mockito.when(portletDaoMock.getPortletDefinitionByFname(VALID_NAME))
                .thenReturn(validPortletDefinitionMock);
        Mockito.when(validPortletDefinitionMock.getPortletDefinitionId())
                .thenReturn(validPortletDefIDMock);
        Mockito.when(
                        portletEntityRegistryMock.getOrCreateDefaultPortletEntity(
                                requestMock, validPortletDefIDMock))
                .thenReturn(validEntityMock);

        Mockito.when(personManagerMock.getPerson(requestMock)).thenReturn(validPersonMock);
        Mockito.when(validPersonMock.isGuest()).thenReturn(false);
        Mockito.when(validPersonMock.getSecurityContext().isAuthenticated()).thenReturn(true);
    }

    @Test // test that it returns the correct json when the fname is present in the database
    public void getEntity() {

        String valid_layoutid = "bobLayoutNode";
        String valid_entityid = "bobEntityID";
        int userid = 2;

        // test specific mocks
        IPortletEntityId entityIDMock = mock(IPortletEntityId.class);
        Map<Long, WindowState> windowStateMock = new HashMap<>();
        windowStateMock.put(new Long(90), new WindowState("bob-state"));
        // test specific mock methods
        Mockito.when(validPortletDefinitionMock.getFName()).thenReturn(VALID_NAME);
        Mockito.when(validEntityMock.getLayoutNodeId()).thenReturn(valid_layoutid);
        Mockito.when(validEntityMock.getUserId()).thenReturn(userid);
        Mockito.when(validEntityMock.getPortletEntityId()).thenReturn(entityIDMock);
        Mockito.when(entityIDMock.toString()).thenReturn(valid_entityid);
        Mockito.when(validEntityMock.getWindowStates()).thenReturn(windowStateMock);

        // test specific json result
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{\"" + VALID_NAME + "\": {");
        stringBuilder.append(" \"layoutNodeID\": \"" + valid_layoutid + "\",");
        stringBuilder.append(" \"userID\": \"" + userid + "\",");
        stringBuilder.append(" \"entityID\": \"" + valid_entityid + "\",");
        stringBuilder.append(" \"windowStates\": [");
        boolean hasComma = false;
        for (Map.Entry<Long, WindowState> entry : windowStateMock.entrySet()) {
            stringBuilder.append(" { \"stylesheetID\": \"");
            stringBuilder.append(entry.getKey());
            stringBuilder.append("\", \"windowState\": \"");
            stringBuilder.append(entry.getValue().toString());
            stringBuilder.append("\"},");
            hasComma = true;
        }
        if (hasComma) {
            stringBuilder.setLength(stringBuilder.length() - 1);
        }
        stringBuilder.append(" ] } }");

        ResponseEntity<String> response =
                portletPreferencesRESTController.getEntity(requestMock, responseMock, VALID_NAME);
        Assert.assertEquals(200, response.getStatusCodeValue());

        Assert.assertEquals(stringBuilder.toString(), (String) response.getBody());
    }

    @Test // test that it returns a 400 when the portlet fname does not exits
    public void getEntityNonExistent() {
        ResponseEntity<String> response =
                portletPreferencesRESTController.getEntity(
                        requestMock, responseMock, NON_EXISTANT_NAME);
        Assert.assertEquals(404, response.getStatusCodeValue());
        Assert.assertEquals("ERROR: Portlet not found", (String) response.getBody());
    }

    @Test
    public void getEntityNotAuthorized() {

        ResponseEntity<String> response =
                portletPreferencesRESTController.getEntity(
                        requestMock, responseMock, NOT_AUTHORIZED_NAME);
        Assert.assertEquals(403, response.getStatusCodeValue());
        Assert.assertEquals(
                "ERROR: User not authorized to access portlet '" + NOT_AUTHORIZED_NAME + "'.",
                (String) response.getBody());
    }

    @Test
    public void getCompositePrefsNonExistent() {

        ResponseEntity<String> response =
                portletPreferencesRESTController.getPreferences(
                        COMPOSITE_METHOD, NON_EXISTANT_NAME, requestMock, responseMock);
        Assert.assertEquals(404, response.getStatusCodeValue());
        Assert.assertEquals("ERROR: Portlet not found", (String) response.getBody());
    }

    @Test
    public void getCompositePrefsNotAuthorized() {

        ResponseEntity<String> response =
                portletPreferencesRESTController.getPreferences(
                        COMPOSITE_METHOD, NOT_AUTHORIZED_NAME, requestMock, responseMock);
        Assert.assertEquals(403, response.getStatusCodeValue());
        Assert.assertEquals(
                "ERROR: User not authorized to access portlet '" + NOT_AUTHORIZED_NAME + "'.",
                (String) response.getBody());
    }

    @Test
    public void getCompositePrefs() {

        PortletPreferences prefs = mock(PortletPreferences.class);
        Map<String, String[]> map = new HashMap<>();
        String[] pref1 = {"bob1", "bob2", "bob3"};
        String[] pref2 = {"true"};
        map.put("pref1", pref1);
        map.put("pref2", pref2);
        JsonNode jsonmap = mapper.valueToTree(map);

        Mockito.when(
                        portletPreferencesFactoryMock.createAPIPortletPreferences(
                                requestMock, validEntityMock, false, false))
                .thenReturn(prefs);
        Mockito.when(prefs.getMap()).thenReturn(map);

        ResponseEntity<String> response =
                portletPreferencesRESTController.getPreferences(
                        COMPOSITE_METHOD, VALID_NAME, requestMock, responseMock);
        Assert.assertEquals(200, response.getStatusCodeValue());
        JsonNode body = null;
        try {
            body = mapper.readTree((String) response.getBody());
        } catch (IOException e) {
            fail("failed to parse the json from the api: " + e.getMessage());
        }
        Assert.assertTrue(
                "Json is not equal, \nExpected: "
                        + jsonmap.toString()
                        + "\nActual: "
                        + body.toString(),
                jsonmap.equals(body));
    }

    @Test
    public void getEntityOnlyPrefsNonExistant() {
        ResponseEntity<String> response =
                portletPreferencesRESTController.getPreferences(
                        SINGLE_ONLY_METHOD, NON_EXISTANT_NAME, requestMock, responseMock);
        Assert.assertEquals(404, response.getStatusCodeValue());
        Assert.assertEquals("ERROR: Portlet not found", (String) response.getBody());
    }

    @Test
    public void getEntityOnlyPrefsNotAuthorized() {

        ResponseEntity<String> response =
                portletPreferencesRESTController.getPreferences(
                        SINGLE_ONLY_METHOD, NOT_AUTHORIZED_NAME, requestMock, responseMock);
        Assert.assertEquals(403, response.getStatusCodeValue());
        Assert.assertEquals(
                "ERROR: User not authorized to access portlet '" + NOT_AUTHORIZED_NAME + "'.",
                (String) response.getBody());
    }

    @Test
    public void getEntityOnlyPrefs() {
        PortletPreferences defPrefs = mock(PortletPreferences.class);
        PortletPreferences entPrefs = mock(PortletPreferences.class);

        Map<String, String[]> defMap = new HashMap<>();
        String[] pref1 = {"bob1", "bob2", "bob3"};
        String[] pref2 = {"true"};
        defMap.put("pref1", pref1);
        defMap.put("pref2", pref2);

        Map<String, String[]> entMap = new HashMap<>();
        entMap.put("pref1", pref1);
        entMap.put("pref2", pref2);
        String[] newPref2 = {"false"};
        entMap.put("pref2", newPref2);

        Map<String, String[]> entOnlyMap = new HashMap<>();
        entOnlyMap.put("pref2", newPref2);

        JsonNode jsonmap = mapper.valueToTree(entOnlyMap);

        Mockito.when(
                        portletPreferencesFactoryMock.createAPIPortletPreferences(
                                requestMock, validEntityMock, false, true))
                .thenReturn(defPrefs);
        Mockito.when(defPrefs.getMap()).thenReturn(defMap);
        Mockito.when(
                        portletPreferencesFactoryMock.createAPIPortletPreferences(
                                requestMock, validEntityMock, false, false))
                .thenReturn(entPrefs);
        Mockito.when(entPrefs.getMap()).thenReturn(entMap);

        ResponseEntity<String> response =
                portletPreferencesRESTController.getPreferences(
                        SINGLE_ONLY_METHOD, VALID_NAME, requestMock, responseMock);
        Assert.assertEquals(200, response.getStatusCodeValue());
        JsonNode body = null;
        try {
            body = mapper.readTree((String) response.getBody());
        } catch (IOException e) {
            fail("failed to parse the json from the api: " + e.getMessage());
        }
        Assert.assertTrue(
                "Json is not equal, \nExpected: "
                        + jsonmap.toString()
                        + "\nActual: "
                        + body.toString(),
                jsonmap.equals(body));
    }

    @Test
    public void getDefinitionPrefsNoSession() {
        MockHttpServletRequest requestMock2 = new MockHttpServletRequest();
        requestMock2.setSession(null);

        ResponseEntity<String> response =
                portletPreferencesRESTController.getDefinitionPreferences(
                        COMPOSITE_METHOD, VALID_NAME, requestMock2, responseMock);
        Assert.assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    public void getDefinitionPrefsGuest() {
        IPerson person = mock(IPerson.class);
        MockHttpServletRequest requestMock2 = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("session", "true");
        requestMock2.setSession(session);
        Mockito.when(personManagerMock.getPerson(requestMock2)).thenReturn(person);
        Mockito.when(person.isGuest()).thenReturn(true);

        ResponseEntity<String> response =
                portletPreferencesRESTController.getDefinitionPreferences(
                        COMPOSITE_METHOD, VALID_NAME, requestMock2, responseMock);
        Assert.assertEquals(401, response.getStatusCodeValue());
        Assert.assertEquals("ERROR: guest cannot use this action.", (String) response.getBody());
    }

    @Test
    public void getDefinitionPrefsSessionNotAuthenticated() {
        IPerson person = mock(IPerson.class, RETURNS_DEEP_STUBS);
        MockHttpServletRequest requestMock2 = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("session", "false");
        requestMock2.setSession(session);
        Mockito.when(personManagerMock.getPerson(requestMock2)).thenReturn(person);
        Mockito.when(person.isGuest()).thenReturn(false);
        Mockito.when(person.getSecurityContext().isAuthenticated()).thenReturn(false);

        ResponseEntity<String> response =
                portletPreferencesRESTController.getDefinitionPreferences(
                        COMPOSITE_METHOD, VALID_NAME, requestMock2, responseMock);
        Assert.assertEquals(401, response.getStatusCodeValue());
        Assert.assertEquals(
                "ERROR: must be logged in to use this action.", (String) response.getBody());
    }

    @Test
    public void getDefinitionPrefsSessionNonExistant() {

        ResponseEntity<String> response =
                portletPreferencesRESTController.getDefinitionPreferences(
                        COMPOSITE_METHOD, NON_EXISTANT_NAME, requestMock, responseMock);
        Assert.assertEquals(404, response.getStatusCodeValue());
        Assert.assertEquals("ERROR: Portlet not found", (String) response.getBody());
    }

    @Test
    public void getDefinitionPrefsSessionNonConfig() {
        // can't programatically test this one without setting up a full stack since it calls the
        // static method:
        //      AuthorizationPrincipalHelper.principalFromUser(user)
        // to insure that only portal administrators or others with configuration authority can
        // configure.
        // *Could easily be implemented by using PowerMock
        // has been thouroughly tested through manual testing though
    }

    @Test
    public void getDefinitionPrefs() {
        // can't progamatically test this one without setting up a full stack since it calls the
        // static method:
        //      AuthorizationPrincipalHelper.principalFromUser(user)
        // to insure that only portal administrators or others with configuration authority can
        // configure.
        // *Could easily be implemented by using PowerMock
        // has been thouroughly tested through manual testing though
    }

    @Test
    public void putDefinitionPrefsNoSession() {
        String body = "{\"pref1\":\"value1\"}";
        MockHttpServletRequest requestMock2 = new MockHttpServletRequest();
        requestMock2.setSession(null);

        ResponseEntity<String> response =
                portletPreferencesRESTController.putDefinitionPreferences(
                        requestMock2, responseMock, VALID_NAME, body);
        Assert.assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    public void putDefinitionPrefsGuest() {
        String body = "{\"pref1\":\"value1\"}";
        IPerson person = mock(IPerson.class);
        MockHttpServletRequest requestMock2 = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("session", "true");
        requestMock2.setSession(session);
        Mockito.when(personManagerMock.getPerson(requestMock2)).thenReturn(person);
        Mockito.when(person.isGuest()).thenReturn(true);

        ResponseEntity<String> response =
                portletPreferencesRESTController.putDefinitionPreferences(
                        requestMock2, responseMock, VALID_NAME, body);
        Assert.assertEquals(403, response.getStatusCodeValue());
        Assert.assertEquals("ERROR: guests may not use this action.", (String) response.getBody());
    }

    @Test
    public void putDefinitionPrefsSessionNotAuthenticated() {
        String body = "{\"pref1\":\"value1\"}";
        IPerson person = mock(IPerson.class, RETURNS_DEEP_STUBS);
        MockHttpServletRequest requestMock2 = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("session", "false");
        requestMock2.setSession(session);
        Mockito.when(personManagerMock.getPerson(requestMock2)).thenReturn(person);
        Mockito.when(person.isGuest()).thenReturn(false);
        Mockito.when(person.getSecurityContext().isAuthenticated()).thenReturn(false);

        ResponseEntity<String> response =
                portletPreferencesRESTController.putDefinitionPreferences(
                        requestMock2, responseMock, VALID_NAME, body);
        Assert.assertEquals(401, response.getStatusCodeValue());
        Assert.assertEquals(
                "ERROR: must be logged in to use this action.", (String) response.getBody());
    }

    @Test
    public void putDefinitionPrefsSessionNonExistant() {
        String body = "{\"pref1\":\"value1\"}";
        ResponseEntity<String> response =
                portletPreferencesRESTController.putDefinitionPreferences(
                        requestMock, responseMock, NON_EXISTANT_NAME, body);
        Assert.assertEquals(404, response.getStatusCodeValue());
        Assert.assertEquals("ERROR: Portlet not found", (String) response.getBody());
    }

    @Test
    public void putDefinitionInvalidJson() throws Exception {
        String body = "{\"pref1\":\"bob\"";
        PortletPreferences prefs = mock(PortletPreferences.class);

        Mockito.when(
                        portletPreferencesFactoryMock.createAPIPortletPreferences(
                                requestMock, validEntityMock, false, true))
                .thenReturn(prefs);

        ResponseEntity<String> response =
                portletPreferencesRESTController.putDefinitionPreferences(
                        requestMock, responseMock, VALID_NAME, body);
        Assert.assertEquals(400, response.getStatusCodeValue());
        Mockito.verify(prefs, times(0)).setValues(anyString(), any());
        Mockito.verify(prefs, times(0)).store();
    }

    @Test
    public void putDefinitionPrefsSessionNonConfig() {
        // can't programatically test this one without setting up a full stack since it calls the
        // static method:
        //      AuthorizationPrincipalHelper.principalFromUser(user)
        // to insure that only portal administrators or others with configuration authority can
        // configure.
        // *Could easily be implemented by using PowerMock
        // has been thouroughly tested through manual testing though
    }

    @Test
    public void putDefinitionPrefs() {
        // can't programatically test this one without setting up a full stack since it calls the
        // static method:
        //      AuthorizationPrincipalHelper.principalFromUser(user)
        // to insure that only portal administrators or others with configuration authority can
        // configure.
        // *Could easily be implemented by using PowerMock
        // has been thouroughly tested through manual testing though
    }

    @Test
    public void putEntityPrefsNonExistant() {
        String body = "{\"pref1\":\"bob\"}";
        ResponseEntity<String> response =
                portletPreferencesRESTController.putEntityPreferences(
                        requestMock, responseMock, NON_EXISTANT_NAME, body);
        Assert.assertEquals(404, response.getStatusCodeValue());
        Assert.assertEquals("ERROR: Portlet not found", (String) response.getBody());
    }

    @Test
    public void putEntityPrefsNotAuthorized() {
        String body = "{\"pref1\":\"bob\"}";
        ResponseEntity<String> response =
                portletPreferencesRESTController.putEntityPreferences(
                        requestMock, responseMock, NOT_AUTHORIZED_NAME, body);
        Assert.assertEquals(403, response.getStatusCodeValue());
        Assert.assertEquals(
                "ERROR: User not authorized to access portlet '" + NOT_AUTHORIZED_NAME + "'.",
                (String) response.getBody());
    }

    @Test
    public void putEntityPrefsInvalidJson() throws Exception {
        String body = "{\"pref1\":\"bob\"";
        PortletPreferences prefs = mock(PortletPreferences.class);

        Mockito.when(
                        portletPreferencesFactoryMock.createAPIPortletPreferences(
                                requestMock, validEntityMock, false, false))
                .thenReturn(prefs);

        ResponseEntity<String> response =
                portletPreferencesRESTController.putEntityPreferences(
                        requestMock, responseMock, VALID_NAME, body);
        Assert.assertEquals(400, response.getStatusCodeValue());
        Mockito.verify(prefs, times(0)).setValues(anyString(), any());
        Mockito.verify(prefs, times(0)).store();
    }

    @Test
    public void putEntityPrefsObjectPref() throws Exception {
        String body = "{\"pref1\":{\"name\":\"bob\"}}";
        PortletPreferences prefs = mock(PortletPreferences.class);

        Mockito.when(
                        portletPreferencesFactoryMock.createAPIPortletPreferences(
                                requestMock, validEntityMock, false, false))
                .thenReturn(prefs);

        ResponseEntity<String> response =
                portletPreferencesRESTController.putEntityPreferences(
                        requestMock, responseMock, VALID_NAME, body);
        Assert.assertEquals(400, response.getStatusCodeValue());
        Assert.assertEquals(
                "ERROR: preferences must be strings, numbers, booleans, null, or arrays of strings, numbers, booleans, or nulls",
                (String) response.getBody());
        Mockito.verify(prefs, times(0)).setValues(anyString(), any());
        Mockito.verify(prefs, times(0)).store();
    }

    @Test
    public void putEntityPrefsMultipleArray() throws Exception {
        String body = "{\"pref1\":[\"bob\",[\"name\",\"bob\"]]}";
        PortletPreferences prefs = mock(PortletPreferences.class);

        Mockito.when(
                        portletPreferencesFactoryMock.createAPIPortletPreferences(
                                requestMock, validEntityMock, false, false))
                .thenReturn(prefs);

        ResponseEntity<String> response =
                portletPreferencesRESTController.putEntityPreferences(
                        requestMock, responseMock, VALID_NAME, body);
        Assert.assertEquals(400, response.getStatusCodeValue());
        Assert.assertEquals(
                "ERROR: preference arrays must only contain strings, numbers, booleans, or null",
                (String) response.getBody());
        Mockito.verify(prefs, times(0)).setValues(anyString(), any());
        Mockito.verify(prefs, times(0)).store();
    }

    @Test
    public void putEntityPrefsObjectArray() throws Exception {
        String body = "{\"pref1\":[{\"bob\":[\"name\",\"bob\"]}]}";
        PortletPreferences prefs = mock(PortletPreferences.class);

        Mockito.when(
                        portletPreferencesFactoryMock.createAPIPortletPreferences(
                                requestMock, validEntityMock, false, false))
                .thenReturn(prefs);

        ResponseEntity<String> response =
                portletPreferencesRESTController.putEntityPreferences(
                        requestMock, responseMock, VALID_NAME, body);
        Assert.assertEquals(400, response.getStatusCodeValue());
        Assert.assertEquals(
                "ERROR: preference arrays must only contain strings, numbers, booleans, or null",
                (String) response.getBody());
        Mockito.verify(prefs, times(0)).setValues(anyString(), any());
        Mockito.verify(prefs, times(0)).store();
    }

    @Test
    public void putEntityPrefsReadOnly() throws Exception {
        String body = "{\"pref1\":\"bob\"}";
        String[] values = {"bob"};
        PortletPreferences prefs = mock(PortletPreferences.class);

        Mockito.when(
                        portletPreferencesFactoryMock.createAPIPortletPreferences(
                                requestMock, validEntityMock, false, false))
                .thenReturn(prefs);

        Mockito.doThrow(new ReadOnlyException("Preference '" + "pref1" + "' is read only"))
                .when(prefs)
                .setValues("pref1", values);

        ResponseEntity<String> response =
                portletPreferencesRESTController.putEntityPreferences(
                        requestMock, responseMock, VALID_NAME, body);
        Assert.assertEquals(400, response.getStatusCodeValue());
        Assert.assertEquals("Preference 'pref1' is read only", (String) response.getBody());
        Mockito.verify(prefs, times(1)).setValues(anyString(), any());
        Mockito.verify(prefs, times(0)).store();
    }

    @Test // regresponseMocksion test. found it returned true even though it stored nothing. make
    // sure it
    // rejects non-key-value-pairs
    public void putEntityPrefsSingleValue() throws Exception {
        String body = "\"string\"";
        PortletPreferences prefs = mock(PortletPreferences.class);

        Mockito.when(
                        portletPreferencesFactoryMock.createAPIPortletPreferences(
                                requestMock, validEntityMock, false, false))
                .thenReturn(prefs);

        ResponseEntity<String> response =
                portletPreferencesRESTController.putEntityPreferences(
                        requestMock, responseMock, VALID_NAME, body);
        Assert.assertEquals(400, response.getStatusCodeValue());
        Assert.assertEquals(
                "ERROR: invalid json. json must be in key:value pairs.",
                (String) response.getBody());
        Mockito.verify(prefs, times(0)).setValues(anyString(), any());
        Mockito.verify(prefs, times(0)).store();
    }

    @Test
    public void putEntityPrefs() throws Exception {
        String body = "{\"pref1\":\"bob\"}";
        String[] values = {"bob"};
        PortletPreferences prefs = mock(PortletPreferences.class);

        Mockito.when(
                        portletPreferencesFactoryMock.createAPIPortletPreferences(
                                requestMock, validEntityMock, false, false))
                .thenReturn(prefs);

        ResponseEntity<String> response =
                portletPreferencesRESTController.putEntityPreferences(
                        requestMock, responseMock, VALID_NAME, body);
        Mockito.verify(prefs, times(1)).setValues("pref1", values);
        Mockito.verify(prefs, times(1)).setValues(anyString(), any());
        Mockito.verify(prefs, times(1)).store();
        Assert.assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void putEntityPrefsArray() throws Exception {
        String body = "{\"pref1\":[\"bob\",\"joe\"]}";
        String[] values = {"bob", "joe"};
        PortletPreferences prefs = mock(PortletPreferences.class);

        Mockito.when(
                        portletPreferencesFactoryMock.createAPIPortletPreferences(
                                requestMock, validEntityMock, false, false))
                .thenReturn(prefs);

        ResponseEntity<String> response =
                portletPreferencesRESTController.putEntityPreferences(
                        requestMock, responseMock, VALID_NAME, body);
        Mockito.verify(prefs, times(1)).setValues("pref1", values);
        Mockito.verify(prefs, times(1)).setValues(anyString(), any());
        Mockito.verify(prefs, times(1)).store();
        Assert.assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void putEntityPrefsAllTypes() throws Exception {
        String body = "{\"pref1\":[\"bob\",true,false,null,1,50.9]}";
        String[] values = {"bob", "true", "false", "null", "1", "50.9"};
        PortletPreferences prefs = mock(PortletPreferences.class);

        Mockito.when(
                        portletPreferencesFactoryMock.createAPIPortletPreferences(
                                requestMock, validEntityMock, false, false))
                .thenReturn(prefs);

        ResponseEntity<String> response =
                portletPreferencesRESTController.putEntityPreferences(
                        requestMock, responseMock, VALID_NAME, body);
        Mockito.verify(prefs, times(1)).setValues("pref1", values);
        Mockito.verify(prefs, times(1)).setValues(anyString(), any());
        Mockito.verify(prefs, times(1)).store();
        Assert.assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void putEntityPrefsMultiplePrefs() throws Exception {
        String body = "{\"pref1\":[\"bob\",\"joe\"],\"pref2\":true}";
        String[] values1 = {"bob", "joe"};
        String[] values2 = {"true"};
        PortletPreferences prefs = mock(PortletPreferences.class);

        Mockito.when(
                        portletPreferencesFactoryMock.createAPIPortletPreferences(
                                requestMock, validEntityMock, false, false))
                .thenReturn(prefs);

        ResponseEntity<String> response =
                portletPreferencesRESTController.putEntityPreferences(
                        requestMock, responseMock, VALID_NAME, body);
        Mockito.verify(prefs, times(1)).setValues("pref1", values1);
        Mockito.verify(prefs, times(1)).setValues("pref2", values2);
        Mockito.verify(prefs, times(2)).setValues(anyString(), any());
        Mockito.verify(prefs, times(1)).store();
        Assert.assertEquals(200, response.getStatusCodeValue());
    }
}
