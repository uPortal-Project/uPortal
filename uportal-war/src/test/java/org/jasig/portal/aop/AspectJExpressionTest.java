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

package org.jasig.portal.aop;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:aopTestContext.xml")
public class AspectJExpressionTest {
    @Autowired
    private TestAspect testAspect;
    
    @Autowired
    private RepositoryPointcutInterface repositoryPointcutInterface;
    
    @Before
    public void setup() {
        testAspect.resetCallCount();
    }
    
    @Test
    public void testRespotioryPointcut() {
        assertEquals(0, testAspect.getCallCount());
        final String r1 = this.repositoryPointcutInterface.methodOne("test");
        assertEquals(1, testAspect.getCallCount());
        final String r2 = this.repositoryPointcutInterface.methodTwo("test");
        assertEquals(2, testAspect.getCallCount());
    }
}
