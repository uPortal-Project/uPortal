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
package org.apereo.portal.version.dao;

import org.apereo.portal.version.om.Version;

/**
 * Access information about version numbers for various parts of uPortal
 *
 */
public interface VersionDao {
    /** Get the version information for the specified product, returns null if no version is set. */
    Version getVersion(String product);

    /** Create or update a the version number for the specified product */
    Version setVersion(String product, int major, int minor, int patch, Integer local);

    /** @see #setVersion(String, int, int, int) */
    Version setVersion(String product, Version version);
}
