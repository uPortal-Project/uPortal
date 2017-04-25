/**
 *  Copyright 2003-2010 Terracotta, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


package net.sf.ehcache.distribution.jgroups;

import java.net.URL;
import java.util.Collections;
import java.util.List;

import javax.management.MBeanServer;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Status;
import net.sf.ehcache.distribution.CacheManagerPeerProvider;
import net.sf.ehcache.distribution.CachePeer;
import net.sf.ehcache.management.ManagedCacheManagerPeerProvider;
import org.jgroups.JChannel;
import org.jgroups.jmx.JmxConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main Jgroup class for replication via JGroup. Starts up the Jgroup communication bus and listen for message in
 * the bus. Because of Ehcache design we have to register this as a CachePeer. In reality this class listen for change
 * on the bus and tells the cachemanager to update.
 *
 * @author Pierre Monestie (pmonestie__REMOVE__THIS__@gmail.com)
 * @author <a href="mailto:gluck@gregluck.com">Greg Luck</a>
 * @version $Id: JGroupsCacheManagerPeerProvider.java 2608 2010-08-05 06:06:01Z gluck $
 */
public class JGroupsCacheManagerPeerProvider implements ManagedCacheManagerPeerProvider {

    /**
     * Constant that defines the value returned by {@link #getScheme()}
     */
    public static final String SCHEME_NAME = "JGroups";

    private static final String JMX_DOMAIN_NAME = "JGroupsReplication";

    private static final Logger LOG = LoggerFactory.getLogger(JGroupsCacheManagerPeerProvider.class.getName());

    private final CacheManager cacheManager;
    private final String groupProperties;
    private final URL groupUrl;
    private String channelName;

    private JChannel channel;
    private JGroupsCachePeer cachePeer;
    private JGroupsCacheReceiver cacheReceiver;
    private List<CachePeer> cachePeersListCache;

    private JGroupsBootstrapManager bootstrapManager;

    private MBeanServer mBeanServer;


    /**
     * Construct a new JGroupsCacheManagerPeerProvider with a specific JGroups connection String
     *
     * @param cacheManager the cache manager
     * @param properties   the JGroups connection String
     */
    public JGroupsCacheManagerPeerProvider(CacheManager cacheManager, String properties) {
        this.cacheManager = cacheManager;
        this.groupProperties = properties;
        this.groupUrl = null;
    }

    /**
     * Construct a new JGroupsCacheManagerPeerProvider with a specific JGroups connection String
     *
     * @param cacheManager the cache manager
     * @param configUrl    the JGroups configuration file
     */
    public JGroupsCacheManagerPeerProvider(CacheManager cacheManager, URL configUrl) {
        this.cacheManager = cacheManager;
        this.groupProperties = null;
        this.groupUrl = configUrl;
    }

    /**
     * Set the name of the JChannel, if null the cache manager name is used.
     */
    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    /**
     * Given an {@link Ehcache} get the corresponding instance of this class.
     */
    public static JGroupsCacheManagerPeerProvider getCachePeerProvider(Ehcache cache) {
        final CacheManager cacheManager = cache.getCacheManager();
        return getCachePeerProvider(cacheManager);
    }
    /**
     * Given an {@link CacheManager} get the corresponding instance of this class.
     */
    public static JGroupsCacheManagerPeerProvider getCachePeerProvider(CacheManager cacheManager) {
        final CacheManagerPeerProvider provider = cacheManager.getCacheManagerPeerProvider(JGroupsCacheManagerPeerProvider.SCHEME_NAME);
        if (provider == null) {
            LOG.warn("No CacheManagerPeerProvider registered for {} scheme.", JGroupsCacheManagerPeerProvider.SCHEME_NAME);
            return null;
        }
        if (!(provider instanceof JGroupsCacheManagerPeerProvider)) {
            LOG.warn("{} for scheme {} cannot be cast to {}.", new Object[] {provider.getClass(),
                    JGroupsCacheManagerPeerProvider.SCHEME_NAME, JGroupsCacheManagerPeerProvider.class, });
            return null;
        }
        return (JGroupsCacheManagerPeerProvider)provider;
    }

