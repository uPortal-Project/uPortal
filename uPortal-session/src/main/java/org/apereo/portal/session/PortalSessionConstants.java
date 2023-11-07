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
package org.apereo.portal.session;

public class PortalSessionConstants {

    private PortalSessionConstants() {}

    public static final String REDIS_STORE_TYPE = "redis";
    public static final String REDIS_STANDALONE_MODE = "standalone";
    public static final String REDIS_SENTINEL_MODE = "sentinel";
    public static final String REDIS_CLUSTER_MODE = "cluster";
    public static final String SESSION_STORE_TYPE_ENV_PROPERTY_NAME =
            "ORG_APEREO_PORTAL_SESSION_STORETYPE";
    public static final String SESSION_STORE_TYPE_SYSTEM_PROPERTY_NAME =
            "org.apereo.portal.session.storetype";
}
