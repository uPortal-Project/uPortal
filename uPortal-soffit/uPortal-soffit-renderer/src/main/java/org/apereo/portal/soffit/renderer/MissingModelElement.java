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

import java.util.List;
import java.util.Map;
import org.apereo.portal.soffit.model.v1_0.Bearer;
import org.apereo.portal.soffit.model.v1_0.Definition;
import org.apereo.portal.soffit.model.v1_0.PortalRequest;
import org.apereo.portal.soffit.model.v1_0.Preferences;

/**
 * Used in the place of a missing element from the Soffit data model. Throws an appropriate {@link
 * MissingModelElementException} if any method is called.
 *
 * @since 5.1
 */
public class MissingModelElement {

    public static final PortalRequest PORTAL_REQUEST =
            new PortalRequest() {
                @Override
                public Map<String, String> getProperties() {
                    throw new MissingModelElementException(PortalRequest.class);
                }

                @Override
                public Map<String, List<String>> getAttributes() {
                    throw new MissingModelElementException(PortalRequest.class);
                }

                @Override
                public Map<String, List<String>> getParameters() {
                    throw new MissingModelElementException(PortalRequest.class);
                }

                @Override
                public String getEncryptedToken() {
                    throw new MissingModelElementException(PortalRequest.class);
                }
            };

    // Bearer
    public static final Bearer BEARER =
            new Bearer() {
                @Override
                public String getUsername() {
                    throw new MissingModelElementException(Bearer.class);
                }

                @Override
                public Map<String, List<String>> getAttributes() {
                    throw new MissingModelElementException(Bearer.class);
                }

                @Override
                public List<String> getGroups() {
                    throw new MissingModelElementException(Bearer.class);
                }

                @Override
                public String getEncryptedToken() {
                    throw new MissingModelElementException(Bearer.class);
                }
            };

    // Preferences
    public static final Preferences PREFERENCES =
            new Preferences() {
                @Override
                public List<String> getValues(String name) {
                    throw new MissingModelElementException(Preferences.class);
                }

                @Override
                public Map<String, List<String>> getPreferencesMap() {
                    throw new MissingModelElementException(Preferences.class);
                }

                @Override
                public String getEncryptedToken() {
                    throw new MissingModelElementException(Preferences.class);
                }
            };

    // Definition
    public static final Definition DEFINITION =
            new Definition() {
                @Override
                public String getTitle() {
                    throw new MissingModelElementException(Definition.class);
                }

                @Override
                public String getFname() {
                    throw new MissingModelElementException(Definition.class);
                }

                @Override
                public String getDescription() {
                    throw new MissingModelElementException(Definition.class);
                }

                @Override
                public List<String> getCategories() {
                    throw new MissingModelElementException(Definition.class);
                }

                @Override
                public Map<String, List<String>> getParameters() {
                    throw new MissingModelElementException(Definition.class);
                }

                @Override
                public String getEncryptedToken() {
                    throw new MissingModelElementException(Definition.class);
                }
            };
}
