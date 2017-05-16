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
package org.apereo.portal.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.springframework.core.io.Resource;

/**
 * Utilities for working with Spring {@link Resource} objects
 *
 */
public final class ResourceUtils {
    private ResourceUtils() {}

    /**
     * First tries {@link Resource#getURI()} and if that fails with a FileNotFoundException then
     * {@link Resource#getDescription()} is returned.
     */
    public static String getResourceUri(Resource resource) {
        try {
            return resource.getURI().toString();
        } catch (FileNotFoundException e) {
            return resource.getDescription();
        } catch (IOException e) {
            throw new RuntimeException("Could not create URI for resource: " + resource, e);
        }
    }
}
