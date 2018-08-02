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
package org.apereo.portal.io.xml

import org.apereo.portal.io.xml.IDataTemplatingStrategy
import org.apereo.portal.io.xml.SpELDataTemplatingStrategy
import org.apereo.portal.spring.spel.IPortalSpELService
import org.apereo.portal.spring.spel.PortalSpELServiceImpl
import org.apereo.portal.tenants.ITenant
import org.apereo.portal.tenants.TemplateDataTenantOperationsListener

import static org.junit.Assert.*
import org.junit.Test
import org.springframework.expression.spel.support.StandardEvaluationContext

class SpELDataTemplatingStrategyTest {

    private IPortalSpELService portalSpELService = new PortalSpELServiceImpl()

    @Test
    void testProcessText() {
        ITenant tenant = [
            getName: { 'Mordor' },
            getFname: { 'mordor'}
        ] as ITenant
        StandardEvaluationContext ctx = new StandardEvaluationContext()
        ctx.setRootObject(new TemplateDataTenantOperationsListener.RootObjectImpl(tenant))
        IDataTemplatingStrategy templating = new SpELDataTemplatingStrategy(portalSpELService, ctx)

        def inputs = [
            'foobar': 'foobar',
            '${tenant.name}': 'Mordor',
            'Something ${tenant.name}': 'Something Mordor',
            '${tenant.name} Something': 'Mordor Something',
            '${tenant.fname}': 'mordor',
            'Something ${tenant.fname}': 'Something mordor',
            '${tenant.fname} Something': 'mordor Something'
        ]

        inputs.each { k,v ->
            String output = templating.processText(k)
            assertEquals('Unexpected output from processText() -- ', v, output)
        }
    }

}
