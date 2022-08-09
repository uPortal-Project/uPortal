package org.apereo.portal.rest.portletlist;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.apereo.portal.dao.portletlist.IPortletList;
import org.apereo.portal.dao.portletlist.IPortletListItem;
import org.apereo.portal.dao.portletlist.jpa.PortletList;
import org.apereo.portal.dao.portletlist.jpa.PortletListItem;
import org.apereo.portal.dao.portletlist.jpa.PortletListItemPK;
import org.apereo.portal.rest.utils.ErrorResponse;
import org.apereo.portal.security.RuntimeAuthorizationException;
import org.apereo.portal.services.portletlist.IPortletListService;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.springframework.web.bind.annotation.RequestMethod.*;

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

    /**
     * Provide a JSON view of all portlet lists.
     *
     * If an administrator makes this call, ALL portlet lists will be returned.
     * Otherwise, only the portlet lists that the requester owns will be returned.
     */
    @RequestMapping(
        value = CONTEXT,
        method = GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String getPortletLists(HttpServletRequest request, HttpServletResponse response) {
        final IPerson person = personManager.getPerson(request);
        debugPerson("getAllPortletLists", person);

        if(person.isGuest()) {
            log.warn("Guest is trying to access portlet-list API, which is not allowed.");
            return prepareResponse(response, null, "Not authorized", HttpServletResponse.SC_UNAUTHORIZED);
        }

        List<IPortletList> pLists = portletListService.isPortletListAdmin(person) ?
            portletListService.getPortletLists() : portletListService.getPortletLists(person);
        return prepareResponse(response, pLists, null, HttpServletResponse.SC_OK);
    }

    /**
     * Provide a JSON view of a given portlet list.
     *
     * If an administrator makes the request, the portlet list will be returned, regardless of ownership.
     * If anyone else makes the request, the portlet list will only be returned if the requester is the owner.
     */
    @RequestMapping(
        value = CONTEXT + "{portletListUuid}",
        method = GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String getPortletList(HttpServletRequest request, HttpServletResponse response, @PathVariable String portletListUuid) {
        final IPerson person = personManager.getPerson(request);
        debugPerson("getSpecificPortletList", person);

        if(person.isGuest()) {
            log.warn("Guest is trying to access portlet-list API, which is not allowed.");
            return prepareResponse(response, null, "Not authorized", HttpServletResponse.SC_UNAUTHORIZED);
        }

        IPortletList pList = portletListService.getPortletList(portletListUuid);

        if(pList == null) {
            log.warn("User [{}] tried to access portlet-list [{}], but list was not found.", person.getUserName(), portletListUuid);
            return prepareResponse(response, null, "Entity not found", HttpServletResponse.SC_NOT_FOUND);
        } else if(!portletListService.isPortletListAdmin(person) && !pList.getUserId().equals("" + person.getID())) {
            // Not an admin, and not the owner
            log.warn("Non-admin user [{}][{}] tried to access portlet-list [{}] with owner [{}], but was blocked since they aren't the owner.", person.getID(), person.getUserName(), portletListUuid, pList.getUserId());
            return prepareResponse(response, null, "Entity not found", HttpServletResponse.SC_NOT_FOUND);
        }

        return prepareResponse(response, pList, null, HttpServletResponse.SC_OK);
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
        debugPerson("createPortletList", person);
        log.debug("createPortletList > JSON body is = {}", json);

        if(person.isGuest()) {
            log.warn("createPortletList > Guest is trying to access portlet-list API, which is not allowed.");
            return prepareResponse(response, null, "Not authorized", HttpServletResponse.SC_UNAUTHORIZED);
        }

        PortletList input;

        try {
            input = objectMapper.readValue(json, PortletList.class);
            if(!portletListService.isPortletListAdmin(person)) {
                // Default - non-admins can only create lists for themselves.
                input.setUserId("" + person.getID());
            } else if (StringUtils.isEmpty(input.getUserId())) {
                // Default - admins don't have to specify a user name
                input.setUserId("" + person.getID());
            }
        } catch (Exception e) {
            log.warn("User [{}][{}] tried to create a portlet-list with bad json - {}", person.getID(), person.getUserName(), e.getMessage());
            return prepareResponse(response, null, "Unparsable portlet-list JSON", HttpServletResponse.SC_BAD_REQUEST);
        }

        try {
            // Sets the parent-child relationships
            input.overrideItems(input.getItems());
            final IPortletList created = portletListService.createPortletList(
                person, input);
            response.setHeader("Location", created.getId());
            return prepareResponse(response, null, null, HttpServletResponse.SC_CREATED);
        } catch (RuntimeAuthorizationException rae) {
            log.warn("RuntimeAuthorizationException thrown - {}", rae.getMessage(), rae);
            return prepareResponse(response, null, "not authorized", HttpServletResponse.SC_FORBIDDEN);
        } catch (IllegalArgumentException iae) {
            log.warn("IllegalArgumentException thrown - {}", iae.getMessage(), iae);
            return prepareResponse(response, null, iae.getMessage(), HttpServletResponse.SC_CONFLICT);
        } catch (DataIntegrityViolationException dive) {
            log.warn("Attempted violation of data integrity when creating a portlet list {}", dive.getMessage(), dive);
            return prepareResponse(response, null, "Data integrity issue - such as specifying a non-unique name.", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            log.warn("Just hit an exception of type {}", e.getClass().getCanonicalName(), e);
            return prepareResponse(response, null, "Something unexpected occurred. Please check with your System Administrator", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(
        value = CONTEXT + "{portletListUuid}",
        method = PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String updatePortletList(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestBody String json,
        @PathVariable String portletListUuid) {

        final IPerson person = personManager.getPerson(request);
        debugPerson("updatePortletList", person);
        log.debug("updatePortletList > JSON body is = {}", json);

        if(person.isGuest()) {
            log.warn("updatePortletList > Guest is trying to access portlet-list API, which is not allowed.");
            return prepareResponse(response, null, "Not authorized", HttpServletResponse.SC_UNAUTHORIZED);
        }

        IPortletList input;
        try {
            input = objectMapper.readValue(json, PortletList.class);
        } catch (Exception e) {
            log.warn("User [{}][{}] tried to create a portlet-list with bad json - {}", person.getID(), person.getUserName(), e.getMessage());
            return prepareResponse(response, null, "Unparsable portlet-list JSON", HttpServletResponse.SC_BAD_REQUEST);
        }

//        // Overlay changes onto a known entity, and then persist that entity.
//        IPortletList toUpdate = portletListService.getPortletList(portletListUuid);
//
//        if(toUpdate == null) {
//            return prepareResponse(response, null, "Unknown portlet-list", HttpServletResponse.SC_NOT_FOUND);
//        }
//
//        if(portletListService.isPortletListAdmin(person)) {
//            // If admin, allow admin-level changes
//            if(!StringUtils.isEmpty(input.getUserId())) {
//                toUpdate.setUserId(input.getUserId());
//            }
//
//            if(!StringUtils.isEmpty(input.getName())) {
//                toUpdate.setName(input.getName());
//            }
//        } else if (toUpdate.getUserId().equals("" + person.getID())) {
//            // If owner of portlet-list, allow only owner-level changes
//
//            if(!StringUtils.isEmpty(input.getUserId())) {
//                log.warn("updatePortletList - non-admin user [{}][{}] tried to update portlet-list [{}][{}] with a new owner [{}], which is not allowed.", person.getID(), person.getUserName(), toUpdate.getId(), toUpdate.getName(), input.getUserId());
//                return prepareResponse(response, null, "Non-admin user cannot change portlet-list owner", HttpServletResponse.SC_BAD_REQUEST);
//            }
//        } else {
//            // Otherwise, disallow changes
//            log.warn("updatePortletList - user [{}][{}] tried to update portlet-list [{}][{}], but was not the owner.", person.getID(), person.getUserName(), toUpdate.getId(), toUpdate.getName());
//            return prepareResponse(response, null, "Unknown portlet-list", HttpServletResponse.SC_UNAUTHORIZED);
//        }
//
//        // Either an owner or an admin. allow general-level changes:
//        if(!StringUtils.isEmpty(input.getName())) {
//            toUpdate.setName(input.getName());
//        }
//
//        if(input.getItems() != null) {
//            toUpdate.overrideItems(input.getItems());
//        }

        try {
            final IPortletList updated = portletListService.updatePortletList(
                person, input, portletListUuid);
            if(updated == null) {
                log.warn("update returned null for portlet-list uuid [{}]. Failing request.", portletListUuid);
                return prepareResponse(response, null, "Unable to update portlet list.", HttpServletResponse.SC_BAD_REQUEST);
            } else {
                return prepareResponse(response, null, null, HttpServletResponse.SC_OK);
            }
        } catch (RuntimeAuthorizationException rae) {
            log.warn("RuntimeAuthorizationException thrown - {}", rae.getMessage(), rae);
            return prepareResponse(response, null, "not authorized", HttpServletResponse.SC_FORBIDDEN);
        } catch (IllegalArgumentException iae) {
            log.warn("IllegalArgumentException thrown - {}", iae.getMessage(), iae);
            return prepareResponse(response, null, iae.getMessage(), HttpServletResponse.SC_CONFLICT);
        } catch (DataIntegrityViolationException dive) {
            log.warn("Attempted violation of data integrity when updating a portlet list {}", dive.getMessage(), dive);
            return prepareResponse(response, null, "Data integrity issue - such as specifying a non-unique name.", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            log.warn("Just hit an exception of type {}", e.getClass().getCanonicalName(), e);
            return prepareResponse(response, null, "Something unexpected occurred. Please check with your System Administrator", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private String prepareResponse(
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

    private void debugPerson(String flow, IPerson person) {
        log.debug("{} > Current user: username={}, isGuest={}, isAdmin={}",
            flow,
            person.getUserName(),
            person.isGuest(),
            portletListService.isPortletListAdmin(person));
    }
}
