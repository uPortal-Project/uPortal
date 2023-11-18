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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apereo.portal.soffit.model.v1_0.Bearer;
import org.apereo.portal.soffit.model.v1_0.Definition;
import org.apereo.portal.soffit.model.v1_0.PortalRequest;
import org.apereo.portal.soffit.model.v1_0.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * Responsible for marshalling data required for rendering based on the {@link SoffitModelAttribute}
 * annotation.
 *
 * @since 5.0
 */
public class ModelAttributeService {

    @Autowired private ApplicationContext applicationContext;

    private Map<AnnotatedElement, Object> modelAttributes;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {
        /*
         * Gather classes & methods that reference @SoffitMoldelAttribute
         */
        final Map<AnnotatedElement, Object> map = new HashMap<>();
        final String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String name : beanNames) {
            final Object bean = applicationContext.getBean(name);
            final Class clazz =
                    AopUtils.isAopProxy(bean) ? AopUtils.getTargetClass(bean) : bean.getClass();
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

    /* package-private */ Map<String, Object> gatherModelAttributes(
            String viewName,
            HttpServletRequest req,
            HttpServletResponse res,
            PortalRequest portalRequest,
            Bearer bearer,
            Preferences preferences,
            Definition definition) {

        final Map<String, Object> result = new HashMap<>();

        logger.debug("Processing model attributes for viewName='{}'", viewName);

        for (Map.Entry<AnnotatedElement, Object> y : modelAttributes.entrySet()) {
            final AnnotatedElement annotatedElement = y.getKey();
            final Object bean = y.getValue();
            final SoffitModelAttribute sma =
                    annotatedElement.getAnnotation(SoffitModelAttribute.class);
            if (attributeAppliesToView(sma, viewName)) {
                logger.debug(
                        "The following  SoffitModelAttribute applies to viewName='{}':  {}",
                        viewName,
                        sma);
                final String modelAttributeName = sma.value();
                // Are we looking at a class or a method?
                if (annotatedElement instanceof Class) {
                    // The bean itself is the model attribute
                    result.put(modelAttributeName, bean);
                } else if (annotatedElement instanceof Method) {
                    final Method m = (Method) annotatedElement;
                    final Object modelAttribute =
                            getModelAttributeFromMethod(
                                    bean,
                                    m,
                                    req,
                                    res,
                                    portalRequest,
                                    bearer,
                                    preferences,
                                    definition);
                    result.put(modelAttributeName, modelAttribute);
                } else {
                    final String msg =
                            "Unsupported AnnotatedElement type:  "
                                    + AnnotatedElement.class.getName();
                    throw new UnsupportedOperationException(msg);
                }
            }
        }

        logger.debug(
                "Calculated the following model attributes for viewName='{}':  {}",
                viewName,
                result);

        return result;
    }

    protected Object getModelAttributeFromMethod(
            Object bean,
            Method method,
            HttpServletRequest req,
            HttpServletResponse res,
            PortalRequest portalRequest,
            Bearer bearer,
            Preferences preferences,
            Definition definition) {

        // This Method must NOT have a void return type...
        if (method.getReturnType().equals(Void.TYPE)) {
            final String msg =
                    "Methods annotated with SoffitModelAttribute must not specify a void return type;  "
                            + method.getName();
            throw new IllegalStateException(msg);
        }
        final Object[] parameters =
                prepareMethodParameters(
                        method, req, res, portalRequest, bearer, preferences, definition);
        try {
            final Object result = method.invoke(bean, parameters);
            return result;
        } catch (IllegalAccessException | InvocationTargetException e) {
            final String msg =
                    "Failed to generate a model attribute by invoking '"
                            + method.getName()
                            + "' on the following bean:  "
                            + bean.toString();
            throw new RuntimeException(msg);
        }
    }

    protected Object[] prepareMethodParameters(
            Method method,
            HttpServletRequest req,
            HttpServletResponse res,
            PortalRequest portalRequest,
            Bearer bearer,
            Preferences preferences,
            Definition definition) {

        // Examine the parameters this Method declares and try to match them.
        final Class<?>[] parameterTypes = method.getParameterTypes();
        final Object[] result = new Object[parameterTypes.length];
        for (int i = 0; i < result.length; i++) {
            final Class<?> pType = parameterTypes[i];
            // At present, these are the parameter types we support...
            if (HttpServletRequest.class.equals(pType)) {
                result[i] = req;
            } else if (HttpServletResponse.class.equals(pType)) {
                result[i] = res;
            } else if (PortalRequest.class.equals(pType)) {
                result[i] = portalRequest;
            } else if (Bearer.class.equals(pType)) {
                result[i] = bearer;
            } else if (Preferences.class.equals(pType)) {
                result[i] = preferences;
            } else if (Definition.class.equals(pType)) {
                result[i] = definition;
            } else {
                final String msg =
                        "Unsupported parameter type for SoffitModelAttribute method:  " + pType;
                throw new UnsupportedOperationException(msg);
            }
        }

        return result;
    }

    protected boolean attributeAppliesToView(
            SoffitModelAttribute attributeAnnotation, String viewName) {
        final Pattern pattern = Pattern.compile(attributeAnnotation.viewRegex());
        final Matcher matcher = pattern.matcher(viewName);
        return matcher.matches();
    }
}
