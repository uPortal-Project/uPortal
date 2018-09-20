package org.apereo.portal.context.security;

import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.security.provider.RemoteUserPersonManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserConfiguration {
    /**
     * For uPortal 5, the various flavors of PersonManager were combined into a single class to
     * avoid unnecessary bean tweaking on the part of deployers.
     */
    @Bean(name = "personManager")
    public IPersonManager getPersonManager() {
        return new RemoteUserPersonManager();
    }
}
