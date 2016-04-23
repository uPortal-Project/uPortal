/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.security.provider;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.permission.IPermissionActivity;
import org.jasig.portal.permission.IPermissionOwner;
import org.jasig.portal.permission.dao.IPermissionOwnerDao;
import org.jasig.portal.permission.target.IPermissionTarget;
import org.jasig.portal.permission.target.IPermissionTargetProvider;
import org.jasig.portal.permission.target.IPermissionTargetProviderRegistry;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IAuthorizationService;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.PermissionHelper;
import org.jasig.portal.services.GroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

/**
 * Responsible for pre-loading the permissions cache maintained by the
 * anyUnblockedGrantPermissionPolicy bean with some portlet-related evaluation
 * decisions.  This is computationally expensive work, especially for operations
 * that need a decision for every portlet in the registry;  it's better to do it
 * outside of a request thread.
 *
 * @author drewwills
 * @since uPortal 4.3
 */
public class PortletPermissionsCachePrimer {

    @Autowired
    private IPortletDefinitionRegistry portletDefinitionRegistry;

    @Autowired
    private IPermissionOwnerDao permissionOwnerDao;

    @Autowired
    private IPermissionTargetProviderRegistry targetProviderRegistry;

    @Autowired
    private IAuthorizationService authorizationService;

    @Autowired
    private AnyUnblockedGrantPermissionPolicy policy;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private ThreadPoolExecutor executor;
    private Map<String,Set<String>> permissionsMap;

    @Required
    public void setExecutor(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    @Required
    public void setPermissionsMap(Map<String,Set<String>> permissionsMap) {
        this.permissionsMap = Collections.unmodifiableMap(permissionsMap);
    }

    public void primeCache() {

        if (executor.getActiveCount() != 0) {
            log.warn("Skipping this run becasue there are active threads in the executor, signifying the previous run is not complete");
            return;
        }

        log.info("STARTING PortletPermissionsCachePrimer.primeCache()...");
        final long timestamp = System.currentTimeMillis();

        /*
         * This task is pretty effort-intensive and may take in excess of a
         * minute to run in a single thread.  Going to use a divide-and-conquer
         * approach.
         */
        final Map<NodeWalker,Future<NodeWalkerReport>> futures = new HashMap<>();

        final IEntityGroup rootGroup = GroupService.getRootGroup(IPerson.class);
        for (Map.Entry<String,Set<String>> y : permissionsMap.entrySet()) {
            final IPermissionOwner owner = permissionOwnerDao.getPermissionOwner(y.getKey());
            for (String s : y.getValue()) {
                final IPermissionActivity activity = permissionOwnerDao.getPermissionActivity(y.getKey(), s);
                final IPermissionTargetProvider targetProvider = targetProviderRegistry.getTargetProvider(activity.getTargetProviderKey());
                final NodeWalker walker = new NodeWalker(rootGroup, owner, activity, targetProvider);
                final Future<NodeWalkerReport> future = this.executor.submit(walker);
                futures.put(walker, future);
            }
        }

        int totalCombinations = 0;
        for (Map.Entry<NodeWalker,Future<NodeWalkerReport>> y : futures.entrySet()) {
            try {
                final NodeWalkerReport report = y.getValue().get();
                totalCombinations += report.getCombinationCount();
                log.debug("NodeWalker '{}' processed {} combinations in {}ms",
                        y.getKey(), report.getCombinationCount(), report.getDuration());
            } catch (InterruptedException | ExecutionException e) {
                log.error("NodeWalker '{}' failed", y.getKey());
            }
        }

        log.info("COMPLETED PortletPermissionsCachePrimer.primeCache();  processed {} total combinations in {}ms",
                totalCombinations, Long.toString(System.currentTimeMillis() - timestamp));

    }

    /*
     * Nested Types
     */

    private /* non-static */ final class NodeWalker implements Callable<NodeWalkerReport> {

        final IEntityGroup rootGroup;
        final IPermissionOwner owner;
        final IPermissionActivity activity;
        final IPermissionTargetProvider targetProvider;

        public NodeWalker(IEntityGroup rootGroup, IPermissionOwner owner, IPermissionActivity activity, IPermissionTargetProvider targetProvider) {
            this.rootGroup = rootGroup;
            this.owner = owner;
            this.activity = activity;
            this.targetProvider = targetProvider;
        }

        @Override
        public NodeWalkerReport call() throws Exception {
            final NodeWalkerReport rslt = new NodeWalkerReport();
            final long timestamp = System.currentTimeMillis();
            walk(rootGroup, new HashSet<EntityIdentifier>(), rslt);
            rslt.setDuration(System.currentTimeMillis() - timestamp);
            return rslt;
        }

        private void walk(final IEntityGroup group, final Set<EntityIdentifier> visitedNodes, final NodeWalkerReport report) {

            /*
             * Recursive groups structures are a bad idea, but
             * we will attempt to prevent issues with them.
             */
            if (visitedNodes.contains(group.getUnderlyingEntityIdentifier())) {
                return;
            }
            visitedNodes.add(group.getUnderlyingEntityIdentifier());

            /*
             * First we load ourselves.
             */
            final IAuthorizationPrincipal principal = authorizationService.newPrincipal(group);
            final List<IPortletDefinition> portletDefinitions = portletDefinitionRegistry.getAllPortletDefinitions();
            for (IPortletDefinition pdef : portletDefinitions) {
                final String targetString = PermissionHelper.permissionTargetIdForPortletDefinition(pdef);
                final IPermissionTarget target = targetProvider.getTarget(targetString);
                policy.loadInCache(authorizationService, principal, owner, activity, target);
                report.incrementCombinationCount();
            }

            /*
             * Then we load our children.
             */
            if (group.hasMembers()) {
                @SuppressWarnings("rawtypes")
                final Iterator members = group.getMembers();
                for (IGroupMember next = (IGroupMember) members.next(); members.hasNext(); next = (IGroupMember) members.next()) {
                    if (next.isGroup()) {
                        IEntityGroup child = (IEntityGroup) next;
                        walk(child, visitedNodes, report);
                    }
                }
            }

        }

        @Override
        public String toString() {
            return "NodeWalker [rootGroup=" + rootGroup + ", owner=" + owner + ", activity=" + activity + "]";
        }

    }

    private static final class NodeWalkerReport {

        private int combinationCount;
        private long duration;

        public int getCombinationCount() {
            return combinationCount;
        }
        public void incrementCombinationCount() {
            combinationCount += 1;
        }
        public long getDuration() {
            return duration;
        }
        public void setDuration(long duration) {
            this.duration = duration;
        }

    }

}
