package org.jasig.portal.character.stream.events;

import org.jasig.portal.jmx.GuavaCacheStatsBean;

import com.google.common.cache.Cache;

public class CharacterDataEventCacheStatsBean extends GuavaCacheStatsBean {
    @Override
    protected Cache<?, ?> getCache() {
        return CharacterDataEventImpl.getEventCache();
    }
}
