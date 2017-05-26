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
package org.apereo.portal.utils.cache;

import com.googlecode.ehcache.annotations.key.AbstractDeepCacheKeyGenerator;
import java.io.Serializable;

public class PortalCacheKeyGenerator
        extends AbstractDeepCacheKeyGenerator<
                CacheKey.CacheKeyBuilder<Serializable, Serializable>, CacheKey> {
    @Override
    protected CacheKey.CacheKeyBuilder<Serializable, Serializable> getGenerator(Object... data) {
        final CacheKey.CacheKeyBuilder<Serializable, Serializable> builder =
                CacheKey.builder(PortalCacheKeyGenerator.class.getName());
        builder.addAll(data);
        return builder;
    }

    @Override
    protected CacheKey generateKey(CacheKey.CacheKeyBuilder<Serializable, Serializable> generator) {
        return generator.build();
    }

    @Override
    protected void append(
            CacheKey.CacheKeyBuilder<Serializable, Serializable> generator, Object e) {
        generator.add((Serializable) e);
    }

    @Override
    protected void appendGraphCycle(
            CacheKey.CacheKeyBuilder<Serializable, Serializable> generator, Object o) {
        this.appendNull(generator);
    }

    @Override
    protected void appendNull(CacheKey.CacheKeyBuilder<Serializable, Serializable> generator) {
        generator.add(null);
    }
}
