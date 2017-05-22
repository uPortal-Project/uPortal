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
package org.apereo.portal;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.lang.StringUtils;
import org.apereo.portal.utils.RandomTokenGenerator;
import org.apereo.portal.utils.threading.ReadResult;
import org.apereo.portal.utils.threading.ReadWriteCallback;
import org.apereo.portal.utils.threading.ReadWriteLockTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 */
@Service("portalInfoProvider")
public class PortalInfoProviderImpl implements IPortalInfoProvider, ReadWriteCallback<String> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private String serverName;
    private String networkInterfaceName;

    private String resolvedServerName;
    private String resolvedUniqueServerName;

    /** @param serverName A specific server name for {@link #getServerName()} to return */
    @Value("${org.apereo.portal.PortalInfoProvider.serverName:}")
    public void setServerName(String serverName) {
        this.serverName = StringUtils.trimToNull(serverName);
    }

    /**
     * @param networkInterfaceName The name to use to lookup a NetworkInterface via {@link
     *     NetworkInterface#getByName(String)}
     */
    @Value("${org.apereo.portal.PortalInfoProvider.networkInterfaceName:}")
    public void setNetworkInterfaceName(String networkInterfaceName) {
        this.networkInterfaceName = StringUtils.trimToNull(networkInterfaceName);
    }

    private final ReadWriteLock serverNameResolutionLock = new ReentrantReadWriteLock();

    @Override
    public final String getServerName() {
        return ReadWriteLockTemplate.doWithLock(serverNameResolutionLock, this);
    }

    @Override
    public String getUniqueServerName() {
        ReadWriteLockTemplate.doWithLock(serverNameResolutionLock, this);
        return this.resolvedUniqueServerName;
    }

    @Override
    public ReadResult<String> doInReadLock() {
        if (this.resolvedServerName != null) {
            return ReadResult.create(false, this.resolvedServerName);
        }

        return ReadResult.create(true);
    }

    @Override
    public String doInWriteLock(ReadResult<String> readResult) {
        this.resolvedServerName = resolveServerName();
        this.resolvedUniqueServerName =
                this.resolvedServerName
                        + "_"
                        + RandomTokenGenerator.INSTANCE.generateRandomToken(4);
        return this.resolvedServerName;
    }

    protected String resolveServerName() {
        if (this.serverName != null) {
            return this.serverName;
        }

        String name = getNetworkInterfaceName(this.networkInterfaceName);
        if (name != null) {
            return name;
        }

        name = getLocalHostName();
        if (name != null) {
            return name;
        }

        name = getDefaultNetworkInterfaceName();
        if (name != null) {
            return name;
        }

        this.logger.warn(
                "Failed to get serverName for NetworkInterface ("
                        + this.networkInterfaceName
                        + "), for InetAddress.getLocalHost(), for any NetworkInterface. Reverting to JVM instance specific UUID string.");
        return UUID.randomUUID().toString();
    }

    protected String getDefaultNetworkInterfaceName() {
        this.logger.info(
                "Attempting to resolve serverName by iterating over NetworkInterface.getNetworkInterfaces()");

        //Fail back to our best attempt at resolution
        final Enumeration<NetworkInterface> networkInterfaceEnum;
        try {
            networkInterfaceEnum = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            logger.warn("Failed to get list of available NetworkInterfaces.", e);
            return null;
        }

        //Use a local variable here to try and return the first hostName found that doesn't start with localhost
        String name = null;
        while (networkInterfaceEnum.hasMoreElements()) {
            final NetworkInterface networkInterface = networkInterfaceEnum.nextElement();

            for (Enumeration<InetAddress> inetAddressEnum = networkInterface.getInetAddresses();
                    inetAddressEnum.hasMoreElements();
                    ) {
                final InetAddress inetAddress = inetAddressEnum.nextElement();
                name = inetAddress.getHostName();
                if (!name.startsWith("localhost")) {
                    return name;
                }
            }
        }

        return name;
    }

    protected String getLocalHostName() {
        this.logger.info("Attempting to resolve serverName using InetAddress.getLocalHost()");

        final InetAddress localhost;
        try {
            localhost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            logger.warn("Failed to find InetAddress for InetAddress.getLocalHost()", e);
            return null;
        }

        return localhost.getHostName();
    }

    protected String getNetworkInterfaceName(String networkInterfaceName) {
        if (networkInterfaceName == null) {
            return null;
        }

        this.logger.info(
                "Attempting to resolve serverName using NetworkInterface named ({})",
                networkInterfaceName);

        final NetworkInterface networkInterface;
        try {
            networkInterface = NetworkInterface.getByName(networkInterfaceName);
        } catch (SocketException e) {
            logger.warn(
                    "Failed to get NetworkInterface for name (" + networkInterfaceName + ").", e);
            return null;
        }

        if (networkInterface == null) {
            logger.warn(
                    "No NetworkInterface could be found for name ("
                            + networkInterfaceName
                            + "). Available interface names: "
                            + getNetworkInterfaceNames());
            return null;
        }

        final Enumeration<InetAddress> inetAddressesEnum = networkInterface.getInetAddresses();
        if (!inetAddressesEnum.hasMoreElements()) {
            logger.warn(
                    "NetworkInterface ("
                            + networkInterface.getName()
                            + ") has no InetAddresses to get a name from.");
            return null;
        }

        final InetAddress inetAddress = inetAddressesEnum.nextElement();
        if (inetAddressesEnum.hasMoreElements()) {
            logger.warn(
                    "NetworkInterface ("
                            + networkInterface.getName()
                            + ") has more than one InetAddress, the hostName of the first will be returned.");
        }

        return inetAddress.getHostName();
    }

    protected Set<String> getNetworkInterfaceNames() {
        final Enumeration<NetworkInterface> networkInterfacesEnum;
        try {
            networkInterfacesEnum = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            logger.warn("Failed to get list of available NetworkInterfaces.", e);
            return Collections.emptySet();
        }

        final Set<String> names = new LinkedHashSet<String>();
        while (networkInterfacesEnum.hasMoreElements()) {
            final NetworkInterface networkInterface = networkInterfacesEnum.nextElement();
            names.add(networkInterface.getName());
        }

        return names;
    }
}
