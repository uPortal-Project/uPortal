/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apereo.portal.soffit.renderer;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
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
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * <code>Controller</code> bean for remote soffits.  This class is provided as a
 * convenience.
 *
 * @since 5.0
 * @author drewwills
 */
@Controller
@RequestMapping("/soffit")
public class SoffitRendererController {

    /**
     * The default value for the <code>Cache-Control</code> header is "no-store,"
     * which indicates the response should never be cached.  Currently, this
     * header value will be sent if the Soffit does not specify a value for
     * <strong>both</strong> scope or max-age.
     */
    public static final String CACHE_CONTROL_NOSTORE = "no-store";

    /**
     * Indicates the response may be cached with validation caching based on
     * Last-Modified or ETag.  These features are not currently implemented.
     */
    public static final String CACHE_CONTROL_NOCACHE = "no-cache";

    /**
     * Prefix for all custom properties.
     */
    public static final String PROPERTY_PREFIX = "soffit.";

    /**
     * Used to create a property key specific to the soffit for cache scope.
     */
    public static final String CACHE_SCOPE_PROPERTY_FORMAT = PROPERTY_PREFIX + "%s.cache.scope";

    /**
     * Used to create a property key specific to the soffit for cache max-age.
     */
    public static final String CACHE_MAXAGE_PROPERTY_FORMAT = PROPERTY_PREFIX + "%s.cache.max-age";

    private static final String PORTAL_REQUEST_MODEL_NAME = "portalRequest";
    private static final String BEARER_MODEL_NAME = "bearer";
    private static final String PREFERENCES_MODEL_NAME = "preferences";
    private static final String DEFINITION_MODEL_NAME = "definition";

    private static final String DEFAULT_MODE = "view";
    private static final String DEFAULT_WINDOW_STATE = "normal";

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Environment environment;

    @Autowired
    private PortalRequestService portalRequestService;

    @Autowired
    private BearerService bearerService;

    @Autowired
    private PreferencesService preferencesService;

    @Autowired
    private DefinitionService definitionService;

    @Value("${soffit.renderer.viewsLocation:/WEB-INF/soffit/}")
    private String viewsLocation;
    private final Map<ViewTuple,String> availableViews = new HashMap<>();

