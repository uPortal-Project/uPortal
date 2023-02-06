## v5.14.0

- new properties, HTTP Security Headers in `security.properties`
    ```properties
    ##
    ## Tomcat HTTP Security Headers
    ##

    # antiClickJackingEnabled:  X-Frame-Options header
    sec.anti.click.jacking.enabled=false
    # X-Frame-Options: deny, sameorigin, allow-from
    sec.anti.click.jacking.options=sameorigin
    # If allow-from is selected above, add URI
    sec.anti.click.jacking.uri=

    # Content-Security-Policy: default-src, script-src, style-src, img-src
    # See more details at: https://content-security-policy.com/
    sec.content.sec.policy.enabled=false
    sec.content.sec.policy=default-src 'self'

    # Strict-Transport-Security: max-age=###; includeSubDomains; preload
    sec.hsts.enabled=false
    sec.hsts.maxage.seconds=31536000
    sec.hsts.include.subdomains=true
    sec.hsts.preload=false

    # X-Content-Type-Options: "nosniff" will be used if enabled is set to true
    sec.x.content.type.enabled=false

    # Referrer-Policy available directives to pass include:
    # See more details at: https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Referrer-Policy
    sec.referrer.policy.enabled=false
    sec.referrer.policy=no-referrer
    ```

v5.13.1
- N/A

v5.13.0 - Portlet List, Analytics Event
- new table, UP_PORTLET_LIST for PortletList.java in uPortal-api-rest module
- new table, UP_PORTLET_LIST_ITEM for PortletListItem.java in uPortal-api-rest module
- new property, events.analytics.log.level in `portal.properties`
    ```properties
    ##
    ## Configure which analytics events (e.g. link clicks) are saved based on the the following:
    ## - NONE - no events are recorded
    ## - AUTHENTICATED - only record events from known users
    ## - ALL - record events from all users, including guest/anonymous users
    #events.analytics.log.level:NONE
    ```
- new property, org.apereo.portal.events.LoginEvent.captureUserIpAddresses in `portal.properties`
    ```properties
    ##
    ## Configure Login Events (raw) to capture user IP addresses. This is not included in aggregation.
    ## N.B. This raises privacy concerns and should only be enabled after very careful consideration.
    org.apereo.portal.events.LoginEvent.captureUserIpAddresses=false
    ```

v5.12.0
- N/A

v5.11.1
- new property, org.apereo.portal.ehcache.filename in `portal.properties`
    ```properties
    ##
    ## Configure which file in the properties/ directory in the classpath for ehcache.
    ## This is often used to selected between the default ehcache.xml file with JGroups
    ## vs. the ehache-no-jgroups.xml file that has JGroups removed.
    #
    #org.apereo.portal.ehcache.filename:ehcache.xml
    ```

v5.11.0
- fixed table reference, UP_PORTLET_MDATA -> UP_PORTLET_DEF_MDATA for PortletLocalizationData.java in uPortal-content-portlet module

v5.10.0
- new properties, PersonalizationFilter in `portal.properties`
    ```properties
    # Personalization
    # ---------------
    # Disabled by default
    #
    # org.apereo.portal.utils.web.PersonalizationFilter.enable=false

    # `prefix` and `pattern` are combined to define how the placement tokens
    # appear in the portlet-definitions, such as:
    #   `<desc>Bookmarks for @up@apereo.displayName@up@</desc>`
    #
    # org.apereo.portal.utils.personalize.PersonalizerImpl.prefix=apereo.
    # org.apereo.portal.utils.personalize.PersonalizerImpl.pattern=@up@(.*?)@up@
    ```

v5.9.0
- new property, org.apereo.portal.portlet.worker.threadPool.queueSize in `portal.properties`
    ```properties
    ##
    ## Portlet worker thread pool queue max size
    ##
    #org.apereo.portal.portlet.worker.threadPool.queueSize=0
    ```

v5.8.2
- replace property, cas.authenticationFilter.service with portal.allServerNames in `security.properties`
    ```properties
    # All server names values for multi server name management, separator is a space
    # This property should be set/overridden in PORTAL_HOME/uPortal.properties
    # Example: portal1.univ.edu portal2.univ.edu
    portal.allServerNames=${portal.server}
    ```
- new property, org.apereo.portal.index.relativePath in `portal.properties`
    ```properties
    # Search Indexing
    # ---------------
    # Search indexing was introduced in v5.5.0. To disable indexing, uncomment
    # and set the value of this property to "#{null}" (without double quotes)
    #
    #org.apereo.portal.index.relativePath=/WEB-INF/index
    ```

v5.8.1
- new cache, org.apereo.portal.groups.RDBMEntityGroupStore.search in `ehcache.xml`/`ehcache-no-jgroups.xml`
    ```xml
        <!--
         | Caches search results from searchForGroups() in RDBMEntityGroupStore.
         | - 1 x search criteria
         +-->
        <cache name="org.apereo.portal.groups.RDBMEntityGroupStore.search"
            eternal="false" maxElementsInMemory="500" overflowToDisk="false" diskPersistent="false"
            timeToIdleSeconds="0" timeToLiveSeconds="300" memoryStoreEvictionPolicy="LRU" statistics="true" >
            <cacheEventListenerFactory class="org.apereo.portal.utils.cache.SpringCacheEventListenerFactory" properties="beanName=insufficientSizeCacheEventListener" listenFor="local" />
        </cache>
    ```

v5.8.0
- new property, cas.ticketValidationFilter.encodeServiceUrl in `security.properties`
    ```properties
    ## Some CAS servers, like the CAS server in uPortal-start can not handle encoded service URLs.
    ## Set the following property to false to disable encoding of service URLs.
    ## See https://groups.google.com/a/apereo.org/d/msg/uportal-user/44Uw1YP8_Mg/hLaTlEVZFAAJ
    ## for the discussion regarding this property
    #
    #cas.ticketValidationFilter.encodeServiceUrl=true
    ```
- new property, org.apereo.portal.rest.search.PortletsSearchStrategy.displayScore in `portal.properties`
    ```properties
    ##
    ## Flag to enable or disable the display the the search strategy score of the results
    ##
    org.apereo.portal.rest.search.PortletsSearchStrategy.displayScore:true
    ```

v5.7.1
- N/A

v5.7.0
- N/A

v5.6.1
- N/A




