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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apereo.portal.soffit.Headers;
import org.apereo.portal.soffit.model.v1_0.Bearer;
import org.apereo.portal.soffit.model.v1_0.Definition;
import org.apereo.portal.soffit.model.v1_0.PortalRequest;
import org.apereo.portal.soffit.model.v1_0.PortalRequest.Attributes;
import org.apereo.portal.soffit.model.v1_0.Preferences;
import org.apereo.portal.soffit.service.BearerService;
import org.apereo.portal.soffit.service.DefinitionService;
import org.apereo.portal.soffit.service.PortalRequestService;
import org.apereo.portal.soffit.service.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * <code>Controller</code> bean for remote soffits. This class is provided as a convenience.
 *
 * @since 5.0
 */
@Controller
@RequestMapping("/soffit")
public class SoffitRendererController {

    /**
     * The default value for the <code>Cache-Control</code> header is "no-store," which indicates
     * the response should never be cached. Currently, this header value will be sent if the Soffit
     * does not specify a value for <strong>both</strong> scope or max-age.
     */
    public static final String CACHE_CONTROL_NOSTORE = "no-store";

    /**
     * Indicates the response may be cached with validation caching based on Last-Modified or ETag.
     * These features are not currently implemented.
     */
    public static final String CACHE_CONTROL_NOCACHE = "no-cache";

    /** Prefix for all custom properties. */
    public static final String PROPERTY_PREFIX = "soffit.";

    /** Used to create a property key specific to the soffit for cache scope. */
    public static final String CACHE_SCOPE_PROPERTY_FORMAT = PROPERTY_PREFIX + "%s.cache.scope";

    /** Used to create a property key specific to the soffit for cache max-age. */
    public static final String CACHE_MAXAGE_PROPERTY_FORMAT = PROPERTY_PREFIX + "%s.cache.max-age";

    private static final String PORTAL_REQUEST_MODEL_NAME = "portalRequest";
    private static final String BEARER_MODEL_NAME = "bearer";
    private static final String PREFERENCES_MODEL_NAME = "preferences";
    private static final String DEFINITION_MODEL_NAME = "definition";

    private static final String DEFAULT_MODE = "view";
    private static final String DEFAULT_WINDOW_STATE = "normal";

    @Autowired private Environment environment;

    @Autowired private PortalRequestService portalRequestService;

    @Autowired private BearerService bearerService;

    @Autowired private PreferencesService preferencesService;

    @Autowired private DefinitionService definitionService;

    @Autowired private ModelAttributeService modelAttributeService;

    @Value("${soffit.renderer.viewsLocation:/WEB-INF/soffit/}")
    private String viewsLocation;

    private final Map<ViewTuple, String> availableViews = new HashMap<>();

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping(value = "/{module}", method = RequestMethod.GET)
    public ModelAndView render(
            final HttpServletRequest req,
            final HttpServletResponse res,
            final @PathVariable String module) {

        logger.debug("Rendering for request URI '{}'", req.getRequestURI());

        // Soffit Object Model
        final PortalRequest portalRequest = getPortalRequest(req);
        final Bearer bearer = getBearer(req);
        final Preferences preferences = getPreferences(req);
        final Definition definition = getDefinition(req);

        // Select a view
        final String viewName = selectView(req, module, portalRequest);

        final Map<String, Object> model =
                modelAttributeService.gatherModelAttributes(
                        viewName, req, res, portalRequest, bearer, preferences, definition);
        model.put(PORTAL_REQUEST_MODEL_NAME, portalRequest);
        model.put(BEARER_MODEL_NAME, bearer);
        model.put(PREFERENCES_MODEL_NAME, preferences);
        model.put(DEFINITION_MODEL_NAME, definition);

        // Set up cache headers appropriately
        configureCacheHeaders(res, module);

        return new ModelAndView(viewName, model);
    }

    /*
     * Implementation
     */