    private Map<AnnotatedElement,Object> modelAttributes;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {
        /*
         * Gather classes & methods that reference @SoffitMoldelAttribute
         */
        final Map<AnnotatedElement,Object> map = new HashMap<>();
        final String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String name : beanNames) {
            final Object bean = applicationContext.getBean(name);
            final Class clazz = AopUtils.isAopProxy(bean)
                    ? AopUtils.getTargetClass(bean)
                    : bean.getClass();
            if (clazz.isAnnotationPresent(SoffitModelAttribute.class)) {
                // The bean itself is the model attribute
                map.put(clazz, bean);
            } else {
                // Check the bean for annotated methods...
                for (Method m : clazz.getMethods()) {
                    if (m.isAnnotationPresent(SoffitModelAttribute.class)) {
                        map.put(m, bean);
                    }
                }
            }
        }
        logger.debug("Found {} beans and/or methods referencing @SoffitModelAttribute", map.size());
        modelAttributes = Collections.unmodifiableMap(map);
    }

    @RequestMapping(value="/{module}", method=RequestMethod.GET)
    public ModelAndView render(final HttpServletRequest req, final HttpServletResponse res, final @PathVariable String module) {

        logger.debug("Rendering for request URI '{}'", req.getRequestURI());

        // Soffit Object Model
        final PortalRequest portalRequest = getPortalRequest(req);
        final Bearer bearer = getBearer(req);
        final Preferences preferences = getPreferences(req);
        final Definition definition = getDefinition(req);

        // Select a view
        final String viewName = selectView(req, module, portalRequest);

        final Map<String,Object> model = gatherModelAttributes(viewName, req, res, portalRequest, bearer, preferences, definition);
        model.put(PORTAL_REQUEST_MODEL_NAME, portalRequest);
        model.put(BEARER_MODEL_NAME, bearer);
        model.put(PREFERENCES_MODEL_NAME, preferences);
        model.put(DEFINITION_MODEL_NAME, definition);

        // Set up cache headers appropriately
        configureCacheHeaders(res, module);

        return new ModelAndView(viewName.toString(), model);

    }

    /*
     * Implementation
     */

    private PortalRequest getPortalRequest(final HttpServletRequest req) {
        final String portalRequestToken = req.getHeader(Headers.PORTAL_REQUEST.getName());
        return portalRequestService.parsePortalRequest(portalRequestToken);
    }

    private Bearer getBearer(final HttpServletRequest req) {
        final String authorizationHeader = req.getHeader(Headers.AUTHORIZATION.getName());
        final String bearerToken = authorizationHeader.substring(Headers.BEARER_TOKEN_PREFIX.length());
        return bearerService.parseBearerToken(bearerToken);
    }

    private Preferences getPreferences(final HttpServletRequest req) {
        final String preferencesToken = req.getHeader(Headers.PREFERECES.getName());
        return preferencesService.parsePreferences(preferencesToken);
    }

    private Definition getDefinition(final HttpServletRequest req) {
        final String definitionToken = req.getHeader(Headers.DEFINITION.getName());
        return definitionService.parseDefinition(definitionToken);
    }

    private String selectView(final HttpServletRequest req, final String module, final PortalRequest portalRequest) {

        final StringBuilder modulePathBuilder = new StringBuilder().append(viewsLocation);
        if (!viewsLocation.endsWith("/")) {
            // Final slash in the configs is optional
            modulePathBuilder.append("/");
        }
        modulePathBuilder.append(module).append("/");
        final String modulePath = modulePathBuilder.toString();

        logger.debug("Calculated modulePath of '{}'", modulePath);

        @SuppressWarnings("unchecked")
        final Set<String> moduleResources = req.getSession().getServletContext().getResourcePaths(modulePath);

        // Need to make a selection based on 3 things:  module (above), mode, & windowState
        final Map<String,List<String>> requestAttributes = portalRequest.getAttributes();
        final String modeLowercase = !requestAttributes.get(Attributes.MODE.getName()).isEmpty()
                ? requestAttributes.get(Attributes.MODE.getName()).get(0).toLowerCase()
                : DEFAULT_MODE;
        final String windowStateLowercase = !requestAttributes.get(Attributes.WINDOW_STATE.getName()).isEmpty()
                ? requestAttributes.get(Attributes.WINDOW_STATE.getName()).get(0).toLowerCase()
                : DEFAULT_WINDOW_STATE;

        final ViewTuple viewTuple = new ViewTuple(modulePath, modeLowercase, windowStateLowercase);
        String rslt = availableViews.get(viewTuple);
        if (rslt == null) {
            /*
             * This circumstance means that we haven't looked (yet);
             * check for a file named to match all 3.
             */
            final String pathBasedOnModeAndState = getCompletePathforParts(modulePath, modeLowercase, windowStateLowercase);
            if (moduleResources.contains(pathBasedOnModeAndState)) {
                // We have a winner!
                availableViews.put(viewTuple, pathBasedOnModeAndState);
                rslt = pathBasedOnModeAndState;
            } else {
                // Widen the search (within this module) based on Mode only
                final String pathBasedOnModeOnly = getCompletePathforParts(modulePath, modeLowercase);
                if (moduleResources.contains(pathBasedOnModeOnly)) {
                    // We still need to store the choice so we're not constantly looking
                    availableViews.put(viewTuple, pathBasedOnModeOnly);
                    rslt = pathBasedOnModeOnly;
                } else {
                    throw new IllegalStateException("Unable to select a view for Mode="
                            + modeLowercase + " and WindowState=" + windowStateLowercase);
                }
            }
        }

        logger.info("Selected viewName='{}' for Mode='{}' and WindowState='{}'",
                                rslt, modeLowercase, windowStateLowercase);

        return rslt;

    }

    private String getCompletePathforParts(final String... parts) {

        StringBuilder rslt = new StringBuilder();

        for (String part : parts) {
            rslt.append(part);
            if (!part.endsWith("/")) {
                // First part will be a directory
                rslt.append(".");
            }
        }

        rslt.append("jsp");  // TODO:  support more options

        logger.debug("Calculated path '{}' for parts={}", rslt, parts);

        return rslt.toString();

    }

    private Map<String,Object> gatherModelAttributes(String viewName, HttpServletRequest req, HttpServletResponse res,
                        PortalRequest portalRequest, Bearer bearer, Preferences preferences, Definition definition) {

        final Map<String,Object> rslt = new HashMap<>();

        logger.debug("Processing model attributes for viewName='{}'", viewName);

        for (Map.Entry<AnnotatedElement,Object> y : modelAttributes.entrySet()) {
            final AnnotatedElement annotatedElement = y.getKey();
            final Object bean = y.getValue();
            final SoffitModelAttribute sma = annotatedElement.getAnnotation(SoffitModelAttribute.class);
            if (attributeAppliesToView(sma, viewName)) {
                logger.debug("The following  SoffitModelAttribute applies to viewName='{}':  {}", viewName, sma);
                final String modelAttributeName = sma.value();
                // Are we looking at a class or a method?
                if (annotatedElement instanceof Class) {
                    // The bean itself is the model attribute
                    rslt.put(modelAttributeName, bean);
                } else if (annotatedElement instanceof Method) {
                    Method m = (Method) annotatedElement;
                    // This Method must NOT have a void return type...
                    if (m.getReturnType().equals(Void.TYPE)) {
                        final String msg = "Methods annotated with SoffitModelAttribute must not specify a void return type;  " + m.getName();
                        throw new IllegalStateException(msg);
                    }
                    // Examine the parameters this Method declares and try to match them.
                    final Class<?>[] parameterTypes = m.getParameterTypes();
                    final Object[] parameters = new Object[parameterTypes.length];
                    for (int i=0; i < parameters.length; i++) {
                        final Class<?> pType = parameterTypes[i];
                        // At present, these are the parameter types we support...
                        if (HttpServletRequest.class.equals(pType)) {
                            parameters[i] = req;
                        } else if (HttpServletResponse.class.equals(pType)) {
                            parameters[i] = res;
                        } else if (PortalRequest.class.equals(pType)) {
                            parameters[i] = portalRequest;
                        } else if (Bearer.class.equals(pType)) {
                            parameters[i] = bearer;
                        } else if (Preferences.class.equals(pType)) {
                            parameters[i] = preferences;
                        } else if (Definition.class.equals(pType)) {
                            parameters[i] = definition;
                        } else {
                            final String msg = "Unsupported parameter type for SoffitModelAttribute method:  " + pType;
                            throw new UnsupportedOperationException(msg);
                        }
                    }
                    try {
                        final Object value = m.invoke(bean, parameters);
                        rslt.put(modelAttributeName, value);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        final String msg = "Failed to evaluate the specified model attribute:  " + sma.value();
                        throw new RuntimeException(msg);
                    }
                } else {
                    final String msg = "Unsuppored AnnotatedElement type:  " + AnnotatedElement.class.getName();
                    throw new UnsupportedOperationException(msg);
                }
            }
        }

        logger.debug("Calculated the following model attributes for viewName='{}':  {}", viewName, rslt);

        return rslt;

    }

    private boolean attributeAppliesToView(SoffitModelAttribute attributeAnnotation, String viewName) {
        final Pattern pattern = Pattern.compile(attributeAnnotation.viewRegex());
        final Matcher matcher = pattern.matcher(viewName);
        return matcher.matches();
    }

    private void configureCacheHeaders(final HttpServletResponse res, final String module) {

        final String cacheScopeProperty = String.format(CACHE_SCOPE_PROPERTY_FORMAT, module);
        final String cacheScopeValue = environment.getProperty(cacheScopeProperty);
        logger.debug("Selecting cacheScopeValue='{}' for property '{}'", cacheScopeValue, cacheScopeProperty);

        final String cacheMaxAgeProperty = String.format(CACHE_MAXAGE_PROPERTY_FORMAT, module);
        final String cacheMaxAgeValue = environment.getProperty(cacheMaxAgeProperty);
        logger.debug("Selecting cacheMaxAgeValue='{}' for property '{}'", cacheMaxAgeValue, cacheMaxAgeProperty);

        // Both must be specified, else we just use the default...
        final String cacheControl = (StringUtils.isNotEmpty(cacheScopeValue) && StringUtils.isNotEmpty(cacheMaxAgeValue))
                ? cacheScopeValue + ", max-age=" + cacheMaxAgeValue
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
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ViewTuple other = (ViewTuple) obj;
            if (mode == null) {
                if (other.mode != null)
                    return false;
            } else if (!mode.equals(other.mode))
                return false;
            if (moduleName == null) {
                if (other.moduleName != null)
                    return false;
            } else if (!moduleName.equals(other.moduleName))
                return false;
            if (windowState == null) {
                if (other.windowState != null)
                    return false;
            } else if (!windowState.equals(other.windowState))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "ViewTuple [moduleName=" + moduleName + ", mode=" + mode + ", windowState=" + windowState + "]";
        }

    }

}
