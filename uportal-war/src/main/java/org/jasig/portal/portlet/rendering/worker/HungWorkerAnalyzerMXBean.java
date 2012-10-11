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
package org.jasig.portal.portlet.rendering.worker;

import java.util.Map;


/**
 * JMX stats for HungWorkerAnalyzer
 * 
 * @author Eric Dalquist
 */
public interface HungWorkerAnalyzerMXBean {

    /**
     * @return Total number of hung workers
     */
    int getHungPortletCountTotal();
    
    /**
     * @return Hung worker counts by portlet
     */
    Map<String, Integer> getHungPortletCounts();
    
    /**
     * @param percentPermittedErrantByFname The allowed ratio of hung portlets to available workers per fname
     */
    void setPercentPermittedErrantByFname(double percentPermittedErrantByFname);

    /**
     * @return The allowed ratio of hung portlets to available workers per fname 
     */
    double getPercentPermittedErrantByFname();
}