    /**
     * Returns the {@link PortalRequest} or a {@link MissingModelElement} proxy if the header is not
     * present.
     */
    private PortalRequest getPortalRequest(final HttpServletRequest req) {
        final String portalRequestToken = req.getHeader(Headers.PORTAL_REQUEST.getName());
        return StringUtils.isNotBlank(portalRequestToken)
                ? portalRequestService.parsePortalRequest(portalRequestToken)
                : MissingModelElement.PORTAL_REQUEST;
    }

    /**
     * Returns the {@link Bearer} or a {@link MissingModelElement} proxy if the header is not
     * present.
     */
    private Bearer getBearer(final HttpServletRequest req) {
        final String authorizationHeader = req.getHeader(Headers.AUTHORIZATION.getName());
        if (StringUtils.isNotBlank(authorizationHeader)) {
            final String bearerToken =
                    authorizationHeader.substring(Headers.BEARER_TOKEN_PREFIX.length());
            return bearerService.parseBearerToken(bearerToken);
        }
        return MissingModelElement.BEARER;
    }

    /**
     * Returns the {@link Preferences} or a {@link MissingModelElement} proxy if the header is not
     * present.
     */
    private Preferences getPreferences(final HttpServletRequest req) {
        final String preferencesToken = req.getHeader(Headers.PREFERECES.getName());
        return StringUtils.isNotBlank(preferencesToken)
                ? preferencesService.parsePreferences(preferencesToken)
                : MissingModelElement.PREFERENCES;
    }

    /**
     * Returns the {@link Definition} or a {@link MissingModelElement} proxy if the header is not
     * present.
     */
    private Definition getDefinition(final HttpServletRequest req) {
        final String definitionToken = req.getHeader(Headers.DEFINITION.getName());
        return StringUtils.isNotBlank(definitionToken)
                ? definitionService.parseDefinition(definitionToken)
                : MissingModelElement.DEFINITION;
    }

    private String selectView(
            final HttpServletRequest req, final String module, final PortalRequest portalRequest) {

        final StringBuilder modulePathBuilder = new StringBuilder().append(viewsLocation);
        if (!viewsLocation.endsWith("/")) {
            // Final slash in the configs is optional
            modulePathBuilder.append("/");
        }
        modulePathBuilder.append(module).append("/");
        final String modulePath = modulePathBuilder.toString();

        logger.debug("Calculated modulePath of '{}'", modulePath);

        @SuppressWarnings("unchecked")
        final Set<String> moduleResources =
                req.getSession().getServletContext().getResourcePaths(modulePath);

        // Choose Mode & WindowState
        String modeLowercase, windowStateLowercase;
        if (!MissingModelElement.PORTAL_REQUEST.equals(portalRequest)) {
            // Need to make a selection based on 3 things:  module (above), mode, & windowState
            final Map<String, List<String>> attr = portalRequest.getAttributes();
            modeLowercase =
                    !attr.get(Attributes.MODE.getName()).isEmpty()
                            ? attr.get(Attributes.MODE.getName()).get(0).toLowerCase()
                            : DEFAULT_MODE;
            windowStateLowercase =
                    !attr.get(Attributes.WINDOW_STATE.getName()).isEmpty()
                            ? attr.get(Attributes.WINDOW_STATE.getName()).get(0).toLowerCase()
                            : DEFAULT_WINDOW_STATE;
        } else {
            // Use the defaults
            modeLowercase = DEFAULT_MODE;
            windowStateLowercase = DEFAULT_WINDOW_STATE;
        }

        final ViewTuple viewTuple = new ViewTuple(modulePath, modeLowercase, windowStateLowercase);
        String result = availableViews.get(viewTuple);
        if (result == null) {
            /*
             * This circumstance means that we haven't looked (yet);
             * check for a file named to match all 3.
             */
            final String pathBasedOnModeAndState =
                    getCompletePathforParts(modulePath, modeLowercase, windowStateLowercase);
            if (moduleResources.contains(pathBasedOnModeAndState)) {
                // We have a winner!
                availableViews.put(viewTuple, pathBasedOnModeAndState);
                result = pathBasedOnModeAndState;
            } else {
                // Widen the search (within this module) based on Mode only
                final String pathBasedOnModeOnly =
                        getCompletePathforParts(modulePath, modeLowercase);
                if (moduleResources.contains(pathBasedOnModeOnly)) {
                    // We still need to store the choice so we're not constantly looking
                    availableViews.put(viewTuple, pathBasedOnModeOnly);
                    result = pathBasedOnModeOnly;
                } else {
                    throw new IllegalStateException(
                            "Unable to select a view for Mode="
                                    + modeLowercase
                                    + " and WindowState="
                                    + windowStateLowercase);
                }
            }
        }

        logger.info(
                "Selected viewName='{}' for Mode='{}' and WindowState='{}'",
                result,
                modeLowercase,
                windowStateLowercase);

        return result;
    }

