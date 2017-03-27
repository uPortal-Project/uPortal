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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that binds a bean or the return value of a bean method to a named model attribute,
 * exposed to a web view. This annotation is inspired by Spring MVC's <code>ModelAttribute</code>.
 * Methods marked with this annotation support a similar, flexible list of optional parameters
 * (below):
 *
 * <ul>
 *   <li>Request and/or Response objects
 *   <li>Soffit Data Model objects, i.e. PortalRequest, Bearer, Preferences, and Definition
 * </ul>
 *
 * @since 5.0
 */
@Target(value = {ElementType.TYPE, ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface SoffitModelAttribute {

    /** The name of the model attribute to bind to. Required. */
    String value();

    /**
     * Allows the bean or method to restrict which views (by name) have access to this model
     * attribute. The specified regex expression must match the view name for the attribute to be
     * available. The default is <code>.*</code> (match all views).
     */
    String viewRegex() default ".*";
}
