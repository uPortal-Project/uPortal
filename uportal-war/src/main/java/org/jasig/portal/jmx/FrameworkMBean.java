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

package org.jasig.portal.jmx;

import java.util.Date;

public interface FrameworkMBean {
  public Date getStartedAt();

  public long getRenderAverage();
  public long getRenderHighMax();
  public long getRenderLast();
  public long getRenderMin();
  public long getRenderMax();
  public long getRenderTotalRenders();

  /* Database information */
  public int getRDBMActiveConnectionCount();
  public int getRDBMMaxConnectionCount();
  public long getDatabaseAverage();
  public long getDatabaseHighMax();
  public long getDatabaseLast();
  public long getDatabaseMin();
  public long getDatabaseMax();
  public long getDatabaseTotalConnections();

  public long getAuthenticationAverage();
  public long getAuthenticationHighMax();
  public long getAuthenticationLast();
  public long getAuthenticationMin();
  public long getAuthenticationMax();
  public long getAuthenticationTotalLogins();

  // Threads
  public long getThreadCount();
}