    private String getCompletePathforParts(final String... parts) {

        StringBuilder result = new StringBuilder();

        for (String part : parts) {
            result.append(part);
            if (!part.endsWith("/")) {
                // First part will be a directory
                result.append(".");
            }
        }

        result.append("jsp"); // TODO:  support more options

        logger.debug("Calculated path '{}' for parts={}", result, parts);

        return result.toString();
    }

    private void configureCacheHeaders(final HttpServletResponse res, final String module) {

        final String cacheScopeProperty = String.format(CACHE_SCOPE_PROPERTY_FORMAT, module);
        final String cacheScopeValue = environment.getProperty(cacheScopeProperty);
        logger.debug(
                "Selecting cacheScopeValue='{}' for property '{}'",
                cacheScopeValue,
                cacheScopeProperty);

        final String cacheMaxAgeProperty = String.format(CACHE_MAXAGE_PROPERTY_FORMAT, module);
        final Integer cacheMaxAgeValueSeconds =
                environment.getProperty(cacheMaxAgeProperty, Integer.class);
        logger.debug(
                "Selecting cacheMaxAgeValueSeconds='{}' for property '{}'",
                cacheMaxAgeValueSeconds,
                cacheMaxAgeProperty);

        // Both must be specified, else we just use the default...
        final String cacheControl =
                (StringUtils.isNotEmpty(cacheScopeValue) && cacheMaxAgeValueSeconds != null)
                        ? cacheScopeValue + ", max-age=" + cacheMaxAgeValueSeconds
                        : CACHE_CONTROL_NOSTORE;
        logger.debug("Setting cache-control='{}' for module '{}'", cacheControl, module);

        // TODO: support validation caching

        res.setHeader(Headers.CACHE_CONTROL.getName(), cacheControl);
    }

    /*
     * Nested Types
     */

    private static final class ViewTuple {

        private final String moduleName;
        private final String mode;
        private final String windowState;

        public ViewTuple(String moduleName, String mode, String windowState) {
            this.moduleName = moduleName;
            this.mode = mode;
            this.windowState = windowState;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((mode == null) ? 0 : mode.hashCode());
            result = prime * result + ((moduleName == null) ? 0 : moduleName.hashCode());
            result = prime * result + ((windowState == null) ? 0 : windowState.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            ViewTuple other = (ViewTuple) obj;
            if (mode == null) {
                if (other.mode != null) return false;
            } else if (!mode.equals(other.mode)) return false;
            if (moduleName == null) {
                if (other.moduleName != null) return false;
            } else if (!moduleName.equals(other.moduleName)) return false;
            if (windowState == null) {
                if (other.windowState != null) return false;
            } else if (!windowState.equals(other.windowState)) return false;
            return true;
        }

        @Override
        public String toString() {
            return "ViewTuple [moduleName="
                    + moduleName
                    + ", mode="
                    + mode
                    + ", windowState="
                    + windowState
                    + "]";
        }
    }
}
