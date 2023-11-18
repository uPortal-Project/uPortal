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
package org.apereo.portal.spring;

import org.apereo.portal.utils.PortalApplicationContextLocator;
import org.junit.Ignore;
import org.junit.Test;

@Ignore // Breaks on move to Gradle
public class LazyPortalApplicationContextTest {

    @Test(expected = IllegalStateException.class)
    public void testLazyLoadingPortalApplicationContext() throws Exception {
        PortalApplicationContextLocator.getApplicationContext();
    }
}
