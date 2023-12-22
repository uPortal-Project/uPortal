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
package org.apereo.portal.security.provider;

import org.apereo.portal.security.ISecurityContext;
import org.apereo.portal.security.ISecurityContextFactory;

/**
 * The factory class for the cache security context. Unlike most {@link ISecurityContextFactory}
 * implementations, objects of this class are not Spring-managed beans. This class must be leveraged
 * from within another {@link ISecurityContextFactory} that is a Spring-managed bean. See the notes
 * and warnings associated with the CacheSecurityContext class.
 */
public class CacheSecurityContextFactory implements ISecurityContextFactory {

    @Override
    public String getName() {
        return "cache";
    }

    @Override
    public boolean isEnabled() {
        final String msg =
                "CacheSecurityContextFactory must not be used directly by "
                        + "InitialSecurityContextFactory";
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public ISecurityContext getSecurityContext() {
        return new CacheSecurityContext();
    }
}
