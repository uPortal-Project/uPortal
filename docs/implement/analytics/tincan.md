# XAPI Analytics

## Overview

Support has been added to track uPortal events in an [LRS](https://en.wikipedia.org/wiki/Learning_Record_Store). The [TinCan API](https://xapi.com/) uses the built-in uPortal event aggregation system. When enabled, the TinCan will monitor the uportal event aggregation queue and process uportal events. As events are dequeued, the TinCan event aggregator will attempt to convert uPortal events into LRS statements and forward them on to all configured LRS instances.
Enabling TinCan API event tracking

By default, TinCan API event handling is disabled. To enable, edit the _uPortal.properties_ file:

```properties
org.apereo.portal.tincan-api.enabled=true
```

In addition, add at least one TinCan API provider.

## TinCan API Providers

The TinCan API provider is responsible for processing LRS statements. Multiple providers can be configured. There are 3 built-in LRS providers that can be used:

| Type                                                                  | Description                                                                                                                                                                                                                                                   |
| --------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `org.apereo.portal.events.tincan.providers.LogEventTinCanAPIProvider` | The `LogEventTinCanAPIProvider` writes LRS events to a log file. By default, the log file is located at _${catalina_home}/logs/portal/tin-can-events.log_.                                                                                                    |
| `org.apereo.portal.events.tincan.providers.DefaultTinCanAPIProvider`  | The `DefaultTinCanAPIProvider` uses the standard REST API provided by the LRS to submit LRS statements.                                                                                                                                                       |
| `org.apereo.portal.events.tincan.providers.BatchTinCanAPIProvider`    | The `BatchTinCanAPIProvider` sends LRS statements to the LRS via an extension of the REST API that allows multiple LRS Statements to be sent in a single request. This has been tested with both LearningLocker and ScormCloud but may not work for all LRSs. |

## Configuring LogEventTinCanAPIProvider

To enable the `LogEventTinCanAPIProvider`, a new `LogEventTinCanAPIProvider` must be defined and added to the `tinCanProvider` list. Edit _tincanAPIContext.xml_:

```xml
<!-- The list of "providers" to handle xAPI events -->
<util:list id="tinCanProviders">
   <!-- provider that just logs xAPI events -->
   <bean class="org.apereo.portal.events.tincan.providers.LogEventTinCanAPIProvider"/>

   <!-- provider that sends individual xAPI events to scorm-cloud -->
   <!-- <ref bean="scormCloudTinCanProvider"/> -->

   <!-- provider that sends xAPI events to scorm-cloud in batches -->
   <!--<ref bean="scormCloudTinCanBatchProvider"/>-->
</util:list>
```

## Configuring DefaultTinCanAPIProvider

The `DefaultTinCanAPIProvider` is an interface that uses the LRS REST API to submit statements over HTTP. When the portal starts, the TinCan API will immediately use the LRS activities/state API to check if the LRS is available. If the LRS does not handle the activities/state request, the provider will be disabled.

To set up the `DefaultTinCanAPIProvider`, a new `DefaultTinCanAPIProvider` bean must be defined and configured. That bean must then be added to the `tinCanProviders` list. Edit _tincanAPIContext.xml_.

```xml
<!--
 - Example configuration for a remote LRS.
 -
 - The "id" property of the *TinCanAPIProvider is used to configure the LRS provider.
 - The actual configuration properties are stored in a properties file (eg. portal.properties)
 - See the javadoc in DefaultTinCanAPIProvider or portal.properties for configuration details.
 -
 - The "id" property of the *AuthInterceptor is used to configure the authentication
 - interceptor.  The actual configuration properties are stored in a properties file.
 - See portal.properties for example configurations.
 -->
<bean id="scormCloudTinCanProvider" class="org.apereo.portal.events.tincan.providers.DefaultTinCanAPIProvider">
   <property name="id" value="scorm-cloud-lrs"/>
   <property name="restTemplate">
       <bean class="org.springframework.web.client.RestTemplate">
           <property name="interceptors">
               <list>
                   <!-- Enable a MAXIMUM of 1 of the following interceptors -->
                   <!-- -->
                   <bean class="org.springframework.web.client.interceptors.BasicAuthInterceptor">
                       <property name="id" value="scorm-cloud-lrs"/>
                   </bean>
                   <!-- -->
                   <!--
                   <bean class="org.springframework.web.client.interceptors.ZeroLeggedOAuthInterceptor">
                       <property name="id" value="scorm-cloud-lrs"/>
                   </bean>
                   -->
               </list>
           </property>
       </bean>
   </property>
</bean>

...

<!-- The list of "providers" to handle xAPI events -->
<util:list id="tinCanProviders">
   <!-- provider that just logs xAPI events -->
   <!-- <bean class="org.apereo.portal.events.tincan.providers.LogEventTinCanAPIProvider"/> -->

   <!-- provider that sends individual xAPI events to scorm-cloud -->
   <ref bean="scormCloudTinCanProvider"/>

   <!-- provider that sends xAPI events to scorm-cloud in batches -->
   <!--<ref bean="scormCloudTinCanBatchProvider"/>-->
</util:list>
```

There are a couple of things worth noting in this configuration.

1.  The "id" attribute of the `DefaultTinCanAPIProvider` bean must match the "bean" attribute in the "ref" tag defined inside the "tinCanProviders" list.
2.  The property with the name of "id" defines the name for the LRS. That "id" is used to read provider specific configuration from the local properties file. More details here
3.  The property with the name of "interceptors" configures the HTTP authentication that is used when communicating with the LRS. More details here

### Provider specific configuration

Once the `DefaultTinCanAPIProvider` has been defined, it must be configured. Configuration lines for the provider will all have the form: `org.apereo.portal.tincan-api.{ID}.*` where `{ID}` refers to the "id" property for the provider. Properties in _uPortal.properties_:

| Property                                                      | Required                                       | Default Value        | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| ------------------------------------------------------------- | ---------------------------------------------- | -------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `org.apereo.portal.tincan-api.{ID}.url`                       | true                                           |                      | The base REST endpoint for the LRS. See the LRS documentation for what this should be.                                                                                                                                                                                                                                                                                                                                                                      |
| `org.apereo.portal.tincan-api.{ID}.form-encode-activity-data` | false (except if using the LearningLocker LRS) | false                | By default, the activities/state endpoint accepts JSON in the POST body. The LearningLocker LRS requires that the content be form encoded instead. Setting this property to true will convert the request to a multipart form POST. For LearningLocker, this should always be set to true. For ScormCloud, this should always be set to false or omitted. Installations will need to experiment with other LRSs, but "false" more closely matches the spec. |
| `org.apereo.portal.tincan-api.{ID}.activity-form-param-name`  | false                                          | "content"            | The name of the form element that is used if the `form-encode-activity-data` configuration is set to true.                                                                                                                                                                                                                                                                                                                                                  |
| `org.apereo.portal.tincan-api.{ID}.actor-name`                | false                                          | "uPortal"            | When submitting the initial activities/state request, user information is required. This allows implementations to update the name associated with the request. Currently, this data is only used for an initial throw-away request, and should probably just stick with the default.                                                                                                                                                                       |
| `org.apereo.portal.tincan-api.{ID}.actor-email`               | false                                          | "noreply@apereo.org" | The activites/state request also requires an email address. Currently, this data is only used for an initial throw-away request, and should probably just stick with the default.                                                                                                                                                                                                                                                                           |
| `org.apereo.portal.tincan-api.{ID}.activityId`                | false                                          | "activityId"         | The `activityId` to use for the initial request. Currently, this data is only used for an initial throw-away request, and should probably just stick with the default.                                                                                                                                                                                                                                                                                      |
| `org.apereo.portal.tincan-api.{ID}.stateId`                   | false                                          | "stateId"            | The `stateId` to use for the initial request. Currently, this data is only used for an initial throw-away request, and should probably just stick with the default.                                                                                                                                                                                                                                                                                         |

An example of a configuration with an "id" of "learning-locker-demo-lrs" might look like:

```properties
org.apereo.portal.tincan-api.learning-locker-demo-lrs.url=http://demo.learninglocker.net/data/xAPI
# IMPORTANT: For LearningLocker, the activities/states API requires that you
# pass the state information as multipart form data instead of as JSON.  ScormCloud,
# on the other hand will not accept the form encoded data, but requires JSON
# in the POST body.
#
# The following 2 properties are required for LearningLocker, but should *NOT* be used
# for scorm cloud.  If not using LearningLocker or Scorm Cloud, you will need to
# research their impl.  Default is to use the Scorm Cloud configuration.
org.apereo.portal.tincan-api.learning-locker-demo-lrs.form-encode-activity-data=true
org.apereo.portal.tincan-api.learning-locker-demo-lrs.activity-form-param-name=content


# Additional LRS provider properties that may be configured.  These properties
# are all optional.  These values are only used during the initial request to
# check connectivity with the LRS.  Default values are shown below
org.apereo.portal.tincan-api.learning-locker-demo-lrs.actor-name=uportal
org.apereo.portal.tincan-api.learning-locker-demo-lrs.actor-email=noreply@apereo.org
org.apereo.portal.tincan-api.learning-locker-demo-lrs.activity-id=urn:tincan:uportal:activities:state:status
org.apereo.portal.tincan-api.learning-locker-demo-lrs.state-id=urn:tincan:uportal:activities:state:status:stateId
```

## BatchTinCanAPIProvider

The `BatchTinCanAPIProvider` is an interface that uses the LRS REST API to submit statements over HTTP. This instance will bundle up multiple TinCan API Statements and submit them all in a single HTTP request to the LRS server. When the portal starts, the TinCan API will immediately use the LRS activities/state API to check if the LRS is available. If the LRS does not handle the activities/state request, the provider will be disabled.

The `BatchTinCanProvider` configuration is similar to the configuration for `DefaultTinCanAPIProvider` with a few notable additions. Edit _tincanAPIContext.xml_.

```xml
<!--
 - If you enable the batching provider, you also need to add a scheduler
 - for how often to check the queue.
 -
 - This example, schedules the scormCloudTinCanBatchProvider to flush its
 - LRS queue every 2 seconds.
 -
 - If you have multiple batching providers defined, each provider will need
 - to be listed here.
 -
 - IMPORTANT: If using the BatchTinCanAPIProvider, this MUST be enabled!
 -
 - TODO:  When we upgrade to a newer spring (3.2+) should switch to the annotated
 - configuration.
 -->
<task:scheduled-tasks scheduler="uPortalTaskScheduler">
   <task:scheduled ref="scormCloudTinCanBatchProvider" method="sendBatch" fixed-delay="${org.apereo.portal.tincan-api.batch-scheduler.delayMS:2000}"/>
</task:scheduled-tasks>

<!--
- Example of a batching provider.  The batching provider will queue up LRS events.  Every
- ${org.apereo.portal.tincan-api.batch-scheduler.delayMS} it will check the queue and then
- post all LRSStatements to the LRS at once.
-->
<bean id="scormCloudTinCanBatchProvider" class="org.apereo.portal.events.tincan.providers.BatchTinCanAPIProvider">
   <property name="id" value="scorm-cloud-lrs"/>
   <property name="restTemplate">
       <bean class="org.springframework.web.client.RestTemplate">
           <property name="interceptors">
               <list>
                   <!-- Enable a MAXIMUM of 1 of the following interceptors -->
                   <!-- -->
                   <bean class="org.springframework.web.client.interceptors.BasicAuthInterceptor">
                       <property name="id" value="scorm-cloud-lrs"/>
                   </bean>
                   <!-- -->
                   <!--
                   <bean class="org.springframework.web.client.interceptors.ZeroLeggedOAuthInterceptor">
                       <property name="id" value="scorm-cloud-lrs"/>
                   </bean>
                   -->
               </list>
           </property>
       </bean>
   </property>
</bean>

...

<!-- The list of "providers" to handle xAPI events -->
<util:list id="tinCanProviders">
   <!-- provider that just logs xAPI events -->
   <!-- <bean class="org.apereo.portal.events.tincan.providers.LogEventTinCanAPIProvider"/> -->

   <!-- provider that sends individual xAPI events to scorm-cloud -->
   <!-- <ref bean="scormCloudTinCanProvider"/> -->

   <!-- provider that sends xAPI events to scorm-cloud in batches -->
   <ref bean="scormCloudTinCanBatchProvider"/>
</util:list>
```

There are a couple of things worth noting in this configuration.

-   The "id" attribute of the `BatchTinCanAPIProvider` bean must match the "bean" attribute in the "ref" tag defined inside the "tinCanProviders" list.
-   The property with the name of "id" defines the name for the LRS. That "id" is used to read provider specific configuration from the local properties file. More details here
-   The property with the name of "interceptors" configures the HTTP authentication that is used when communicating with the LRS. More details here

### Provider specific configuration

Once the `BatchTinCanAPIProvider` has been defined, it must be configured. Most configuration lines for the provider will all have the form: `org.apereo.portal.tincan-api.{ID}.*` where `{ID}` refers to the "id" property for the provider.

Properties in `uPortal.properties`:

| Property                                                      | Required                                       | Default              | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| ------------------------------------------------------------- | ---------------------------------------------- | -------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `org.apereo.portal.tincan-api.batch-scheduler.delayMS`        | false                                          | 2000                 | Time to wait between sending LRS batches. Note: uPortal event aggregation is occurs asynchronously. This property will not affect the uPortal event aggregation settings. It only controls the duration between sending requests to the LRS, not how soon events are passed to the TinCan API module. Very high values could have negative consequences on memory consumption. This setting is global and will affect all defined `BatchTinCanAPIProviders`.          |
| `org.apereo.portal.tincan-api.{ID}.url`                       | true                                           |                      | The base REST endpoint for the LRS. See the LRS documentation for what this should be.                                                                                                                                                                                                                                                                                                                                                                                |
| `org.apereo.portal.tincan-api.{ID}.form-encode-activity-data` | false (except if using the LearningLocker LRS) | false                | By default, the activities/state endpoint accepts JSON in the POST body. The LearningLocker LRS requires that the content be form encoded instead. Setting this property to true will convert the request to a multipart form POST. For LearningLocker, this should always be set to true. For ScormCloud, this should always be set to false or omitted. Installations will need to experiment with other LRSs, but I believe "false" more closely matches the spec. |
| `org.apereo.portal.tincan-api.{ID}.activity-form-param-name`  | false                                          |                      | "content" The name of the form element that is used if the form-encode-activity-data configuration is set to true.                                                                                                                                                                                                                                                                                                                                                    |
| `org.apereo.portal.tincan-api.{ID}.actor-name`                | false                                          | "uPortal"            | When submitting the initial activities/state request, user information is required. This allows implementations to update the name associated with the request. Currently, this data is only used for an initial throw-away request, and should probably just stick with the default.                                                                                                                                                                                 |
| `org.apereo.portal.tincan-api.{ID}.actor-email`               | false                                          | "noreply@apereo.org" | The activites/state request also requires an email address. Currently, this data is only used for an initial throw-away request, and should probably just stick with the default.                                                                                                                                                                                                                                                                                     |
| `org.apereo.portal.tincan-api.{ID}.activityId`                | false                                          | "activityId"         | The activityId to use for the initial request. Currently, this data is only used for an initial throw-away request, and should probably just stick with the default.                                                                                                                                                                                                                                                                                                  |
| `org.apereo.portal.tincan-api.{ID}.stateId`                   | false                                          | "stateId"            | The stateId to use for the initial request. Currently, this data is only used for an initial throw-away request, and should probably just stick with the default.                                                                                                                                                                                                                                                                                                     |

### Authentication

The Tin Can API supports LRS that require no authentication, BASIC authentication, or authentication using OAuth signatures. The authentication configuration is handled by defining interceptors to pass to a RestTemplate object. In the case where no authentication is required, simply omit all interceptors:

```xml
<bean class="org.springframework.web.client.RestTemplate">
</bean>
```

#### Basic Authentication

Basic Authentication is configured by adding a `BasicAuthInterceptor` to the `RestTemplate` interceptors. When defining the interceptor, you must pass an "id" to the interceptor. The "id" will be used to configure each auth interceptor independently.

To add Basic Authentication, edit _tincanAPIContext.xml_ and to the `restTemplate` property, add a `BasicAuthInterceptor` element like:

```xml
<bean class="org.springframework.web.client.RestTemplate">
    <property name="interceptors">
        <list>
            <bean class="org.springframework.web.client.interceptors.BasicAuthInterceptor">
                <property name="id" value="scorm-cloud-lrs"/>
            </bean>
        </list>
    </property>
</bean>
```

The specific configuration for this interceptor must be defined in uportal-war/src/main/resources/properties/portal.properties or an override file. Example:

```properties
# basic auth configuration...
org.apereo.rest.interceptor.basic-auth.scorm-cloud-lrs.username=UsernameForProvider
org.apereo.rest.interceptor.basic-auth.scorm-cloud-lrs.password=PasswordForProvider
```

#### OAuth Authenication

OAuth Authentication

The TinCan API can be configured to use basic Oauth signatures for authentication. OAuth authentication is configured by adding a new `ZeroLeggedOAuthInterceptor` interceptor to the `RestTemplate` object passed to the LRS provider.

```xml
<bean class="org.springframework.web.client.RestTemplate">
    <property name="interceptors">
        <list>
            <bean class="org.springframework.web.client.interceptors.ZeroLeggedOAuthInterceptor">
                <property name="id" value="scorm-cloud-lrs"/>
            </bean>
        </list>
    </property>
</bean>
```

The specific configuration for this interceptor must be defined in _uPortal.properties_ or an override file. Example:

```property
# oauth configuration...
org.apereo.rest.interceptor.oauth.scorm-cloud-lrs.realm=ProviderRealm  (optional)
org.apereo.rest.interceptor.oauth.scorm-cloud-lrs.consumerKey=ConsumerKeyValueForProvider
org.apereo.rest.interceptor.oauth.scorm-cloud-lrs.secretKey=SecretKeyValueForProvider
```

## Event Filtering

The TinCan API support in uPortal works by converting uPortal events into LRS statements and the passing the statements to LRS providers. uPortal converters for most common uPortal events. The list of uPortal events are listed here. Every event that needs to be converted to an LRS statement must define a converter. Prebuilt converters have been added for LoginEvent, LogoutEvent, PortalRenderEvent and PortalExecutionEvent.

Each of the predefined converters allows the verb in the LRS statement to be customized. To see the list of predefined/supported verbs see: _portal-war/src/main/java/org/apereo/portal/events/tincan/om/LrsVerb_.

The `PortalExecutionEvent` filter has additional filtering built in. Execution events can be further filtered by `type` and by `fname`. The uPortal TinCan API event filtering configuration can be found in _tincanAPIContext.xml_. And example configuration:

```xml
<!--
  - Example config to control the list of events that should be passed to xAPI
  -
  - Enable the set of events that should be converted to xAPI events
  -->
<util:list id="tinCanEventConverters">
    <!-- send xAPI events on login -->
    <bean class="org.apereo.portal.events.tincan.converters.LoginEventConverter">
        <property name="verb" value="INITIALIZED"/>
    </bean>

    <!-- send xAPI events on logout -->
    <bean class="org.apereo.portal.events.tincan.converters.LogoutEventConverter">
        <property name="verb" value="EXITED"/>
    </bean>

    <!-- send xAPI events Action events, Event events and Resource events.  This filter
        excludes the portlets with fnames of "emergeny-alert" and "notification-icon" -->
    <bean class="org.apereo.portal.events.tincan.converters.PortletExecutionEventConverter">
        <!-- can filter by specific type of action -->
        <property name="supportedEventTypes">
            <util:list>
                <value>org.apereo.portal.events.PortletActionExecutionEvent</value>
                <value>org.apereo.portal.events.PortletEventExecutionEvent</value>
                <value>org.apereo.portal.events.PortletResourceExecutionEvent</value>
            </util:list>
        </property>

        <!-- for execution events, can also filter events by fname.
            If fnameFilterType is set to Blacklist, events from portlets with
            matching fnames will be ignored.   If the fnameFilterType is set
            to Whitelist, events from all portlets will be ignored except
            those listed in the whitelist.
        -->
        <property name="fnameFilterType" value="Blacklist"/>
        <property name="filterFNames">
            <util:list>
                <value>emergency-alert</value>
                <value>notification-icon</value>
            </util:list>
        </property>
        <property name="verb" value="INTERACTED"/>
    </bean>

    <!-- Example of using a different action for specific set of events.-->
    <!--
    <bean class="org.apereo.portal.events.tincan.converters.PortletExecutionEventConverter">
        <property name="supportedEventTypes">
            <util:list>
                <value>org.apereo.portal.events.PortletRenderExecutionEvent</value>
                <value>org.apereo.portal.events.PortletRenderHeaderExecutionEvent</value>
            </util:list>
        </property>
        <property name="fnameFilterType" value="Whitelist"/>
        <property name="verb" value="EXPERIENCED"/>
    </bean>
    -->

    <!-- send xAPI events on portlet render -->
    <!--
    <bean class="org.apereo.portal.events.tincan.converters.PortletRenderEventConverter">
        <property name="verb" value="EXPERIENCED"/>
    </bean>
    -->

    <!--
        Catch-all filter that is mostly intended to determine which types of
        events are being fired.  It is primarily a debugging aid.  If enabled,
        this will fire for every event not handled elsewhere.  Not intended
        for production use!
    -->
    <!--
    <bean class="org.apereo.portal.events.tincan.converters.GeneralEventConverter">
        <property name="verb" value="INTERACTED"/>
    </bean>
    -->
</util:list>
```