    /**
     * {@inheritDoc}
     */
    public void init() {
        try {
            if (groupProperties != null) {
                channel = new JChannel(groupProperties);
            } else if (groupUrl != null) {
                channel = new JChannel(groupUrl);
            } else {
                channel = new JChannel();
            }
        } catch (Exception e) {
            LOG.error("Failed to create JGroups Channel, replication will not function. JGroups properties:\n" + this.groupProperties, e);
            this.dispose();
            return;
        }

        final String clusterName = this.getClusterName();

        this.cachePeer = new JGroupsCachePeer(this.channel, clusterName);
        this.bootstrapManager = new JGroupsBootstrapManager(clusterName, this.cachePeer, this.cacheManager);
        this.cacheReceiver = new JGroupsCacheReceiver(this.cacheManager, this.bootstrapManager);
        this.channel.setReceiver(this.cacheReceiver);
        this.channel.setDiscardOwnMessages(true);

        try {
            this.channel.connect(clusterName);
        } catch (Exception e) {
            LOG.error("Failed to connect to JGroups cluster '" + clusterName +
                    "', replication will not function. JGroups properties:\n" + this.groupProperties, e);
            this.dispose();
            return;
        }

        this.cachePeersListCache = Collections.singletonList((CachePeer)this.cachePeer);



        LOG.info("JGroups Replication started for '" + clusterName + "'. JChannel: {}", this.channel.toString(true));
    }

    /**
     * {@inheritDoc}
     */
    public void register(MBeanServer mBeanServer) {
        this.mBeanServer = mBeanServer;

        try {
            final String clusterName = this.getClusterName();
            JmxConfigurator.registerChannel(this.channel, mBeanServer, JMX_DOMAIN_NAME, clusterName, true);
            LOG.debug("Registered JGroups channel with MBeanServer under domain {} with name {}", JMX_DOMAIN_NAME, clusterName);
        } catch (Exception e) {
            LOG.error("Error occured while registering MBeans. Management of JGroups will not be enabled.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() throws CacheException {
        if (this.bootstrapManager != null) {
            this.bootstrapManager.dispose();
            this.bootstrapManager = null;
        }

        this.shutdownCachePeer();

        this.shutdownChannel();
    }

    private void shutdownCachePeer() {
        if (this.cachePeer != null) {
            this.cachePeersListCache = null;
            this.cacheReceiver = null;
            this.cachePeer.dispose();
            this.cachePeer = null;
        }
    }

    private void shutdownChannel() {
        if (this.channel != null) {
            final String clusterName = this.getClusterName();

            if (this.mBeanServer != null) {
                try {
                    JmxConfigurator.unregisterChannel(this.channel, mBeanServer, JMX_DOMAIN_NAME, clusterName);
                    LOG.debug("Unregistered JGroups channel with MBeanServer under domain {} with name {}", JMX_DOMAIN_NAME, clusterName);
                } catch (Exception e) {
                    LOG.error("Error unregistering JGroups channel with MBeanServer under domain " + JMX_DOMAIN_NAME +
                            " with name " + clusterName, e);
                }
            }

            if (this.channel.isConnected()) {
                try {
                    this.channel.close();
                    LOG.debug("Closing JChannel for cluster {}", clusterName);
                } catch (Exception e) {
                    LOG.error("Error closing JChannel for cluster " + clusterName, e);
                }
            }

            this.channel = null;
        }
    }

    /**
     * JGroups will be connected once {@link #init()} returns and there is no per-cache connect time to worry about.
     */
    public long getTimeForClusterToForm() {
        return 0;
    }

    /**
     * The replication scheme. Each peer provider has a scheme name, which can be used to specify the scheme for
     * replication and bootstrap purposes. Each <code>CacheReplicator</code> should lookup the provider for its scheme
     * type during replication. Similarly a <code>BootstrapCacheLoader</code> should also look up the provider for its
     * scheme.
     * <p/>
     *
     * @return the well-known scheme name, which is determined by the replication provider author.
     * @since 1.6 introduced to permit multiple distribution schemes to be used in the same CacheManager
     */
    public String getScheme() {
        return SCHEME_NAME;
    }

    /**
     * {@inheritDoc}
     */
    public List<CachePeer> listRemoteCachePeers(Ehcache cache) throws CacheException {
        if (this.cachePeersListCache == null) {
            return Collections.emptyList();
        }
        return this.cachePeersListCache;
    }

    /**
     * {@inheritDoc}
     */
    public void registerPeer(String rmiUrl) {
        //Ignore, only used for RMI
    }

    /**
     * {@inheritDoc}
     */
    public void unregisterPeer(String rmiUrl) {
        //Ignore, only used for RMI
    }

    /**
     * @return the JGroupsBootstrapManager
     */
    public JGroupsBootstrapManager getBootstrapManager() {
        return this.bootstrapManager;
    }

    /**
     * @return the {@link Status} of the manager
     */
    public Status getStatus() {
        if (this.channel == null) {
            return Status.STATUS_UNINITIALISED;
        }
        if (!this.channel.isConnected()) {
            return Status.STATUS_SHUTDOWN;
        }

        return Status.STATUS_ALIVE;
    }

    /**
     * @return The cluster name for JMX registration
     */
    public String getClusterName() {
        if (this.channelName != null) {
            return this.channelName;
        }

        if (this.cacheManager.isNamed()) {
            return this.cacheManager.getName();
        }

        return "EH_CACHE";
    }
}
