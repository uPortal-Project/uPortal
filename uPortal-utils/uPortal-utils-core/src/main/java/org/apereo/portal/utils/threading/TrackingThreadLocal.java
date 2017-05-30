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
package org.apereo.portal.utils.threading;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * A ThreadLocal subclass that keeps track of all created instances. Using this ThreadLocal subclass
 * in uPortal allows any portlet execution thread wrapped by
 * TrackingThreadLocalPortletExecutionInterceptor to have the values copied into the appropriate
 * ThreadLocals in the child thread when the thread is run. This avoids having to write explicit
 * code for each ThreadLocal that needs to be copied.<br>
 * <br>
 * The references kept the the ThreadLocal instances is done using a weak set so this tracking
 * subclass will not cause leaks by holding on to thread local instances that are no longer
 * referenced.
 *
 */
public class TrackingThreadLocal<T> extends ThreadLocal<T> {
    @SuppressWarnings("unchecked")
    private static Map<TrackingThreadLocal<Object>, Class<TrackingThreadLocal>> INSTANCES =
            new WeakHashMap<TrackingThreadLocal<Object>, Class<TrackingThreadLocal>>();

    private static Set<TrackingThreadLocal<Object>> KEY_SET =
            Collections.unmodifiableSet(INSTANCES.keySet());

    @SuppressWarnings("unchecked")
    public TrackingThreadLocal() {
        synchronized (INSTANCES) {
            INSTANCES.put((TrackingThreadLocal<Object>) this, TrackingThreadLocal.class);
        }
    }

    /**
     * Gets an umodifiable version of the Set of TrackingThreadLocal's that have been created and
     * are still referenced strongly somewhere in the JVM.
     *
     * @return An unmodifiable set of TrackingThreadLocals
     */
    public static Set<TrackingThreadLocal<Object>> getInstances() {
        return KEY_SET;
    }

    public static Map<TrackingThreadLocal<Object>, Object> getCurrentData() {
        final Map<TrackingThreadLocal<Object>, Object> localCopies =
                new HashMap<TrackingThreadLocal<Object>, Object>();

        synchronized (INSTANCES) {
            for (final TrackingThreadLocal<Object> local : KEY_SET) {
                final Object obj = local.get();
                localCopies.put(local, obj);
            }
        }

        return localCopies;
    }

    public static void setCurrentData(Map<TrackingThreadLocal<Object>, Object> localCopies) {
        for (final Map.Entry<TrackingThreadLocal<Object>, Object> localCopyEntry :
                localCopies.entrySet()) {
            final TrackingThreadLocal<Object> local = localCopyEntry.getKey();
            final Object obj = localCopyEntry.getValue();

            local.set(obj);
        }
    }

    public static void clearCurrentData() {
        synchronized (INSTANCES) {
            clearCurrentData(KEY_SET);
        }
    }

    public static void clearCurrentData(Set<TrackingThreadLocal<Object>> locals) {
        for (final TrackingThreadLocal<Object> local : locals) {
            local.remove();
        }
    }
}
