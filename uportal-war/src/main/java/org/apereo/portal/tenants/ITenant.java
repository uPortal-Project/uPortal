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
package org.apereo.portal.tenants;

import java.io.Serializable;
import java.util.Map;

/**
 * Represents a portal tenant. Tenants may be departments, colleges, campuses, clients -- whatever.
 * Tenants support a few strongly-typed members -- name, fname, Id -- plus an open-ended collection
 * of attributes. Adopters may define custom attributes in servicesContext.xml, and even use them in
 * custom {@link ITenantOperationsListener} implementations.
 *
 * <p>Tenancy is an optional concept in uPortal.
 *
 * @since 4.1
 */
public interface ITenant extends Comparable<ITenant>, Serializable {

    /** Each tenant will be assigned a numeric identifier by the persistence infrastructure. */
    long getId();

    /**
     * The name of this tenant in a format suitable for display. Tenant names must be unique in the
     * portal.
     */
    String getName();

    /**
     * Tenant names must be unique and adhere to the ${@link ITenant.TENANT_NAME_VALIDATOR_REGEX}.
     */
    void setName(String name);

    /**
     * The name of this tenant in a format suitable for namespacing. Tenant fnames must be unique in
     * the portal.
     */
    String getFname();

    /**
     * Tenant fnames must be unique and adhere to the ${@link ITenant.TENANT_NAME_VALIDATOR_REGEX}.
     */
    void setFname(String fname);

    /** Provides the current value of the specified attribute. May return <code>null</code>. */
    String getAttribute(String name);

    void setAttribute(String name, String value);

    /** Provides a read-only {@link Map} of current attributes and their values. */
    Map<String, String> getAttributesMap();
}
