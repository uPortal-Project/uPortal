package org.apereo.portal.rest.portletlist;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apereo.portal.dao.portletlist.IPortletList;
import org.apereo.portal.dao.portletlist.jpa.PortletList;
import org.apereo.portal.rest.utils.ErrorResponse;
import org.apereo.portal.security.RuntimeAuthorizationException;
import org.apereo.portal.services.portletlist.IPortletListService;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

/** PortletListRESTController provides a REST endpoint for interacting with portlet lists. */

@Controller
@Slf4j
public class PortletListRESTController {
    public static final String CONTEXT = "/portlet-list/";
    @Autowired
    private IPortletListService portletListService;

    @Autowired
    private IPersonManager personManager;

    @Autowired private ObjectMapper objectMapper;

    /** Provide a JSON view of all portlet lists. */
//    @PreAuthorize(
//        "hasPermission('ALL', 'java.lang.String', new org.apereo.portal.spring.security.evaluator.AuthorizableActivity('UP_PERMISSIONS', 'VIEW_PERMISSIONS'))")
    @RequestMapping(
        value = CONTEXT,
        method = GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String getPortletLists(HttpServletRequest request, HttpServletResponse response) {
        final IPerson person = personManager.getPerson(request);
        log.debug("Person.isGuest() = {}", person.isGuest());
        log.debug("Person.getUserName() = {}", person.getUserName());

        if(person.isGuest()) {
            log.warn("Guest is trying to access portlet-list API, which is not allowed.");
            return respondPortletListJson(response, null, "Not authorized", HttpServletResponse.SC_UNAUTHORIZED);
        }

        List<IPortletList> pLists = portletListService.getPortletLists(person);
        return respondPortletListJson(response, pLists, null, HttpServletResponse.SC_OK);
    }

    /** Provide a JSON view of all portlet lists. */
//    @PreAuthorize(
//        "hasPermission('ALL', 'java.lang.String', new org.apereo.portal.spring.security.evaluator.AuthorizableActivity('UP_PERMISSIONS', 'VIEW_PERMISSIONS'))")
    @RequestMapping(
        value = CONTEXT + "{portletListUuid}",
        method = GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String getPortletList(HttpServletRequest request, HttpServletResponse response, @PathVariable String portletListUuid) {
        final IPerson person = personManager.getPerson(request);
        log.debug("Person.isGuest() = {}", person.isGuest());
        log.debug("Person.getUserName() = {}", person.getUserName());

        if(person.isGuest()) {
            log.warn("Guest is trying to access portlet-list API, which is not allowed.");
            return respondPortletListJson(response, null, "Not authorized", HttpServletResponse.SC_UNAUTHORIZED);
        }

        IPortletList pList = portletListService.getPortletList(person, portletListUuid);
        if(pList == null)  {
            return respondPortletListJson(response, null, "Entity not found", HttpServletResponse.SC_NOT_FOUND);
        }
        return respondPortletListJson(response, pList, null, HttpServletResponse.SC_OK);
    }

    @RequestMapping(
        value = CONTEXT,
        method = POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String createPortletList(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestBody String json) {

        final IPerson person = personManager.getPerson(request);
        log.debug("createPortletList > Person.isGuest() = {}", person.isGuest());
        log.debug("createPortletList > Person.getUserName() = {}", person.getUserName());
        log.debug("createPortletList > JSON body is = {}", json);

        if(person.isGuest()) {
            log.warn("createPortletList > Guest is trying to access portlet-list API, which is not allowed.");
            return respondPortletListJson(response, null, "Not authorized", HttpServletResponse.SC_UNAUTHORIZED);

        }

//        /*
//         * This step is necessary;  the incoming URLs will sometimes have '+'
//         * characters for spaces, and the @PathVariable magic doesn't convert them.
//         */
//        String name;
//        try {
//            name = URLDecoder.decode(parentGroupName, "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            respondPortletListJson(response, null, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
//        }

        IPortletList input;

        try {
            input = objectMapper.readValue(json, PortletList.class);

            // TODO This should be a default instead of a reset...
            input.setUserId("" + person.getID());
        } catch (Exception e) {
            return respondPortletListJson(response, null, e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        try {
            final IPortletList created = portletListService.createPortletList(
                person, input);
            response.setHeader("Location", created.getId());
            return respondPortletListJson(response, null, null, HttpServletResponse.SC_CREATED);
        } catch (RuntimeAuthorizationException rae) {
            return respondPortletListJson(response, null, "not authorized", HttpServletResponse.SC_FORBIDDEN);
        } catch (IllegalArgumentException iae) {
            return respondPortletListJson(response, null, iae.getMessage(), HttpServletResponse.SC_CONFLICT);
        } catch (DataIntegrityViolationException dive) {
            log.warn("Attempted violation of data integrity when creating a portlet list {}", dive);
            return respondPortletListJson(response, null, "Data integrity issue - likely tried to use a non-unique name.", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            log.warn("Just hit an exception of type {}", e.getClass().getCanonicalName());
            return respondPortletListJson(response, null, e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // WIP
//    @RequestMapping(
//        value = CONTEXT + "{portletListUuid}",
//        method = PUT,
//        produces = MediaType.APPLICATION_JSON_VALUE)
//    public @ResponseBody String updatePortletList(
//        HttpServletRequest request,
//        HttpServletResponse response,
//        @RequestBody String json,
//        @PathVariable String portletListUuid) {
//
//        final IPerson person = personManager.getPerson(request);
//        log.debug("updatePortletList > Person.isGuest() = {}", person.isGuest());
//        log.debug("updatePortletList > Person.getUserName() = {}", person.getUserName());
//        log.debug("updatePortletList > JSON body is = {}", json);
//
//        if(person.isGuest()) {
//            log.warn("updatePortletList > Guest is trying to access portlet-list API, which is not allowed.");
//            return respondPortletListJson(response, null, "Not authorized", HttpServletResponse.SC_UNAUTHORIZED);
//
//        }
//
////        /*
////         * This step is necessary;  the incoming URLs will sometimes have '+'
////         * characters for spaces, and the @PathVariable magic doesn't convert them.
////         */
////        String name;
////        try {
////            name = URLDecoder.decode(parentGroupName, "UTF-8");
////        } catch (UnsupportedEncodingException e) {
////            respondPortletListJson(response, null, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
////        }
//
//        IPortletList input;
//
//        try {
//            input = objectMapper.readValue(json, PortletList.class);
//
//            // TODO This should be a default instead of a reset...
//            input.setUserId("" + person.getID());
//        } catch (Exception e) {
//            return respondPortletListJson(response, null, e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//        }
//
//        try {
//            final IPortletList created = portletListService.createPortletList(
//                person, input);
//            response.setHeader("Location", created.getId());
//            return respondPortletListJson(response, null, null, HttpServletResponse.SC_CREATED);
//        } catch (RuntimeAuthorizationException rae) {
//            return respondPortletListJson(response, null, "not authorized", HttpServletResponse.SC_FORBIDDEN);
//        } catch (IllegalArgumentException iae) {
//            return respondPortletListJson(response, null, iae.getMessage(), HttpServletResponse.SC_CONFLICT);
//        } catch (DataIntegrityViolationException dive) {
//            log.warn("Attempted violation of data integrity when creating a portlet list {}", dive);
//            return respondPortletListJson(response, null, "Data integrity issue - likely tried to use a non-unique name.", HttpServletResponse.SC_BAD_REQUEST);
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.warn("Just hit an exception of type {}", e.getClass().getCanonicalName());
//            return respondPortletListJson(response, null, e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//        }
//    }
//    Maybe later:
//    final EntityIdentifier ei = person.getEntityIdentifier();
//    final IAuthorizationPrincipal ap =
//        AuthorizationServiceFacade.instance().newPrincipal(ei.getKey(), ei.getType());
//        if (!ap.hasPermission("UP_SYSTEM", "IMPORT_ENTITY", target)) {
//        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//        return;
//    }

    private String respondPortletListJson(
        HttpServletResponse response,
        Object returnPayload,
        String errorMessage,
        int status) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Object payloadToReturn = returnPayload;
        int statusToReturn = status;

        if (returnPayload == null && errorMessage != null) {
            payloadToReturn = new ErrorResponse(errorMessage);
        }

        log.debug("returnPayload is null = {}, errorMessage is null = {}, final return payload is null = {}",
            (returnPayload == null),
            (errorMessage == null),
            (payloadToReturn == null));

        try {
            response.setStatus(statusToReturn);
            // If there is no payload, and no error, return a null body in the response
            final String payloadAsString = ((returnPayload == null) && (errorMessage == null)) ? null : objectMapper.writeValueAsString(payloadToReturn);
            log.debug("Prepared JSON Response - response code [{}], object type [{}], JSON as string: {}",
                statusToReturn,
                (payloadToReturn == null) ? "NULL" : payloadToReturn.getClass().getCanonicalName(),
                payloadAsString);
            return payloadAsString;
        } catch (Exception e) {
            log.error("Unable to write out payload object as JSON. Returning a 500.", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }
    }
}
