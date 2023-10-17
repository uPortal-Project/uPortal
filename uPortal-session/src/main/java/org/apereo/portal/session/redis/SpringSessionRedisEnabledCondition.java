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
package org.apereo.portal.session.redis;

import static org.apereo.portal.session.PortalSessionConstants.REDIS_STORE_TYPE;
import static org.apereo.portal.session.PortalSessionConstants.SESSION_STORE_TYPE_ENV_PROPERTY_NAME;
import static org.apereo.portal.session.PortalSessionConstants.SESSION_STORE_TYPE_SYSTEM_PROPERTY_NAME;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class SpringSessionRedisEnabledCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return REDIS_STORE_TYPE.equals(this.getSessionStoreTypeValue(context));
    }

    private String getSessionStoreTypeValue(ConditionContext context) {
        String result =
                context.getEnvironment()
                        .getProperty(SESSION_STORE_TYPE_SYSTEM_PROPERTY_NAME, String.class, null);
        if (result == null) {
            result =
                    context.getEnvironment()
                            .getProperty(SESSION_STORE_TYPE_ENV_PROPERTY_NAME, String.class, null);
        }
        return result;
    }
}
