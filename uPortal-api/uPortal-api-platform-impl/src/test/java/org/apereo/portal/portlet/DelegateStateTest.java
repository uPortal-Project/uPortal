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
package org.apereo.portal.portlet;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import org.apereo.portal.api.portlet.DelegateState;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class DelegateStateTest {
    DelegateState delegateState;

    @Before
    public void setup() {
        delegateState =
                new DelegateState(
                        new Mockito().mock(PortletMode.class),
                        new Mockito().mock(WindowState.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPortletModeNull() {
        delegateState = new DelegateState(null, null);
        delegateState.getPortletMode();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetWindowStateNull() {
        delegateState = new DelegateState(null, null);
        delegateState.getWindowState();
    }

    @Test
    public void testGetWindowState() {
        Assert.assertNotNull(delegateState.getWindowState());
    }

    @Test
    public void testGetPortletState() {
        Assert.assertNotNull(delegateState.getPortletMode());
    }
}
