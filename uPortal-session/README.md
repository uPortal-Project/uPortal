# uPortal Session

The purpose of this submodule is to allow Spring Session to be optionally used 
in order to provide support for web session clustering, replication, and 
failover.

Spring Session provides the capability to store web sessions external to the 
Servlet container (Tomcat).  While Spring Session supports several storage 
options, uPortal Session currently only supports using Redis.

## Enabling

The use of Spring Session is optional.  By default, it is disabled.  In order 
to enable it, use the following environment variable or system property with 
the value of 'redis':

- environment variable:  ORG_APEREO_PORTAL_SESSION_STORETYPE
- system property:  org.apereo.portal.session.storetype

Note that an application property was not used because at the time of servlet 
context initialization, the application properties are not available for use.

## Redis Connection Config

### Mode

There are three modes supported for connecting to Redis:
- cluster
- sentinel
- standalone

The 'org.apero.portal.session.redis.mode' property should be set to one of these values.
If the property is not found, then the value of 'standalone' will be used by 
default.

#### Cluster

When using cluster mode, the following properties should be used:

- org.apereo.portal.session.redis.cluster.nodes
- org.apereo.portal.session.redis.cluster.maxredirects

#### Sentinel

When using sentinel mode, the following properties should be used:

- org.apereo.portal.session.redis.sentinel.master
- org.apereo.portal.session.redis.sentinel.nodes

#### Standalone

When using standalone mode, the following default values will be used:

- host: 127.0.0.1
- port: 6379

These can be overwritten by using the following properties:

- org.apereo.portal.session.redis.host
- org.apereo.portal.session.redis.port

#### Additional Config

The following properties can optionally be used to additionally configure the 
Redis connection:

- org.apereo.portal.session.redis.timeout
- org.apereo.portal.session.redis.password
- org.apereo.portal.session.redis.database
