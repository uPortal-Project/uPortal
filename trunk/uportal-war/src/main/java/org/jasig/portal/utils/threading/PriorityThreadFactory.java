/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.utils.threading;

import java.util.concurrent.ThreadFactory;

/**
 * Implementation of a ThreadFactory that allows you to set the thread
 * priority.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @deprecated Use {@link org.springframework.scheduling.concurrent.CustomizableThreadFactory} instead
 */
@Deprecated
public class PriorityThreadFactory implements ThreadFactory {
    private final int threadPriority;
    private final ThreadGroup tg;
    private final String threadName;

    public PriorityThreadFactory(int threadPriority, final String threadPoolName, final ThreadGroup parentGroup) {
        this.threadPriority = threadPriority;
        threadName = threadPoolName;
        this.tg = new ThreadGroup(parentGroup, threadPoolName);
    }

    public Thread newThread(Runnable runnable) {
        Thread t = new Thread(tg, runnable);
        t.setPriority(this.threadPriority);
        t.setName(threadName + "#" + t.getId());
        t.setDaemon(true);
        return t;
    }
}
