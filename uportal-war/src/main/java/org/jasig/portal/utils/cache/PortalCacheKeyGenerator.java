package org.jasig.portal.utils.cache;

import java.io.Serializable;

import org.jasig.portal.utils.cache.CacheKey.CacheKeyBuilder;

import com.googlecode.ehcache.annotations.key.AbstractDeepCacheKeyGenerator;

public class PortalCacheKeyGenerator extends AbstractDeepCacheKeyGenerator<CacheKeyBuilder<Serializable, Serializable>, CacheKey> {
    @Override
    protected CacheKeyBuilder<Serializable, Serializable> getGenerator(Object... data) {
        final CacheKeyBuilder<Serializable, Serializable> builder = CacheKey.builder(PortalCacheKeyGenerator.class.getName());
        builder.addAll(data);
        return builder;
    }

    @Override
    protected CacheKey generateKey(CacheKeyBuilder<Serializable, Serializable> generator) {
        return generator.build();
    }

    @Override
    protected void append(CacheKeyBuilder<Serializable, Serializable> generator, Object e) {
        generator.add((Serializable)e);
    }

    @Override
    protected void appendGraphCycle(CacheKeyBuilder<Serializable, Serializable> generator, Object o) {
        this.appendNull(generator);
    }

    @Override
    protected void appendNull(CacheKeyBuilder<Serializable, Serializable> generator) {
        generator.add(null);        
    }
}
