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

/**
 * Basic Runnable that sets the name of the thread doing the execution
 * @author Eric Dalquist
 */
public abstract class ThreadNamingRunnable implements Runnable {
    /**
     * The suffix that will be appended to the thread name when the runnable is executing
     */
    protected final String threadNameSuffix;

    /**
     * Create new ThreadNamingRunnable with the specified suffix
     */
    public ThreadNamingRunnable(String threadNameSuffix) {
        this.threadNameSuffix = threadNameSuffix;
    }

    /**
     * {@inheritDoc}
     */
    public final void run() {
        final Thread currentThread = Thread.currentThread();
        final String name = currentThread.getName();
        try {
            currentThread.setName(name + this.threadNameSuffix);
            this.runInternal();
        } finally {
            currentThread.setName(name);
        }
    }

    /**
     * @see Runnable#run()
     */
    public abstract void runInternal();
}